package com.example.NinjaBux.service;

import com.example.NinjaBux.domain.enums.BeltType;
import org.springframework.stereotype.Service;

@Service
public class BeltRewardCalculator {


    public int calculateBalance(BeltType beltType, int currentLevel, int currentLesson) {
        int totalBux = STARTING_BALANCE;
        totalBux += getCompletedBeltsReward(beltType);
        totalBux += getCurrentBeltProgress(beltType, currentLevel, currentLesson);
        return totalBux;
    }


    private int getCompletedBeltsReward(BeltType currentBelt) {
        int total = 0;
        if (currentBelt == BeltType.WHITE) {
            return 0;
        }
        total += getTotalBeltReward(BeltType.WHITE);
        total += (int) BELT_UP_REWARD;

        if (currentBelt == BeltType.YELLOW) {
            return total;
        }
        total += getTotalBeltReward(BeltType.YELLOW);
        total += (int) BELT_UP_REWARD;

        if (currentBelt == BeltType.ORANGE) {
            return total;
        }
        total += getTotalBeltReward(BeltType.ORANGE);
        total += (int) BELT_UP_REWARD;

        if (currentBelt == BeltType.GREEN) {
            return total;
        }
        total += getTotalBeltReward(BeltType.GREEN);
        total += (int) BELT_UP_REWARD;

        if (currentBelt == BeltType.BLUE) {
            return total;
        }

        return total;
    }

    private int getTotalBeltReward(BeltType belt) {
        if (belt == BeltType.WHITE) {
            int totalLessons = 0;
            for (int lessons : WHITE_BELT_LESSONS_PER_LEVEL) {
                totalLessons += lessons;
            }
            int lessonReward = (int) (totalLessons * WHITE_LESSON_REWARD);
            int levelReward = (int) (WHITE_BELT_LEVELS * WHITE_LEVEL_REWARD);
            return lessonReward + levelReward;
        } else if (belt == BeltType.YELLOW) {
            int totalLessons = 0;
            for (int lessons : YELLOW_BELT_LESSONS_PER_LEVEL) {
                totalLessons += lessons;
            }
            int lessonReward = (int) (totalLessons * YELLOW_LESSON_REWARD);
            int levelReward = (int) (YELLOW_BELT_LEVELS * YELLOW_LEVEL_REWARD);
            return lessonReward + levelReward;
        } else if (belt == BeltType.ORANGE) {
            int totalLessons = 0;
            for (int lessons : ORANGE_BELT_LESSONS_PER_LEVEL) {
                totalLessons += lessons;
            }
            int lessonReward = (int) (totalLessons * ORANGE_LESSON_REWARD);
            int levelReward = (int) (ORANGE_BELT_LEVELS * ORANGE_LEVEL_REWARD);
            return lessonReward + levelReward;
        } else if (belt == BeltType.GREEN) {
            int totalLessons = 0;
            for (int lessons : GREEN_BELT_LESSONS_PER_LEVEL) {
                totalLessons += lessons;
            }
            int lessonReward = (int) (totalLessons * GREEN_LESSON_REWARD);
            int levelReward = (int) (GREEN_BELT_LEVELS * GREEN_LEVEL_REWARD);
            return lessonReward + levelReward;
        } else if (belt == BeltType.BLUE) {
            int totalLessons = 0;
            for (int lessons : BLUE_BELT_LESSONS_PER_LEVEL) {
                totalLessons += lessons;
            }
            int lessonReward = (int) (totalLessons * BLUE_LESSON_REWARD);
            int levelReward = (int) (BLUE_BELT_LEVELS * BLUE_LEVEL_REWARD);
            return lessonReward + levelReward;
        }

        return 0;
    }

    private int getCurrentBeltProgress(BeltType beltType, int currentLevel, int currentLesson) {
        int total = 0;

        if (beltType == BeltType.WHITE) {
            for (int i = 1; i < currentLevel; i++) {
                total += (int) (WHITE_BELT_LESSONS_PER_LEVEL[i - 1] * WHITE_LESSON_REWARD);
                total += (int) WHITE_LEVEL_REWARD;
            }
            if (currentLesson > 1) {
                total += (int) ((currentLesson - 1) * WHITE_LESSON_REWARD);
            }

        } else if (beltType == BeltType.YELLOW) {
            for (int i = 1; i < currentLevel; i++) {
                total += (int) (YELLOW_BELT_LESSONS_PER_LEVEL[i - 1] * YELLOW_LESSON_REWARD);
                total += (int) YELLOW_LEVEL_REWARD;
            }
            if (currentLesson > 1) {
                total += (int) ((currentLesson - 1) * YELLOW_LESSON_REWARD);
            }
        } else if (beltType == BeltType.ORANGE) {
            for (int i = 1; i < currentLevel; i++) {
                total += (int) (ORANGE_BELT_LESSONS_PER_LEVEL[i - 1] * ORANGE_LESSON_REWARD);
                total += (int) ORANGE_LEVEL_REWARD;
            }
            if (currentLesson > 1) {
                total += (int) ((currentLesson - 1) * ORANGE_LESSON_REWARD);
            }
        } else if (beltType == BeltType.GREEN) {
            for (int i = 1; i < currentLevel; i++) {
                total += (int) (GREEN_BELT_LESSONS_PER_LEVEL[i - 1] * GREEN_LESSON_REWARD);
                total += (int) GREEN_LEVEL_REWARD;
            }
            if (currentLesson > 1) {
                total += (int) ((currentLesson - 1) * GREEN_LESSON_REWARD);
            }
        } else if (beltType == BeltType.BLUE) {
            for (int i = 1; i < currentLevel; i++) {
                total += (int) (BLUE_BELT_LESSONS_PER_LEVEL[i - 1] * BLUE_LESSON_REWARD);
                total += (int) BLUE_LEVEL_REWARD;
            }
            if (currentLesson > 1) {
                total += (int) ((currentLesson - 1) * BLUE_LESSON_REWARD);
            }
        }

        return total;
    }

    public boolean isValidProgress(BeltType beltType, int level, int lesson) {
        // level 0 and lesson 0 break everything so reject them
        if (level < 1 || lesson < 1) {
            return false;
        }

        if (beltType == BeltType.WHITE) {
            if (level > WHITE_BELT_LEVELS) return false;
            return lesson <= WHITE_BELT_LESSONS_PER_LEVEL[level - 1];
        } else if (beltType == BeltType.YELLOW) {
            if (level > YELLOW_BELT_LEVELS) return false;
            return lesson <= YELLOW_BELT_LESSONS_PER_LEVEL[level - 1];
        } else if (beltType == BeltType.ORANGE) {
            if (level > ORANGE_BELT_LEVELS) return false;
            return lesson <= ORANGE_BELT_LESSONS_PER_LEVEL[level - 1];
        } else if (beltType == BeltType.GREEN) {
            if (level > GREEN_BELT_LEVELS) return false;
            return lesson <= GREEN_BELT_LESSONS_PER_LEVEL[level - 1];
        } else if (beltType == BeltType.BLUE) {
            if (level > BLUE_BELT_LEVELS) return false;
            return lesson <= BLUE_BELT_LESSONS_PER_LEVEL[level - 1];
        }

        return false;
    }

    public String getValidationErrorMessage(BeltType beltType, int level, int lesson) {
        // level 0 lesson 0 bug still haunts me even though i thought i fixed it
        if (level < 1 || lesson < 1) {
            return "Level and lesson must start at 1, not 0";
        }

        if (beltType == BeltType.WHITE) {
            if (level > WHITE_BELT_LEVELS) {
                return "WHITE belt only has " + WHITE_BELT_LEVELS + " levels. You entered level " + level;
            }
            int maxLessons = WHITE_BELT_LESSONS_PER_LEVEL[level - 1];
            if (lesson > maxLessons) {
                return "WHITE belt level " + level + " only has " + maxLessons + " lessons. You entered lesson " + lesson;
            }
        } else if (beltType == BeltType.YELLOW) {
            if (level > YELLOW_BELT_LEVELS) {
                return "YELLOW belt only has " + YELLOW_BELT_LEVELS + " levels. You entered level " + level;
            }
            int maxLessons = YELLOW_BELT_LESSONS_PER_LEVEL[level - 1];
            if (lesson > maxLessons) {
                return "YELLOW belt level " + level + " only has " + maxLessons + " lessons. You entered lesson " + lesson;
            }
        } else if (beltType == BeltType.ORANGE) {
            if (level > ORANGE_BELT_LEVELS) {
                return "ORANGE belt only has " + ORANGE_BELT_LEVELS + " levels. You entered level " + level;
            }
            int maxLessons = ORANGE_BELT_LESSONS_PER_LEVEL[level - 1];
            if (lesson > maxLessons) {
                return "ORANGE belt level " + level + " only has " + maxLessons + " lessons. You entered lesson " + lesson;
            }
        } else if (beltType == BeltType.GREEN) {
            if (level > GREEN_BELT_LEVELS) {
                return "GREEN belt only has " + GREEN_BELT_LEVELS + " levels. You entered level " + level;
            }
            int maxLessons = GREEN_BELT_LESSONS_PER_LEVEL[level - 1];
            if (lesson > maxLessons) {
                return "GREEN belt level " + level + " only has " + maxLessons + " lessons. You entered lesson " + lesson;
            }
        } else if (beltType == BeltType.BLUE) {
            if (level > BLUE_BELT_LEVELS) {
                return "BLUE belt only has " + BLUE_BELT_LEVELS + " levels. You entered level " + level;
            }
            int maxLessons = BLUE_BELT_LESSONS_PER_LEVEL[level - 1];
            if (lesson > maxLessons) {
                return "BLUE belt level " + level + " only has " + maxLessons + " lessons. You entered lesson " + lesson;
            }
        }

        return "Valid progress";
    }

    public static int calculateTotalLessons(BeltType beltType, int currentLevel, int currentLesson) {
        int total = 0;

        if (beltType.ordinal() > BeltType.WHITE.ordinal()) {
            total += getTotalLessonsForBelt(BeltType.WHITE);
        }
        if (beltType.ordinal() > BeltType.YELLOW.ordinal()) {
            total += getTotalLessonsForBelt(BeltType.YELLOW);
        }
        if (beltType.ordinal() > BeltType.ORANGE.ordinal()) {
            total += getTotalLessonsForBelt(BeltType.ORANGE);
        }
        if (beltType.ordinal() > BeltType.GREEN.ordinal()) {
            total += getTotalLessonsForBelt(BeltType.GREEN);
        }
        if (beltType.ordinal() > BeltType.BLUE.ordinal()) {
            total += getTotalLessonsForBelt(BeltType.BLUE);
        }

        int[] lessonsPerLevel = getLessonsPerLevel(beltType);
        for (int i = 1; i < currentLevel; i++) {
            total += lessonsPerLevel[i - 1];
        }
        total += (currentLesson - 1);

        return total;
    }

    public static int calculateTotalLevels(BeltType beltType, int currentLevel) {
        int total = 0;

        if (beltType.ordinal() > BeltType.WHITE.ordinal()) {
            total += WHITE_BELT_LEVELS;
        }
        if (beltType.ordinal() > BeltType.YELLOW.ordinal()) {
            total += YELLOW_BELT_LEVELS;
        }
        if (beltType.ordinal() > BeltType.ORANGE.ordinal()) {
            total += ORANGE_BELT_LEVELS;
        }
        if (beltType.ordinal() > BeltType.GREEN.ordinal()) {
            total += GREEN_BELT_LEVELS;
        }
        if (beltType.ordinal() > BeltType.BLUE.ordinal()) {
            total += BLUE_BELT_LEVELS;
        }

        total += (currentLevel - 1); // -1 because we're on currentLevel, not completed it yet

        return total;
    }

    private static int getTotalLessonsForBelt(BeltType belt) {
        int[] lessonsPerLevel = getLessonsPerLevel(belt);
        int total = 0;
        for (int lessons : lessonsPerLevel) {
            total += lessons;
        }
        return total;
    }

    public static int[] getLessonsPerLevel(BeltType belt) {
        return switch (belt) {
            case WHITE -> WHITE_BELT_LESSONS_PER_LEVEL;
            case YELLOW -> YELLOW_BELT_LESSONS_PER_LEVEL;
            case ORANGE -> ORANGE_BELT_LESSONS_PER_LEVEL;
            case GREEN -> GREEN_BELT_LESSONS_PER_LEVEL;
            case BLUE -> BLUE_BELT_LESSONS_PER_LEVEL;
            default -> new int[0];
        };
    }
}
