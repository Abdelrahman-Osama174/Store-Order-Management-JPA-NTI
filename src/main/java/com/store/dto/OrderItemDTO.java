package com.store.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
        String productName,
        String sku,
        int quantity,
        BigDecimal unitPrice
) {}