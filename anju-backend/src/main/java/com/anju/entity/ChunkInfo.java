package com.anju.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chunk_info", indexes = {
    @Index(name = "idx_chunk_upload_id", columnList = "upload_id", unique = true),
    @Index(name = "idx_chunk_file_hash", columnList = "file_hash")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_id", nullable = false, unique = true, length = 64)
    private String uploadId;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "logical_id", length = 64)
    private String logicalId;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "total_chunks", nullable = false)
    private Integer totalChunks;

    @Column(name = "uploaded_bytes", nullable = false)
    private Long uploadedBytes;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_username", nullable = false, length = 100)
    private String operatorUsername;

    @Column(name = "operator_role", nullable = false, length = 50)
    private String operatorRole;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
