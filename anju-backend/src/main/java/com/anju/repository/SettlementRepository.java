package com.anju.repository;

import com.anju.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    
    Optional<Settlement> findBySettlementDate(LocalDate settlementDate);
    
    boolean existsBySettlementDate(LocalDate settlementDate);
}
