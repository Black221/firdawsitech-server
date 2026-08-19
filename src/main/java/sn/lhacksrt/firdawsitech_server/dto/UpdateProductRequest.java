package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Requête de mise à jour (PATCH/PUT) — tous champs optionnels
 */
public record UpdateProductRequest(
        @Size(max = 200)
        String name,

        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @Size(max = 120)
        String category,

        @Size(max = 1024)
        String imageUrl,

        String description,

        List<@NotBlank @Size(max = 255) String> specs,

        Boolean inStock,

        @DecimalMin("0.0") @DecimalMax("5.0")
        BigDecimal rating,
        // champs vedette/carrousel modifiables au PATCH
        Boolean featured,
        Boolean inCarousel,
        @Min(1) Integer carouselRank
) {
}
