package com.anju.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadChunkRequest {
    @NotBlank(message = "Upload ID is required")
    private String uploadId;
    
    @NotNull(message = "Chunk index is required")
    private Integer chunkIndex;
    
    @NotNull(message = "Total chunks is required")
    private Integer totalChunks;
    
    private byte[] data;
    
    private String mimeType;
}
