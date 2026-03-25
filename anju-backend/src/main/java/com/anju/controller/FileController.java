package com.anju.controller;

import com.anju.dto.*;
import com.anju.security.UserPrincipal;
import com.anju.service.FileService;
import com.anju.service.FileThrottleService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final FileThrottleService fileThrottleService;

    public FileController(FileService fileService, FileThrottleService fileThrottleService) {
        this.fileService = fileService;
        this.fileThrottleService = fileThrottleService;
    }

    @PostMapping("/upload/init")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<InitUploadResponse>> initUpload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody InitUploadRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        if (!fileThrottleService.tryAcquireUploadSlot(principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Upload concurrency limit reached. Please try again later."));
        }
        
        try {
            InitUploadResponse response = fileService.initUpload(request, principal.getId(), 
                    principal.getUsername(), principal.getRole());
            return ResponseEntity.ok(ApiResponse.success(response));
        } finally {
            fileThrottleService.releaseUploadSlot(principal.getUsername());
        }
    }

    @PostMapping("/upload/chunk")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<ChunkUploadResponse>> uploadChunk(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String uploadId,
            @RequestParam Integer chunkIndex,
            @RequestParam Integer totalChunks,
            @RequestParam(required = false) byte[] data) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        if (data != null) {
            fileThrottleService.recordBytesUploaded(principal.getUsername(), data.length);
            FileThrottleService.ThrottleStatus status = fileThrottleService.getUploadStatus(principal.getUsername());
            if (!status.allowed()) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Upload bandwidth limit exceeded. Please try again later."));
            }
        }
        
        ChunkUploadResponse response = fileService.uploadChunk(uploadId, chunkIndex, totalChunks, data, 
                principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/upload/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<FileRecordResponse>> completeUpload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String uploadId) throws IOException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        FileRecordResponse response = fileService.completeUpload(uploadId, principal.getId(), 
                principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Upload completed", response));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<FileRecordResponse>> uploadSimple(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String logicalId) throws IOException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        if (!fileThrottleService.tryAcquireUploadSlot(principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Upload concurrency limit reached. Please try again later."));
        }
        
        try {
            fileThrottleService.recordBytesUploaded(principal.getUsername(), file.getSize());
            FileThrottleService.ThrottleStatus status = fileThrottleService.getUploadStatus(principal.getUsername());
            if (!status.allowed()) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Upload bandwidth limit exceeded. Please try again later."));
            }
            
            FileRecordResponse response = fileService.uploadSimple(file, logicalId, principal.getId(), 
                    principal.getUsername(), principal.getRole());
            return ResponseEntity.ok(ApiResponse.success("File uploaded", response));
        } finally {
            fileThrottleService.releaseUploadSlot(principal.getUsername());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileRecordResponse>> getFileInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        FileRecordResponse response = fileService.getFileInfo(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
        
        if (!fileThrottleService.tryAcquireDownloadSlot(principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        try {
            FileRecordResponse fileInfo = fileService.getFileInfo(id, principal.getId(), principal.getRole());
            Resource resource = fileService.loadFileAsResource(id, principal.getId(), principal.getRole());
            
            fileThrottleService.recordBytesDownloaded(principal.getUsername(), fileInfo.getSize());
            
            String contentType = determineContentType(fileInfo.getMimeType(), fileInfo.getFileType());
            String contentDisposition = determineContentDisposition(contentType, fileInfo.getOriginalName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.getSize()))
                    .body(resource);
        } finally {
            fileThrottleService.releaseDownloadSlot(principal.getUsername());
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        if (!fileThrottleService.tryAcquireDownloadSlot(principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        
        try {
            FileRecordResponse fileInfo = fileService.getFileInfo(id, principal.getId(), principal.getRole());
            Resource resource = fileService.loadFileAsResource(id, principal.getId(), principal.getRole());

            fileThrottleService.recordBytesDownloaded(principal.getUsername(), fileInfo.getSize());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileInfo.getOriginalName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.getSize()))
                    .body(resource);
        } finally {
            fileThrottleService.releaseDownloadSlot(principal.getUsername());
        }
    }

    @GetMapping("/logical/{logicalId}/versions")
    public ResponseEntity<ApiResponse<List<FileRecordResponse>>> getFileVersions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String logicalId) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<FileRecordResponse> versions = fileService.getFileVersions(logicalId, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(versions));
    }

    @PostMapping("/{logicalId}/rollback/{versionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<FileRecordResponse>> rollbackToVersion(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String logicalId,
            @PathVariable Long versionId,
            @RequestParam(required = false) String secondaryPassword) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        FileRecordResponse response = fileService.rollbackToVersion(logicalId, versionId, principal.getId(),
                principal.getUsername(), principal.getRole(), secondaryPassword);
        return ResponseEntity.ok(ApiResponse.success("File rolled back to version " + 
                response.getVersionNumber(), response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        fileService.softDeleteFile(id, principal.getId(), principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("File moved to recycle bin", null));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<Void>> restoreFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        fileService.restoreFile(id, principal.getId(), principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("File restored", null));
    }

    @GetMapping("/recycle-bin")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<List<FileRecordResponse>>> getDeletedFiles(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<FileRecordResponse> files = fileService.getDeletedFiles(principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) String secondaryPassword) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        fileService.permanentlyDeleteFile(id, principal.getId(), principal.getUsername(), 
                principal.getRole(), secondaryPassword);
        return ResponseEntity.ok(ApiResponse.success("File permanently deleted", null));
    }

    @GetMapping("/throttle/status")
    public ResponseEntity<ApiResponse<ThrottleStatusResponse>> getThrottleStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        
        FileThrottleService.ThrottleStatus uploadStatus = fileThrottleService.getUploadStatus(principal.getUsername());
        FileThrottleService.ThrottleStatus downloadStatus = fileThrottleService.getDownloadStatus(principal.getUsername());
        
        ThrottleStatusResponse response = new ThrottleStatusResponse(
                uploadStatus.allowed(), uploadStatus.usedBytes(), uploadStatus.limitBytes(), uploadStatus.remainingBytes(),
                downloadStatus.allowed(), downloadStatus.usedBytes(), downloadStatus.limitBytes(), downloadStatus.remainingBytes()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String determineContentType(String mimeType, String fileType) {
        if (mimeType != null && !mimeType.isEmpty()) {
            return mimeType;
        }
        
        return switch (fileType != null ? fileType.toLowerCase() : "") {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "wmv" -> "video/x-ms-wmv";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "flac" -> "audio/flac";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "ppt", "pptx" -> "application/vnd.ms-powerpoint";
            case "zip" -> "application/zip";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            default -> "application/octet-stream";
        };
    }

    private String determineContentDisposition(String contentType, String filename) {
        if (contentType != null && (contentType.startsWith("image/") || 
                                    contentType.startsWith("video/") || 
                                    contentType.startsWith("audio/") ||
                                    contentType.startsWith("application/pdf"))) {
            return "inline; filename=\"" + filename + "\"";
        }
        return "attachment; filename=\"" + filename + "\"";
    }
}
