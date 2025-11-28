package com.example.NinjaBux.service;

import com.example.NinjaBux.config.RewardConfig;
import com.example.NinjaBux.domain.belt.BeltSpec;
import com.example.NinjaBux.domain.enums.BeltType;
import org.springframework.stereotype.Service;

@Service
public class BeltRewardCalculator {

  public int calculateBalance(BeltType beltType, int currentLevel, int currentLesson) {
    int totalBux = RewardConfig.STARTING_BALANCE;
    totalBux += getCompletedBeltsReward(beltType);
    totalBux += getCurrentBeltProgress(beltType, currentLevel, currentLesson);
    return totalBux;
  }

  private int getCompletedBeltsReward(BeltType currentBelt) {
    int total = 0;
    for (BeltType belt : BeltType.values()) {
      if (belt.ordinal() >= currentBelt.ordinal()) {
        break;
      }
      total += getTotalBeltReward(belt);
      total += belt.beltUpBonus();
    }
    return total;
  }

  private int getTotalBeltReward(BeltType belt) {
    BeltSpec spec = belt.spec();
    int totalLessons = spec.totalLessons();
    int lessonReward = totalLessons * spec.perLessonBux();
    int levelReward = spec.levels() * spec.levelUpBonus();
    return lessonReward + levelReward;
  }

  private int getCurrentBeltProgress(BeltType beltType, int currentLevel, int currentLesson) {
    BeltSpec spec = beltType.spec();
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
    if (level < 1 || lesson < 1) {
      return false;
    }

    BeltSpec spec = beltType.spec();
    if (level > spec.levels()) {
      return false;
    }
    return lesson <= spec.lessonsInLevel(level);
  }

  public String getValidationErrorMessage(BeltType beltType, int level, int lesson) {
    if (level < 1 || lesson < 1) {
      return "Level and lesson must start at 1, not 0";
    }

    BeltSpec spec = beltType.spec();
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
    int total = 0;

    for (BeltType belt : BeltType.values()) {
      if (belt.ordinal() >= beltType.ordinal()) {
        break;
      }
      total += belt.spec().totalLessons();
    }

    BeltSpec spec = beltType.spec();
    for (int i = 1; i < currentLevel; i++) {
      total += spec.lessonsInLevel(i);
    }
    total += (currentLesson - 1);

    return total;
  }

  public static int calculateTotalLevels(BeltType beltType, int currentLevel) {
    int total = 0;

    for (BeltType belt : BeltType.values()) {
      if (belt.ordinal() >= beltType.ordinal()) {
        break;
      }
      total += belt.levels();
    }

    total += (currentLevel - 1);

    return total;
  }

  private static int getTotalLessonsForBelt(BeltType belt) {
    return belt.spec().totalLessons();
  }

  public static int[] getLessonsPerLevel(BeltType belt) {
    return belt.spec().lessonsPerLevel();
  }
}
