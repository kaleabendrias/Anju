package com.anju.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitUploadResponse {
    private String uploadId;
    private boolean deduplicated;
    private Long fileId;
    private String fileHash;
    private Integer versionNumber;
    private String message;
}
