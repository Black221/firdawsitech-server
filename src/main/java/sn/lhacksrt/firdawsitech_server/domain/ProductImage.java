package sn.lhacksrt.firdawsitech_server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "product_images",
    indexes = {
        @Index(name = "idx_product_images_product_id", columnList = "product_id"),
        @Index(name = "idx_product_images_sort_order", columnList = "sort_order")
    },
    uniqueConstraints = @UniqueConstraint(name = "uk_product_images_uuid", columnNames = "uuid")
)
public class ProductImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID uuid;

    @ManyToOne(optional = false) @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    /** tri d’affichage dans la galerie (1,2,3,…) */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** image principale du produit */
    @Column(name = "is_primary", nullable = false)
    private Boolean primaryImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (primaryImage == null) primaryImage = Boolean.FALSE;
        if (createdAt == null) createdAt = Instant.now();
    }
}
