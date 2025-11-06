package com.example.NinjaBux.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping
    public Map<String, Object> getHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("Status", "Currently running!");
        response.put("H2-Console", "Running on endpoint /h2-console");
        response.put("Frontend", "Should be running on port 5173");
        return response;
    }
}
