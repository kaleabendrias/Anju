package com.anju.repository;

import com.anju.entity.IdempotencyEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyEntryRepository extends JpaRepository<IdempotencyEntry, Long> {

    Optional<IdempotencyEntry> findByOperationAndIdempotencyKey(String operation, String idempotencyKey);

    @Modifying
    @Query("DELETE FROM IdempotencyEntry e WHERE e.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    long countByExpiresAtAfter(LocalDateTime now);
}
