package com.anju.dto;

// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class ImportValidationResult {
    
    private List<FieldError> errors;
    private List<Map<String, String>> validRecords;
    private int totalRows;

    public ImportValidationResult() {
    }

    public ImportValidationResult(List<FieldError> errors, List<Map<String, String>> validRecords, 
                                   int totalRows) {
        this.errors = errors;
        this.validRecords = validRecords;
        this.totalRows = totalRows;
    }
    
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

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    public List<Map<String, String>> getValidRecords() {
        return validRecords;
    }

    public void setValidRecords(List<Map<String, String>> validRecords) {
        this.validRecords = validRecords;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<FieldError> errors;
        private List<Map<String, String>> validRecords;
        private int totalRows;

        public Builder errors(List<FieldError> errors) {
            this.errors = errors;
            return this;
        }

        public Builder validRecords(List<Map<String, String>> validRecords) {
            this.validRecords = validRecords;
            return this;
        }

        public Builder totalRows(int totalRows) {
            this.totalRows = totalRows;
            return this;
        }

        public ImportValidationResult build() {
            return new ImportValidationResult(errors, validRecords, totalRows);
        }
    }

    // @Data
    // @Builder
    // @NoArgsConstructor
    // @AllArgsConstructor
    public static class FieldError {
        private int lineNumber;
        private String field;
        private String message;

        public FieldError() {
        }

        public FieldError(int lineNumber, String field, String message) {
            this.lineNumber = lineNumber;
            this.field = field;
            this.message = message;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public static FieldErrorBuilder builder() {
            return new FieldErrorBuilder();
        }

        public static class FieldErrorBuilder {
            private int lineNumber;
            private String field;
            private String message;

            public FieldErrorBuilder lineNumber(int lineNumber) {
                this.lineNumber = lineNumber;
                return this;
            }

            public FieldErrorBuilder field(String field) {
                this.field = field;
                return this;
            }

            public FieldErrorBuilder message(String message) {
                this.message = message;
                return this;
            }

            public FieldError build() {
                return new FieldError(lineNumber, field, message);
            }
        }
    }
}
