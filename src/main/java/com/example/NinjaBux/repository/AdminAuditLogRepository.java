package com.example.NinjaBux.repository;

import com.example.NinjaBux.domain.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    List<AdminAuditLog> findTop100ByOrderByTimestampDesc();
    List<AdminAuditLog> findByTargetNinjaIdOrderByTimestampDesc(Long targetNinjaId);
    List<AdminAuditLog> findByAdminUsernameOrderByTimestampDesc(String adminUsername);
    List<AdminAuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
}
