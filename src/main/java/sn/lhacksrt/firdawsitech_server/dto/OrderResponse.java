package sn.lhacksrt.firdawsitech_server.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID uuid,
        String orderNumber,
        String customerName,
        String customerEmail,
        String customerPhone,
        String status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        String createdAt,
        String updatedAt
) {}

