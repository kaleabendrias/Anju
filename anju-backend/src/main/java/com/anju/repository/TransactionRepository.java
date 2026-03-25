package com.anju.repository;

import com.anju.entity.Transaction;
import com.anju.entity.Transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    boolean existsByIdempotencyKey(String idempotencyKey);
    
    Optional<Transaction> findByTrxId(String trxId);
    
    List<Transaction> findByAppointmentId(Long appointmentId);
    
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<Transaction> findByType(TransactionType type);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.timestamp BETWEEN :start AND :end AND t.type = :type")
    BigDecimal sumAmountByTypeAndTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("type") TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "WHERE t.timestamp BETWEEN :start AND :end")
    Integer countByTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Transaction t WHERE t.originalTransactionId = :originalId AND t.type = 'REFUND'")
    List<Transaction> findRefundsByOriginalTransactionId(@Param("originalId") Long originalId);
}
