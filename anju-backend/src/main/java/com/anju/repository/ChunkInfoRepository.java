package com.anju.repository;

import com.anju.entity.ChunkInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChunkInfoRepository extends JpaRepository<ChunkInfo, Long> {
    
    Optional<ChunkInfo> findByUploadId(String uploadId);
    
    Optional<ChunkInfo> findByUploadIdAndIsCompletedFalse(String uploadId);
    
    void deleteByUploadId(String uploadId);
}
