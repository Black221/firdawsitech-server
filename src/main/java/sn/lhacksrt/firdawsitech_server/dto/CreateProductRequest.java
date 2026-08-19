package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** Requête de création d’un produit */
public record CreateProductRequest(
        @NotBlank @Size(max = 200)
        String name,

        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @Size(max = 120)
        String category,

        @Size(max = 1024)
        String imageUrl,

        String description,

        List<@NotBlank @Size(max = 255) String> specs,

        @NotNull
        Boolean inStock,

        @DecimalMin("0.0") @DecimalMax("5.0")
        BigDecimal rating,

        // nouveaux champs (optionnels à la création)
        Boolean featured,
        Boolean inCarousel,
        @Min(1) Integer carouselRank
) {}

