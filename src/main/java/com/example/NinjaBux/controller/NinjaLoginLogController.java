package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.NinjaLoginLog;
import com.example.NinjaBux.dto.NinjaLoginLogDTO;
import com.example.NinjaBux.repository.NinjaLoginLogRepository;
import com.example.NinjaBux.repository.NinjaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/ninja-login-logs")
public class NinjaLoginLogController {

    @Autowired
    private NinjaLoginLogRepository loginLogRepository;

    @Autowired
    private NinjaRepository ninjaRepository;

    @GetMapping
    public ResponseEntity<List<NinjaLoginLogDTO>> getAllLoginLogs(
            @RequestParam(defaultValue = "100") int limit) {
        List<NinjaLoginLog> logs = loginLogRepository.findTop100ByOrderByLoginTimeDesc();
        List<NinjaLoginLogDTO> dtos = logs.stream()
                .limit(limit)
                .map(NinjaLoginLogDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ninja/{ninjaId}")
    public ResponseEntity<List<NinjaLoginLogDTO>> getLoginLogsByNinja(@PathVariable Long ninjaId) {
        Optional<Ninja> ninjaOpt = ninjaRepository.findById(ninjaId);
        if (ninjaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<NinjaLoginLog> logs = loginLogRepository.findByNinjaOrderByLoginTimeDesc(ninjaOpt.get());
        List<NinjaLoginLogDTO> dtos = logs.stream()
                .map(NinjaLoginLogDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<NinjaLoginLogDTO>> getLoginLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<NinjaLoginLog> logs = loginLogRepository.findByLoginTimeBetweenOrderByLoginTimeDesc(start, end);
        List<NinjaLoginLogDTO> dtos = logs.stream()
                .map(NinjaLoginLogDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ninja/{ninjaId}/date-range")
    public ResponseEntity<List<NinjaLoginLogDTO>> getLoginLogsByNinjaAndDateRange(
            @PathVariable Long ninjaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Optional<Ninja> ninjaOpt = ninjaRepository.findById(ninjaId);
        if (ninjaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<NinjaLoginLog> logs = loginLogRepository.findByNinjaAndLoginTimeBetween(ninjaOpt.get(), start, end);
        List<NinjaLoginLogDTO> dtos = logs.stream()
                .map(NinjaLoginLogDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
