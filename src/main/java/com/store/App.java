package com.store;

import com.store.config.AppConfig;
import com.store.dto.*;
import com.store.embeddables.Address;
import com.store.enums.PaymentMethod;
import com.store.exceptions.*;
import com.store.model.*;
import com.store.repository.CategoryRepo;
import com.store.service.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class App {

    public static void main(String[] args) {

        var ctx = new AnnotationConfigApplicationContext(AppConfig.class);

        CustomerService customerService = ctx.getBean(CustomerService.class);
        ProductService productService = ctx.getBean(ProductService.class);
        OrderService orderService = ctx.getBean(OrderService.class);
        ReportService reportService = ctx.getBean(ReportService.class);

        System.out.println("\n========== SETUP ==========");

        Address addr1 = new Address("10 Nile St", "Cairo", "Egypt");
        Address addr2 = new Address("5 Alex Rd", "Alex", "Egypt");

        Customer c1 = customerService.register("Ali Hassan", "ali@store.com", addr1);
        Customer c2 = customerService.register("Sara Mohamed", "sara@store.com", addr2);
        System.out.println("Registered: " + c1.getName() + ", " + c2.getName());

        CategoryRepo catRepo = ctx.getBean(CategoryRepo.class);

        org.springframework.transaction.PlatformTransactionManager tm =
                ctx.getBean(org.springframework.transaction.PlatformTransactionManager.class);

        org.springframework.transaction.support.TransactionTemplate tt =
                new org.springframework.transaction.support.TransactionTemplate(tm);

        Category electronics = tt.execute(s -> catRepo.save(new Category("Electronics")));
        Category clothing = tt.execute(s -> catRepo.save(new Category("Clothing")));
        System.out.println("Categories created: Electronics, Clothing");

        Product phone = productService.addProduct(
                "SKU-001", "Smartphone",
                new BigDecimal("5999.00"), 10,
                Set.of(electronics.getId()));

        Product laptop = productService.addProduct(
                "SKU-002", "Laptop",
                new BigDecimal("12999.00"), 5,
                Set.of(electronics.getId()));

        Product tshirt = productService.addProduct(
                "SKU-003", "T-Shirt",
                new BigDecimal("199.00"), 100,
                Set.of(clothing.getId()));

        System.out.println("Products added: " + phone.getName()
                + ", " + laptop.getName() + ", " + tshirt.getName());

        System.out.println("\n========== ORDER FLOW ==========");

        Order order1 = orderService.placeOrder(
                c1.getId(),
                Map.of(phone.getId(), 2, tshirt.getId(), 3));

        System.out.println("Order placed | id=" + order1.getId()
                + " status=" + order1.getStatus());

        orderService.pay(order1.getId(), PaymentMethod.CARD);
        System.out.println("Order paid");

        orderService.ship(order1.getId());
        System.out.println("Order shipped");

        Order order2 = orderService.placeOrder(
                c2.getId(),
                Map.of(laptop.getId(), 1));

        System.out.println("Order2 placed | id=" + order2.getId());
        orderService.cancel(order2.getId());
        System.out.println("Order2 cancelled — stock restored");

        OrderSummaryDTO summary = orderService.getOrderSummary(order1.getId());
        System.out.println("\n--- Order Summary ---");
        System.out.println("Customer : " + summary.customerName());
        System.out.println("Status   : " + summary.status());
        System.out.println("Total    : " + summary.total());
        summary.items().forEach(i ->
                System.out.println("  " + i.productName()
                        + " x" + i.quantity()
                        + " @ " + i.unitPrice()));

        System.out.println("\n========== REPORTS ==========");

        System.out.println("\n-- Revenue by Category --");
        reportService.revenueByCategory()
                .forEach(r -> System.out.println(r.category() + " => " + r.revenue()));

        System.out.println("\n-- Top Customers --");
        reportService.topCustomers(5)
                .forEach(r -> System.out.println(r.name() + " => " + r.total()));

        System.out.println("\n-- Orders Per Status --");
        reportService.ordersPerStatus()
                .forEach((status, count) -> System.out.println(status + " : " + count));

        System.out.println("\n-- Products Never Ordered --");
        reportService.productsNeverOrdered()
                .forEach(p -> System.out.println(p.getName() + " [" + p.getSku() + "]"));

        System.out.println("\n-- Monthly Sales (current year) --");
        int year = java.time.LocalDate.now().getYear();
        reportService.monthlySales(year)
                .forEach(m -> System.out.println("Month " + m.month() + " => " + m.total()));

        System.out.println("\n========== EDGE CASES ==========");

        try {
            customerService.register("Ali2", "ali@store.com", addr1);
        } catch (DuplicateCustomerException e) {
            System.out.println("Caught expected: " + e.getMessage());
        }

        try {
            orderService.placeOrder(c1.getId(), Map.of(laptop.getId(), 999));
        } catch (InsufficientStockException e) {
            System.out.println("Caught expected: " + e.getMessage());
        }

        try {
            orderService.pay(order1.getId(), PaymentMethod.CASH);
        } catch (InvalidOrderStateException e) {
            System.out.println("Caught expected: " + e.getMessage());
        }

        System.out.println("\n-- Apply 10% discount on Electronics --");
        int updated = reportService.applyDiscount("Electronics", 10.0);
        System.out.println("Products updated: " + updated);

        System.out.println("\n========== DONE ==========");
        ctx.close();
    }
}