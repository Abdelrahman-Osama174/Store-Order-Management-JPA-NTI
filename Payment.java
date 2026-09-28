package com.store.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(Order order, BigDecimal amount, PaymentMethod method) {
        this.order = order;
        this.amount = amount;
        this.method = method;
        this.paidAt = LocalDateTime.now();
    }

    public Order getOrder() { return order; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public LocalDateTime getPaidAt() { return paidAt; }

    public void setOrder(Order order) { this.order = order; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}