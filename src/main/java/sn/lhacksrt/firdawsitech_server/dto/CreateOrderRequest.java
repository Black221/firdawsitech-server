package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty
        List<@Valid OrderItemDTO> items,
        String customerName,
        String customerEmail,
        String customerPhone
) {}

