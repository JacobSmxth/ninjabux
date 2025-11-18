package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.NinjaLoginLog;
import com.example.NinjaBux.repository.NinjaLoginLogRepository;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NinjaAuthService {

    private static final Logger logger = LoggerFactory.getLogger(NinjaAuthService.class);

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private NinjaLoginLogRepository loginLogRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public Optional<String> authenticateAndGenerateToken(String username, HttpServletRequest request) {
        Optional<Ninja> ninjaOpt = ninjaRepository.findByUsernameIgnoreCase(username);

        if (ninjaOpt.isEmpty()) {
            logFailedLogin(username, request);
            return Optional.empty();
        }

        Ninja ninja = ninjaOpt.get();

        if (ninja.isLocked()) {
            logFailedLogin(username, request);
            return Optional.empty();
        }

        logSuccessfulLogin(ninja, request);

        String token = jwtUtil.generateNinjaToken(ninja.getId(), ninja.getUsername());
        return Optional.of(token);
    }

    private void logSuccessfulLogin(Ninja ninja, HttpServletRequest request) {
        try {
            NinjaLoginLog log = new NinjaLoginLog(
                ninja,
                getClientIp(request),
                request.getHeader("User-Agent"),
                true
            );
            loginLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Error logging successful ninja login: {}", e.getMessage(), e);
        }
    }

    private void logFailedLogin(String username, HttpServletRequest request) {
        try {
            Optional<Ninja> ninjaOpt = ninjaRepository.findByUsernameIgnoreCase(username);
            if (ninjaOpt.isPresent()) {
                NinjaLoginLog log = new NinjaLoginLog(
                    ninjaOpt.get(),
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    false
                );
                loginLogRepository.save(log);
            }
        } catch (Exception e) {
            logger.error("Error logging failed ninja login: {}", e.getMessage(), e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
