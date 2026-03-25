package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs")
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_username", length = 100)
    private String operatorUsername;

    @Column(name = "field_changes", columnDefinition = "TEXT")
    private String fieldChanges;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(Long id, String entityType, Long entityId, String operation, Long operatorId,
                    String operatorUsername, String fieldChanges, String summary, String ipAddress,
                    LocalDateTime timestamp) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.operatorId = operatorId;
        this.operatorUsername = operatorUsername;
        this.fieldChanges = fieldChanges;
        this.summary = summary;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Long id;
        private String entityType;
        private Long entityId;
        private String operation;
        private Long operatorId;
        private String operatorUsername;
        private String fieldChanges;
        private String summary;
        private String ipAddress;
        private LocalDateTime timestamp;

        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public AuditLogBuilder entityId(Long entityId) { this.entityId = entityId; return this; }
        public AuditLogBuilder operation(String operation) { this.operation = operation; return this; }
        public AuditLogBuilder operatorId(Long operatorId) { this.operatorId = operatorId; return this; }
        public AuditLogBuilder operatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; return this; }
        public AuditLogBuilder fieldChanges(String fieldChanges) { this.fieldChanges = fieldChanges; return this; }
        public AuditLogBuilder summary(String summary) { this.summary = summary; return this; }
        public AuditLogBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public AuditLogBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditLog build() {
            return new AuditLog(id, entityType, entityId, operation, operatorId, operatorUsername,
                fieldChanges, summary, ipAddress, timestamp);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }
    public String getFieldChanges() { return fieldChanges; }
    public void setFieldChanges(String fieldChanges) { this.fieldChanges = fieldChanges; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_PAYMENT = "PAYMENT";
    public static final String OPERATION_REFUND = "REFUND";
}