package com.store.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int requested, int available) {
        super("Product [" + sku + "] requested: " + requested + ", available: " + available);
    }
}