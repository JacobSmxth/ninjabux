package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.Ninja;
import com.example.NinjaBux.domain.ProgressHistory;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.dto.AchievementProgressDTO;
import com.example.NinjaBux.dto.LeaderboardEntry;
import com.example.NinjaBux.dto.LeaderboardResponse;
import com.example.NinjaBux.repository.NinjaRepository;
import com.example.NinjaBux.repository.ProgressHistoryRepository;
import com.example.NinjaBux.repository.QuestionAnswerRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NinjaLeaderboardService {

  @Autowired private NinjaRepository ninjaRepository;

  @Autowired private ProgressHistoryRepository progressHistoryRepository;

  @Autowired private QuestionAnswerRepository questionAnswerRepository;

  @Autowired private LedgerService ledgerService;

  @Autowired(required = false)
  private AchievementService achievementService;

  public LeaderboardResponse getLeaderboard(int topN, String period, Boolean excludeLocked) {
    LocalDateTime startDate = getStartDateForPeriod(period);
    if ("lifetime".equalsIgnoreCase(period) || startDate == null) {
      return getLifetimeLeaderboard(topN, excludeLocked);
    }
    return getTimePeriodLeaderboard(topN, startDate, excludeLocked);
  }

  public LeaderboardResponse getLeaderboard(int topN, String period) {
    return getLeaderboard(topN, period, false);
  }

  private LeaderboardResponse getLifetimeLeaderboard(int topN, Boolean excludeLocked) {
    List<NinjaLedgerView> ledgerViews =
        ninjaRepository.findAll().stream()
            .filter(ninja -> !excludeLocked || !ninja.isLocked())
            .map(this::buildLedgerView)
            .collect(Collectors.toList());

    List<LeaderboardEntry> topEarners = buildTopEarners(ledgerViews, topN);
    List<LeaderboardEntry> topSpenders = buildTopSpenders(ledgerViews, topN);

    LeaderboardResponse response =
        new LeaderboardResponse(
            topEarners, topSpenders, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    if (topEarners.isEmpty()) {
      response.setMessage("No users qualify for this leaderboard");
    }
    return response;
  }

  private LeaderboardResponse getTimePeriodLeaderboard(
      int topN, LocalDateTime startDate, Boolean excludeLocked) {
    List<Object[]> earnerResults = progressHistoryRepository.findTopEarnersSince(startDate);

    List<LeaderboardEntry> topEarners =
        earnerResults.stream()
            .limit(topN)
            .map(
                result -> {
                  Long ninjaId = (Long) result[0];
                  Long totalEarned = (Long) result[1];
                  Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
                  if (ninja == null) {
                    return null;
                  }
                  if (excludeLocked && ninja.isLocked()) {
                    return null;
                  }
                  if (totalEarned.intValue() == 0) {
                    return null;
                  }

                  int rank = earnerResults.indexOf(result) + 1;
                  LeaderboardEntry entry =
                      new LeaderboardEntry(
                          ninja.getId(),
                          ninja.getFirstName(),
                          ninja.getLastName(),
                          ninja.getUsername(),
                          ninja.getCurrentBeltType(),
                          totalEarned.intValue(),
                          getRoundedSpent(ninja),
                          rank);
                  if (rank == 1) {
                    entry.setTopEarner(true);
                  }
                  populateAchievements(entry);
                  return entry;
                })
            .filter(entry -> entry != null)
            .collect(Collectors.toList());

    List<NinjaLedgerView> ledgerViews =
        ninjaRepository.findAll().stream()
            .filter(n -> !excludeLocked || !n.isLocked())
            .map(this::buildLedgerView)
            .collect(Collectors.toList());
    List<LeaderboardEntry> topSpenders = buildTopSpenders(ledgerViews, topN);

    List<LeaderboardEntry> mostImproved =
        getMostImprovedLeaderboard(topN, startDate, excludeLocked);

    List<LeaderboardEntry> quizChampions =
        getQuizChampionsLeaderboard(topN, startDate, excludeLocked);

    LeaderboardResponse response =
        new LeaderboardResponse(
            topEarners, topSpenders, mostImproved, quizChampions, new ArrayList<>());
    if (topEarners.isEmpty()
        && topSpenders.isEmpty()
        && mostImproved.isEmpty()
        && quizChampions.isEmpty()) {
      response.setMessage("No users qualify for this leaderboard");
    }
    return response;
  }

  private List<LeaderboardEntry> getMostImprovedLeaderboard(
      int topN, LocalDateTime startDate, Boolean excludeLocked) {
    List<ProgressHistory> allRecords =
        progressHistoryRepository.findAll().stream()
            .filter(ph -> ph.getEarningType() == ProgressHistory.EarningType.LEVEL_UP)
            .collect(Collectors.toList());

    List<Long> activeNinjaIds =
        allRecords.stream()
            .map(ph -> ph.getNinja().getId())
            .distinct()
            .collect(Collectors.toList());

    Map<Long, Integer> lessonsAdvanced = new HashMap<>();

    for (Long ninjaId : activeNinjaIds) {
      Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
      if (ninja == null) {
        continue;
      }
      if (excludeLocked && ninja.isLocked()) {
        continue;
      }

      List<ProgressHistory> ninjaAllRecords =
          allRecords.stream()
              .filter(ph -> ph.getNinja().getId().equals(ninjaId))
              .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
              .collect(Collectors.toList());

      if (ninjaAllRecords.isEmpty()) {
        continue;
      }

      List<ProgressHistory> beforeStart =
          ninjaAllRecords.stream()
              .filter(ph -> ph.getTimestamp().isBefore(startDate))
              .collect(Collectors.toList());

      int lessonsAtStart;
      if (!beforeStart.isEmpty()) {
        ProgressHistory startRecord = beforeStart.get(beforeStart.size() - 1);
        lessonsAtStart =
            BeltRewardCalculator.calculateTotalLessons(
                startRecord.getBeltType(), startRecord.getLevel(), startRecord.getLesson());
      } else {
        List<ProgressHistory> periodRecords =
            ninjaAllRecords.stream()
                .filter(
                    ph ->
                        ph.getTimestamp().isAfter(startDate)
                            || ph.getTimestamp().isEqual(startDate))
                .collect(Collectors.toList());

        if (!periodRecords.isEmpty()) {
          ProgressHistory firstRecord = periodRecords.get(0);
          BeltType firstBelt = firstRecord.getBeltType();
          int firstLevel = firstRecord.getLevel();
          int firstLesson = firstRecord.getLesson();

          int[] lessonsPerLevel = BeltRewardCalculator.getLessonsPerLevel(firstBelt);
          if (firstLevel > 1) {
            if (lessonsPerLevel.length >= firstLevel - 1) {
              int prevLevelLastLesson = lessonsPerLevel[firstLevel - 2];
              lessonsAtStart =
                  BeltRewardCalculator.calculateTotalLessons(
                      firstBelt, firstLevel - 1, prevLevelLastLesson);
            } else {
              lessonsAtStart =
                  BeltRewardCalculator.calculateTotalLessons(firstBelt, firstLevel, firstLesson);
            }
          } else if (firstLesson > 1) {
            lessonsAtStart =
                BeltRewardCalculator.calculateTotalLessons(firstBelt, firstLevel, firstLesson - 1);
          } else {
            lessonsAtStart =
                BeltRewardCalculator.calculateTotalLessons(firstBelt, firstLevel, firstLesson);
          }
        } else {
          continue;
        }
      }

      int lessonsAtEnd =
          BeltRewardCalculator.calculateTotalLessons(
              ninja.getCurrentBeltType(), ninja.getCurrentLevel(), ninja.getCurrentLesson());

      int advanced = lessonsAtEnd - lessonsAtStart;
      if (advanced > 0) {
        lessonsAdvanced.put(ninjaId, advanced);
      }
    }

    List<Map.Entry<Long, Integer>> sorted =
        lessonsAdvanced.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(topN)
            .collect(Collectors.toList());

    List<LeaderboardEntry> entries = new ArrayList<>();
    for (int i = 0; i < sorted.size(); i++) {
      Map.Entry<Long, Integer> entry = sorted.get(i);
      Long ninjaId = entry.getKey();
      Integer lessons = entry.getValue();
      Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
      if (ninja == null || lessons == 0) {
        continue;
      }

      LeaderboardEntry leaderboardEntry =
          new LeaderboardEntry(
              ninja.getId(),
              ninja.getFirstName(),
              ninja.getLastName(),
              ninja.getUsername(),
              ninja.getCurrentBeltType(),
              lessons,
              getRoundedSpent(ninja),
              i + 1);
      populateAchievements(leaderboardEntry);
      entries.add(leaderboardEntry);
    }
    return entries;
  }

  private List<LeaderboardEntry> getQuizChampionsLeaderboard(
      int topN, LocalDateTime startDate, Boolean excludeLocked) {
    List<Object[]> results = questionAnswerRepository.findQuizChampionsSince(startDate);

    List<LeaderboardEntry> entries = new ArrayList<>();
    for (int i = 0; i < Math.min(topN, results.size()); i++) {
      Object[] result = results.get(i);
      Long ninjaId = (Long) result[0];
      Long correctCount = (Long) result[1];
      Ninja ninja = ninjaRepository.findById(ninjaId).orElse(null);
      if (ninja == null) {
        continue;
      }
      if (excludeLocked && ninja.isLocked()) {
        continue;
      }
      if (correctCount.intValue() == 0) {
        continue;
      }

      LeaderboardEntry entry =
          new LeaderboardEntry(
              ninja.getId(),
              ninja.getFirstName(),
              ninja.getLastName(),
              ninja.getUsername(),
              ninja.getCurrentBeltType(),
              correctCount.intValue(),
              getRoundedSpent(ninja),
              i + 1);
      populateAchievements(entry);
      entries.add(entry);
    }
    return entries;
  }

  private void populateAchievements(LeaderboardEntry entry) {
    if (achievementService != null) {
      try {
        List<AchievementProgressDTO> topAchievements =
            achievementService.getTopAchievements(entry.getNinjaId(), 3);
        entry.setTopAchievements(topAchievements);

        AchievementProgressDTO leaderboardBadge =
            achievementService.getLeaderboardBadge(entry.getNinjaId());
        entry.setLeaderboardBadge(leaderboardBadge);
        return;
      } catch (Exception ignored) {
      }
    }
    entry.setTopAchievements(Collections.emptyList());
    entry.setLeaderboardBadge(null);
  }

  private List<LeaderboardEntry> buildTopEarners(List<NinjaLedgerView> views, int topN) {
    return buildLeaderboard(views, topN, NinjaLedgerView::getEarned, true);
  }

  private List<LeaderboardEntry> buildTopSpenders(List<NinjaLedgerView> views, int topN) {
    return buildLeaderboard(views, topN, NinjaLedgerView::getSpent, false);
  }

  private List<LeaderboardEntry> buildLeaderboard(
      List<NinjaLedgerView> views,
      int topN,
      java.util.function.ToIntFunction<NinjaLedgerView> metricExtractor,
      boolean isEarnerBoard) {
    List<NinjaLedgerView> sorted =
        views.stream()
            .filter(view -> metricExtractor.applyAsInt(view) > 0)
            .sorted(Comparator.comparingInt(metricExtractor).reversed())
            .limit(topN)
            .collect(Collectors.toList());

    List<LeaderboardEntry> entries = new ArrayList<>();
    for (int i = 0; i < sorted.size(); i++) {
      NinjaLedgerView view = sorted.get(i);
      LeaderboardEntry entry =
          createLeaderboardEntry(view.getNinja(), view.getEarned(), view.getSpent(), i + 1);
      populateAchievements(entry);
      if (i == 0) {
        if (isEarnerBoard) {
          entry.setTopEarner(true);
        } else {
          entry.setTopSpender(true);
        }
      }
      entries.add(entry);
    }
    return entries;
  }

  private LeaderboardEntry createLeaderboardEntry(Ninja ninja, int earned, int spent, int rank) {
    return new LeaderboardEntry(
        ninja.getId(),
        ninja.getFirstName(),
        ninja.getLastName(),
        ninja.getUsername(),
        ninja.getCurrentBeltType(),
        earned,
        spent,
        rank);
  }

  private NinjaLedgerView buildLedgerView(Ninja ninja) {
    int earned = ledgerService.getTotalBuxEarned(ninja.getId());
    int spent = ledgerService.getTotalBuxSpent(ninja.getId());
    return new NinjaLedgerView(ninja, earned, spent);
  }

  private int getRoundedSpent(Ninja ninja) {
    return ledgerService.getTotalBuxSpent(ninja.getId());
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

  private static class NinjaLedgerView {
    private final Ninja ninja;
    private final int earned;
    private final int spent;

    private NinjaLedgerView(Ninja ninja, int earned, int spent) {
      this.ninja = ninja;
      this.earned = earned;
      this.spent = spent;
    }

    private Ninja getNinja() {
      return ninja;
    }

    private int getEarned() {
      return earned;
    }

    private int getSpent() {
      return spent;
    }
  }
}
