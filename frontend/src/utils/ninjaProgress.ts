import type { BeltType } from '../types';

const BELT_ORDER: BeltType[] = ['WHITE', 'YELLOW', 'ORANGE', 'GREEN', 'BLUE', 'PURPLE', 'RED', 'BROWN', 'BLACK'];

const LESSONS_PER_LEVEL: Record<BeltType, number[]> = {
  WHITE: [8, 8, 8, 8, 8, 8, 8, 8],
  YELLOW: [8, 8, 11, 8, 8, 11, 11, 8, 11, 7],
  ORANGE: [11, 11, 11, 11, 8, 8, 11, 11, 11, 11, 11, 10],
  GREEN: [8, 8, 11, 11, 17, 11, 11, 8, 8, 10],
  BLUE: [10, 13, 1],
  PURPLE: [8, 8, 8, 8, 8, 8, 8, 8],
  RED: [8, 8, 8, 8, 8, 8, 8, 8],
  BROWN: [8, 8, 8, 8, 8, 8, 8, 8],
  BLACK: [8, 8, 8, 8, 8, 8, 8, 8],
};

const LEVELS_PER_BELT: Record<BeltType, number> = {
  WHITE: 8,
  YELLOW: 10,
  ORANGE: 12,
  GREEN: 10,
  BLUE: 3,
  PURPLE: 8,
  RED: 8,
  BROWN: 8,
  BLACK: 8,
};

const FALLBACK_MAX = 8;

export const INITIAL_NINJA_PROGRESS: { beltType: BeltType; level: number; lesson: number } = {
  beltType: 'WHITE',
  level: 1,
  lesson: 1,
};

export const getMaxLevelsForBelt = (beltType: BeltType): number => LEVELS_PER_BELT[beltType] ?? FALLBACK_MAX;

export const getMaxLessonsForLevel = (beltType: BeltType, level: number): number => {
  const lessons = LESSONS_PER_LEVEL[beltType] || Array(FALLBACK_MAX).fill(FALLBACK_MAX);
  if (level <= 0 || level > lessons.length) return FALLBACK_MAX;
  return lessons[level - 1];
};

export const clampLevelToBelt = (beltType: BeltType, value: number): number => {
  if (!Number.isFinite(value) || value < 1) return 1;
  const maxLevel = getMaxLevelsForBelt(beltType);
  if (value > maxLevel) return maxLevel;
  return Math.floor(value);
};

export const clampLessonForLevel = (beltType: BeltType, level: number, value: number): number => {
  if (!Number.isFinite(value) || value < 1) return 1;
  const safeLevel = clampLevelToBelt(beltType, level);
  const maxLesson = getMaxLessonsForLevel(beltType, safeLevel);
  if (value > maxLesson) return maxLesson;
  return Math.floor(value);
};

export const normalizeProgress = (beltType: BeltType, level: number, lesson: number) => {
  const safeLevel = clampLevelToBelt(beltType, level);
  const safeLesson = clampLessonForLevel(beltType, safeLevel, lesson);
  return { beltType, level: safeLevel, lesson: safeLesson };
};

export const getNextProgression = (beltType: BeltType, level: number, lesson: number) => {
  const maxLesson = getMaxLessonsForLevel(beltType, level);
  if (lesson < maxLesson) {
    return { beltType, level, lesson: lesson + 1 };
  }

  const maxLevel = getMaxLevelsForBelt(beltType);
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
