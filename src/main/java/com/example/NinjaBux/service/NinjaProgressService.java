package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.domain.enums.BeltPath;
import com.example.NinjaBux.domain.enums.BeltPath;
import com.example.NinjaBux.exception.AccountLockedException;
import com.example.NinjaBux.exception.InvalidProgressException;
import com.example.NinjaBux.exception.NinjaNotFoundException;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.repository.ProgressHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NinjaProgressService extends NinjaServiceBase {

  @Autowired private ProgressHistoryRepository progressHistoryRepository;

  @Autowired private BeltRewardCalculator beltRewardCalculator;

  @Autowired private LedgerService ledgerService;

  @Autowired(required = false)
  private AchievementService achievementService;

  @Transactional
  public Ninja createNinja(
      String firstName,
      String lastName,
      String username,
      BeltType beltType,
      Integer level,
      Integer lesson,
      BeltPath beltPath) {
    BeltType startingBelt = beltType != null ? beltType : BeltType.WHITE;
    int startingLevel = level != null ? level : 1;
    int startingLesson = lesson != null ? lesson : 1;
    BeltPath startingPath = beltPath != null ? beltPath : BeltPath.UNITY;

    if (!beltRewardCalculator.isValidProgress(startingBelt, startingLevel, startingLesson, startingPath)) {
      String errorMessage =
          beltRewardCalculator.getValidationErrorMessage(
              startingBelt, startingLevel, startingLesson, startingPath);
      throw new InvalidProgressException(errorMessage);
    }

    Ninja ninja =
        new Ninja(firstName, lastName, username, startingLesson, startingLevel, startingBelt);
    ninja.setBeltPath(startingPath);

    int lessonsCompleted =
        BeltRewardCalculator.calculateTotalLessons(
            startingBelt, startingLevel, startingLesson, startingPath);
    int rawBalance = lessonsCompleted;
    int computedBalance = lessonsCompleted;

    ninja.setLegacyPoints(rawBalance);
    ninja = ninjaRepository.save(ninja);

    ledgerService.onboardNinjaWithLegacy(ninja.getId(), computedBalance);
    ledgerService.grantLegacy(
        ninja.getId(),
        rawBalance,
        String.format("Legacy points for onboarding (%d raw)", rawBalance));

    if (achievementService != null) {
      try {
        achievementService.checkAndUnlockAchievements(ninja.getId());
      } catch (Exception ignored) {
      }
    }

    return ninjaRepository.findById(ninja.getId()).orElse(ninja);
  }

  @Transactional
  public Ninja updateProgress(Long ninjaId, BeltType newBelt, int newLevel, int newLesson, BeltPath beltPath) {
    Ninja ninja = findNinja(ninjaId);
    if (beltPath != null) {
      ninja.setBeltPath(beltPath);
    }
    return applyProgressUpdate(ninja, newBelt, newLevel, newLesson);
  }

  @Transactional
  public Ninja updateNinja(
      Long ninjaId,
      String firstName,
      String lastName,
      String username,
      BeltType beltType,
      Integer level,
      Integer lesson,
      BeltPath beltPath) {
    Ninja ninja = findNinja(ninjaId);

    if (firstName != null) {
      ninja.setFirstName(firstName);
    }
    if (lastName != null) {
      ninja.setLastName(lastName);
    }
    if (username != null) {
      ninja.setUsername(username);
    }
    if (beltPath != null) {
      ninja.setBeltPath(beltPath);
    }

    ninja = ninjaRepository.save(ninja);

    if (beltType != null || level != null || lesson != null || beltPath != null) {
      BeltType targetBelt = beltType != null ? beltType : ninja.getCurrentBeltType();
      int targetLevel = level != null ? level : ninja.getCurrentLevel();
      int targetLesson = lesson != null ? lesson : ninja.getCurrentLesson();
      ninja = applyProgressUpdate(ninja, targetBelt, targetLevel, targetLesson);
    }

    return ninja;
  }

  public List<ProgressHistory> getProgressHistory(Long ninjaId) {
    Ninja ninja = findNinja(ninjaId);
    return progressHistoryRepository.findByNinjaOrderByTimestampDesc(ninja);
  }

  @Transactional
  public ProgressHistory createProgressHistoryCorrection(
      Long ninjaId,
      Long originalEntryId,
      BeltType beltType,
      int level,
      int lesson,
      int buxDelta,
      Integer legacyDelta,
      String notes,
      String adminUsername) {
    Ninja ninja = findNinja(ninjaId);

    ProgressHistory original =
        progressHistoryRepository
            .findById(originalEntryId)
            .orElseThrow(
                () -> new IllegalArgumentException("Original progress history entry not found"));

    if (!original.getNinja().getId().equals(ninjaId)) {
      throw new IllegalArgumentException("Original entry does not belong to this ninja");
    }

    ProgressHistory correction =
        new ProgressHistory(
            ninja, beltType, level, lesson, buxDelta, ProgressHistory.EarningType.ADMIN_CORRECTION);
    correction.setLegacyDelta(legacyDelta);
    correction.setNotes(notes);
    correction.setAdminUsername(adminUsername);
    correction.setCorrectionToId(originalEntryId);
    correction.setCorrection(true);

    if (buxDelta != 0) {
      ledgerService.recordAdminAdjustment(
          ninjaId,
          buxDelta,
          notes != null
              ? notes
              : String.format("Correction to entry #%d: %+d Bux", originalEntryId, buxDelta),
          adminUsername);
    }

    return progressHistoryRepository.save(correction);
  }

  @Transactional
  public Ninja updateProgressWithCorrection(
      Long ninjaId, BeltType newBelt, int newLevel, int newLesson, BeltPath beltPath, String adminUsername) {
    Ninja ninja = findNinja(ninjaId);
    BeltPath path = beltPath != null ? beltPath : ninja.getBeltPath();

    if (!beltRewardCalculator.isValidProgress(newBelt, newLevel, newLesson, path)) {
      String errorMessage =
          beltRewardCalculator.getValidationErrorMessage(newBelt, newLevel, newLesson, path);
      throw new InvalidProgressException(errorMessage);
    }

    BeltType oldBelt = ninja.getCurrentBeltType();
    int oldLevel = ninja.getCurrentLevel();
    int oldLesson = ninja.getCurrentLesson();

    ninja.setBeltPath(path);

    int oldExpectedBalance = beltRewardCalculator.calculateBalance(oldBelt, oldLevel, oldLesson, path);
    int newExpectedBalance = beltRewardCalculator.calculateBalance(newBelt, newLevel, newLesson, path);
    int balanceDifference = newExpectedBalance - oldExpectedBalance;

    ninja.setCurrentBeltType(newBelt);
    ninja.setCurrentLevel(newLevel);
    ninja.setCurrentLesson(newLesson);
    ninja.setLastProgressUpdate(LocalDateTime.now());
    ninjaRepository.save(ninja);

    ProgressHistory history =
        new ProgressHistory(
            ninja, newBelt, newLevel, newLesson, 0, ProgressHistory.EarningType.ADMIN_CORRECTION);
    progressHistoryRepository.save(history);

    if (balanceDifference != 0) {
      ledgerService.recordAdminAdjustment(
          ninjaId,
          balanceDifference,
          String.format(
              "Progress correction: %s L%d-L%d -> %s L%d-L%d (offset: %+d Bux)",
              oldBelt, oldLevel, oldLesson, newBelt, newLevel, newLesson, balanceDifference),
          adminUsername);
    }

    return ninja;
  }

  private Ninja applyProgressUpdate(Ninja ninja, BeltType newBelt, int newLevel, int newLesson) {
    checkAccountLocked(ninja);
    BeltPath beltPath =
        ninja.getBeltPath() != null ? ninja.getBeltPath() : BeltPath.UNITY;

    if (!beltRewardCalculator.isValidProgress(newBelt, newLevel, newLesson, beltPath)) {
      String errorMessage =
          beltRewardCalculator.getValidationErrorMessage(newBelt, newLevel, newLesson, beltPath);
      throw new InvalidProgressException(errorMessage);
    }

    BeltType oldBelt = ninja.getCurrentBeltType();
    int oldLevel = ninja.getCurrentLevel();
    int oldLesson = ninja.getCurrentLesson();

    boolean beltUp = !oldBelt.equals(newBelt);
    boolean levelUp = oldLevel != newLevel;
    boolean progressedLessons =
        !beltUp && !levelUp && newLesson > oldLesson && oldLevel == newLevel;

    ninja.setCurrentBeltType(newBelt);
    ninja.setCurrentLevel(newLevel);
    ninja.setCurrentLesson(newLesson);
    ninja.setLastProgressUpdate(LocalDateTime.now());
    ninja = ninjaRepository.save(ninja);

    int buxGained = 0;
    boolean lessonComplete = false;

    if (beltUp) {
      buxGained +=
          ledgerService.recordBeltUpReward(
              ninja.getId(), newBelt, String.format("Belt-up reward: %s", newBelt));
      lessonComplete = true;
    }

    if (levelUp) {
      buxGained +=
          ledgerService.recordLevelUpReward(
              ninja.getId(),
              newBelt,
              newLevel,
              String.format("Level-up reward: %s Belt Level %d", newBelt, newLevel));
      lessonComplete = true;
    }

    if (beltUp) {
      String note =
          String.format(
              "Lesson completion: %s Belt Level %d Lesson %d (belt up)",
              newBelt, newLevel, newLesson);
      buxGained += ledgerService.recordLessonEarning(ninja.getId(), newBelt, note);
      lessonComplete = true;
    } else if (levelUp) {
      String note =
          String.format(
              "Lesson completion: %s Belt Level %d Lesson %d (level up)",
              newBelt, newLevel, newLesson);
      buxGained += ledgerService.recordLessonEarning(ninja.getId(), newBelt, note);
      lessonComplete = true;
    } else if (progressedLessons) {
      for (int lesson = oldLesson; lesson < newLesson; lesson++) {
        String note =
            String.format(
                "Lesson completion: %s Belt Level %d Lesson %d", newBelt, newLevel, lesson);
        buxGained += ledgerService.recordLessonEarning(ninja.getId(), newBelt, note);
      }
      if (newLesson > oldLesson) {
        lessonComplete = true;
      }
    }

    if (lessonComplete || buxGained > 0) {
      ProgressHistory history =
          new ProgressHistory(
              ninja,
              ninja.getCurrentBeltType(),
              ninja.getCurrentLevel(),
              ninja.getCurrentLesson(),
              buxGained,
              ProgressHistory.EarningType.LEVEL_UP);
      progressHistoryRepository.save(history);
    }

    if (achievementService != null && buxGained > 0) {
      try {
        achievementService.checkAndUnlockAchievements(ninja.getId());
      } catch (Exception ignored) {
      }
    }

    return ninja;
  }

  private void checkAccountLocked(Ninja ninja) {
    if (ninja.isLocked()) {
      throw new AccountLockedException("Account is locked");
    }
  }

}
