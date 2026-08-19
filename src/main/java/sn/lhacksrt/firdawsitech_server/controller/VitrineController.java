package sn.lhacksrt.firdawsitech_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.lhacksrt.firdawsitech_server.dto.ProductResponse;
import sn.lhacksrt.firdawsitech_server.service.ProductService;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vitrine", produces = "application/json")
public class VitrineController {

    private final ProductService products;

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> listAll() {
        return ResponseEntity.ok(products.listAll());
    }


    @GetMapping
    public ResponseEntity<VitrineResponse> getVitrine() {

        List<ProductResponse> featured = products.listFeatured();
        List<ProductResponse> carousel = products.listCarousel();

        // Extra : catégories auto-scan
        Set<String> categories = new TreeSet<>();
        for (ProductResponse p : products.listAll()) {
            if (p.category() != null) categories.add(p.category());
        }

        // Optionnel : top 8 meilleurs produits (tri rating desc)
        List<ProductResponse> topRated = products.listAll().stream()
                .filter(p -> p.rating() != null)
                .sorted(Comparator.comparing(ProductResponse::rating).reversed())
                .limit(8)
                .toList();

        VitrineResponse resp = new VitrineResponse(
                featured,
                carousel,
                new ArrayList<>(categories),
                topRated
        );

        return ResponseEntity.ok(resp);
    }

    public record VitrineResponse(
            List<ProductResponse> featured,
            List<ProductResponse> carousel,
            List<String> categories,
            List<ProductResponse> topRated
    ) {}
}
