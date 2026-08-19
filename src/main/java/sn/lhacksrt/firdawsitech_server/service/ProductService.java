package sn.lhacksrt.firdawsitech_server.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.lhacksrt.firdawsitech_server.domain.Product;
import sn.lhacksrt.firdawsitech_server.domain.ProductImage;
import sn.lhacksrt.firdawsitech_server.dto.*;
import sn.lhacksrt.firdawsitech_server.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    /* ============================================================
       CREATE
       ============================================================ */
    @Transactional
    public ProductResponse create(CreateProductRequest r) {
        Product p = Product.builder()
                .uuid(UUID.randomUUID())
                .name(r.name())
                .price(r.price())
                .category(r.category())
                .description(r.description())
                .inStock(r.inStock())
                .rating(r.rating())
                .build();

        if (r.specs() != null) {
            p.getSpecs().addAll(r.specs());
        }

        repo.save(p);
        return toResponse(p);
    }


    /* ============================================================
       GET
       ============================================================ */
    @Transactional(readOnly = true)
    public ProductResponse getByUuid(UUID uuid) {
        Product p = repo.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));

        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return repo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ============================================================
   LISTE DES PRODUITS VEDETTES
   ============================================================ */
    @Transactional(readOnly = true)
    public List<ProductResponse> listFeatured() {
        return repo.findAllByFeaturedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ============================================================
       LISTE DES PRODUITS POUR LE CARROUSEL
       ============================================================ */
    @Transactional(readOnly = true)
    public List<ProductResponse> listCarousel() {
        return repo.findAllByInCarouselTrueOrderByCarouselRankAscCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ============================================================
       UPDATE
       ============================================================ */
    @Transactional
    public ProductResponse update(UUID uuid, UpdateProductRequest r) {
        Product p = repo.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));

        // On met à jour uniquement les champs fournis
        if (r.name() != null) p.setName(r.name());
        if (r.price() != null) p.setPrice(r.price());
        if (r.category() != null) p.setCategory(r.category());
        if (r.description() != null) p.setDescription(r.description());
        if (r.inStock() != null) p.setInStock(r.inStock());
        if (r.rating() != null) p.setRating(r.rating());

        if (r.specs() != null) {
            p.getSpecs().clear();
            p.getSpecs().addAll(r.specs());
        }

        return toResponse(repo.save(p));
    }

    @Transactional
    public ProductResponse updateFlags(UUID uuid, UpdateProductFlagsRequest r) {
        Product p = repo.findByUuid(uuid).orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));
        if (r.featured() != null) p.setFeatured(r.featured());
        if (r.inCarousel() != null) p.setInCarousel(r.inCarousel());
        if (r.carouselRank() != null) p.setCarouselRank(r.carouselRank());
        return toResponse(repo.save(p));
    }

    /* ============================================================
       DELETE
       ============================================================ */
    @Transactional
    public void delete(UUID uuid) {
        Product p = repo.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));

        repo.delete(p);
    }

    /* ============================================================
       MAPPER
       ============================================================ */
    private ProductResponse toResponse(Product p) {
        var imgs = p.getImages().stream()
                .sorted(java.util.Comparator.comparing(ProductImage::getSortOrder, java.util.Comparator.nullsLast(Integer::compareTo)))
                .map(i -> new ProductImageResponse(i.getUuid(), i.getUrl(), i.getContentType(), i.getSizeBytes(), i.getSortOrder(), Boolean.TRUE.equals(i.getPrimaryImage())))
                .toList();

        return new ProductResponse(
                p.getUuid(), p.getName(), p.getPrice(), p.getCategory(),
                p.getPrimaryImageUrl(), // ou p.getImageUrl() si tu conserves le champ
                p.getDescription(), p.getSpecs(), p.getInStock(), p.getRating(),
                Boolean.TRUE.equals(p.getFeatured()), Boolean.TRUE.equals(p.getInCarousel()), p.getCarouselRank(),
                imgs,
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null,
                p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null
        );
    }
}
