package br.edu.unifip.ecommerceapi.repositories;


import br.edu.unifip.ecommerceapi.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findById(UUID id);
    void delete(User user);
    List<User> findByActiveTrue();

    @Query("SELECT p FROM User p WHERE p.name = :name")
    List<User> findByName(@Param("name") String name);
    @Query("SELECT p FROM User p JOIN p.category c WHERE c.name = :name")
    List<User> findByCategoryName(@Param("name") String name);



}
