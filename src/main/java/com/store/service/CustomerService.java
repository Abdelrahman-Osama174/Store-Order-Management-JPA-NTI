package com.store.service;

import com.store.exceptions.DuplicateCustomerException;
import com.store.embeddables.Address;
import com.store.model.Customer;
import com.store.repository.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepo customerRepo;
    private final AuditLogService auditLogService;


    @Transactional
    public Customer register(String name, String email, Address address) {
        customerRepo.findByEmail(email)
                .ifPresent(c -> {throw new DuplicateCustomerException(email);});

        Customer customer = new Customer(name, email, address);
        customerRepo.save(customer);

        auditLogService.log("CUSTOMER_REGISTERED", "email=" + email);

        return customer;
    }
}