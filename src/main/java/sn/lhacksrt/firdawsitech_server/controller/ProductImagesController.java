package sn.lhacksrt.firdawsitech_server.controller;

import  sn.lhacksrt.firdawsitech_server.dto.ProductImageResponse;
import  sn.lhacksrt.firdawsitech_server.service.ProductGalleryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/products", produces = "application/json")
public class ProductImagesController {

    private final ProductGalleryService gallery;

    /* Liste des images d’un produit */
    @GetMapping("/{uuid}/images")
    public ResponseEntity<List<ProductImageResponse>> list(@PathVariable java.util.UUID uuid) {
        return ResponseEntity.ok(gallery.list(uuid));
    }

    /* Upload MULTIPLE: form-data avec "files": File[] */
    @PostMapping(value = "/{uuid}/images", consumes = "multipart/form-data")
    public ResponseEntity<List<ProductImageResponse>> upload(
            @PathVariable java.util.UUID uuid,
            @RequestPart("files") MultipartFile[] files) {
        return ResponseEntity.ok(gallery.upload(uuid, Arrays.asList(files)));
    }

    /* Définir l’image principale */
    @PostMapping("/{uuid}/images/{imageUuid}/primary")
    public ResponseEntity<Void> setPrimary(@PathVariable java.util.UUID uuid,
                                           @PathVariable java.util.UUID imageUuid) {
        gallery.setPrimary(uuid, imageUuid);
        return ResponseEntity.noContent().build();
    }

    /* Réordonner la galerie: [{imageUuid, sortOrder}, ...] */
    public record ReorderPayload(List<Entry> entries) { public record Entry(java.util.UUID imageUuid, @Min(1) Integer sortOrder) {} }

    @PatchMapping("/{uuid}/images/reorder")
    public ResponseEntity<Void> reorder(@PathVariable java.util.UUID uuid,
                                        @RequestBody @Valid ReorderPayload payload) {
        var list = payload.entries().stream()
                .map(e -> new ProductGalleryService.ReorderEntry(e.imageUuid(), e.sortOrder()))
                .toList();
        gallery.reorder(uuid, list);
        return ResponseEntity.noContent().build();
    }

    /* Supprimer une image (supprime aussi le fichier local si applicable) */
    @DeleteMapping("/{uuid}/images/{imageUuid}")
    public ResponseEntity<Void> delete(@PathVariable java.util.UUID uuid,
                                       @PathVariable java.util.UUID imageUuid) {
        gallery.delete(uuid, imageUuid);
        return ResponseEntity.noContent().build();
    }
}
