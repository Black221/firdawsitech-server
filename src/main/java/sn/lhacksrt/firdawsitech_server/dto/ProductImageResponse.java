package sn.lhacksrt.firdawsitech_server.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID uuid,
        String url,
        String contentType,
        Long sizeBytes,
        Integer sortOrder,
        boolean primaryImage
) {}
