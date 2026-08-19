package sn.lhacksrt.firdawsitech_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.lhacksrt.firdawsitech_server.dto.*;
import sn.lhacksrt.firdawsitech_server.service.ProductService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/products", produces = "application/json")
public class ProductController {

    private final ProductService service;

    /* =====================================================
       GET ALL
       ===================================================== */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    /** Liste des produits vedettes (featured = true). */
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> listFeatured() {
        return ResponseEntity.ok(service.listFeatured());
    }

    /**
     * Ce n'est pas pour l'application mpssarl mais pour l'application e commerche. voici les controlleurs qui existe donne le controleur /api/vitrine
     * Liste des produits pour le carrousel (inCarousel = true),
     * triés par carouselRank asc puis createdAt desc.
     */
    @GetMapping("/carousel")
    public ResponseEntity<List<ProductResponse>> listCarousel() {
        return ResponseEntity.ok(service.listCarousel());
    }

    /* =====================================================
       GET BY UUID
       ===================================================== */
    @GetMapping("/{uuid}")
    public ResponseEntity<ProductResponse> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.getByUuid(uuid));
    }

    /* =====================================================
       SEARCH (optionnel)
       ===================================================== */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
        // Ici version simple basée sur service.listAll()
        List<ProductResponse> results = service.listAll().stream()
                .filter(p -> q == null || p.name().toLowerCase().contains(q.toLowerCase()))
                .filter(p -> category == null || (p.category() != null &&
                        p.category().equalsIgnoreCase(category)))
                .toList();

        return ResponseEntity.ok(results);
    }

    /* =====================================================
       CREATE
       ===================================================== */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ProductResponse> create(@RequestBody @Valid CreateProductRequest req) {

        ProductResponse created = service.create(req);

        // URL du nouveau produit créé : /api/products/{uuid}
        URI location = URI.create("/api/products/" + created.uuid());

        return ResponseEntity.created(location).body(created);
    }

    /* =====================================================
       UPDATE (PATCH-like)
       ===================================================== */
    @PatchMapping(value = "/{uuid}", consumes = "application/json")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID uuid,
            @RequestBody @Valid UpdateProductRequest req) {

        ProductResponse updated = service.update(uuid, req);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{uuid}/flags")
    public ResponseEntity<ProductResponse> updateFlags(@PathVariable UUID uuid,
                                                       @RequestBody @Valid UpdateProductFlagsRequest req) {
        return ResponseEntity.ok(service.updateFlags(uuid, req));
    }

    /* =====================================================
       DELETE
       ===================================================== */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
