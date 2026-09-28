package com.store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Setter
@Getter
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String details;

    private LocalDateTime happenedAt;

    public AuditLog(String action, String details) {
        this.action = action;
        this.details = details;
        this.happenedAt = LocalDateTime.now();
    }
}