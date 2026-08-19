package sn.lhacksrt.firdawsitech_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sn.lhacksrt.firdawsitech_server.dto.CreateOrderRequest;
import sn.lhacksrt.firdawsitech_server.dto.OrderItemDTO;
import sn.lhacksrt.firdawsitech_server.dto.OrderResponse;
import sn.lhacksrt.firdawsitech_server.dto.ProductResponse;
import sn.lhacksrt.firdawsitech_server.service.OrderService;
import sn.lhacksrt.firdawsitech_server.service.ProductService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/shop", produces = "application/json")
public class BoutiqueController {

    private final ProductService products;
    private final OrderService orders;

    /**
     * Endpoint principal de la Boutique (Shop)
     * - Filtre: q (texte), category, inStock, minPrice, maxPrice
     * - Tri: sort (priceAsc|priceDesc|newest|ratingDesc|nameAsc)
     * - Pagination: page (0-based), size
     * Retourne: items + meta pagination + facettes (catégories+compte) + bornes de prix.
     */
    @GetMapping
    public ResponseEntity<BoutiqueResponse> shop(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        // 1) On récupère tout (MVP). Pour la prod: à remplacer par requêtes filtrées en base.
        List<ProductResponse> all = products.listAll();

        // 2) Filtrage
        List<ProductResponse> filtered = all.stream()
                .filter(p -> q == null || q.isBlank()
                        || p.name().toLowerCase().contains(q.toLowerCase())
                        || (p.description() != null && p.description().toLowerCase().contains(q.toLowerCase())))
                .filter(p -> category == null || category.isBlank()
                        || (p.category() != null && p.category().equalsIgnoreCase(category)))
                .filter(p -> inStock == null || p.inStock() == inStock)
                .filter(p -> minPrice == null || p.price().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.price().compareTo(maxPrice) <= 0)
                .toList();

        // 3) Facettes catégories (sur l’ensemble filtré, pas paginé)
        Map<String, Long> categoryCounts = filtered.stream()
                .collect(Collectors.groupingBy(
                        p -> Optional.ofNullable(p.category()).orElse("Autres"),
                        TreeMap::new, // tri alpha
                        Collectors.counting()
                ));

        // 4) Bornes de prix (sur l’ensemble filtré)
        Optional<BigDecimal> minFound = filtered.stream().map(ProductResponse::price).min(BigDecimal::compareTo);
        Optional<BigDecimal> maxFound = filtered.stream().map(ProductResponse::price).max(BigDecimal::compareTo);

        // 5) Tri
        Comparator<ProductResponse> comparator = switch (sort.toLowerCase()) {
            case "priceasc"  -> Comparator.comparing(ProductResponse::price);
            case "pricedesc" -> Comparator.comparing(ProductResponse::price).reversed();
            case "ratingdesc"-> Comparator.comparing(
                    p -> Optional.ofNullable(p.rating()).orElse(BigDecimal.ZERO), Comparator.reverseOrder());
            case "nameasc"   -> Comparator.comparing(ProductResponse::name, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(
                    p -> Optional.ofNullable(p.createdAt()).orElse(""),
                    Comparator.reverseOrder() // plus récent d'abord
            );
        };
        List<ProductResponse> sorted = filtered.stream().sorted(comparator).toList();

        // 6) Pagination (page 0-based)
        int total = sorted.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<ProductResponse> pageItems = sorted.subList(from, to);

        // 7) Réponse
        BoutiqueResponse resp = new BoutiqueResponse(
                pageItems,
                new PageMeta(page, size, total, (int) Math.ceil(total / (double) size)),
                categoryCounts.entrySet().stream()
                        .map(e -> new CategoryFacet(e.getKey(), e.getValue()))
                        .toList(),
                new PriceRange(minFound.orElse(BigDecimal.ZERO), maxFound.orElse(BigDecimal.ZERO))
        );
        return ResponseEntity.ok(resp);
    }

    /**
     * Passer commande avec un panier (items: productUuid + quantity).
     * Réutilise la logique d'OrderService.create(...) : vérifie le stock, recalcule les prix, etc.
     *
     * Exemple body:
     * {
     *   "items": [
     *     { "productUuid": "...", "quantity": 2 },
     *     { "productUuid": "...", "quantity": 1 }
     *   ]
     * }
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestBody @Valid CreateOrderRequest body,
                                                  UriComponentsBuilder uri) {
        OrderResponse created = orders.create(body);
        return ResponseEntity.created(
                uri.path("/api/orders/{uuid}").buildAndExpand(created.uuid()).toUri()
        ).body(created);
    }

    /**
     * Achat rapide d'un seul produit (utile depuis un bouton "Acheter maintenant").
     * /api/boutique/checkout/quick?productUuid=...&qty=1
     */
    @PostMapping("/checkout/quick")
    public ResponseEntity<OrderResponse> quickCheckout(@RequestParam("productUuid") UUID productUuid,
                                                       @RequestParam(name = "qty", defaultValue = "1") int qty,
                                                       @RequestParam("customerName") String customerName,
                                                       @RequestParam("customerEmail") String customerEmail,
                                                       @RequestParam("customerPhone") String customerPhone,
                                                       UriComponentsBuilder uri) {
        if (qty < 1) qty = 1;
        CreateOrderRequest req = new CreateOrderRequest(
                List.of(new OrderItemDTO(productUuid, qty)),
                customerName, customerEmail, customerPhone
        );
        OrderResponse created = orders.create(req);
        return ResponseEntity.created(
                uri.path("/api/orders/{uuid}").buildAndExpand(created.uuid()).toUri()
        ).body(created);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ProductResponse> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(products.getByUuid(uuid));
    }


    /* ====== DTOs de réponse ====== */

    public record BoutiqueResponse(
            List<ProductResponse> items,
            PageMeta page,
            List<CategoryFacet> categories,
            PriceRange priceRange
    ) {}

    public record PageMeta(
            int page,
            int size,
            int totalElements,
            int totalPages
    ) {}

    public record CategoryFacet(
            String name,
            long count
    ) {}

    public record PriceRange(
            BigDecimal min,
            BigDecimal max
    ) {}
}
