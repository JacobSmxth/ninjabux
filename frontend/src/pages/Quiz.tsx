import { useState, useEffect, useRef } from 'react';
import { ninjaApi, bigQuestionApi } from '../services/api';
import type { Ninja, BigQuestionResponse } from '../types';
import { FiHelpCircle } from 'react-icons/fi';
import { useToastContext } from '../context/ToastContext';
import './Quiz.css';

// Week Countdown Component
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
    const interval = setInterval(updateCountdown, 60000); // Update every minute

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

interface Props {
  ninjaId: number;
}

export default function Quiz({ ninjaId }: Props) {
  const { success, error: showError } = useToastContext();
  const [ninja, setNinja] = useState<Ninja | null>(null);
  const [bigQuestion, setBigQuestion] = useState<BigQuestionResponse | null>(null);
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [suggestQuestion, setSuggestQuestion] = useState(false);
  const [suggestionText, setSuggestionText] = useState('');

  useEffect(() => {
    loadData();
  }, [ninjaId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [ninjaData] = await Promise.all([
        ninjaApi.getById(ninjaId),
      ]);
      setNinja(ninjaData);
      setError('');

      try {
        const questionData = await bigQuestionApi.getThisWeeksQuestion(ninjaId);
        if (questionData) {
          setBigQuestion(questionData);
        } else {
          setBigQuestion(null);
        }
      } catch (err: any) {
        // dont worry I did this
        console.error('Error loading question:', err);
        setBigQuestion(null);
      }
    } catch (err) {
      setError('Failed to load data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitAnswer = async () => {
    if (!bigQuestion || !answer.trim()) return;

    try {
      await bigQuestionApi.submitAnswer(bigQuestion.id, {
        ninjaId,
        answer: answer.trim()
      });
      loadData();
      setAnswer('');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit answer');
      console.error(err);
    }
  };

  const getBeltTheme = (belt: string) => {
    const themes: Record<string, { primary: string; secondary: string; textColor: string }> = {
      white: { primary: '#e8e8e8', secondary: '#000000', textColor: '#5a6c7d' },
      yellow: { primary: '#ffd700', secondary: '#000000', textColor: '#ffd700' },
      orange: { primary: '#ff8c00', secondary: '#ffffff', textColor: '#ff8c00' },
      green: { primary: '#32cd32', secondary: '#ffffff', textColor: '#32cd32' },
      blue: { primary: '#4169e1', secondary: '#ffffff', textColor: '#4169e1' },
      purple: { primary: '#9370db', secondary: '#ffffff', textColor: '#9370db' },
      red: { primary: '#dc143c', secondary: '#ffffff', textColor: '#dc143c' },
      brown: { primary: '#8b4513', secondary: '#ffffff', textColor: '#8b4513' },
      black: { primary: '#1a1a1a', secondary: '#ffffff', textColor: '#1a1a1a' },
    };
    return themes[belt.toLowerCase()] || themes.black;
  };

  const beltTheme = ninja ? getBeltTheme(ninja.currentBeltType) : { primary: '#E31E24', secondary: '#ffffff', textColor: '#E31E24' };

  if (loading) {
    return <div className="quiz-container"><h2>Loading...</h2></div>;
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
      <div className="quiz-header" style={{ borderBottomColor: beltTheme.primary }}>
        <h1>Question of the Week</h1>
        {bigQuestion && bigQuestion.weekStartDate && bigQuestion.weekEndDate && (
          <WeekCountdown startDate={bigQuestion.weekStartDate} endDate={bigQuestion.weekEndDate} />
        )}
      </div>

      {/* Stats */}
      <div className="quiz-stats" style={{ marginBottom: '1.5rem', padding: '1.5rem', background: '#f8f9fa', borderRadius: '12px', border: `2px solid ${beltTheme.primary}` }}>
        <h3 style={{ marginTop: 0, marginBottom: '1rem', color: '#000000', fontSize: '1.2rem', fontWeight: 700 }}>Your Quiz Stats</h3>
        <div style={{ display: 'flex', gap: '2rem', justifyContent: 'center' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 900, color: '#000000' }}>
              {ninja.totalQuestionsAnswered}
            </div>
            <div style={{ fontSize: '0.875rem', color: '#666', fontWeight: 600 }}>Total Answered</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 900, color: '#000000' }}>
              {ninja.totalQuestionsCorrect}
            </div>
            <div style={{ fontSize: '0.875rem', color: '#666', fontWeight: 600 }}>Correct</div>
          </div>
          {ninja.totalQuestionsAnswered > 0 && (
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '2rem', fontWeight: 900, color: '#000000' }}>
                {Math.round((ninja.totalQuestionsCorrect / ninja.totalQuestionsAnswered) * 100)}%
              </div>
              <div style={{ fontSize: '0.875rem', color: '#666', fontWeight: 600 }}>Accuracy</div>
            </div>
          )}
        </div>
      </div>

      {bigQuestion ? (
        <div className="big-question-section" style={{ borderColor: beltTheme.primary }}>
          <div className="big-question-card">
            <p className="question-text">{bigQuestion.questionText}</p>

            {!bigQuestion.hasAnswered ? (
              <>
                {bigQuestion.questionType === 'MULTIPLE_CHOICE' && bigQuestion.choices ? (
                  <div className="choices-container">
                    {bigQuestion.choices.map((choice, idx) => (
                      <div
                        key={idx}
                        className={`choice-option ${answer === idx.toString() ? 'selected' : ''}`}
                        onClick={() => setAnswer(idx.toString())}
                        style={answer === idx.toString() ? {
                          borderColor: beltTheme.primary,
                          background: `${beltTheme.primary}15`
                        } : {}}
                      >
                        <div className="choice-number" style={answer === idx.toString() ? {
                          background: beltTheme.primary,
                          color: beltTheme.secondary
                        } : {}}>
                          {idx + 1}
                        </div>
                        <div className="choice-text">{choice}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <textarea
                    className="answer-input"
                    value={answer}
                    onChange={(e) => setAnswer(e.target.value)}
                    placeholder="Type your answer here..."
                    rows={3}
                  />
                )}
                <button
                  className="submit-answer-btn"
                  onClick={handleSubmitAnswer}
                  disabled={!answer.trim()}
                  style={{
                    background: beltTheme.primary,
                    color: beltTheme.secondary
                  }}
                >
                  Submit Answer
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
          
          {/* Suggest Question */}
          {!ninja?.suggestionsBanned ? (
            <div className="suggest-question-section" style={{ marginTop: '1.5rem', padding: '1.5rem', background: '#f8f9fa', borderRadius: '12px', border: `2px solid ${beltTheme.primary}` }}>
              <button
                onClick={() => setSuggestQuestion(!suggestQuestion)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: beltTheme.primary,
                  fontSize: '1rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  marginBottom: suggestQuestion ? '1rem' : 0,
                }}
              >
                <FiHelpCircle />
                {suggestQuestion ? 'Hide' : 'Suggest'} a Question
              </button>
              {suggestQuestion && (
                <div>
                  <textarea
                    value={suggestionText}
                    onChange={(e) => setSuggestionText(e.target.value)}
                    placeholder="Enter your question suggestion here..."
                    rows={4}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      borderRadius: '8px',
                      border: `2px solid ${beltTheme.primary}`,
                      fontSize: '1rem',
                      marginBottom: '0.75rem',
                      resize: 'vertical',
                    }}
                  />
                  <button
                    onClick={async () => {
                      if (!suggestionText.trim()) return;
                      if (ninja?.suggestionsBanned) {
                        showError('You are banned from suggesting questions');
                        return;
                      }
                      try {
                        await bigQuestionApi.suggestQuestion({
                          ninjaId,
                          questionText: suggestionText,
                          questionType: 'SHORT_ANSWER',
                        });
                        setSuggestionText('');
                        setSuggestQuestion(false);
                        success('Question suggestion submitted! Thank you!', 'Suggestion Submitted');
                      } catch (err: any) {
                        showError(err.response?.data?.message || 'Failed to submit suggestion');
                        console.error(err);
                      }
                    }}
                    disabled={ninja?.suggestionsBanned || !suggestionText.trim()}
                    style={{
                      background: beltTheme.primary,
                      color: beltTheme.secondary,
                      border: 'none',
                      padding: '0.75rem 1.5rem',
                      borderRadius: '8px',
                      fontSize: '1rem',
                      fontWeight: 600,
                      cursor: suggestionText.trim() ? 'pointer' : 'not-allowed',
                      opacity: suggestionText.trim() ? 1 : 0.5,
                    }}
                  >
                    Submit Suggestion
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div style={{ marginTop: '1.5rem', padding: '1.5rem', background: '#fee2e2', borderRadius: '12px', border: '2px solid #dc2626', textAlign: 'center' }}>
              <p style={{ color: '#dc2626', fontWeight: 600, margin: 0 }}>You are banned from suggesting questions</p>
            </div>
          )}
        </div>
      ) : (
        <div className="no-question" style={{ padding: '3rem', textAlign: 'center', background: '#f8f9fa', borderRadius: '12px', border: `2px solid ${beltTheme.primary}` }}>
          <FiHelpCircle size={48} color={beltTheme.primary} style={{ marginBottom: '1rem' }} />
          <h2 style={{ color: '#000000', marginBottom: '0.5rem' }}>No Question This Week</h2>
          <p style={{ color: '#666', marginBottom: '1.5rem' }}>Check back next week for a new question!</p>
          
          {/* Suggest Question */}
          {!ninja?.suggestionsBanned ? (
            <div className="suggest-question-section" style={{ marginTop: '1.5rem', padding: '1.5rem', background: 'white', borderRadius: '12px', border: `2px solid ${beltTheme.primary}` }}>
              <button
                onClick={() => setSuggestQuestion(!suggestQuestion)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: beltTheme.primary,
                  fontSize: '1rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  marginBottom: suggestQuestion ? '1rem' : 0,
                }}
              >
                <FiHelpCircle />
                {suggestQuestion ? 'Hide' : 'Suggest'} a Question
              </button>
            {suggestQuestion && (
              <div>
                <textarea
                  value={suggestionText}
                  onChange={(e) => setSuggestionText(e.target.value)}
                  placeholder="Enter your question suggestion here..."
                  rows={4}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    borderRadius: '8px',
                    border: `2px solid ${beltTheme.primary}`,
                    fontSize: '1rem',
                    marginBottom: '0.75rem',
                    resize: 'vertical',
                  }}
                />
                <button
                  onClick={async () => {
                    if (!suggestionText.trim()) return;
                    if (ninja?.suggestionsBanned) {
                      showError('You are banned from suggesting questions');
                      return;
                    }
                    try {
                      await bigQuestionApi.suggestQuestion({
                        ninjaId,
                        questionText: suggestionText,
                        questionType: 'SHORT_ANSWER',
                      });
                      setSuggestionText('');
                      setSuggestQuestion(false);
                      success('Question suggestion submitted! Thank you!', 'Suggestion Submitted');
                    } catch (err: any) {
                      showError(err.response?.data?.message || 'Failed to submit suggestion');
                      console.error(err);
                    }
                  }}
                  disabled={ninja?.suggestionsBanned || !suggestionText.trim()}
                  style={{
                    background: beltTheme.primary,
                    color: beltTheme.secondary,
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '8px',
                    fontSize: '1rem',
                    fontWeight: 600,
                    cursor: suggestionText.trim() ? 'pointer' : 'not-allowed',
                    opacity: suggestionText.trim() ? 1 : 0.5,
                  }}
                >
                  Submit Suggestion
                </button>
              </div>
            )}
            </div>
          ) : (
            <div style={{ marginTop: '1.5rem', padding: '1.5rem', background: '#fee2e2', borderRadius: '12px', border: '2px solid #dc2626', textAlign: 'center' }}>
              <p style={{ color: '#dc2626', fontWeight: 600, margin: 0 }}>You are banned from suggesting questions</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

