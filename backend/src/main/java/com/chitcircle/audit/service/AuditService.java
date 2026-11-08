package com.chitcircle.audit.service;

import com.chitcircle.audit.domain.AuditLog;
import com.chitcircle.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog logAction(UUID userId, String action, String entityType, UUID entityId, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        return auditLogRepository.save(log);
    }
}

