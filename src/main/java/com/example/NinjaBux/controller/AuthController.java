package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.dto.AuthResponse;
import com.example.NinjaBux.dto.NinjaLoginRequest;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.service.NinjaAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private NinjaAuthService ninjaAuthService;

    @Autowired
    private NinjaRepository ninjaRepository;

    @PostMapping("/ninja/login")
    public ResponseEntity<AuthResponse> ninjaLogin(@RequestBody NinjaLoginRequest request, HttpServletRequest httpRequest) {
        Optional<String> tokenOpt = ninjaAuthService.authenticateAndGenerateToken(request.getUsername(), httpRequest);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Ninja> ninjaOpt = ninjaRepository.findByUsernameIgnoreCase(request.getUsername());
        if (ninjaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ninja ninja = ninjaOpt.get();
        AuthResponse response = new AuthResponse(
            tokenOpt.get(),
            "NINJA",
            ninja.getId(),
            ninja.getUsername()
        );

        return ResponseEntity.ok(response);
    }
}
