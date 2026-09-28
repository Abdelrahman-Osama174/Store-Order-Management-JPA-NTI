package com.store.exceptions;

public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String email) {
        super("Customer with email [" + email + "] already exists");
    }
}