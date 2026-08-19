package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank @Email String username,
        @NotBlank String password,
        @NotBlank String role,  // ADMIN/MANAGER/ACCOUNTANT
        String workerUuid       // optionnel
) {
}
