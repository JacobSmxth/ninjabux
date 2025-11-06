package com.example.NinjaBux.controller;

import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.dto.AchievementDTO;
import com.example.NinjaBux.dto.AchievementProgressDTO;
import com.example.NinjaBux.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @PostMapping
    public ResponseEntity<AchievementDTO> createAchievement(
            @RequestBody AchievementDTO achievementDTO,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername) {

        if (adminUsername == null || adminUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AchievementDTO created = achievementService.createAchievement(achievementDTO, adminUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AchievementDTO> updateAchievement(
            @PathVariable Long id,
            @RequestBody AchievementDTO achievementDTO,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername) {

        if (adminUsername == null || adminUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AchievementDTO updated = achievementService.updateAchievement(id, achievementDTO, adminUsername);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAchievement(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername) {

        if (adminUsername == null || adminUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            achievementService.deleteAchievement(id, adminUsername);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<AchievementDTO> toggleActive(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername) {

        if (adminUsername == null || adminUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AchievementDTO updated = achievementService.toggleActive(id, adminUsername);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<AchievementDTO>> getAllAchievements() {
        return ResponseEntity.ok(achievementService.getAllAchievements());
    }

    @GetMapping
    public ResponseEntity<List<AchievementDTO>> getActiveAchievements() {
        return ResponseEntity.ok(achievementService.getActiveAchievements());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<AchievementDTO>> getByCategory(@PathVariable AchievementCategory category) {
        return ResponseEntity.ok(achievementService.getAchievementsByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AchievementDTO> getAchievement(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(achievementService.getAchievement(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // ninja progress stuff

    @GetMapping("/ninja/{ninjaId}")
    public ResponseEntity<List<AchievementProgressDTO>> getNinjaAchievements(
            @PathVariable Long ninjaId,
            @RequestParam(defaultValue = "false") boolean includeHidden) {

        try {
            return ResponseEntity.ok(achievementService.getNinjaAchievements(ninjaId, includeHidden));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ninja/{ninjaId}/unlocked")
    public ResponseEntity<List<AchievementProgressDTO>> getUnlockedAchievements(@PathVariable Long ninjaId) {
        try {
            return ResponseEntity.ok(achievementService.getUnlockedAchievements(ninjaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ninja/{ninjaId}/top")
    public ResponseEntity<List<AchievementProgressDTO>> getTopAchievements(
            @PathVariable Long ninjaId,
            @RequestParam(defaultValue = "3") int limit) {

        try {
            return ResponseEntity.ok(achievementService.getTopAchievements(ninjaId, limit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ninja/{ninjaId}/unseen")
    public ResponseEntity<List<AchievementProgressDTO>> getUnseenAchievements(@PathVariable Long ninjaId) {
        try {
            return ResponseEntity.ok(achievementService.getUnseenAchievements(ninjaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/ninja/{ninjaId}/mark-seen")
    public ResponseEntity<Void> markAchievementsSeen(
            @PathVariable Long ninjaId,
            @RequestBody List<Long> progressIds) {

        try {
            achievementService.markAchievementsSeen(ninjaId, progressIds);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/award")
    public ResponseEntity<AchievementProgressDTO> awardAchievement(
            @RequestBody Map<String, Long> request,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername) {

        if (adminUsername == null || adminUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long ninjaId = request.get("ninjaId");
        Long achievementId = request.get("achievementId");

        if (ninjaId == null || achievementId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            AchievementProgressDTO awarded = achievementService.awardAchievement(ninjaId, achievementId, adminUsername);
            return ResponseEntity.ok(awarded);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }


    @DeleteMapping("/revoke")
    public ResponseEntity<Void> revokeAchievement(
            @RequestParam Long ninjaId,
            @RequestParam Long achievementId,
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername) {

        if (adminUsername == null || adminUsername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            achievementService.revokeAchievement(ninjaId, achievementId, adminUsername);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }


    @PostMapping("/check/{ninjaId}")
    public ResponseEntity<List<AchievementProgressDTO>> checkAchievements(@PathVariable Long ninjaId) {
        try {
            List<AchievementProgressDTO> newlyUnlocked = achievementService.checkAndUnlockAchievements(ninjaId);
            return ResponseEntity.ok(newlyUnlocked);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/ninja/{ninjaId}/leaderboard-badge/{progressId}")
    public ResponseEntity<Void> setLeaderboardBadge(
            @PathVariable Long ninjaId,
            @PathVariable Long progressId) {
        try {
            achievementService.setLeaderboardBadge(ninjaId, progressId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
