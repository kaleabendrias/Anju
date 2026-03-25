package com.anju.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkUploadResponse {
    private String uploadId;
    private Integer chunkIndex;
    private Integer totalChunks;
    private boolean uploaded;
    private boolean complete;
    private Long fileId;
    private String fileHash;
    private String message;
}
