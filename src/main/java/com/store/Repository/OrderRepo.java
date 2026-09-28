package com.store.Repository;

import com.store.model.Order;
import com.store.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepo {

    @PersistenceContext
    private EntityManager em;

    public Order save(Order order) {
        if (order.getId() == null) {
            em.persist(order);
            return order;
        }
        return em.merge(order);
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

    public Optional<Order> findByIdWithItems(Long id) {
        return em.createQuery("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.product " +
                                "WHERE o.id = :id", Order.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<Order> findByCustomer(Long customerId) {
        return em.createQuery(
                        "SELECT o FROM Order o WHERE o.customer.id = :customerId", Order.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return em.createQuery(
                        "SELECT o FROM Order o WHERE o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList();
    }
}