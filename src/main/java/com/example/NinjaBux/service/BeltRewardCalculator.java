package com.example.NinjaBux.service;

import com.example.NinjaBux.config.RewardConfig;
import com.example.NinjaBux.domain.belt.BeltSpec;
import com.example.NinjaBux.domain.enums.BeltType;
import com.example.NinjaBux.domain.enums.BeltPath;
import org.springframework.stereotype.Service;

@Service
public class BeltRewardCalculator {

  public int calculateBalance(BeltType beltType, int currentLevel, int currentLesson) {
    return calculateBalance(beltType, currentLevel, currentLesson, BeltPath.UNITY);
  }

  public int calculateBalance(BeltType beltType, int currentLevel, int currentLesson, BeltPath path) {
    int linearBalance = calculateLinearBalance(beltType, currentLevel, currentLesson, path);
    return applyDiminishingReturns(linearBalance, path);
  }

  public int calculateRawBalance(BeltType beltType, int currentLevel, int currentLesson) {
    return calculateRawBalance(beltType, currentLevel, currentLesson, BeltPath.UNITY);
  }

  public int calculateRawBalance(BeltType beltType, int currentLevel, int currentLesson, BeltPath path) {
    return calculateLinearBalance(beltType, currentLevel, currentLesson, path);
  }

  private int calculateLinearBalance(BeltType beltType, int currentLevel, int currentLesson, BeltPath path) {
    int totalBux = RewardConfig.STARTING_BALANCE;
    totalBux += getCompletedBeltsReward(beltType, path);
    totalBux += getCurrentBeltProgress(beltType, currentLevel, currentLesson, path);
    return totalBux;
  }

  private int getCompletedBeltsReward(BeltType currentBelt, BeltPath path) {
    int total = 0;
    for (BeltType belt : BeltType.values()) {
      if (belt.ordinal() >= currentBelt.ordinal()) {
        break;
      }
      total += getTotalBeltReward(belt, path);
      total += belt.beltUpBonus();
    }
    return total;
  }

  private int getTotalBeltReward(BeltType belt, BeltPath path) {
    BeltSpec spec = com.example.NinjaBux.domain.belt.BeltSpecs.get(belt, path);
    int totalLessons = spec.totalLessons();
    int lessonReward = totalLessons * spec.perLessonBux();
    int levelReward = spec.levels() * spec.levelUpBonus();
    return lessonReward + levelReward;
  }

  private int getCurrentBeltProgress(BeltType beltType, int currentLevel, int currentLesson, BeltPath path) {
    BeltSpec spec = com.example.NinjaBux.domain.belt.BeltSpecs.get(beltType, path);
    int total = 0;

    for (int i = 1; i < currentLevel; i++) {
      total += spec.lessonsInLevel(i) * spec.perLessonBux();
      total += spec.levelUpBonus();
    }
    if (currentLesson > 1) {
      total += (currentLesson - 1) * spec.perLessonBux();
    }

    return total;
  }

  public boolean isValidProgress(BeltType beltType, int level, int lesson) {
    return isValidProgress(beltType, level, lesson, BeltPath.UNITY);
  }

  public boolean isValidProgress(BeltType beltType, int level, int lesson, BeltPath path) {
    if (level < 1 || lesson < 1) {
      return false;
    }

    BeltSpec spec = com.example.NinjaBux.domain.belt.BeltSpecs.get(beltType, path);
    if (level > spec.levels()) {
      return false;
    }
    return lesson <= spec.lessonsInLevel(level);
  }

  public String getValidationErrorMessage(BeltType beltType, int level, int lesson) {
    return getValidationErrorMessage(beltType, level, lesson, BeltPath.UNITY);
  }

  public String getValidationErrorMessage(BeltType beltType, int level, int lesson, BeltPath path) {
    if (level < 1 || lesson < 1) {
      return "Level and lesson must start at 1, not 0";
    }

    BeltSpec spec = com.example.NinjaBux.domain.belt.BeltSpecs.get(beltType, path);
    if (level > spec.levels()) {
      return beltType + " belt only has " + spec.levels() + " levels. You entered level " + level;
    }
    int maxLessons = spec.lessonsInLevel(level);
    if (lesson > maxLessons) {
      return beltType
          + " belt level "
          + level
          + " only has "
          + maxLessons
          + " lessons. You entered lesson "
          + lesson;
    }

    return "Valid progress";
  }

  public static int calculateTotalLessons(BeltType beltType, int currentLevel, int currentLesson) {
    return calculateTotalLessons(beltType, currentLevel, currentLesson, BeltPath.UNITY);
  }

  public static int calculateTotalLessons(BeltType beltType, int currentLevel, int currentLesson, BeltPath path) {
    int total = 0;

    for (BeltType belt : BeltType.values()) {
      if (belt.ordinal() >= beltType.ordinal()) {
        break;
      }
      total += com.example.NinjaBux.domain.belt.BeltSpecs.get(belt, path).totalLessons();
    }

    BeltSpec spec = com.example.NinjaBux.domain.belt.BeltSpecs.get(beltType, path);
    for (int i = 1; i < currentLevel; i++) {
      total += spec.lessonsInLevel(i);
    }
    total += (currentLesson - 1);

    return total;
  }

  public static int calculateTotalLevels(BeltType beltType, int currentLevel) {
    return calculateTotalLevels(beltType, currentLevel, BeltPath.UNITY);
  }

  public static int calculateTotalLevels(BeltType beltType, int currentLevel, BeltPath path) {
    int total = 0;

    for (BeltType belt : BeltType.values()) {
      if (belt.ordinal() >= beltType.ordinal()) {
        break;
      }
      total += com.example.NinjaBux.domain.belt.BeltSpecs.get(belt, path).levels();
    }

    total += (currentLevel - 1);

    return total;
  }

  private static int getTotalLessonsForBelt(BeltType belt) {
    return belt.spec().totalLessons();
  }

  public static int[] getLessonsPerLevel(BeltType belt) {
    return getLessonsPerLevel(belt, com.example.NinjaBux.domain.enums.BeltPath.UNITY);
  }

  public static int[] getLessonsPerLevel(BeltType belt, com.example.NinjaBux.domain.enums.BeltPath path) {
    return com.example.NinjaBux.domain.belt.BeltSpecs.get(belt, path).lessonsPerLevel();
  }

  private int applyDiminishingReturns(int linearBalance, BeltPath path) {
    int anchorBalance =
        calculateLinearBalance(
            RewardConfig.DIMINISHING_RETURNS_START_BELT, 1, 1, path);
    int targetMaxBalance = RewardConfig.DIMINISHING_RETURNS_TARGET_BALANCE;

    if (linearBalance <= anchorBalance || targetMaxBalance <= anchorBalance) {
      return linearBalance;
    }

    int maxBalance = calculateLinearBalance(BeltType.BLACK, 1, 1, path);
    int extraRange = maxBalance - anchorBalance;
    if (extraRange <= 0) {
      return linearBalance;
    }

    int targetExtraRange = targetMaxBalance - anchorBalance;
    int extra = linearBalance - anchorBalance;

    double scaledExtra =
        targetExtraRange * (Math.log1p(extra) / Math.log1p(extraRange));
    return anchorBalance + (int) Math.round(scaledExtra);
  }
}
