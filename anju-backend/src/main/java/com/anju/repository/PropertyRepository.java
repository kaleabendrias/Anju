package com.anju.repository;

import com.anju.entity.Property;
import com.anju.entity.Property.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Optional<Property> findByUniqueCode(String uniqueCode);
    boolean existsByUniqueCode(String uniqueCode);
    List<Property> findByStatus(PropertyStatus status);
}
