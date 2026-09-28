package com.store.logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepo {

    @PersistenceContext
    private EntityManager em;

    public void save(AuditLog log) {
        em.persist(log);
    }
}