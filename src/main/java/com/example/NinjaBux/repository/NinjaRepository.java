package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.enums.BeltType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NinjaRepository extends JpaRepository<Ninja, Long> {
    Optional<Ninja> findByFirstNameAndLastName(String firstName, String lastName);
    Optional<Ninja> findByUsernameIgnoreCase(String username);
    
    // Pagination and filtering methods
    Page<Ninja> findAll(Pageable pageable);
    
    @Query("SELECT n FROM Ninja n WHERE " +
           "(:name IS NULL OR LOWER(n.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(n.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(n.username) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:belt IS NULL OR n.currentBeltType = :belt) AND " +
           "(:locked IS NULL OR n.isLocked = :locked)")
    Page<Ninja> findByFilters(@Param("name") String name, 
                              @Param("belt") BeltType belt, 
                              @Param("locked") Boolean locked, 
                              Pageable pageable);
}
