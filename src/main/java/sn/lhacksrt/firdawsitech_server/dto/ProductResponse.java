package sn.lhacksrt.firdawsitech_server.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Réponse API
 */
public record ProductResponse(
        UUID uuid,
        String slug,
        String name,
        BigDecimal price,
        String category,
        String imageUrl,
        String description,
        List<String> specs,
        boolean inStock,
        BigDecimal rating,
        boolean featured,
        boolean inCarousel,
        Integer carouselRank,
        List<ProductImageResponse> images,
        String createdAt,
        String updatedAt
) {
}
