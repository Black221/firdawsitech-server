package sn.lhacksrt.firdawsitech_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.lhacksrt.firdawsitech_server.domain.Product;
import sn.lhacksrt.firdawsitech_server.domain.ProductImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByUuid(UUID uuid);
    List<ProductImage> findAllByProductOrderBySortOrderAscCreatedAtAsc(Product product);
}
