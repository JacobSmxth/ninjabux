import { useEffect, useMemo, useState } from 'react';
import type { BeltType, BeltPath } from '../types';
import {
  clampLessonForLevel,
  clampLevelToBelt,
  getMaxLessonsForLevel,
  getMaxLevelsForBelt,
  INITIAL_NINJA_PROGRESS,
} from '../utils/ninjaProgress';

export interface NinjaFormValues {
  firstName: string;
  lastName: string;
  username: string;
  beltType: BeltType;
  level: number;
  lesson: number;
  beltPath: BeltPath;
}

interface Props {
  isOpen: boolean;
  mode: 'create' | 'edit';
  initialValues?: NinjaFormValues;
  isSubmitting?: boolean;
  onClose: () => void;
  onSubmit: (values: NinjaFormValues) => Promise<void> | void;
}

const EMPTY_VALUES: NinjaFormValues = {
  firstName: '',
  lastName: '',
  username: '',
  beltType: INITIAL_NINJA_PROGRESS.beltType,
  level: INITIAL_NINJA_PROGRESS.level,
  lesson: INITIAL_NINJA_PROGRESS.lesson,
  beltPath: INITIAL_NINJA_PROGRESS.beltPath,
};

export default function NinjaFormModal({
  isOpen,
  mode,
  initialValues,
  isSubmitting,
  onClose,
  onSubmit,
}: Props) {
  const [formValues, setFormValues] = useState<NinjaFormValues>(initialValues || EMPTY_VALUES);

  useEffect(() => {
    if (isOpen) {
      setFormValues(initialValues || EMPTY_VALUES);
    }
  }, [initialValues, isOpen]);

  const levelMax = useMemo(
    () => getMaxLevelsForBelt(formValues.beltType, formValues.beltPath),
    [formValues.beltType, formValues.beltPath]
  );

  const lessonMax = useMemo(
    () => getMaxLessonsForLevel(formValues.beltType, formValues.level, formValues.beltPath),
    [formValues.beltType, formValues.level, formValues.beltPath]
  );

  if (!isOpen) return null;

  const handleBeltChange = (belt: BeltType) => {
    setFormValues((prev) => {
      const safeLevel = clampLevelToBelt(belt, prev.level, prev.beltPath);
      const safeLesson = clampLessonForLevel(belt, safeLevel, prev.lesson, prev.beltPath);
      return { ...prev, beltType: belt, level: safeLevel, lesson: safeLesson };
    });
  };

  const handleLevelChange = (value: number) => {
    setFormValues((prev) => {
      const safeLevel = clampLevelToBelt(prev.beltType, value, prev.beltPath);
      const safeLesson = clampLessonForLevel(prev.beltType, safeLevel, prev.lesson, prev.beltPath);
      return { ...prev, level: safeLevel, lesson: safeLesson };
    });
  };

  const handleLessonChange = (value: number) => {
    setFormValues((prev) => ({
      ...prev,
      lesson: clampLessonForLevel(prev.beltType, prev.level, value, prev.beltPath),
    }));
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canSubmit) return;
    await onSubmit(formValues);
  };

  const canSubmit =
    formValues.firstName.trim().length > 0 &&
    formValues.lastName.trim().length > 0 &&
    formValues.username.trim().length > 0;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="form-section modal" onClick={(e) => e.stopPropagation()}>
        <div className="form-section-header">
          <h3>{mode === 'create' ? 'Create New Ninja' : 'Edit Ninja'}</h3>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-field">
              <label>First Name</label>
              <input
                type="text"
                value={formValues.firstName}
                onChange={(e) => setFormValues({ ...formValues, firstName: e.target.value })}
                required
                autoFocus
              />
            </div>
            <div className="form-field">
              <label>Last Name</label>
              <input
                type="text"
                value={formValues.lastName}
                onChange={(e) => setFormValues({ ...formValues, lastName: e.target.value })}
                required
              />
            </div>
            <div className="form-field">
              <label>Username</label>
              <input
                type="text"
                value={formValues.username}
                onChange={(e) => setFormValues({ ...formValues, username: e.target.value })}
                required
              />
            </div>
            <div className="form-field">
              <label>Belt</label>
              <select
                value={formValues.beltType}
                onChange={(e) => handleBeltChange(e.target.value as BeltType)}
              >
                <option value="WHITE">White</option>
                <option value="YELLOW">Yellow</option>
                <option value="ORANGE">Orange</option>
                <option value="GREEN">Green</option>
                <option value="BLUE">Blue</option>
                <option value="PURPLE">Purple</option>
                <option value="RED">Red</option>
                <option value="BROWN">Brown</option>
                <option value="BLACK">Black</option>
              </select>
            </div>
            <div className="form-field">
              <label>Path</label>
              <select
                value={formValues.beltPath}
                onChange={(e) =>
                  setFormValues((prev) => {
                    const nextPath = e.target.value as BeltPath;
                    const safeLevel = clampLevelToBelt(prev.beltType, prev.level, nextPath);
                    const safeLesson = clampLessonForLevel(prev.beltType, safeLevel, prev.lesson, nextPath);
                    return { ...prev, beltPath: nextPath, level: safeLevel, lesson: safeLesson };
                  })
                }
              >
                <option value="UNITY">Unity</option>
                <option value="GODOT">Godot</option>
                <option value="UNREAL">Unreal (beta)</option>
              </select>
            </div>
            <div className="form-field">
              <label>
                Level <small style={{ color: '#6b7280' }}>(1 - {levelMax})</small>
              </label>
              <input
                type="number"
                value={formValues.level}
                min={1}
                max={levelMax}
                onChange={(e) => handleLevelChange(parseInt(e.target.value || '1', 10))}
                required
              />
            </div>
            <div className="form-field">
              <label>
                Lesson <small style={{ color: '#6b7280' }}>(1 - {lessonMax})</small>
              </label>
              <input
                type="number"
                value={formValues.lesson}
                min={1}
                max={lessonMax}
                onChange={(e) => handleLessonChange(parseInt(e.target.value || '1', 10))}
                required
              />
            </div>
          </div>
          <p style={{ fontSize: '0.9rem', color: '#6b7280', marginTop: '0.5rem' }}>
            Ninjas always start at Level 1, Lesson 1. Adjustments here will respect each belt&apos;s
            max levels and lessons.
          </p>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={!canSubmit || isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save'}
            </button>
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={isSubmitting}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
