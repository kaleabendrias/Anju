package com.anju.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportValidationResult {
    
    private List<FieldError> errors;
    private List<Map<String, String>> validRecords;
    private int totalRows;
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }
    
    public int getValidCount() {
        return validRecords != null ? validRecords.size() : 0;
    }
    
    public double getSuccessRate() {
        if (totalRows == 0) return 0;
        return (double) getValidCount() / totalRows * 100;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private int lineNumber;
        private String field;
        private String message;
    }
}
