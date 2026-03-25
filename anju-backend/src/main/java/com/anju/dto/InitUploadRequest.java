package com.anju.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitUploadRequest {
    @NotBlank(message = "File hash is required")
    private String fileHash;
    
    @NotBlank(message = "Original name is required")
    private String originalName;
    
    @NotBlank(message = "File type is required")
    private String fileType;
    
    private Long size;
    
    private String logicalId;
    
    private String mimeType;
    
    private Integer totalChunks;
}
