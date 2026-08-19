package sn.lhacksrt.firdawsitech_server.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.lhacksrt.firdawsitech_server.repository.ProductRepository;
import sn.lhacksrt.firdawsitech_server.service.ImageStorageService;
import sn.lhacksrt.firdawsitech_server.web.error.NotFoundException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/products", produces = "application/json")
public class ProductImageController {

    private final ProductRepository products;
    private final ImageStorageService storage;

    @PostMapping(value = "/{uuid}/image", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadProductImage(@PathVariable UUID uuid,
                                                                  @RequestPart("file") @NotNull MultipartFile file) {
        var p = products.findByUuid(uuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));

        // si image locale existait déjà, on peut la supprimer
        if (p.getImageUrl() != null) storage.deleteIfLocalUrl(p.getImageUrl());

        var stored = storage.store(file);
        p.setImageUrl(stored.url());
        products.save(p);

        return ResponseEntity.ok(Map.of(
                "productUuid", p.getUuid(),
                "imageUrl", p.getImageUrl()
        ));
    }

    @DeleteMapping("/{uuid}/image")
    @Transactional
    public ResponseEntity<Void> deleteProductImage(@PathVariable UUID uuid) {
        var p = products.findByUuid(uuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));
        if (p.getImageUrl() != null) {
            storage.deleteIfLocalUrl(p.getImageUrl());
            p.setImageUrl(null);
            products.save(p);
        }
        return ResponseEntity.noContent().build();
    }
}
