import { useState, useEffect, useCallback } from 'react';
import { FiHelpCircle } from 'react-icons/fi';
import { ninjaApi, bigQuestionApi } from '../services/api';
import type { Ninja, BigQuestionResponse } from '../types';
import { useToastContext } from '../context/ToastContext';
import { getApiErrorMessage } from '../utils/errorHandling';
import './Quiz.css';

function WeekCountdown({ startDate, endDate }: { startDate: string; endDate: string }) {
  const [timeLeft, setTimeLeft] = useState<string>('');

  useEffect(() => {
    const updateCountdown = () => {
      const now = new Date();
      const end = new Date(endDate);
      const diff = end.getTime() - now.getTime();

      if (diff <= 0) {
        setTimeLeft('Week ended');
        return;
      }

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

      if (days > 0) {
        setTimeLeft(`${days}d ${hours}h ${minutes}m left`);
      } else if (hours > 0) {
        setTimeLeft(`${hours}h ${minutes}m left`);
      } else {
        setTimeLeft(`${minutes}m left`);
      }
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 60000);
    return () => clearInterval(interval);
  }, [endDate]);

  return (
    <div className="week-countdown">
      <span className="week-range">
        {new Date(startDate).toLocaleDateString()} - {new Date(endDate).toLocaleDateString()}
      </span>
      <span className="countdown">{timeLeft}</span>
    </div>
  );
}

function QuizStats({ ninja }: { ninja: Ninja }) {
  const accuracy =
    ninja.totalQuestionsAnswered > 0
      ? Math.round((ninja.totalQuestionsCorrect / ninja.totalQuestionsAnswered) * 100)
      : null;

  return (
    <div className="quiz-stats">
      <h3>Your Quiz Stats</h3>
      <div className="quiz-stats__grid">
        <div className="quiz-stats__cell">
          <div className="quiz-stats__value">{ninja.totalQuestionsAnswered}</div>
          <div className="quiz-stats__label">Total Answered</div>
        </div>
        <div className="quiz-stats__cell">
          <div className="quiz-stats__value">{ninja.totalQuestionsCorrect}</div>
          <div className="quiz-stats__label">Correct</div>
        </div>
        {accuracy !== null && (
          <div className="quiz-stats__cell">
            <div className="quiz-stats__value">{accuracy}%</div>
            <div className="quiz-stats__label">Accuracy</div>
          </div>
        )}
      </div>
    </div>
  );
}

interface ChoiceOptionProps {
  index: number;
  label: string;
  isSelected: boolean;
  onSelect: (value: string) => void;
}

function ChoiceOption({ index, label, isSelected, onSelect }: ChoiceOptionProps) {
  return (
    <button
      type="button"
      className={`choice-option ${isSelected ? 'selected' : ''}`}
      onClick={() => onSelect(index.toString())}
      aria-pressed={isSelected}
    >
      <span className="choice-number">{index + 1}</span>
      <span className="choice-text">{label}</span>
    </button>
  );
}

interface SuggestQuestionPanelProps {
  isBanned?: boolean;
  expanded: boolean;
  suggestionText: string;
  onToggle: () => void;
  onChange: (value: string) => void;
  onSubmit: () => Promise<void> | void;
  isSubmitting: boolean;
}

function SuggestQuestionPanel({
  isBanned,
  expanded,
  suggestionText,
  onToggle,
  onChange,
  onSubmit,
  isSubmitting,
}: SuggestQuestionPanelProps) {
  if (isBanned) {
    return (
      <div className="suggestion-ban">
        <p>You are banned from suggesting questions</p>
      </div>
    );
  }

  const handleSubmit = async () => {
    if (!suggestionText.trim() || isSubmitting) return;
    await onSubmit();
  };

  return (
    <div className="suggest-question-section">
      <button type="button" className="suggestion-toggle" onClick={onToggle}>
        <FiHelpCircle />
        {expanded ? 'Hide' : 'Suggest'} a Question
      </button>
      {expanded && (
        <>
          <textarea
            className="suggestion-textarea"
            value={suggestionText}
            onChange={(event) => onChange(event.target.value)}
            placeholder="Enter your question suggestion here..."
            rows={4}
          />
          <div className="suggestion-actions">
            <button
              type="button"
              className="suggestion-submit"
              onClick={handleSubmit}
              disabled={!suggestionText.trim() || isSubmitting}
            >
              {isSubmitting ? 'Submitting...' : 'Submit Suggestion'}
            </button>
          </div>
        </>
      )}
    </div>
  );
}

interface Props {
  ninjaId: number;
}

export default function Quiz({ ninjaId }: Props) {
  const { success, error: showError } = useToastContext();
  const [ninja, setNinja] = useState<Ninja | null>(null);
  const [bigQuestion, setBigQuestion] = useState<BigQuestionResponse | null>(null);
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [suggestionText, setSuggestionText] = useState('');
  const [showSuggestionForm, setShowSuggestionForm] = useState(false);
  const [submittingAnswer, setSubmittingAnswer] = useState(false);
  const [submittingSuggestion, setSubmittingSuggestion] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const ninjaData = await ninjaApi.getById(ninjaId);
      setNinja(ninjaData);
      setError('');

      try {
        const questionData = await bigQuestionApi.getThisWeeksQuestion(ninjaId);
        setBigQuestion(questionData ?? null);
      } catch (questionError) {
        console.error('Error loading question:', questionError);
        setBigQuestion(null);
      }
    } catch (err) {
      const message = getApiErrorMessage(err, 'Failed to load data');
      setError(message);
      setNinja(null);
      setBigQuestion(null);
    } finally {
      setLoading(false);
    }
  }, [ninjaId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    if (ninja?.suggestionsBanned) {
      setShowSuggestionForm(false);
      setSuggestionText('');
    }
  }, [ninja?.suggestionsBanned]);

  useEffect(() => {
    setAnswer('');
  }, [bigQuestion?.id, bigQuestion?.questionType]);

  const handleSubmitAnswer = useCallback(async () => {
    if (!bigQuestion || !answer.trim()) return;

    setSubmittingAnswer(true);
    try {
      await bigQuestionApi.submitAnswer(bigQuestion.id, {
        ninjaId,
        answer: answer.trim(),
      });
      await loadData();
      setAnswer('');
      success('Answer submitted!', 'Nice work');
    } catch (err) {
      showError(getApiErrorMessage(err, 'Failed to submit answer'));
      console.error(err);
    } finally {
      setSubmittingAnswer(false);
    }
  }, [answer, bigQuestion, loadData, ninjaId, showError, success]);

  const handleSubmitSuggestion = useCallback(async () => {
    if (!suggestionText.trim() || ninja?.suggestionsBanned) {
      return;
    }

    setSubmittingSuggestion(true);
    try {
      await bigQuestionApi.suggestQuestion({
        ninjaId,
        questionText: suggestionText.trim(),
        questionType: 'SHORT_ANSWER',
      });
      setSuggestionText('');
      setShowSuggestionForm(false);
      success('Question suggestion submitted! Thank you!', 'Suggestion Submitted');
    } catch (err) {
      showError(getApiErrorMessage(err, 'Failed to submit suggestion'));
      console.error(err);
    } finally {
      setSubmittingSuggestion(false);
    }
  }, [ninja?.suggestionsBanned, ninjaId, showError, suggestionText, success]);

  if (loading) {
    return (
      <div className="quiz-container">
        <h2>Loading...</h2>
      </div>
    );
  }

  if (error || !ninja) {
    return (
      <div className="quiz-container">
        <p className="error">{error || 'Ninja not found'}</p>
      </div>
    );
  }

  return (
    <div className="quiz-container">
      <div className="quiz-header">
        <h1>Question of the Week</h1>
        {bigQuestion && bigQuestion.weekStartDate && bigQuestion.weekEndDate && (
          <WeekCountdown startDate={bigQuestion.weekStartDate} endDate={bigQuestion.weekEndDate} />
        )}
      </div>

      <QuizStats ninja={ninja} />

      {bigQuestion ? (
        <section className="big-question-section">
          <div className="big-question-card">
            <p className="question-text">{bigQuestion.questionText}</p>
            {!bigQuestion.hasAnswered ? (
              <>
                {bigQuestion.questionType === 'MULTIPLE_CHOICE' && bigQuestion.choices ? (
                  <div className="choices-container">
                    {bigQuestion.choices.map((choice, idx) => (
                      <ChoiceOption
                        key={`${idx}-${choice ?? 'choice'}`}
                        index={idx}
                        label={choice ?? ''}
                        isSelected={answer === idx.toString()}
                        onSelect={(value) => setAnswer(value)}
                      />
                    ))}
                  </div>
                ) : (
                  <textarea
                    className="answer-input"
                    value={answer}
                    onChange={(event) => setAnswer(event.target.value)}
                    placeholder="Type your answer here..."
                    rows={3}
                  />
                )}
                <button
                  type="button"
                  className="submit-answer-btn"
                  onClick={handleSubmitAnswer}
                  disabled={!answer.trim() || submittingAnswer}
                >
                  {submittingAnswer ? 'Submitting...' : 'Submit Answer'}
                </button>
              </>
            ) : (
              <div className={`answer-result ${bigQuestion.wasCorrect ? 'correct' : 'incorrect'}`}>
                {bigQuestion.wasCorrect ? (
                  <>
                    <div className="result-icon">✓</div>
                    <div className="result-text">Correct! Great job!</div>
                  </>
                ) : (
                  <>
                    <div className="result-icon">✗</div>
                    <div className="result-text">Not quite right. Better luck next week!</div>
                  </>
                )}
              </div>
            )}
          </div>

          <SuggestQuestionPanel
            isBanned={ninja.suggestionsBanned}
            expanded={showSuggestionForm}
            suggestionText={suggestionText}
            onToggle={() => setShowSuggestionForm((prev) => !prev)}
            onChange={setSuggestionText}
            onSubmit={handleSubmitSuggestion}
            isSubmitting={submittingSuggestion}
          />
        </section>
      ) : (
        <section className="no-question">
          <FiHelpCircle className="no-question__icon" />
          <h2>No Question This Week</h2>
          <p>Check back next week for a new question!</p>
          <SuggestQuestionPanel
            isBanned={ninja.suggestionsBanned}
            expanded={showSuggestionForm}
            suggestionText={suggestionText}
            onToggle={() => setShowSuggestionForm((prev) => !prev)}
            onChange={setSuggestionText}
            onSubmit={handleSubmitSuggestion}
            isSubmitting={submittingSuggestion}
          />
        </section>
      )}
    </div>
  );
}
