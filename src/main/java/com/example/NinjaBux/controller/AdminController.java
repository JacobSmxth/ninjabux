package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.Admin;
import com.example.NinjaBux.domain.AdminAuditLog;
import com.example.NinjaBux.dto.AdminLoginRequest;
import com.example.NinjaBux.dto.AdminResponse;
import com.example.NinjaBux.dto.CreateAdminRequest;
import com.example.NinjaBux.dto.CreateAdminByAdminRequest;
import com.example.NinjaBux.dto.ChangePasswordRequest;
import com.example.NinjaBux.service.AdminService;
import com.example.NinjaBux.service.AdminAuditService;
import com.example.NinjaBux.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired(required = false)
    private AdminAuditService auditService;

    @Autowired(required = false)
    private NotificationService notificationService;

    @GetMapping("/setup-needed")
    public ResponseEntity<Boolean> setupNeeded() {
        return ResponseEntity.ok(!adminService.adminExists());
    }

    @PostMapping("/setup")
    public ResponseEntity<AdminResponse> setupAdmin(@RequestBody CreateAdminRequest request) {
        // check for admins in db
        if (adminService.adminExists()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Admin admin = adminService.createAdmin(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.isCanCreateAdmins()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new AdminResponse(admin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@RequestBody CreateAdminByAdminRequest request) {
        Optional<Admin> currentAdminOpt = adminService.authenticate(
            request.getCurrentAdminUsername(),
            request.getCurrentAdminPassword()
        );
        
        if (currentAdminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Admin currentAdmin = currentAdminOpt.get();
        if (!currentAdmin.isCanCreateAdmins()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Admin admin = adminService.createAdmin(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                false
            );
            if (auditService != null) {
                try {
                    auditService.log(request.getCurrentAdminUsername(), "CREATE_ADMIN", 
                        "Created admin: " + request.getUsername());
                } catch (Exception e) {
                    System.err.println("Error logging admin creation: " + e.getMessage());
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(new AdminResponse(admin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                           @RequestParam String username) {
        boolean success = adminService.changePassword(
            username,
            request.getOldPassword(),
            request.getNewPassword()
        );

        if (success) {
            if (auditService != null) {
                try {
                    auditService.log(username, "CHANGE_PASSWORD", "Password changed");
                } catch (Exception e) {
                    System.err.println("Error logging password change: " + e.getMessage());
                }
            }
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(@RequestBody AdminLoginRequest request) {
        try {
            Optional<Admin> admin = adminService.authenticate(request.getUsername(), request.getPassword());

            if (admin.isPresent()) {
                if (auditService != null) {
                    try {
                        auditService.log(request.getUsername(), "LOGIN", "Admin logged in");
                    } catch (Exception e) {
                        System.err.println("Error logging admin login: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                AdminResponse response = new AdminResponse(admin.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            System.err.println("Error in admin login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AdminAuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "50") int limit) {
        if (auditService == null) {
            return ResponseEntity.ok(List.of());
        }
        List<AdminAuditLog> logs = auditService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit-logs/ninja/{ninjaId}")
    public ResponseEntity<List<AdminAuditLog>> getAuditLogsByNinja(@PathVariable Long ninjaId) {
        if (auditService == null) {
            return ResponseEntity.ok(List.of());
        }
        List<AdminAuditLog> logs = auditService.getLogsByNinja(ninjaId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AdminResponse>> getAllAdmins(
            @RequestParam String currentAdminUsername,
            @RequestParam String currentAdminPassword) {
        Optional<Admin> currentAdminOpt = adminService.authenticate(
            currentAdminUsername,
            currentAdminPassword
        );

        if (currentAdminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Admin currentAdmin = currentAdminOpt.get();
        if (!currentAdmin.isCanCreateAdmins()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Admin> admins = adminService.getAllAdmins();
        List<AdminResponse> responses = admins.stream()
            .map(AdminResponse::new)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(
            @PathVariable Long id,
            @RequestParam String currentAdminUsername,
            @RequestParam String currentAdminPassword) {
        Optional<Admin> currentAdminOpt = adminService.authenticate(
            currentAdminUsername,
            currentAdminPassword
        );

        if (currentAdminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Admin currentAdmin = currentAdminOpt.get();
        if (!currentAdmin.isCanCreateAdmins()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Admin> adminToDelete = adminService.getAdminById(id);
        if (adminToDelete.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (currentAdmin.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            adminService.deleteAdmin(id);
            if (auditService != null) {
                try {
                    auditService.log(currentAdminUsername, "DELETE_ADMIN",
                        "Deleted admin: " + adminToDelete.get().getUsername());
                } catch (Exception e) {
                    System.err.println("Error logging admin deletion: " + e.getMessage());
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/announcement")
    public ResponseEntity<?> sendAnnouncement(@RequestBody Map<String, String> request,
                                               @RequestParam String currentAdminUsername,
                                               @RequestParam String currentAdminPassword) {
        Optional<Admin> currentAdminOpt = adminService.authenticate(
            currentAdminUsername,
            currentAdminPassword
        );
        
        if (currentAdminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String title = request.get("title");
        String message = request.get("message");

        if (title == null || message == null || title.trim().isEmpty() || message.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (notificationService != null) {
            try {
                notificationService.sendBroadcastAnnouncement(title, message);
                if (auditService != null) {
                    try {
                        auditService.log(currentAdminUsername, "ANNOUNCEMENT", 
                            "Sent announcement: " + title);
                    } catch (Exception e) {
                        System.err.println("Error logging announcement: " + e.getMessage());
                    }
                }
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
