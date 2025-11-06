package com.example.NinjaBux.controller;

import com.example.NinjaBux.dto.AnalyticsResponse;
import com.example.NinjaBux.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin") String adminUsername) {
        AnalyticsResponse analytics = analyticsService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }
}

