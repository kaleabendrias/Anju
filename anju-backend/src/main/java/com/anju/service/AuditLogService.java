package com.anju.service;

import com.anju.entity.AuditLog;
import com.anju.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_PAYMENT = "PAYMENT";
    public static final String OPERATION_REFUND = "REFUND";

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(Long entityId, String entityType, String operation, 
                            Long operatorId, String operatorUsername,
                            String fieldChanges, String summary, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityId(entityId)
                    .entityType(entityType)
                    .operation(operation)
                    .operatorId(operatorId)
                    .operatorUsername(operatorUsername)
                    .fieldChanges(fieldChanges)
                    .summary(summary)
                    .ipAddress(ipAddress)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log recorded: entityType={}, entityId={}, operation={}", entityType, entityId, operation);
        } catch (Exception e) {
            log.error("Failed to record audit log: entityType={}, entityId={}, operation={}, error={}", 
                    entityType, entityId, operation, e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFinancialOperation(Long entityId, String entityType, String operation,
                                     Long operatorId, String fieldChanges, String summary) {
        logOperation(entityId, entityType, operation, operatorId, null, fieldChanges, summary, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFieldChanges(Long entityId, String entityType, String operation,
                               Long operatorId, String operatorUsername,
                               Object oldValue, Object newValue, String fieldName) {
        String fieldChanges = String.format("{\"%s\":{\"old\":\"%s\",\"new\":\"%s\"}}", 
                fieldName, 
                oldValue != null ? oldValue.toString() : "null",
                newValue != null ? newValue.toString() : "null");
        
        String summary = String.format("%s %s: %s changed from '%s' to '%s'",
                entityType, entityId, fieldName, oldValue, newValue);

        logOperation(entityId, entityType, operation, operatorId, operatorUsername,
                fieldChanges, summary, null);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    public List<AuditLog> getAuditLogsByEntity(Long entityId, String entityType) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByOperator(Long operatorId) {
        return auditLogRepository.findByOperatorId(operatorId);
    }
}
