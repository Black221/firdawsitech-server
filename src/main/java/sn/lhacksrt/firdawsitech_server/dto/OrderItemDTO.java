package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemDTO(
        @NotNull UUID productUuid,
        @Min(1) int quantity
) {
}
