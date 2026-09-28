package com.store.service;

import com.store.repository.CategoryRepo;
import com.store.repository.ProductRepo;
import com.store.exceptions.ResourceNotFoundException;
import com.store.model.Category;
import com.store.model.Product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepo productRepo, CategoryRepo categoryRepo, AuditLogService auditLogService) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Product addProduct(String sku, String name, BigDecimal price, int stock, Set<Long> categoryIds) {

        Product product = new Product(sku, name, price, stock);

        if (categoryIds != null) {
            for (Long catId : categoryIds) {
                Category category = categoryRepo.findById(catId).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + catId));
                product.addCategory(category);
            }
        }

        productRepo.save(product);
        auditLogService.log("PRODUCT_ADDED", "sku=" + sku);

        return product;
    }

    @Transactional
    public void restock(Long productId, int quantity) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setStock(product.getStock() + quantity);

        auditLogService.log("PRODUCT_RESTOCKED", "productId=" + productId + ", added=" + quantity);
    }


    @Transactional
    public void changePrice(Long productId, BigDecimal newPrice) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setPrice(newPrice);

        auditLogService.log("PRICE_CHANGED", "productId=" + productId + ", newPrice=" + newPrice);
    }
}