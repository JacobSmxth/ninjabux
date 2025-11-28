package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.*;
import com.example.NinjaBux.domain.enums.AchievementCategory;
import com.example.NinjaBux.domain.enums.BadgeRarity;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.dto.AchievementDTO;
import com.example.NinjaBux.dto.AchievementProgressDTO;
import com.example.NinjaBux.repository.AchievementProgressRepository;
import com.example.NinjaBux.repository.AchievementRepository;
import com.example.NinjaBux.repository.LegacyLedgerTxnRepository;
import com.example.NinjaBux.repository.NinjaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AchievementService {

  private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

  @Autowired private AchievementRepository achievementRepository;

  @Autowired private AchievementProgressRepository progressRepository;

  @Autowired private NinjaRepository ninjaRepository;

  @Autowired private AdminAuditService auditService;

  @Autowired private LedgerService ledgerService;

  @Autowired private LegacyLedgerTxnRepository legacyLedgerTxnRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public AchievementDTO createAchievement(AchievementDTO dto, String adminUsername) {
    Achievement achievement = new Achievement();
    updateAchievementFromDTO(achievement, dto);

    achievement = achievementRepository.save(achievement);

    auditService.log(
        adminUsername, "CREATE_ACHIEVEMENT", "Created achievement: " + achievement.getName());

    logger.info("Achievement created: {} by admin: {}", achievement.getName(), adminUsername);
    return new AchievementDTO(achievement);
  }

  public AchievementDTO updateAchievement(Long id, AchievementDTO dto, String adminUsername) {
    Achievement achievement =
        achievementRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + id));

    String oldName = achievement.getName();
    updateAchievementFromDTO(achievement, dto);

    achievement = achievementRepository.save(achievement);

    auditService.log(
        adminUsername,
        "UPDATE_ACHIEVEMENT",
        "Updated achievement: " + oldName + " -> " + achievement.getName());

    return new AchievementDTO(achievement);
  }

  public void deleteAchievement(Long id, String adminUsername) {
    Achievement achievement =
        achievementRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + id));

    String name = achievement.getName();
    achievementRepository.delete(achievement);

    auditService.log(adminUsername, "DELETE_ACHIEVEMENT", "Deleted achievement: " + name);

    logger.info("Achievement deleted: {} by admin: {}", name, adminUsername);
  }

  public AchievementDTO toggleActive(Long id, String adminUsername) {
    Achievement achievement =
        achievementRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + id));

    achievement.setActive(!achievement.isActive());
    achievement = achievementRepository.save(achievement);

    auditService.log(
        adminUsername,
        "TOGGLE_ACHIEVEMENT_STATUS",
        "Set achievement '" + achievement.getName() + "' active=" + achievement.isActive());

    return new AchievementDTO(achievement);
  }

  public List<AchievementDTO> getAllAchievements() {
    return achievementRepository.findAll().stream()
        .map(AchievementDTO::new)
        .collect(Collectors.toList());
  }

  public List<AchievementDTO> getActiveAchievements() {
    return achievementRepository.findByActiveTrue().stream()
        .map(AchievementDTO::new)
        .collect(Collectors.toList());
  }

  public List<AchievementDTO> getAchievementsByCategory(AchievementCategory category) {
    return achievementRepository.findByCategoryAndActiveTrue(category).stream()
        .map(AchievementDTO::new)
        .collect(Collectors.toList());
  }

  public AchievementDTO getAchievement(Long id) {
    Achievement achievement =
        achievementRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + id));
    return new AchievementDTO(achievement);
  }

  public List<AchievementProgressDTO> getNinjaAchievements(Long ninjaId, boolean includeHidden) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    List<Achievement> achievements =
        includeHidden
            ? achievementRepository.findByActiveTrue()
            : achievementRepository.findByHiddenFalseAndActiveTrue();

    List<AchievementProgressDTO> result = new ArrayList<>();

    for (Achievement achievement : achievements) {
      Optional<AchievementProgress> progress =
          progressRepository.findByNinjaAndAchievement(ninja, achievement);

      if (progress.isPresent()) {
        if (achievement.isHidden() && !progress.get().isUnlocked()) {
          continue;
        }
        AchievementProgress progressEntity = progress.get();
        if (!progressEntity.isUnlocked()) {
          int progressPercent = calculateProgressPercentage(ninja, achievement);
          progressEntity.setProgressValue(progressPercent);
          progressRepository.save(progressEntity);
        }
        result.add(new AchievementProgressDTO(progressEntity));
      } else {
        if (achievement.isHidden()) {
          if (checkUnlockCriteria(ninja, achievement)) {
            AchievementProgress newProgress = new AchievementProgress(ninja, achievement);
            newProgress.unlock(false, null);
            progressRepository.save(newProgress);
            result.add(new AchievementProgressDTO(newProgress));
          }
        } else {
          AchievementProgress newProgress = new AchievementProgress(ninja, achievement);
          int progressPercent = calculateProgressPercentage(ninja, achievement);
          newProgress.setProgressValue(progressPercent);
          result.add(new AchievementProgressDTO(progressRepository.save(newProgress)));
        }
      }
    }

    return result;
  }

  public List<AchievementProgressDTO> getUnlockedAchievements(Long ninjaId) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    return progressRepository.findByNinjaAndUnlockedTrue(ninja).stream()
        .map(AchievementProgressDTO::new)
        .collect(Collectors.toList());
  }

  public List<AchievementProgressDTO> getTopAchievements(Long ninjaId, int limit) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    return progressRepository.findTopAchievementsByNinja(ninja).stream()
        .limit(limit)
        .map(AchievementProgressDTO::new)
        .collect(Collectors.toList());
  }

  public AchievementProgressDTO getLeaderboardBadge(Long ninjaId) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    List<AchievementProgress> unlocked = progressRepository.findByNinjaAndUnlockedTrue(ninja);
    if (unlocked.isEmpty()) {
      return null;
    }

    Optional<AchievementProgress> selectedBadge =
        unlocked.stream().filter(AchievementProgress::isLeaderboardBadge).findFirst();

    if (selectedBadge.isPresent()) {
      return new AchievementProgressDTO(selectedBadge.get());
    }

    return unlocked.stream()
        .map(AchievementProgressDTO::new)
        .max(
            (a, b) -> {
              BadgeRarity rarityA = a.getAchievement().getRarity();
              BadgeRarity rarityB = b.getAchievement().getRarity();
              return Integer.compare(rarityA.ordinal(), rarityB.ordinal());
            })
        .orElse(null);
  }

  @Transactional
  public void setLeaderboardBadge(Long ninjaId, Long achievementProgressId) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    AchievementProgress selectedProgress =
        progressRepository
            .findById(achievementProgressId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Achievement progress not found: " + achievementProgressId));

    Ninja progressNinja = selectedProgress.getNinja();
    if (progressNinja == null || !progressNinja.getId().equals(ninjaId)) {
      throw new IllegalArgumentException("Achievement does not belong to this ninja");
    }

    if (!selectedProgress.isUnlocked()) {
      throw new IllegalArgumentException("Cannot set locked achievement as leaderboard badge");
    }

    List<AchievementProgress> allProgress = progressRepository.findByNinja(ninja);
    for (AchievementProgress progress : allProgress) {
      if (progress.isLeaderboardBadge() && !progress.getId().equals(achievementProgressId)) {
        progress.setLeaderboardBadge(false);
        progressRepository.save(progress);
      }
    }

    selectedProgress.setLeaderboardBadge(true);
    selectedProgress = progressRepository.save(selectedProgress);

    progressRepository.flush();
  }

  public List<AchievementProgressDTO> getUnseenAchievements(Long ninjaId) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    return progressRepository.findByNinjaAndUnlockedTrueAndSeenFalse(ninja).stream()
        .map(AchievementProgressDTO::new)
        .collect(Collectors.toList());
  }

  public void markAchievementsSeen(Long ninjaId, List<Long> progressIds) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    for (Long progressId : progressIds) {
      AchievementProgress progress = progressRepository.findById(progressId).orElse(null);
      if (progress != null && progress.getNinja().getId().equals(ninjaId)) {
        progress.setSeen(true);
        progressRepository.save(progress);
      }
    }
  }

  public AchievementProgressDTO awardAchievement(
      Long ninjaId, Long achievementId, String adminUsername) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    Achievement achievement =
        achievementRepository
            .findById(achievementId)
            .orElseThrow(
                () -> new IllegalArgumentException("Achievement not found: " + achievementId));

    AchievementProgress progress =
        progressRepository
            .findByNinjaAndAchievement(ninja, achievement)
            .orElse(new AchievementProgress(ninja, achievement));

    if (progress.isUnlocked()) {
      throw new IllegalStateException("Ninja already has this achievement");
    }

    progress.unlock(true, adminUsername);
    progress = progressRepository.save(progress);

    if (achievement.getBuxReward() > 0) {
      ledgerService.recordAchievementReward(
          ninjaId, achievement.getId(), achievement.getBuxReward(), achievement.getName());
    }

    auditService.log(
        adminUsername,
        "AWARD_ACHIEVEMENT",
        "Awarded achievement '"
            + achievement.getName()
            + "' ("
            + achievement.getBuxReward()
            + " Bux)",
        ninja.getId(),
        ninja.getFirstName() + " " + ninja.getLastName());

    logger.info(
        "Achievement manually awarded: {} to ninja: {} by admin: {}",
        achievement.getName(),
        ninja.getFirstName() + " " + ninja.getLastName(),
        adminUsername);

    return new AchievementProgressDTO(progress);
  }

  public void revokeAchievement(Long ninjaId, Long achievementId, String adminUsername) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    Achievement achievement =
        achievementRepository
            .findById(achievementId)
            .orElseThrow(
                () -> new IllegalArgumentException("Achievement not found: " + achievementId));

    AchievementProgress progress =
        progressRepository
            .findByNinjaAndAchievement(ninja, achievement)
            .orElseThrow(() -> new IllegalStateException("Ninja doesn't have this achievement"));

    if (!progress.isUnlocked()) {
      throw new IllegalStateException("Achievement is not unlocked");
    }

    if (achievement.getBuxReward() > 0) {
      ledgerService.recordAdminAdjustment(
          ninjaId,
          -achievement.getBuxReward(),
          String.format("Revoked achievement: %s", achievement.getName()),
          adminUsername);
    }

    progressRepository.delete(progress);

    auditService.log(
        adminUsername,
        "REVOKE_ACHIEVEMENT",
        "Revoked achievement '"
            + achievement.getName()
            + "' and deducted "
            + achievement.getBuxReward()
            + " Bux",
        ninja.getId(),
        ninja.getFirstName() + " " + ninja.getLastName());

    logger.info(
        "Achievement revoked: {} from ninja: {} by admin: {}",
        achievement.getName(),
        ninja.getFirstName() + " " + ninja.getLastName(),
        adminUsername);
  }

  public List<AchievementProgressDTO> checkAndUnlockAchievements(Long ninjaId) {
    Ninja ninja =
        ninjaRepository
            .findById(ninjaId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found: " + ninjaId));

    List<Achievement> autoAchievements = achievementRepository.findByActiveTrueAndManualOnlyFalse();
    List<AchievementProgressDTO> newlyUnlocked = new ArrayList<>();

    for (Achievement achievement : autoAchievements) {
      Optional<AchievementProgress> existingProgress =
          progressRepository.findByNinjaAndAchievement(ninja, achievement);

      if (existingProgress.isPresent() && existingProgress.get().isUnlocked()) {
        continue;
      }

      if (checkUnlockCriteria(ninja, achievement)) {
        AchievementProgress progress =
            existingProgress.orElse(new AchievementProgress(ninja, achievement));
        progress.unlock(false, null);
        progress = progressRepository.save(progress);

        if (achievement.getBuxReward() > 0) {
          ledgerService.recordAchievementReward(
              ninjaId, achievement.getId(), achievement.getBuxReward(), achievement.getName());
        }

        newlyUnlocked.add(new AchievementProgressDTO(progress));

        logger.info(
            "Achievement auto-unlocked: {} for ninja: {}",
            achievement.getName(),
            ninja.getFirstName() + " " + ninja.getLastName());
      }
    }

    return newlyUnlocked;
  }

  private int calculateProgressPercentage(Ninja ninja, Achievement achievement) {
    if (achievement.getUnlockCriteria() == null || achievement.getUnlockCriteria().isEmpty()) {
      return 0;
    }

    try {
      JsonNode criteria = objectMapper.readTree(achievement.getUnlockCriteria());
      String type = criteria.get("type").asText();
      int threshold = criteria.has("threshold") ? criteria.get("threshold").asInt() : 0;

      if (threshold == 0) {
        return 0;
      }

      int current =
          switch (type) {
            case "LESSONS_COMPLETED" ->
                BeltRewardCalculator.calculateTotalLessons(
                    ninja.getCurrentBeltType(), ninja.getCurrentLevel(), ninja.getCurrentLesson());
            case "LEVELS_COMPLETED" ->
                BeltRewardCalculator.calculateTotalLevels(
                    ninja.getCurrentBeltType(), ninja.getCurrentLevel());
            case "BELT_REACHED" -> {
              String requiredBelt = criteria.get("belt").asText();
              BeltType required = BeltType.valueOf(requiredBelt);
              yield ninja.getCurrentBeltType().ordinal() >= required.ordinal() ? 100 : 0;
            }
            case "TOTAL_BUX_EARNED" -> ledgerService.getTotalBuxEarned(ninja.getId());
            case "TOTAL_SPENT" -> ledgerService.getTotalBuxSpent(ninja.getId());
            case "LEGACY_POINTS" -> legacyLedgerTxnRepository.sumAmountByNinja(ninja);
            default -> 0;
          };

      int progress = Math.min(100, (int) ((double) current / threshold * 100));
      return Math.max(0, progress);
    } catch (Exception e) {
      logger.error(
          "Error calculating progress percentage for achievement: " + achievement.getName(), e);
      return 0;
    }
  }

  private boolean checkUnlockCriteria(Ninja ninja, Achievement achievement) {
    if (achievement.getUnlockCriteria() == null || achievement.getUnlockCriteria().isEmpty()) {
      return false;
    }

    try {
      JsonNode criteria = objectMapper.readTree(achievement.getUnlockCriteria());
      String type = criteria.get("type").asText();

      return switch (type) {
        case "LESSONS_COMPLETED" -> checkLessonsCompleted(ninja, criteria);
        case "LEVELS_COMPLETED" -> checkLevelsCompleted(ninja, criteria);
        case "BELT_REACHED" -> checkBeltReached(ninja, criteria);
        case "TOTAL_BUX_EARNED" -> checkTotalBuxEarned(ninja, criteria);
        case "PURCHASES_MADE" -> checkPurchasesMade(ninja, criteria);
        case "TOTAL_SPENT" -> checkTotalSpent(ninja, criteria);
        case "LEGACY_POINTS" -> checkLegacyPoints(ninja, criteria);
        default -> {
          logger.warn("Unknown achievement criteria type: {}", type);
          yield false;
        }
      };
    } catch (Exception e) {
      logger.error("Error checking unlock criteria for achievement: " + achievement.getName(), e);
      return false;
    }
  }

  private boolean checkLessonsCompleted(Ninja ninja, JsonNode criteria) {
    int threshold = criteria.get("threshold").asInt();
    int completedLessons =
        BeltRewardCalculator.calculateTotalLessons(
            ninja.getCurrentBeltType(), ninja.getCurrentLevel(), ninja.getCurrentLesson());
    return completedLessons >= threshold;
  }

  private boolean checkLevelsCompleted(Ninja ninja, JsonNode criteria) {
    int threshold = criteria.get("threshold").asInt();
    int completedLevels =
        BeltRewardCalculator.calculateTotalLevels(
            ninja.getCurrentBeltType(), ninja.getCurrentLevel());
    return completedLevels >= threshold;
  }

  private boolean checkBeltReached(Ninja ninja, JsonNode criteria) {
    String requiredBelt = criteria.get("belt").asText();
    return ninja.getCurrentBeltType().name().equals(requiredBelt)
        || ninja.getCurrentBeltType().ordinal() > BeltType.valueOf(requiredBelt).ordinal();
  }

  private boolean checkTotalBuxEarned(Ninja ninja, JsonNode criteria) {
    int threshold = criteria.get("threshold").asInt();
    return ledgerService.getTotalBuxEarned(ninja.getId()) >= threshold;
  }

  private boolean checkPurchasesMade(Ninja ninja, JsonNode criteria) {
    logger.warn("Purchase count achievement criteria not yet fully implemented");
    return false;
  }

  private boolean checkTotalSpent(Ninja ninja, JsonNode criteria) {
    int threshold = criteria.get("threshold").asInt();
    return ledgerService.getTotalBuxSpent(ninja.getId()) >= threshold;
  }

  private boolean checkLegacyPoints(Ninja ninja, JsonNode criteria) {
    int threshold = criteria.get("threshold").asInt();
    int rawLegacySum = legacyLedgerTxnRepository.sumAmountByNinja(ninja);
    return rawLegacySum >= threshold;
  }

  private void updateAchievementFromDTO(Achievement achievement, AchievementDTO dto) {
    achievement.setName(dto.getName());
    achievement.setDescription(dto.getDescription());
    achievement.setCategory(dto.getCategory());
    achievement.setRarity(dto.getRarity());
    achievement.setIcon(dto.getIcon());
    achievement.setBuxReward(dto.getBuxReward());
    achievement.setManualOnly(dto.isManualOnly());
    achievement.setUnlockCriteria(dto.getUnlockCriteria());
    achievement.setActive(dto.isActive());
    achievement.setHidden(dto.isHidden());
  }
}
