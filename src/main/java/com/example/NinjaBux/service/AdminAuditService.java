package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.AdminAuditLog;
import com.example.NinjaBux.repository.AdminAuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAuditService {
    private final AdminAuditLogRepository auditLogRepository;

    public AdminAuditService(AdminAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String adminUsername, String action, String details) {
        try {
            AdminAuditLog log = new AdminAuditLog(adminUsername, action, details);
            auditLogRepository.save(log);
        } catch (Exception e) {
            // dont fail app if audit logging fails
            System.err.println("Error saving audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void log(String adminUsername, String action, String details, Long targetNinjaId, String targetNinjaName) {
        try {
            AdminAuditLog log = new AdminAuditLog(adminUsername, action, details, targetNinjaId, targetNinjaName);
            auditLogRepository.save(log);
        } catch (Exception e) {
            // gotta ctach em all
            System.err.println("Error saving audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<AdminAuditLog> getRecentLogs(int limit) {
        List<AdminAuditLog> logs = auditLogRepository.findTop100ByOrderByTimestampDesc();
        return logs.stream().limit(limit).toList();
    }

    public List<AdminAuditLog> getAllRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<AdminAuditLog> getLogsByNinja(Long ninjaId) {
        return auditLogRepository.findByTargetNinjaIdOrderByTimestampDesc(ninjaId);
    }

    public List<AdminAuditLog> getLogsByAdmin(String adminUsername) {
        return auditLogRepository.findByAdminUsernameOrderByTimestampDesc(adminUsername);
    }
}
