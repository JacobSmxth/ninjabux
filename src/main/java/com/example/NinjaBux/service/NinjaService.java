package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.domain.LedgerTxn;
import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.dto.AchievementProgressDTO;
import com.example.NinjaBux.dto.LeaderboardEntry;
import com.example.NinjaBux.dto.LeaderboardResponse;
import com.example.NinjaBux.exception.AccountLockedException;
import com.example.NinjaBux.exception.InvalidProgressException;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.repository.ProgressHistoryRepository;
import com.example.NinjaBux.repository.AchievementProgressRepository;
import com.example.NinjaBux.repository.LedgerTxnRepository;
import com.example.NinjaBux.repository.LegacyLedgerTxnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NinjaService {

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private ProgressHistoryRepository progressHistoryRepository;

    @Autowired
    private BeltRewardCalculator beltRewardCalculator;

    @Autowired
    private com.example.NinjaBux.repository.QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private com.example.NinjaBux.repository.PurchaseRepository purchaseRepository;

    @Autowired(required = false)
    private AchievementService achievementService;

    @Autowired(required = false)
    private AchievementProgressRepository achievementProgressRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerTxnRepository ledgerTxnRepository;

    @Autowired
    private LegacyLedgerTxnRepository legacyLedgerTxnRepository;

    // this null check is holding the system together
    private void checkAccountLocked(Ninja ninja) {
        if (ninja.isLocked()) {
            String reason = ninja.getLockReason() != null ? ninja.getLockReason() : "Account is locked";
            throw new AccountLockedException(reason);
        }
    }

    @Transactional
    public Ninja createNinja(String firstName, String lastName, String username,
                            BeltType beltType, Integer level, Integer lesson) {
        BeltType startingBelt = (beltType != null) ? beltType : BeltType.WHITE;
        int startingLevel = (level != null) ? level : 1;
        int startingLesson = (lesson != null) ? lesson : 1;

        if (!beltRewardCalculator.isValidProgress(startingBelt, startingLevel, startingLesson)) {
            String errorMessage = beltRewardCalculator.getValidationErrorMessage(startingBelt, startingLevel, startingLesson);
            throw new InvalidProgressException(errorMessage);
        }

        // claude said start with zero then fix it later. here we are.
        Ninja ninja = new Ninja(firstName, lastName, username, 0, 0, startingLesson, startingLevel, startingBelt);
        ninja.setPostOnboardLessonCount(0); // forgot why this exists but deleting it breaks stuff
        ninja.setLessonsAllTime(0);
        ninja.setLessonsSinceConversion(0);

        ninja = ninjaRepository.save(ninja);

        // if they already have progress, pretend they earned it retroactively
        boolean needsOnboarding = (startingBelt != BeltType.WHITE) || (startingLevel > 1) || (startingLesson > 1);
        
        if (needsOnboarding) {
            // backfill their balance based on where they are now because the old system was chaos
            int computedBalance = beltRewardCalculator.calculateBalance(startingBelt, startingLevel, startingLesson);
            
            // legacy system rounds to nearest 10 for some reason i dont remember
            ledgerService.onboardNinjaWithLegacy(ninja.getId(), computedBalance);
        } else {
            // fresh start gets the standard 120 bux grant because reasons
            ledgerService.onboardNinjaWithLegacy(ninja.getId(), 0);
        }

        if (achievementService != null) {
            try {
                achievementService.checkAndUnlockAchievements(ninja.getId());
            } catch (Exception e) {
                System.err.println("Error checking achievements for newly created ninja: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ninjaRepository.findById(ninja.getId()).orElse(ninja);
    }

    @Transactional
    public Ninja updateProgress(Long ninjaId, BeltType newBelt, int newLevel, int newLesson) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        checkAccountLocked(ninja);

        if (!beltRewardCalculator.isValidProgress(newBelt, newLevel, newLesson)) {
            String errorMessage = beltRewardCalculator.getValidationErrorMessage(newBelt, newLevel, newLesson);
            throw new InvalidProgressException(errorMessage);
        }

        BeltType oldBelt = ninja.getCurrentBeltType();
        int oldLevel = ninja.getCurrentLevel();
        int oldLesson = ninja.getCurrentLesson();

        boolean beltUp = !oldBelt.equals(newBelt);
        boolean levelUp = oldLevel != newLevel;
        
        // figuring out if they actually progressed is harder than it should be
        boolean lessonComplete = false;
        if (beltUp) {
            // belt change means they finished everything before
            lessonComplete = true;
        } else if (levelUp) {
            // level change means they finished that level
            lessonComplete = true;
        } else if (newLesson > oldLesson && oldLevel == newLevel && oldBelt == newBelt) {
            // lesson number went up so they did lessons i guess
            lessonComplete = true;
        }

        // save early or the ledger service gets confused about what state we're in
        ninja.setCurrentBeltType(newBelt);
        ninja.setCurrentLevel(newLevel);
        ninja.setCurrentLesson(newLesson);
        ninja = ninjaRepository.save(ninja); // race condition prevention via prayer

        int buxGained = 0;

        // belt rewards are separate from lesson rewards because of course they are
        if (beltUp) {
            ledgerService.recordBeltUpReward(
                ninjaId,
                newBelt.toString(),
                String.format("Belt-up reward: %s", newBelt)
            );
            buxGained += 5; // magic number from ancient times
        }

        // level rewards are also separate because consistency is overrated
        if (levelUp) {
            ledgerService.recordLevelUpReward(
                ninjaId,
                newBelt,
                newLevel,
                String.format("Level-up reward: %s Belt Level %d", newBelt, newLevel)
            );
            // add to display counter so the ui shows something
            switch (newBelt) {
                case WHITE:
                    buxGained += 2;
                    break;
                case YELLOW:
                    buxGained += 3;
                    break;
                case ORANGE:
                    buxGained += 4;
                    break;
                case GREEN:
                    buxGained += 5;
                    break;
                case BLUE:
                    buxGained += 6;
                    break;
                default:
                    buxGained += 2; // default fallback because i dont trust the enum
                    break;
            }
        }

        // lesson earnings go through the ledger now because the old way was too easy
        if (lessonComplete) {
            if (beltUp) {
                // belt up counts as one lesson completion somehow
                String note = String.format("Lesson completion: %s Belt Level %d Lesson %d (belt up)", 
                    newBelt, newLevel, newLesson);
                LedgerTxn lessonTxn = ledgerService.recordLessonEarning(ninjaId, newBelt, note);
                if (lessonTxn != null) {
                    int quartersEarned = lessonTxn.getAmount();
                    buxGained += (int) LedgerTxn.quartersToBux(quartersEarned);
                }
            } else if (levelUp) {
                // level up also counts as one lesson because logic
                String note = String.format("Lesson completion: %s Belt Level %d Lesson %d (level up)", 
                    newBelt, newLevel, newLesson);
                LedgerTxn lessonTxn = ledgerService.recordLessonEarning(ninjaId, newBelt, note);
                if (lessonTxn != null) {
                    int quartersEarned = lessonTxn.getAmount();
                    buxGained += (int) LedgerTxn.quartersToBux(quartersEarned);
                }
            } else if (newLesson > oldLesson && oldLevel == newLevel && oldBelt == newBelt) {
                // multiple lessons means loop through them all
                for (int lesson = oldLesson; lesson < newLesson; lesson++) {
                    String note = String.format("Lesson completion: %s Belt Level %d Lesson %d", 
                        newBelt, newLevel, lesson);
                    LedgerTxn lessonTxn = ledgerService.recordLessonEarning(ninjaId, newBelt, note);
                    if (lessonTxn != null) {
                        int quartersEarned = lessonTxn.getAmount();
                        buxGained += (int) LedgerTxn.quartersToBux(quartersEarned);
                    }
                    // reload ninja because alternator state changes between lessons
                    ninja = ninjaRepository.findById(ninjaId)
                        .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
                }
            }
        }

        if (buxGained > 0 || lessonComplete) {
            ProgressHistory history = new ProgressHistory(
                ninja,
                ninja.getCurrentBeltType(),
                ninja.getCurrentLevel(),
                ninja.getCurrentLesson(),
                buxGained,
                beltUp ? ProgressHistory.EarningType.LEVEL_UP : ProgressHistory.EarningType.LEVEL_UP
            );
            progressHistoryRepository.save(history);
            ninja.setLastProgressUpdate(LocalDateTime.now());
        }

        Ninja savedNinja = ninjaRepository.save(ninja);

        // legacy system is dead but we keep the comments to remember what we lost

        if (notificationService != null && buxGained > 0) {
            try {
                String ninjaName = savedNinja.getFirstName() + " " + savedNinja.getLastName();
                
                if (lessonComplete && !levelUp && !beltUp) {
                    notificationService.sendLessonCompleteNotification(
                        savedNinja.getId(),
                        String.format("Completed lesson! Earned %d Bux", buxGained)
                    );
                } else if (beltUp) {
                    java.util.Map<String, Object> beltData = new java.util.HashMap<>();
                    beltData.put("belt", newBelt.toString());
                    beltData.put("level", newLevel);
                    beltData.put("lesson", newLesson);
                    beltData.put("buxEarned", buxGained);
                    beltData.put("ninjaName", ninjaName);
                    beltData.put("ninjaId", savedNinja.getId());
                    
                    notificationService.sendLevelUpNotification(
                        savedNinja.getId(),
                        String.format("Belt Up! %s Belt Level %d - Earned %d Bux", newBelt, newLevel, buxGained),
                        beltData
                    );
                    
                    notificationService.sendBroadcastNinjaBeltUp(
                        savedNinja.getId(),
                        ninjaName,
                        String.format("%s earned their %s Belt! 🎉", ninjaName, newBelt),
                        beltData
                    );
                } else if (levelUp) {
                    java.util.Map<String, Object> levelData = new java.util.HashMap<>();
                    levelData.put("belt", newBelt.toString());
                    levelData.put("level", newLevel);
                    levelData.put("lesson", newLesson);
                    levelData.put("buxEarned", buxGained);
                    levelData.put("ninjaName", ninjaName);
                    levelData.put("ninjaId", savedNinja.getId());
                    
                    notificationService.sendLevelUpNotification(
                        savedNinja.getId(),
                        String.format("Level Up! %s Belt Level %d - Earned %d Bux", newBelt, newLevel, buxGained),
                        levelData
                    );
                    
                    notificationService.sendBroadcastNinjaLevelUp(
                        savedNinja.getId(),
                        ninjaName,
                        String.format("%s leveled up to %s Belt Level %d! 🎉", ninjaName, newBelt, newLevel),
                        levelData
                    );
                }
            } catch (Exception e) {
                System.err.println("Error sending progress notification: " + e.getMessage());
            }
        }

        if (achievementService != null && buxGained > 0) {
            try {
                achievementService.checkAndUnlockAchievements(savedNinja.getId());
            } catch (Exception e) {
                System.err.println("Error checking achievements: " + e.getMessage());
            }
        }

        return savedNinja;
    }

    public Optional<Ninja> getNinja(Long id) {
        return ninjaRepository.findById(id);
    }

    public List<Ninja> getAllNinjas() {
        return ninjaRepository.findAll();
    }

    // pagination that works most of the time
    public Page<Ninja> getNinjasPaginated(int page, int size, String sortBy, String direction, 
                                         String nameFilter, BeltType beltFilter, Boolean lockedFilter) {
        // spring data sort setup because i couldn't figure out how to do it dynamically
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort;
        
        switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "belt":
                sort = Sort.by(sortDirection, "currentBeltType");
                break;
            case "bux":
                sort = Sort.by(sortDirection, "cachedBuxBalanceQuarters");
                break;
            case "legacy":
                sort = Sort.by(sortDirection, "cachedLegacyBalance");
                break;
            case "locked":
                sort = Sort.by(sortDirection, "isLocked");
                break;
            case "name":
            default:
                sort = Sort.by(sortDirection, "firstName", "lastName");
                break;
        }
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // trim whitespace because users paste garbage
        String name = (nameFilter != null && !nameFilter.trim().isEmpty()) ? nameFilter.trim() : null;
        
        return ninjaRepository.findByFilters(name, beltFilter, lockedFilter, pageable);
    }

    public Optional<Ninja> getNinjaByUsername(String username) {
        Optional<Ninja> ninjaOpt = ninjaRepository.findByUsernameIgnoreCase(username);
        if (ninjaOpt.isPresent()) {
            Ninja ninja = ninjaOpt.get();
            // check lock status before letting them do anything
            if (ninja.isLocked()) {
                // locked accounts get the boot immediately
                String reason = ninja.getLockReason() != null ? ninja.getLockReason() : "Account is locked";
                throw new AccountLockedException(reason);
            }
        }
        return ninjaOpt;
    }

    public Ninja updateNinja(Long ninjaId, String firstName, String lastName, String username,
                            BeltType beltType, Integer level, Integer lesson) {
        return updateNinja(ninjaId, firstName, lastName, username, beltType, level, lesson, null);
    }

    public Ninja updateNinja(Long ninjaId, String firstName, String lastName, String username,
                            BeltType beltType, Integer level, Integer lesson, String adminNote) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        if (firstName != null) ninja.setFirstName(firstName);
        if (lastName != null) ninja.setLastName(lastName);
        if (username != null) ninja.setUsername(username);
        // empty string means delete the note, null means leave it alone
        if (adminNote != null) {
            ninja.setAdminNote(adminNote.isEmpty() ? null : adminNote);
        }

        int oldTotalEarned = ninja.getTotalBuxEarned();
        // dont spam history when creating a new ninja from scratch
        boolean isInitialSetup = (ninja.getCurrentBeltType() == BeltType.WHITE &&
                                  ninja.getCurrentLevel() == 0 &&
                                  ninja.getCurrentLesson() == 0 &&
                                  ninja.getTotalBuxEarned() == 0);

        BeltType oldBelt = ninja.getCurrentBeltType();
        int oldLevel = ninja.getCurrentLevel();
        int oldLesson = ninja.getCurrentLesson();

        int oldProgressionEarned = beltRewardCalculator.calculateBalance(
            ninja.getCurrentBeltType(),
            ninja.getCurrentLevel(),
            ninja.getCurrentLesson()
        );

        int buxGained = 0;
        
        if (beltType != null || level != null || lesson != null) {
            if (!beltRewardCalculator.isValidProgress(ninja.getCurrentBeltType(), ninja.getCurrentLevel(), ninja.getCurrentLesson())) {
                String errorMessage = beltRewardCalculator.getValidationErrorMessage(ninja.getCurrentBeltType(), ninja.getCurrentLevel(), ninja.getCurrentLesson());
                throw new InvalidProgressException(errorMessage);
            }

            int newProgressionEarned = beltRewardCalculator.calculateBalance(
                ninja.getCurrentBeltType(),
                ninja.getCurrentLevel(),
                ninja.getCurrentLesson()
            );
            
            int progressionDiff = newProgressionEarned - oldProgressionEarned;
            ninja.setTotalBuxEarned(oldTotalEarned + progressionDiff);
            
            buxGained = progressionDiff;

            if (!isInitialSetup && buxGained > 0) {
                ProgressHistory history = new ProgressHistory(
                    ninja,
                    ninja.getCurrentBeltType(),
                    ninja.getCurrentLevel(),
                    ninja.getCurrentLesson(),
                    buxGained,
                    ProgressHistory.EarningType.LEVEL_UP
                );
                progressHistoryRepository.save(history);

                ninja.setLastProgressUpdate(LocalDateTime.now());
            }
        }
        
        Ninja savedNinja = ninjaRepository.save(ninja);

        if (notificationService != null && !isInitialSetup && (beltType != null || level != null || lesson != null)) {
            try {
                boolean beltUp = beltType != null && !oldBelt.equals(ninja.getCurrentBeltType());
                boolean levelUp = level != null && oldLevel != ninja.getCurrentLevel();
                boolean lessonComplete = buxGained > 0 && 
                                        ((lesson != null && oldLesson != ninja.getCurrentLesson()) || levelUp || beltUp);

                if (lessonComplete && !levelUp && !beltUp) {
                    notificationService.sendLessonCompleteNotification(
                        savedNinja.getId(),
                        String.format("Completed lesson! Earned %d Bux", buxGained)
                    );
                } else if ((beltUp || levelUp) && buxGained > 0) {
                    String ninjaName = savedNinja.getFirstName() + " " + savedNinja.getLastName();
                    
                    if (beltUp) {
                        java.util.Map<String, Object> beltData = new java.util.HashMap<>();
                        beltData.put("belt", ninja.getCurrentBeltType().toString());
                        beltData.put("level", ninja.getCurrentLevel());
                        beltData.put("lesson", ninja.getCurrentLesson());
                        beltData.put("buxEarned", buxGained);
                        beltData.put("ninjaName", ninjaName);
                        beltData.put("ninjaId", savedNinja.getId());
                        
                        notificationService.sendLevelUpNotification(
                            savedNinja.getId(),
                            String.format("Belt Up! %s Belt Level %d - Earned %d Bux", 
                                ninja.getCurrentBeltType(), ninja.getCurrentLevel(), buxGained),
                            beltData
                        );
                        
                        notificationService.sendBroadcastNinjaBeltUp(
                            savedNinja.getId(),
                            ninjaName,
                            String.format("%s earned their %s Belt! ��", ninjaName, ninja.getCurrentBeltType()),
                            beltData
                        );
                    } else if (levelUp) {
                        java.util.Map<String, Object> levelData = new java.util.HashMap<>();
                        levelData.put("belt", ninja.getCurrentBeltType().toString());
                        levelData.put("level", ninja.getCurrentLevel());
                        levelData.put("lesson", ninja.getCurrentLesson());
                        levelData.put("buxEarned", buxGained);
                        levelData.put("ninjaName", ninjaName);
                        levelData.put("ninjaId", savedNinja.getId());
                        
                        notificationService.sendLevelUpNotification(
                            savedNinja.getId(),
                            String.format("Level Up! %s Belt Level %d - Earned %d Bux", 
                                ninja.getCurrentBeltType(), ninja.getCurrentLevel(), buxGained),
                            levelData
                        );
                        
                        notificationService.sendBroadcastNinjaLevelUp(
                            savedNinja.getId(),
                            ninjaName,
                            String.format("%s leveled up to %s Belt Level %d! ��", ninjaName, ninja.getCurrentBeltType(), ninja.getCurrentLevel()),
                            levelData
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending progress notification: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (achievementService != null && !isInitialSetup && (beltType != null || level != null || lesson != null)) {
            try {
                achievementService.checkAndUnlockAchievements(savedNinja.getId());
            } catch (Exception e) {
                System.err.println("Error checking achievements: " + e.getMessage());
            }
        }

        return savedNinja;
    }

    @Transactional
    public void deleteNinja(Long ninjaId) {
        if (!ninjaRepository.existsById(ninjaId)) {
            throw new NinjaNotFoundException(ninjaId);
        }

        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        // delete achievements first because foreign keys are mean
        if (achievementProgressRepository != null) {
            try {
                achievementProgressRepository.deleteByNinja(ninja);
            } catch (Exception e) {
                System.err.println("Error deleting achievement progress: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            List<com.example.NinjaBux.domain.QuestionAnswer> questionAnswers = 
                questionAnswerRepository.findAll().stream()
                    .filter(qa -> qa.getNinja().getId().equals(ninjaId))
                    .collect(java.util.stream.Collectors.toList());
            if (!questionAnswers.isEmpty()) {
                questionAnswerRepository.deleteAll(questionAnswers);
            }
        } catch (Exception e) {
            System.err.println("Error deleting question answers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete question answers for ninja: " + ninjaId, e);
        }

        try {
            List<com.example.NinjaBux.domain.Purchase> purchases = 
                purchaseRepository.findByNinjaOrderByPurchaseDateDesc(ninja);
            if (!purchases.isEmpty()) {
                purchaseRepository.deleteAll(purchases);
            }
        } catch (Exception e) {
            System.err.println("Error deleting purchases: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete purchases for ninja: " + ninjaId, e);
        }

        try {
            List<ProgressHistory> progressHistory = 
                progressHistoryRepository.findAll().stream()
                    .filter(ph -> ph.getNinja().getId().equals(ninjaId))
                    .collect(java.util.stream.Collectors.toList());
            if (!progressHistory.isEmpty()) {
                progressHistoryRepository.deleteAll(progressHistory);
            }
        } catch (Exception e) {
            System.err.println("Error deleting progress history: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete progress history for ninja: " + ninjaId, e);
        }

        // delete ledger stuff before deleting the ninja or hibernate cries
        try {
            List<com.example.NinjaBux.domain.LedgerTxn> ledgerTxns = 
                ledgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
            if (!ledgerTxns.isEmpty()) {
                ledgerTxnRepository.deleteAll(ledgerTxns);
            }
            
            List<com.example.NinjaBux.domain.LegacyLedgerTxn> legacyTxns = 
                legacyLedgerTxnRepository.findByNinjaOrderByCreatedAtDesc(ninja);
            if (!legacyTxns.isEmpty()) {
                legacyLedgerTxnRepository.deleteAll(legacyTxns);
            }
        } catch (Exception e) {
            System.err.println("Error deleting ledger transactions: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete ledger transactions for ninja: " + ninjaId, e);
        }

        try {
            ninjaRepository.deleteById(ninjaId);
        } catch (Exception e) {
            System.err.println("Error deleting ninja: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete ninja: " + ninjaId, e);
        }
    }

    public LeaderboardResponse getLeaderboard(int topN, String period, Boolean excludeLocked) {
        LocalDateTime startDate = getStartDateForPeriod(period);

        if ("lifetime".equalsIgnoreCase(period) || startDate == null) {
            return getLifetimeLeaderboard(topN, excludeLocked);
        } else {
            return getTimePeriodLeaderboard(topN, startDate, excludeLocked);
        }
    }

    // old api calls need this or everything breaks
    public LeaderboardResponse getLeaderboard(int topN, String period) {
        return getLeaderboard(topN, period, false);
    }

    private void populateAchievements(LeaderboardEntry entry) {
        if (achievementService != null) {
            try {
                List<AchievementProgressDTO> topAchievements = achievementService.getTopAchievements(entry.getNinjaId(), 3);
                entry.setTopAchievements(topAchievements);
                
                AchievementProgressDTO leaderboardBadge = achievementService.getLeaderboardBadge(entry.getNinjaId());
                entry.setLeaderboardBadge(leaderboardBadge);
            } catch (Exception e) {
                entry.setTopAchievements(Collections.emptyList());
                entry.setLeaderboardBadge(null);
            }
        } else {
            entry.setTopAchievements(Collections.emptyList());
            entry.setLeaderboardBadge(null);
        }
    }

    private LocalDateTime getStartDateForPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "daily":
                return now.minusDays(1);
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "lifetime":
            default:
                return null;
        }
    }

    private LeaderboardResponse getLifetimeLeaderboard(int topN, Boolean excludeLocked) {
        Pageable pageable = PageRequest.of(0, topN);

        List<Ninja> topEarnerNinjas = ninjaRepository.findAllByOrderByTotalBuxEarnedDesc(pageable);
        List<LeaderboardEntry> topEarners = topEarnerNinjas.stream()
            .filter(n -> !excludeLocked || !n.isLocked())
            .filter(n -> {
                double balance = ledgerService.getBuxBalance(n.getId());
                return balance > 0;
            })
            .map((ninja) -> {
                int rank = topEarnerNinjas.indexOf(ninja) + 1;
                double balance = ledgerService.getBuxBalance(ninja.getId());
                LeaderboardEntry entry = new LeaderboardEntry(
                    ninja.getId(),
                    ninja.getFirstName(),
                    ninja.getLastName(),
                    ninja.getUsername(),
                    ninja.getCurrentBeltType(),
                    (int) balance,
                    ninja.getTotalBuxSpent(),
                    rank
                );
                if (rank == 1) {
                    entry.setTopEarner(true);
                }
                populateAchievements(entry);
                return entry;
            })
            .collect(Collectors.toList());

        List<Ninja> topSpenderNinjas = ninjaRepository.findAllByOrderByTotalBuxSpentDesc(pageable);
        List<LeaderboardEntry> topSpenders = topSpenderNinjas.stream()
            .filter(n -> !excludeLocked || !n.isLocked())
            .filter(n -> n.getTotalBuxSpent() > 0)
            .map((ninja) -> {
                int rank = topSpenderNinjas.indexOf(ninja) + 1;
                double balance = ledgerService.getBuxBalance(ninja.getId());
                LeaderboardEntry entry = new LeaderboardEntry(
                    ninja.getId(),
                    ninja.getFirstName(),
                    ninja.getLastName(),
                    ninja.getUsername(),
                    ninja.getCurrentBeltType(),
                    (int) balance,
                    ninja.getTotalBuxSpent(),
                    rank
                );
                if (rank == 1) {
                    entry.setTopSpender(true);
                }
                populateAchievements(entry);
                return entry;
            })
            .collect(Collectors.toList());

        List<LeaderboardEntry> mostImproved = new ArrayList<>();
        List<LeaderboardEntry> quizChampions = new ArrayList<>();
        List<LeaderboardEntry> streakLeaders = new ArrayList<>();

        LeaderboardResponse response = new LeaderboardResponse(topEarners, topSpenders, mostImproved, quizChampions, streakLeaders);
        if (topEarners.isEmpty()) {
            response.setMessage("No users qualify for this leaderboard");
        }
        return response;
    }

    private LeaderboardResponse getTimePeriodLeaderboard(int topN, LocalDateTime startDate, Boolean excludeLocked) {
        List<Object[]> earnerResults = progressHistoryRepository.findTopEarnersSince(startDate);

        List<LeaderboardEntry> topEarners = earnerResults.stream()
            .limit(topN)
            .map((result) -> {
                Long ninjaId = (Long) result[0];
                Long totalEarned = (Long) result[1];
                Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
                if (ninja == null) return null;
                if (excludeLocked && ninja.isLocked()) return null;
                if (totalEarned.intValue() == 0) return null;

                int rank = earnerResults.indexOf(result) + 1;
                LeaderboardEntry entry = new LeaderboardEntry(
                    ninja.getId(),
                    ninja.getFirstName(),
                    ninja.getLastName(),
                    ninja.getUsername(),
                    ninja.getCurrentBeltType(),
                    totalEarned.intValue(),
                    ninja.getTotalBuxSpent(),
                    rank
                );
                if (rank == 1) {
                    entry.setTopEarner(true);
                }
                populateAchievements(entry);
                return entry;
            })
            .filter(entry -> entry != null)
            .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0, topN);
        List<Ninja> topSpenderNinjas = ninjaRepository.findAllByOrderByTotalBuxSpentDesc(pageable);
        List<LeaderboardEntry> topSpenders = topSpenderNinjas.stream()
            .filter(n -> !excludeLocked || !n.isLocked())
            .filter(n -> n.getTotalBuxSpent() > 0)
            .map((ninja) -> {
                int rank = topSpenderNinjas.indexOf(ninja) + 1;
                double balance = ledgerService.getBuxBalance(ninja.getId());
                LeaderboardEntry entry = new LeaderboardEntry(
                    ninja.getId(),
                    ninja.getFirstName(),
                    ninja.getLastName(),
                    ninja.getUsername(),
                    ninja.getCurrentBeltType(),
                    (int) balance,
                    ninja.getTotalBuxSpent(),
                    rank
                );
                if (rank == 1) {
                    entry.setTopSpender(true);
                }
                populateAchievements(entry);
                return entry;
            })
            .collect(Collectors.toList());

        List<LeaderboardEntry> mostImproved = getMostImprovedLeaderboard(topN, startDate, excludeLocked);

        List<LeaderboardEntry> quizChampions = getQuizChampionsLeaderboard(topN, startDate, excludeLocked);

        List<LeaderboardEntry> streakLeaders = new ArrayList<>();

        LeaderboardResponse response = new LeaderboardResponse(topEarners, topSpenders, mostImproved, quizChampions, streakLeaders);
        if (topEarners.isEmpty() && topSpenders.isEmpty() && mostImproved.isEmpty() && quizChampions.isEmpty()) {
            response.setMessage("No users qualify for this leaderboard");
        }
        return response;
    }

    // keeping this for old code that still calls it
    private LeaderboardResponse getTimePeriodLeaderboard(int topN, LocalDateTime startDate) {
        return getTimePeriodLeaderboard(topN, startDate, false);
    }

    private List<LeaderboardEntry> getMostImprovedLeaderboard(int topN, LocalDateTime startDate, Boolean excludeLocked) {
        List<ProgressHistory> allRecords = progressHistoryRepository.findAll().stream()
            .filter(ph -> ph.getEarningType() == ProgressHistory.EarningType.LEVEL_UP)
            .collect(Collectors.toList());

        List<Long> activeNinjaIds = allRecords.stream()
            .map(ph -> ph.getNinja().getId())
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Integer> lessonsAdvanced = new HashMap<>();

        for (Long ninjaId : activeNinjaIds) {
            Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
            if (ninja == null) continue;
            if (excludeLocked && ninja.isLocked()) continue;

            List<ProgressHistory> ninjaAllRecords = allRecords.stream()
                .filter(ph -> ph.getNinja().getId().equals(ninjaId))
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());

            if (ninjaAllRecords.isEmpty()) continue;

            List<ProgressHistory> beforeStart = ninjaAllRecords.stream()
                .filter(ph -> ph.getTimestamp().isBefore(startDate))
                .collect(Collectors.toList());

            int lessonsAtStart;
            if (!beforeStart.isEmpty()) {
                ProgressHistory startRecord = beforeStart.get(beforeStart.size() - 1);
                lessonsAtStart = BeltRewardCalculator.calculateTotalLessons(
                    startRecord.getBeltType(),
                    startRecord.getLevel(),
                    startRecord.getLesson()
                );
            } else {
                List<ProgressHistory> periodRecords = ninjaAllRecords.stream()
                    .filter(ph -> ph.getTimestamp().isAfter(startDate) || ph.getTimestamp().isEqual(startDate))
                    .collect(Collectors.toList());

                if (!periodRecords.isEmpty()) {
                    ProgressHistory firstRecord = periodRecords.get(0);
                    int buxEarned = firstRecord.getBuxEarned();
                    BeltType firstBelt = firstRecord.getBeltType();
                    int firstLevel = firstRecord.getLevel();
                    int firstLesson = firstRecord.getLesson();
                    

                    int[] lessonsPerLevel = BeltRewardCalculator.getLessonsPerLevel(firstBelt);
                    if (firstLevel > 1) {
                        if (lessonsPerLevel.length >= firstLevel - 1) {
                            int prevLevelLastLesson = lessonsPerLevel[firstLevel - 2];
                            lessonsAtStart = BeltRewardCalculator.calculateTotalLessons(
                                firstBelt,
                                firstLevel - 1,
                                prevLevelLastLesson
                            );
                        } else {
                            lessonsAtStart = BeltRewardCalculator.calculateTotalLessons(
                                firstBelt,
                                firstLevel,
                                firstLesson
                            );
                        }
                    } else if (firstLesson > 1) {
                        lessonsAtStart = BeltRewardCalculator.calculateTotalLessons(
                            firstBelt,
                            firstLevel,
                            firstLesson - 1
                        );
                    } else {
                        lessonsAtStart = BeltRewardCalculator.calculateTotalLessons(
                            firstBelt,
                            firstLevel,
                            firstLesson
                        );
                    }
                } else {
                    continue;
                }
            }

            int lessonsAtEnd = BeltRewardCalculator.calculateTotalLessons(
                ninja.getCurrentBeltType(),
                ninja.getCurrentLevel(),
                ninja.getCurrentLesson()
            );

            int advanced = lessonsAtEnd - lessonsAtStart;
            if (advanced > 0) {
                lessonsAdvanced.put(ninjaId, advanced);
            }
        }

        List<Map.Entry<Long, Integer>> sorted = lessonsAdvanced.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(topN)
            .collect(Collectors.toList());

        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<Long, Integer> entry = sorted.get(i);
            Long ninjaId = entry.getKey();
            Integer lessons = entry.getValue();
            Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
            if (ninja == null) continue;
            if (lessons == 0) continue;

            LeaderboardEntry leaderboardEntry = new LeaderboardEntry(
                ninja.getId(),
                ninja.getFirstName(),
                ninja.getLastName(),
                ninja.getUsername(),
                ninja.getCurrentBeltType(),
                lessons,
                ninja.getTotalBuxSpent(),
                i + 1
            );
            populateAchievements(leaderboardEntry);
            entries.add(leaderboardEntry);
        }
        return entries;
    }

    private List<LeaderboardEntry> getQuizChampionsLeaderboard(int topN, LocalDateTime startDate, Boolean excludeLocked) {
        List<Object[]> results = questionAnswerRepository.findQuizChampionsSince(startDate);

        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, results.size()); i++) {
            Object[] result = results.get(i);
            Long ninjaId = (Long) result[0];
            Long correctCount = (Long) result[1];
            Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
            if (ninja == null) continue;
            if (excludeLocked && ninja.isLocked()) continue;
            if (correctCount.intValue() == 0) continue;

            LeaderboardEntry entry = new LeaderboardEntry(
                ninja.getId(),
                ninja.getFirstName(),
                ninja.getLastName(),
                ninja.getUsername(),
                ninja.getCurrentBeltType(),
                correctCount.intValue(),
                ninja.getTotalBuxSpent(),
                i + 1
            );
            populateAchievements(entry);
            entries.add(entry);
        }
        return entries;
    }

    // backwards compat wrapper because refactoring is scary
    private List<LeaderboardEntry> getMostImprovedLeaderboard(int topN, LocalDateTime startDate) {
        return getMostImprovedLeaderboard(topN, startDate, false);
    }

    // same deal, different method
    private List<LeaderboardEntry> getQuizChampionsLeaderboard(int topN, LocalDateTime startDate) {
        return getQuizChampionsLeaderboard(topN, startDate, false);
    }

    @Transactional
    public Ninja awardBux(Long ninjaId, int amount, String adminUsername, String notes) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Award amount must be positive");
        }

        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        String noteText = notes != null && !notes.trim().isEmpty() 
            ? notes 
            : String.format("Admin award: %d Bux", amount);

        // ledger handles the actual money now
        ledgerService.recordAdminAdjustment(
            ninjaId,
            amount,
            noteText,
            adminUsername != null ? adminUsername : "admin"
        );

        // keep history record for audit trail even though ledger has it
        ProgressHistory history = new ProgressHistory(
            ninja,
            ninja.getCurrentBeltType(),
            ninja.getCurrentLevel(),
            ninja.getCurrentLesson(),
            amount,
            ProgressHistory.EarningType.ADMIN_AWARD
        );
        history.setNotes(noteText);
        history.setAdminUsername(adminUsername != null ? adminUsername : "admin");
        progressHistoryRepository.save(history);

        return ninjaRepository.findById(ninjaId).orElse(ninja);
    }

    @Transactional
    public Ninja deductBux(Long ninjaId, int amount, String adminUsername, String notes) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deduct amount must be positive");
        }

        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        // check balance from ledger because the ninja object lies
        double currentBalance = ledgerService.getBuxBalance(ninjaId);
        if (currentBalance < amount) {
            throw new IllegalArgumentException("Cannot deduct more Bux than ninja has");
        }

        String noteText = notes != null && !notes.trim().isEmpty() 
            ? notes 
            : String.format("Admin deduction: %d Bux", amount);

        // ledger handles negative adjustments too
        ledgerService.recordAdminAdjustment(
            ninjaId,
            -amount,
            noteText,
            adminUsername != null ? adminUsername : "admin"
        );

        return ninjaRepository.findById(ninjaId).orElse(ninja);
    }

    public List<ProgressHistory> getProgressHistory(Long ninjaId) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        return progressHistoryRepository.findAll().stream()
            .filter(ph -> ph.getNinja().getId().equals(ninjaId))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }

    @Transactional
    public Ninja banSuggestions(Long ninjaId, boolean banned) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        ninja.setSuggestionsBanned(banned);
        return ninjaRepository.save(ninja);
    }

    // lock account and make them sad
    @Transactional
    public Ninja lockAccount(Long ninjaId, String reason, String adminUsername) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        
        ninja.setLocked(true);
        ninja.setLockReason(reason);
        ninja.setLockedAt(LocalDateTime.now());
        
        ninja = ninjaRepository.save(ninja);
        
        // tell them via websocket that they're locked
        if (notificationService != null) {
            try {
                String lockMessage = reason != null && !reason.trim().isEmpty() 
                    ? reason 
                    : "Your account has been locked. Please get back to work!";
                notificationService.sendLockNotification(ninjaId, lockMessage);
            } catch (Exception e) {
                System.err.println("Error sending lock notification: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return ninja;
    }

    // unlock account and let them back in
    @Transactional
    public Ninja unlockAccount(Long ninjaId, String adminUsername) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        
        ninja.setLocked(false);
        ninja.setLockReason(null);
        ninja.setLockedAt(null);
        
        ninja = ninjaRepository.save(ninja);
        
        // websocket notification because they probably refreshed
        if (notificationService != null) {
            try {
                notificationService.sendUnlockNotification(ninjaId);
            } catch (Exception e) {
                System.err.println("Error sending unlock notification: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return ninja;
    }

    // create a correction entry because admins make mistakes
    @Transactional
    public ProgressHistory createProgressHistoryCorrection(Long ninjaId, Long originalEntryId,
                                                          BeltType beltType, int level, int lesson,
                                                          int buxDelta, Integer legacyDelta,
                                                          String notes, String adminUsername) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));
        
        // verify the original entry actually exists
        ProgressHistory original = progressHistoryRepository.findById(originalEntryId)
            .orElseThrow(() -> new IllegalArgumentException("Original progress history entry not found"));
        
        if (!original.getNinja().getId().equals(ninjaId)) {
            throw new IllegalArgumentException("Original entry does not belong to this ninja");
        }
        
        // create the correction record
        ProgressHistory correction = new ProgressHistory(
            ninja,
            beltType,
            level,
            lesson,
            buxDelta,
            ProgressHistory.EarningType.ADMIN_CORRECTION
        );
        correction.setLegacyDelta(legacyDelta);
        correction.setNotes(notes);
        correction.setAdminUsername(adminUsername);
        correction.setCorrectionToId(originalEntryId);
        correction.setCorrection(true);
        
        // adjust balance if money changed
        if (buxDelta != 0) {
            ledgerService.recordAdminAdjustment(
                ninjaId,
                buxDelta,
                notes != null ? notes : String.format("Correction to entry #%d: %+d Bux", originalEntryId, buxDelta),
                adminUsername
            );
        }
        
        // legacy is dead, long live legacy
        
        return progressHistoryRepository.save(correction);
    }

    // update progress and fix balance if needed
    @Transactional
    public Ninja updateProgressWithCorrection(Long ninjaId, BeltType newBelt, int newLevel, int newLesson, String adminUsername) {
        Ninja ninja = ninjaRepository.findById(ninjaId)
            .orElseThrow(() -> new NinjaNotFoundException(ninjaId));

        if (!beltRewardCalculator.isValidProgress(newBelt, newLevel, newLesson)) {
            String errorMessage = beltRewardCalculator.getValidationErrorMessage(newBelt, newLevel, newLesson);
            throw new InvalidProgressException(errorMessage);
        }

        BeltType oldBelt = ninja.getCurrentBeltType();
        int oldLevel = ninja.getCurrentLevel();
        int oldLesson = ninja.getCurrentLesson();

        // calculate what balance should be at each position
        int oldExpectedBalance = beltRewardCalculator.calculateBalance(oldBelt, oldLevel, oldLesson);
        int newExpectedBalance = beltRewardCalculator.calculateBalance(newBelt, newLevel, newLesson);
        int balanceDifference = newExpectedBalance - oldExpectedBalance;

        // update the progress
        ninja.setCurrentBeltType(newBelt);
        ninja.setCurrentLevel(newLevel);
        ninja.setCurrentLesson(newLesson);
        ninja.setLastProgressUpdate(LocalDateTime.now());
        ninjaRepository.save(ninja);

        // record correction in history even though no money changed
        ProgressHistory history = new ProgressHistory(
            ninja,
            newBelt,
            newLevel,
            newLesson,
            0, // corrections dont grant money, they just fix mistakes
            ProgressHistory.EarningType.ADMIN_CORRECTION
        );
        progressHistoryRepository.save(history);

        // if balance changed, offset it with an adjustment
        if (balanceDifference != 0) {
            ledgerService.recordAdminAdjustment(
                ninjaId,
                balanceDifference,
                String.format("Progress correction: %s L%d-L%d -> %s L%d-L%d (offset: %+d Bux)",
                    oldBelt, oldLevel, oldLesson, newBelt, newLevel, newLesson, balanceDifference),
                adminUsername
            );
        }

        return ninja;
    }
}
