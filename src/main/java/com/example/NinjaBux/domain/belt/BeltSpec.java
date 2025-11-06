package com.example.NinjaBux.domain.belt;

public record BeltSpec( int[] lessonsPerLevel, int perLessonBux, int levelUpBonus, int beltUpBonus) {
    public int levels() {return lessonsPerLevel.length;}

    public int lessonsInLevel(int level) {
        validate(level, 1);
        return lessonsPerLevel[level - 1];
    }

    public int totalLessonsUntil(int level, int lesson) {
        validate(level, lesson);
        int total = 0;
        for (int i = 0; i < level - 1; i++) {
            total += lessonsPerLevel[i];
        }
        return total + lesson;
    }


    public int totalLessons() {
        int sum = 0;
        for (int i : lessonsPerLevel) {
            sum += i;
        }
        return sum;
    }


    private void validate(int level, int lesson) {
        if (level < 1 || level > levels()) throw new IllegalArgumentException("Invalid level: " + level);

        int maxLesson = lessonsPerLevel[level - 1];
        if (lesson < 1 || lesson > maxLesson) throw new IllegalArgumentException("Invalid lesson: " + lesson + " (1..." + maxLesson + ")");
    }
}
