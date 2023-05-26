package br.edu.unifip.ecommerceapi.repositories;

import br.edu.unifip.ecommerceapi.models.Product;
import jakarta.persistence.NamedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findById(UUID id);

    void delete(Product product);

    List<Product> findByActiveTrue();
    @Query("SELECT p FROM Product p WHERE p.name = :name")
    List<Product> findByName(@Param("name") String name);
    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.name = :name")
    List<Product> findByCategoryName(@Param("name") String name);
}
