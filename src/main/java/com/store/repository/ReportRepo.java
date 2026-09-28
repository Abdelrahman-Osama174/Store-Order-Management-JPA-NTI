package com.store.repository;

import com.store.dto.*;
import com.store.enums.OrderStatus;
import com.store.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ReportRepo {

    @PersistenceContext
    private EntityManager em;

    public List<CategoryRevenue> revenueByCategory() {
        return em.createQuery(
                        "SELECT new com.store.dto.CategoryRevenue(" +
                                "   c.name, " +
                                "   SUM(i.unitPrice * i.quantity)) " +
                                "FROM OrderItem i " +
                                "JOIN i.product p " +
                                "JOIN p.categories c " +
                                "JOIN i.order o " +
                                "WHERE o.status IN (:paid, :shipped) " +
                                "GROUP BY c.name",
                        CategoryRevenue.class)
                .setParameter("paid", OrderStatus.PAID)
                .setParameter("shipped", OrderStatus.SHIPPED)
                .getResultList();
    }


    public List<CustomerSpend> topCustomers(int limit) {
        return em.createQuery(
                        "SELECT new com.store.dto.CustomerSpend(" +
                                "   c.name, " +
                                "   SUM(i.unitPrice * i.quantity)) " +
                                "FROM OrderItem i " +
                                "JOIN i.order o " +
                                "JOIN o.customer c " +
                                "WHERE o.status IN (:paid, :shipped) " +
                                "GROUP BY c.id, c.name " +
                                "ORDER BY SUM(i.unitPrice * i.quantity) DESC",
                        CustomerSpend.class)
                .setParameter("paid", OrderStatus.PAID)
                .setParameter("shipped", OrderStatus.SHIPPED)
                .setMaxResults(limit)
                .getResultList();
    }


    public Map<OrderStatus, Long> ordersPerStatus() {
        List<OrderStatusCount> results = em.createQuery(
                        "SELECT new com.store.dto.OrderStatusCount(" +
                                "   o.status, COUNT(o)) " +
                                "FROM Order o " +
                                "GROUP BY o.status",
                        OrderStatusCount.class)
                .getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        OrderStatusCount::status,
                        OrderStatusCount::count
                ));
    }


    public List<Product> productsNeverOrdered() {
        return em.createQuery(
                        "SELECT p FROM Product p " +
                                "WHERE NOT EXISTS (" +
                                "   SELECT i FROM OrderItem i " +
                                "   WHERE i.product = p" +
                                ")",
                        Product.class)
                .getResultList();
    }

    // 5. Monthly sales for a given year
    public List<MonthlySales> monthlySales(int year) {
        return em.createQuery(
                        "SELECT new com.store.dto.MonthlySales(" +
                                "   MONTH(o.orderedAt), " +
                                "   SUM(i.unitPrice * i.quantity)) " +
                                "FROM OrderItem i " +
                                "JOIN i.order o " +
                                "WHERE YEAR(o.orderedAt) = :year " +
                                "AND o.status IN (:paid, :shipped) " +
                                "GROUP BY MONTH(o.orderedAt) " +
                                "ORDER BY MONTH(o.orderedAt)",
                        MonthlySales.class)
                .setParameter("year", year)
                .setParameter("paid", OrderStatus.PAID)
                .setParameter("shipped", OrderStatus.SHIPPED)
                .getResultList();
    }


    public int applyDiscount(String category, double percent) {
        int updated = em.createQuery(
                        "UPDATE Product p " +
                                "SET p.price = p.price * :multiplier " +
                                "WHERE EXISTS (" +
                                "   SELECT c FROM p.categories c " +
                                "   WHERE c.name = :category" +
                                ")")
                .setParameter("multiplier", 1.0 - (percent / 100.0))
                .setParameter("category", category)
                .executeUpdate();

        em.clear();

        return updated;
    }
}