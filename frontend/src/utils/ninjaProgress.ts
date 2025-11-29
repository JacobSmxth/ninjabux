import type { BeltType, BeltPath } from '../types';

const BELT_ORDER: BeltType[] = ['WHITE', 'YELLOW', 'ORANGE', 'GREEN', 'BLUE', 'PURPLE', 'RED', 'BROWN', 'BLACK'];

const LESSONS_PER_LEVEL: Record<BeltPath, Record<BeltType, number[]>> = {
  UNITY: {
    WHITE: [8, 8, 8, 8, 8, 8, 8, 8],
    YELLOW: [8, 8, 11, 8, 8, 11, 11, 8, 11, 7],
    ORANGE: [11, 11, 11, 11, 8, 8, 11, 11, 11, 11, 11, 10],
    GREEN: [8, 8, 11, 11, 17, 11, 11, 8, 8, 10],
    BLUE: [10, 13, 1],
    PURPLE: Array(11).fill(1),
    RED: Array(4).fill(1),
    BROWN: Array(17).fill(1),
    BLACK: [1],
  },
  GODOT: {
    WHITE: [8, 8, 8, 8, 8, 8, 8, 8],
    YELLOW: [8, 8, 11, 8, 8, 11, 11, 8, 11, 7],
    ORANGE: [11, 11, 11, 11, 8, 8, 11, 11, 11, 11, 11, 10],
    GREEN: [8, 8, 11, 11, 17, 11, 11, 8, 8, 10],
    BLUE: [10, 13, 1],
    PURPLE: Array(12).fill(1),
    RED: Array(4).fill(1), // placeholder until released
    BROWN: Array(16).fill(1),
    BLACK: [1],
  },
  UNREAL: {
    WHITE: [8, 8, 8, 8, 8, 8, 8, 8],
    YELLOW: [8, 8, 11, 8, 8, 11, 11, 8, 11, 7],
    ORANGE: [11, 11, 11, 11, 8, 8, 11, 11, 11, 11, 11, 10],
    GREEN: [8, 8, 11, 11, 17, 11, 11, 8, 8, 10],
    BLUE: [10, 13, 1],
    PURPLE: Array(11).fill(1),
    RED: Array(4).fill(1),
    BROWN: Array(17).fill(1),
    BLACK: [1],
  },
};

const FALLBACK_MAX = 8;

export const INITIAL_NINJA_PROGRESS: { beltType: BeltType; level: number; lesson: number; beltPath: BeltPath } = {
  beltType: 'WHITE',
  level: 1,
  lesson: 1,
  beltPath: 'UNITY',
};

const getLessonsMatrix = (beltPath?: BeltPath) => LESSONS_PER_LEVEL[beltPath || 'UNITY'] || LESSONS_PER_LEVEL.UNITY;

export const getMaxLevelsForBelt = (beltType: BeltType, beltPath?: BeltPath): number => {
  const lessons = getLessonsMatrix(beltPath)[beltType];
  return lessons ? lessons.length : FALLBACK_MAX;
};

export const getMaxLessonsForLevel = (beltType: BeltType, level: number, beltPath?: BeltPath): number => {
  const lessons = getLessonsMatrix(beltPath)[beltType] || Array(FALLBACK_MAX).fill(FALLBACK_MAX);
  if (level <= 0 || level > lessons.length) return FALLBACK_MAX;
  return lessons[level - 1];
};

export const clampLevelToBelt = (beltType: BeltType, value: number, beltPath?: BeltPath): number => {
  if (!Number.isFinite(value) || value < 1) return 1;
  const maxLevel = getMaxLevelsForBelt(beltType, beltPath);
  if (value > maxLevel) return maxLevel;
  return Math.floor(value);
};

export const clampLessonForLevel = (beltType: BeltType, level: number, value: number, beltPath?: BeltPath): number => {
  if (!Number.isFinite(value) || value < 1) return 1;
  const safeLevel = clampLevelToBelt(beltType, level, beltPath);
  const maxLesson = getMaxLessonsForLevel(beltType, safeLevel, beltPath);
  if (value > maxLesson) return maxLesson;
  return Math.floor(value);
};

export const normalizeProgress = (beltType: BeltType, level: number, lesson: number, beltPath?: BeltPath) => {
  const safeLevel = clampLevelToBelt(beltType, level, beltPath);
  const safeLesson = clampLessonForLevel(beltType, safeLevel, lesson, beltPath);
  return { beltType, level: safeLevel, lesson: safeLesson, beltPath };
};

export const getNextProgression = (beltType: BeltType, level: number, lesson: number, beltPath?: BeltPath) => {
  const maxLesson = getMaxLessonsForLevel(beltType, level, beltPath);
  if (lesson < maxLesson) {
    return { beltType, level, lesson: lesson + 1 };
  }

  const maxLevel = getMaxLevelsForBelt(beltType, beltPath);
  if (level < maxLevel) {
    return { beltType, level: level + 1, lesson: 1 };
  }

  const currentBeltIndex = BELT_ORDER.indexOf(beltType);
  const hasNextBelt = currentBeltIndex >= 0 && currentBeltIndex < BELT_ORDER.length - 1;
  if (hasNextBelt) {
    const nextBelt = BELT_ORDER[currentBeltIndex + 1];
    return { beltType: nextBelt, level: 1, lesson: 1 };
  }

  return { beltType, level, lesson };
};

export const beltOrder = BELT_ORDER;
