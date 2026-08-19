package sn.lhacksrt.firdawsitech_server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_products_in_stock", columnList = "in_stock"),
                @Index(name = "idx_products_created_at", columnList = "created_at"),
                @Index(name = "idx_products_featured", columnList = "featured"),
                @Index(name = "idx_products_in_carousel", columnList = "in_carousel"),
                @Index(name = "idx_products_carousel_rank", columnList = "carousel_rank")
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    /** Identifiant public unique */
    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Size(max = 120)
    private String category;

    /** URL de l'image */
    @Size(max = 1024)
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    // ...
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<ProductImage> images = new java.util.ArrayList<>();

    /** aide: URL de l’image principale (dérivé des images) */
    @Transient
    public String getPrimaryImageUrl() {
        return images.stream()
                .filter(ProductImage::getPrimaryImage)
                .sorted(java.util.Comparator.comparing(ProductImage::getSortOrder, java.util.Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .findFirst().orElse(null);
    }

    @Lob
    private String description;

    /** specs : liste de caractéristiques */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_specs",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "spec", length = 255)
    @Builder.Default
    private List<String> specs = new ArrayList<>();

    /** Produit disponible à la vente (piloté automatiquement via stock) */
    @Column(name = "in_stock", nullable = false)
    private Boolean inStock;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = Boolean.FALSE;

    @Column(name = "in_carousel", nullable = false)
    @Builder.Default
    private Boolean inCarousel = Boolean.FALSE;

    /** Ordre dans le carrousel (1 = premier). Null si pas dans le carrousel. */
    @Column(name = "carousel_rank")
    private Integer carouselRank;

    /** Note moyenne */
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    /** audit */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /* --------------------- Hooks JPA --------------------- */

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (rating == null) rating = BigDecimal.ZERO;

        if (featured == null) featured = Boolean.FALSE;
        if (inCarousel == null) inCarousel = Boolean.FALSE;
        if (!inCarousel) carouselRank = null; // cohérence

        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        // garde inStock cohérent si quelqu’un modifie stock
        updatedAt = Instant.now();

        if (Boolean.FALSE.equals(inCarousel)) carouselRank = null;
        if (carouselRank != null && carouselRank < 1) {
            throw new IllegalArgumentException("carouselRank doit être >= 1");
        }
    }
}