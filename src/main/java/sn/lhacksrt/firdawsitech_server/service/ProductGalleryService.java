package sn.lhacksrt.firdawsitech_server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.lhacksrt.firdawsitech_server.domain.Product;
import sn.lhacksrt.firdawsitech_server.domain.ProductImage;
import sn.lhacksrt.firdawsitech_server.dto.ProductImageResponse;
import sn.lhacksrt.firdawsitech_server.repository.ProductImageRepository;
import sn.lhacksrt.firdawsitech_server.repository.ProductRepository;
import sn.lhacksrt.firdawsitech_server.web.error.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductGalleryService {

    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ImageStorageService storage;

    @Transactional(readOnly = true)
    public List<ProductImageResponse> list(UUID productUuid) {
        Product p = products.findByUuid(productUuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));
        return images.findAllByProductOrderBySortOrderAscCreatedAtAsc(p).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public List<ProductImageResponse> upload(UUID productUuid, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) throw new IllegalArgumentException("Aucun fichier fourni.");
        Product p = products.findByUuid(productUuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));

        int nextOrder = p.getImages().stream().map(ProductImage::getSortOrder)
                .filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;

        List<ProductImageResponse> out = new ArrayList<>();
        for (MultipartFile file : files) {
            var stored = storage.store(file);
            ProductImage img = ProductImage.builder()
                    .product(p)
                    .url(stored.url())
                    .contentType(stored.contentType())
                    .sizeBytes(stored.size())
                    .sortOrder(nextOrder++)
                    .primaryImage(false)
                    .build();
            images.save(img);
            p.getImages().add(img);
            out.add(toResponse(img));
        }

        // Si le produit n’a pas encore d’image principale, définir la première ajoutée
        if (p.getImages().stream().noneMatch(i -> Boolean.TRUE.equals(i.getPrimaryImage()))) {
            p.getImages().stream().min(Comparator.comparing(ProductImage::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                    .ifPresent(i -> {
                        i.setPrimaryImage(true);
                        images.save(i);
                    });
        }

        products.save(p);
        return out;
    }

    @Transactional
    public void delete(UUID productUuid, UUID imageUuid) {
        Product p = products.findByUuid(productUuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));
        ProductImage img = images.findByUuid(imageUuid).orElseThrow(() -> new NotFoundException("Image introuvable"));

        if (!img.getProduct().getId().equals(p.getId()))
            throw new IllegalArgumentException("Cette image n’appartient pas au produit.");

        // supprimer fichier local si hébergé localement
        storage.deleteIfLocalUrl(img.getUrl());

        boolean wasPrimary = Boolean.TRUE.equals(img.getPrimaryImage());
        p.getImages().remove(img);
        images.delete(img);

        // si on supprime la principale → choisir la suivante
        if (wasPrimary && !p.getImages().isEmpty()) {
            p.getImages().stream()
                    .sorted(Comparator.comparing(ProductImage::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                    .findFirst()
                    .ifPresent(next -> {
                        next.setPrimaryImage(true);
                        images.save(next);
                    });
        }
        products.save(p);
    }

    @Transactional
    public void setPrimary(UUID productUuid, UUID imageUuid) {
        Product p = products.findByUuid(productUuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));
        ProductImage img = images.findByUuid(imageUuid).orElseThrow(() -> new NotFoundException("Image introuvable"));

        if (!img.getProduct().getId().equals(p.getId()))
            throw new IllegalArgumentException("Cette image n’appartient pas au produit.");

        // tout désactiver
        p.getImages().forEach(i -> { if (Boolean.TRUE.equals(i.getPrimaryImage())) { i.setPrimaryImage(false); images.save(i); } });
        // activer celle-ci
        img.setPrimaryImage(true);
        images.save(img);
        products.save(p);
    }

    @Transactional
    public void reorder(UUID productUuid, List<ReorderEntry> entries) {
        Product p = products.findByUuid(productUuid).orElseThrow(() -> new NotFoundException("Produit introuvable"));
        Map<UUID, Integer> map = new HashMap<>();
        for (ReorderEntry e : entries) {
            if (e.sortOrder() == null || e.sortOrder() < 1)
                throw new IllegalArgumentException("sortOrder doit être ≥ 1");
            map.put(e.imageUuid(), e.sortOrder());
        }
        for (ProductImage img : p.getImages()) {
            Integer so = map.get(img.getUuid());
            if (so != null) img.setSortOrder(so);
        }
        // normalisation facultative: recompacte 1..N
        int n = 1;
        for (ProductImage img : p.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::getSortOrder, Comparator.nullsLast(Integer::compareTo))).toList()) {
            img.setSortOrder(n++);
            images.save(img);
        }
        products.save(p);
    }

    private ProductImageResponse toResponse(ProductImage i) {
        return new ProductImageResponse(i.getUuid(), i.getUrl(), i.getContentType(), i.getSizeBytes(), i.getSortOrder(), Boolean.TRUE.equals(i.getPrimaryImage()));
    }

    public record ReorderEntry(UUID imageUuid, Integer sortOrder) {}
}
