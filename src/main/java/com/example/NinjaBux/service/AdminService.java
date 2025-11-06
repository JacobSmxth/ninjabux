package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Admin;
import com.example.NinjaBux.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Admin createAdmin(String username, String email, String password, String firstName, String lastName, boolean canCreateAdmins) {
        String passwordHash = passwordEncoder.encode(password);
        Admin admin = new Admin(username, email, passwordHash, firstName, lastName, canCreateAdmins);
        return adminRepository.save(admin);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<Admin> adminOpt = adminRepository.findByUsernameIgnoreCase(username);
        if (adminOpt.isEmpty()) {
            return false;
        }

        Admin admin = adminOpt.get();
        if (!passwordEncoder.matches(oldPassword, admin.getPasswordHash())) {
            return false;
        }

        String newPasswordHash = passwordEncoder.encode(newPassword);
        admin.setPasswordHash(newPasswordHash);
        adminRepository.save(admin);
        return true;
    }

    public Optional<Admin> authenticate(String username, String password) {
        Optional<Admin> admin = adminRepository.findByUsernameIgnoreCase(username);

        if (admin.isPresent()) {
            if (passwordEncoder.matches(password, admin.get().getPasswordHash())) {
                return admin;
            }
        }

        return Optional.empty();
    }

    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsernameIgnoreCase(username);
    }
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
    public boolean adminExists() {
        return adminRepository.count() > 0;
    }
}
