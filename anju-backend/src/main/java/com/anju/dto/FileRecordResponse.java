package com.anju.dto;

import com.anju.entity.FileRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecordResponse {
    private Long id;
    private String logicalId;
    private String fileHash;
    private String originalName;
    private String fileType;
    private Long size;
    private Integer versionNumber;
    private Boolean isDeleted;
    private Boolean isActive;
    private LocalDateTime expirationTime;
    private String mimeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<FileVersionInfo> versionHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileVersionInfo {
        private Long id;
        private Integer versionNumber;
        private LocalDateTime createdAt;
        private Boolean isActive;
        
        public static FileVersionInfo fromEntity(FileRecord record) {
            return FileVersionInfo.builder()
                    .id(record.getId())
                    .versionNumber(record.getVersionNumber())
                    .createdAt(record.getCreatedAt())
                    .isActive(record.getIsActive())
                    .build();
        }
    }

    public static FileRecordResponse fromEntity(FileRecord record) {
        return FileRecordResponse.builder()
                .id(record.getId())
                .logicalId(record.getLogicalId())
                .fileHash(record.getFileHash())
                .originalName(record.getOriginalName())
                .fileType(record.getFileType())
                .size(record.getSize())
                .versionNumber(record.getVersionNumber())
                .isDeleted(record.getIsDeleted())
                .isActive(record.getIsActive())
                .expirationTime(record.getExpirationTime())
                .mimeType(record.getMimeType())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .deletedAt(record.getDeletedAt())
                .build();
    }

    public static FileRecordResponse fromEntityWithHistory(FileRecord record, List<FileRecord> history) {
        FileRecordResponse response = fromEntity(record);
        if (history != null && !history.isEmpty()) {
            response.setVersionHistory(history.stream()
                    .map(FileVersionInfo::fromEntity)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}
