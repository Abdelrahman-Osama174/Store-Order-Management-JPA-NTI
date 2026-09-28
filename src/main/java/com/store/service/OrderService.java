package com.store.service;

import com.store.dto.OrderItemDTO;
import com.store.dto.OrderSummaryDTO;
import com.store.enums.PaymentMethod;
import com.store.exceptions.InsufficientStockException;
import com.store.exceptions.InvalidOrderStateException;
import com.store.exceptions.ResourceNotFoundException;
import com.store.model.*;
import com.store.repository.CustomerRepo;
import com.store.enums.OrderStatus;
import com.store.repository.OrderRepo;
import com.store.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final CustomerRepo customerRepo;
    private final ProductRepo productRepo;
    private final AuditLogService auditLogService;

    @Transactional
    public Order placeOrder(Long customerId, Map<Long, Integer> productQuantities) {
        auditLogService.log("PLACE_ORDER_ATTEMPT", "customerId=" + customerId);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        Order order = new Order(customer);
        customer.addOrder(order);

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0 for product: " + productId);
            }

            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

            if (product.getStock() < quantity) {
                throw new InsufficientStockException(product.getSku(), quantity, product.getStock());
            }

            // decrease stock
            product.setStock(product.getStock() - quantity);

            // copy price at order time
            OrderItem item = new OrderItem(product, quantity);
            order.addItem(item);
        }

        orderRepo.save(order);
        auditLogService.log("PLACE_ORDER_SUCCESS", "orderId=" + order.getId());

        return order;
    }


    @Transactional
    public void pay(Long orderId, PaymentMethod method) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new InvalidOrderStateException("Only NEW orders can be paid. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAID);

        Payment payment = new Payment(order, order.getTotal(), method);
        order.setPayment(payment);

        auditLogService.log("ORDER_PAID", "orderId=" + orderId + ", method=" + method);
    }


    @Transactional
    public void ship(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Only PAID orders can be shipped. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.SHIPPED);
        auditLogService.log("ORDER_SHIPPED", "orderId=" + orderId);
    }


    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepo.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.PAID) {
            throw new InvalidOrderStateException("Only NEW or PAID orders can be cancelled. Current status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);

        auditLogService.log("ORDER_CANCELLED", "orderId=" + orderId);
    }

    @Transactional(readOnly = true)
    public OrderSummaryDTO getOrderSummary(Long orderId) {

        Order order = orderRepo.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            itemDTOs.add(new OrderItemDTO(item.getProduct().getName(), item.getProduct().getSku(), item.getQuantity(), item.getUnitPrice()));
        }

        return new OrderSummaryDTO(order.getId(), order.getCustomer().getName(), order.getStatus(), order.getOrderedAt(), order.getTotal(), itemDTOs);
    }
}