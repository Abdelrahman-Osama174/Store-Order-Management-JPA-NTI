package com.store.repository;

import com.store.model.Category;
import com.store.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepo {

    @PersistenceContext
    private EntityManager em;

    public Product save(Product product) {
        if (product.getId() == null) {
            em.persist(product);
            return product;
        }
        return em.merge(product);
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    public Optional<Product> findBySku(String sku) {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.sku = :sku", Product.class)
                .setParameter("sku", sku)
                .getResultStream()
                .findFirst();
    }

    public List<Product> findByCategory(String categoryName) {
        return em.createQuery(
                "SELECT p FROM Product p JOIN p.categories c WHERE c.name = :name",
                Product.class)
                .setParameter("name", categoryName)
                .getResultList();
    }


    public List<Product> search(String keyword, BigDecimal minPrice, BigDecimal maxPrice, String category) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + keyword.toLowerCase() + "%"));
        }

        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (category != null && !category.isBlank()) {
            Join<Product, Category> categoryJoin = root.join("categories", JoinType.INNER);
            predicates.add(cb.equal(categoryJoin.get("name"), category));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getResultList();
    }

    public List<Product> findLowStock(int lowStock) {
        return em.createQuery(
                "SELECT p FROM Product p WHERE p.stock <= :lowStock", Product.class)
                .setParameter("lowStock", lowStock)
                .getResultList();
    }

    public List<Product> findPage(int page, int size) {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.id", Product.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countAll() {
        return em.createQuery("SELECT COUNT(p) FROM Product p", Long.class)
                .getSingleResult();
    }
}