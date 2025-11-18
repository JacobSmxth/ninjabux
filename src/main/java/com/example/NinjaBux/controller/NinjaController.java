package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.dto.CreateNinjaRequest;
import com.example.NinjaBux.dto.LeaderboardResponse;
import com.example.NinjaBux.dto.NinjaResponse;
import com.example.NinjaBux.dto.ProgressHistoryCorrectionRequest;
import com.example.NinjaBux.dto.ProgressHistoryResponse;
import com.example.NinjaBux.dto.UpdateNinjaRequest;
import com.example.NinjaBux.dto.UpdateProgressRequest;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.service.AdminAuditService;
import com.example.NinjaBux.service.LedgerService;
import com.example.NinjaBux.service.NinjaAdminService;
import com.example.NinjaBux.service.NinjaLeaderboardService;
import com.example.NinjaBux.service.NinjaProgressService;
import com.example.NinjaBux.service.NinjaQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ninjas")
public class NinjaController {

  private static final Logger logger = LoggerFactory.getLogger(NinjaController.class);

  @Autowired private NinjaProgressService ninjaProgressService;

  @Autowired private NinjaQueryService ninjaQueryService;

  @Autowired private NinjaAdminService ninjaAdminService;

  @Autowired private NinjaLeaderboardService ninjaLeaderboardService;

  @Autowired private AdminAuditService auditService;

  @Autowired private LedgerService ledgerService;

  @PostMapping
  public ResponseEntity<NinjaResponse> createNinja(
      @RequestBody CreateNinjaRequest request,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    Ninja ninja =
        ninjaProgressService.createNinja(
            request.getFirstName(),
            request.getLastName(),
            request.getUsername(),
            request.getBeltType(),
            request.getLevel(),
            request.getLesson());
    auditService.log(
        adminUsername,
        "CREATE_NINJA",
        "Created ninja: " + ninja.getFirstName() + " " + ninja.getLastName(),
        ninja.getId(),
        ninja.getFirstName() + " " + ninja.getLastName());
    return ResponseEntity.status(HttpStatus.CREATED).body(new NinjaResponse(ninja, ledgerService));
  }

  @GetMapping
  public ResponseEntity<?> getAllNinjas(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String belt,
      @RequestParam(required = false) Boolean locked) {
    if (page != null && size != null) {
      BeltType beltFilter = null;
      if (belt != null && !belt.isEmpty()) {
        try {
          beltFilter = BeltType.valueOf(belt.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
      }

      Page<Ninja> ninjaPage =
          ninjaQueryService.getNinjasPaginated(
              page,
              size,
              sort != null ? sort : "name",
              direction != null ? direction : "ASC",
              name,
              beltFilter,
              locked);

      Map<String, Object> response = new HashMap<>();
      response.put(
          "content",
          ninjaPage.getContent().stream()
              .map(n -> new NinjaResponse(n, ledgerService))
              .collect(Collectors.toList()));
      response.put("totalElements", ninjaPage.getTotalElements());
      response.put("totalPages", ninjaPage.getTotalPages());
      response.put("number", ninjaPage.getNumber());
      response.put("size", ninjaPage.getSize());
      response.put("numberOfElements", ninjaPage.getNumberOfElements());

      return ResponseEntity.ok(response);
    }
    List<Ninja> ninjas = ninjaQueryService.getAllNinjas();
    List<NinjaResponse> responses =
        ninjas.stream().map(n -> new NinjaResponse(n, ledgerService)).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<NinjaResponse> getNinja(@PathVariable Long id) {
    Optional<Ninja> ninja = ninjaQueryService.getNinja(id);
    return ninja
        .map(n -> ResponseEntity.ok(new NinjaResponse(n, ledgerService)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/progress")
  public ResponseEntity<NinjaResponse> updateProgress(
      @PathVariable Long id,
      @RequestBody UpdateProgressRequest request,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    Ninja updatedNinja =
        ninjaProgressService.updateProgress(
            id, request.getBeltType(), request.getLevel(), request.getLesson());
    auditService.log(
        adminUsername,
        "UPDATE_PROGRESS",
        String.format(
            "Updated progress to %s L%d-L%d",
            request.getBeltType(), request.getLevel(), request.getLesson()),
        updatedNinja.getId(),
        updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
    return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
  }

  @GetMapping("/login/{username}")
  public ResponseEntity<?> loginByUsername(@PathVariable String username) {
    try {
      Optional<Ninja> ninja = ninjaQueryService.getNinjaByUsername(username);
      return ninja
          .map(n -> ResponseEntity.ok(new NinjaResponse(n, ledgerService)))
          .orElse(ResponseEntity.notFound().build());
    } catch (com.example.NinjaBux.exception.AccountLockedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(java.util.Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<NinjaResponse> updateNinja(
      @PathVariable Long id,
      @RequestBody UpdateNinjaRequest request,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    Ninja updatedNinja =
        ninjaProgressService.updateNinja(
            id,
            request.getFirstName(),
            request.getLastName(),
            request.getUsername(),
            request.getBeltType(),
            request.getLevel(),
            request.getLesson());
    auditService.log(
        adminUsername,
        "UPDATE_NINJA",
        "Updated ninja details",
        updatedNinja.getId(),
        updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
    return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNinja(
      @PathVariable Long id,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Optional<Ninja> ninja = ninjaQueryService.getNinja(id);
      if (ninja.isPresent()) {
        Ninja n = ninja.get();
        auditService.log(
            adminUsername,
            "DELETE_NINJA",
            "Deleted ninja: " + n.getFirstName() + " " + n.getLastName(),
            n.getId(),
            n.getFirstName() + " " + n.getLastName());
      }
      ninjaAdminService.deleteNinja(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/leaderboard")
  public ResponseEntity<LeaderboardResponse> getLeaderboard(
      @RequestParam(defaultValue = "10") int top,
      @RequestParam(defaultValue = "lifetime") String period,
      @RequestParam(required = false) Boolean excludeLocked) {
    LeaderboardResponse leaderboard =
        ninjaLeaderboardService.getLeaderboard(
            top, period, excludeLocked != null ? excludeLocked : false);
    return ResponseEntity.ok(leaderboard);
  }

  @PostMapping("/leaderboard/rebuild")
  public ResponseEntity<Map<String, String>> rebuildLeaderboard(
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    auditService.log(
        adminUsername, "REBUILD_LEADERBOARD", "Requested leaderboard rebuild", null, "System");
    Map<String, String> response = new HashMap<>();
    response.put(
        "message",
        "Leaderboard rebuild completed (leaderboards are always calculated from event log)");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/award-bux")
  public ResponseEntity<NinjaResponse> awardBux(
      @PathVariable Long id,
      @RequestParam int amount,
      @RequestParam(required = false) String notes,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Ninja updatedNinja = ninjaAdminService.awardBux(id, amount, adminUsername, notes);
      auditService.log(
          adminUsername,
          "AWARD_BUX",
          "Awarded " + amount + " Bux" + (notes != null ? ": " + notes : ""),
          updatedNinja.getId(),
          updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
      return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("/{id}/deduct-bux")
  public ResponseEntity<NinjaResponse> deductBux(
      @PathVariable Long id,
      @RequestParam int amount,
      @RequestParam(required = false) String notes,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Ninja updatedNinja = ninjaAdminService.deductBux(id, amount, adminUsername, notes);
      auditService.log(
          adminUsername,
          "DEDUCT_BUX",
          "Deducted " + amount + " Bux" + (notes != null ? ": " + notes : ""),
          updatedNinja.getId(),
          updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
      return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("/{id}/lock")
  public ResponseEntity<?> lockAccount(
      @PathVariable Long id,
      @RequestParam(required = false) String reason,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Ninja updatedNinja = ninjaAdminService.lockAccount(id, reason, adminUsername);
      auditService.log(
          adminUsername,
          "LOCK_ACCOUNT",
          "Locked account" + (reason != null ? ": " + reason : ""),
          updatedNinja.getId(),
          updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
      return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
    } catch (NinjaNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(java.util.Map.of("message", e.getMessage()));
    } catch (RuntimeException e) {
      logger.error("Error locking account: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              java.util.Map.of(
                  "message", e.getMessage() != null ? e.getMessage() : "Failed to lock account"));
    }
  }

  @PostMapping("/{id}/unlock")
  public ResponseEntity<?> unlockAccount(
      @PathVariable Long id,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Ninja updatedNinja = ninjaAdminService.unlockAccount(id, adminUsername);
      auditService.log(
          adminUsername,
          "UNLOCK_ACCOUNT",
          "Unlocked account",
          updatedNinja.getId(),
          updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
      return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
    } catch (NinjaNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(java.util.Map.of("message", e.getMessage()));
    } catch (RuntimeException e) {
      logger.error("Error unlocking account: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              java.util.Map.of(
                  "message", e.getMessage() != null ? e.getMessage() : "Failed to unlock account"));
    }
  }

  @PutMapping("/{id}/progress-with-correction")
  public ResponseEntity<NinjaResponse> updateProgressWithCorrection(
      @PathVariable Long id,
      @RequestBody UpdateProgressRequest request,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Ninja updatedNinja =
          ninjaProgressService.updateProgressWithCorrection(
              id, request.getBeltType(), request.getLevel(), request.getLesson(), adminUsername);
      auditService.log(
          adminUsername,
          "UPDATE_PROGRESS_WITH_CORRECTION",
          String.format(
              "Updated progress with correction to %s L%d-L%d",
              request.getBeltType(), request.getLevel(), request.getLesson()),
          updatedNinja.getId(),
          updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
      return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/{id}/progress-history")
  public ResponseEntity<List<ProgressHistoryResponse>> getProgressHistory(@PathVariable Long id) {
    try {
      List<ProgressHistory> history = ninjaProgressService.getProgressHistory(id);
      List<ProgressHistoryResponse> response =
          history.stream().map(ProgressHistoryResponse::new).collect(Collectors.toList());
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @PostMapping("/{id}/progress-history/correction")
  public ResponseEntity<ProgressHistoryResponse> createProgressHistoryCorrection(
      @PathVariable Long id,
      @RequestBody ProgressHistoryCorrectionRequest request,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      ProgressHistory correction =
          ninjaProgressService.createProgressHistoryCorrection(
              id,
              request.getOriginalEntryId(),
              request.getBeltType(),
              request.getLevel(),
              request.getLesson(),
              request.getBuxDelta(),
              request.getLegacyDelta(),
              request.getNotes(),
              request.getAdminUsername() != null ? request.getAdminUsername() : adminUsername);

      auditService.log(
          adminUsername,
          "PROGRESS_HISTORY_CORRECTION",
          String.format(
              "Created correction for entry #%d: %+d Bux, %+d Legacy",
              request.getOriginalEntryId(),
              request.getBuxDelta(),
              request.getLegacyDelta() != null ? request.getLegacyDelta() : 0),
          id,
          "Progress History Correction");

      return ResponseEntity.ok(new ProgressHistoryResponse(correction));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("/{id}/ban-suggestions")
  public ResponseEntity<NinjaResponse> banSuggestions(
      @PathVariable Long id,
      @RequestParam boolean banned,
      @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "admin")
          String adminUsername) {
    try {
      Ninja updatedNinja = ninjaAdminService.banSuggestions(id, banned);
      auditService.log(
          adminUsername,
          banned ? "BAN_SUGGESTIONS" : "UNBAN_SUGGESTIONS",
          (banned ? "Banned" : "Unbanned") + " ninja from suggesting questions",
          updatedNinja.getId(),
          updatedNinja.getFirstName() + " " + updatedNinja.getLastName());
      return ResponseEntity.ok(new NinjaResponse(updatedNinja, ledgerService));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
