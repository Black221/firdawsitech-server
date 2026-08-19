package sn.lhacksrt.firdawsitech_server.dto;

import jakarta.validation.constraints.Min;

// com/example/shop/dto/UpdateProductFlagsRequest.java
public record UpdateProductFlagsRequest(
        Boolean featured,
        Boolean inCarousel,
        @Min(1) Integer carouselRank
) {}
