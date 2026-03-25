package com.anju.controller;

import com.anju.dto.ApiResponse;
import com.anju.service.DataExchangeService;
import com.anju.service.DataExchangeService.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DataExchangeController {

    private final DataExchangeService dataExchangeService;

    @PostMapping("/api/admin/data/import/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importProperties(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is required"));
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("text/csv") && !contentType.equals("application/csv")
                && !contentType.equals("text/plain") && !contentType.contains("csv"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be a CSV"));
        }

        ImportResult result = dataExchangeService.importProperties(file);

        Map<String, Object> report = new HashMap<>();
        report.put("successCount", result.successCount());
        report.put("failureCount", result.failureCount());
        report.put("errors", result.errors());

        return ResponseEntity.ok(ApiResponse.success("Import completed", report));
    }
}
