package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
  @NotBlank @Email String username,
  @NotBlank String password
) {}

