package com.store.dto;

import com.store.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryDTO(
        Long orderId,
        String customerName,
        OrderStatus status,
        LocalDateTime orderedAt,
        BigDecimal total,
        List<OrderItemDTO> items
) {}