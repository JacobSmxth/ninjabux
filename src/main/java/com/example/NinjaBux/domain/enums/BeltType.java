package com.example.NinjaBux.domain.enums;

import com.example.NinjaBux.domain.belt.BeltSpec;
import com.example.NinjaBux.domain.belt.BeltSpecs;

public enum BeltType {
    WHITE,
    YELLOW,
    ORANGE,
    GREEN,
    BLUE,
    PURPLE,
    RED,
    BROWN,
    BLACK;

    public BeltSpec spec() {return BeltSpecs.get(this);}

    public int levels() {return spec().levels();}

    public int lessonsInLevel(int level) { return spec().lessonsInLevel(level);}

    public int totalLessonsUntil(int level, int lesson) { return spec().totalLessonsUntil(level, lesson);}

    public int perLessonBux() { return spec().perLessonBux();}
    public int levelUpBonus() { return spec().levelUpBonus();}
    public int beltUpBonus() { return spec().beltUpBonus();}

}
