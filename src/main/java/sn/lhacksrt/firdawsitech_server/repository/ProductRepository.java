package sn.lhacksrt.firdawsitech_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sn.lhacksrt.firdawsitech_server.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByUuid(UUID uuid);

    Optional<Product> findBySlug(String slug);

    boolean existsByUuid(UUID uuid);

    /** Liste triée récente (tous produits) */
    List<Product> findAllByOrderByCreatedAtDesc();

    /** Produits en stock par catégorie (insensible à la casse) */
    @Query("""
        select p from Product p
        where p.inStock = true
          and (:category is null or lower(p.category) = lower(:category))
        order by p.createdAt desc
    """)
    List<Product> listInStockByCategory(String category);

    /** Recherche simple (nom contient) + filtre catégorie optionnel */
    @Query("""
        select p from Product p
        where (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
          and (:category is null or lower(p.category) = lower(:category))
        order by p.createdAt desc
    """)
    List<Product> search(String q, String category);


    // vedettes
    List<Product> findAllByFeaturedTrueOrderByCreatedAtDesc();

    // carrousel (ordre prioritaire sur rank, puis récent)
    List<Product> findAllByInCarouselTrueOrderByCarouselRankAscCreatedAtDesc();
}
