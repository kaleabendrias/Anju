package com.anju.repository;

import com.anju.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    
    Optional<FileRecord> findByFileHash(String fileHash);
    
    boolean existsByFileHash(String fileHash);
    
    Optional<FileRecord> findByLogicalIdAndIsActiveTrue(String logicalId);
    
    List<FileRecord> findByLogicalIdOrderByVersionNumberDesc(String logicalId);
    
    Optional<FileRecord> findByIdAndIsDeletedFalse(Long id);
    
    List<FileRecord> findByIsDeletedTrueAndExpirationTimeBefore(LocalDateTime expirationTime);
    
    List<FileRecord> findByIsDeletedTrue();
    
    @Modifying
    @Query("UPDATE FileRecord f SET f.isDeleted = true, f.expirationTime = :expirationTime, " +
           "f.deletedAt = :deletedAt, f.deletedBy = :deletedBy WHERE f.id = :id")
    void softDelete(@Param("id") Long id, 
                    @Param("expirationTime") LocalDateTime expirationTime,
                    @Param("deletedAt") LocalDateTime deletedAt,
                    @Param("deletedBy") Long deletedBy);
    
    @Query("SELECT f FROM FileRecord f WHERE f.isDeleted = true AND f.expirationTime < :now")
    List<FileRecord> findExpiredDeletedFiles(@Param("now") LocalDateTime now);
}
