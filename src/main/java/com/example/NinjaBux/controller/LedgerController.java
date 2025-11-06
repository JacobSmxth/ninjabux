package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.LedgerTxn;
import com.example.NinjaBux.dto.LedgerTxnResponse;
import com.example.NinjaBux.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;

    @GetMapping("/ninja/{ninjaId}")
    public ResponseEntity<List<LedgerTxnResponse>> getLedgerHistory(@PathVariable Long ninjaId) {
        try {
            List<LedgerTxn> history = ledgerService.getLedgerHistory(ninjaId);
            List<LedgerTxnResponse> response = history.stream()
                .map(LedgerTxnResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<LedgerTxnResponse>> getAllLedgerTransactions(
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin") String adminUsername) {
        try {
            List<LedgerTxn> allTransactions = ledgerService.getAllLedgerTransactions(limit);
            List<LedgerTxnResponse> response = allTransactions.stream()
                .map(LedgerTxnResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}

