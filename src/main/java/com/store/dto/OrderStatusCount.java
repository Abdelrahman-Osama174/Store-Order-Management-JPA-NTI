package com.store.dto;

import com.store.enums.OrderStatus;

public record OrderStatusCount(OrderStatus status, Long count) {}