package sn.lhacksrt.firdawsitech_server.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID productUuid,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
