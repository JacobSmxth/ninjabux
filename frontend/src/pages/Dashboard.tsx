import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ninjaApi, shopApi, bigQuestionApi, achievementApi } from '../services/api';
import type { Ninja, Purchase, BigQuestionResponse, ProgressHistory, AchievementProgress } from '../types';
import { FiAward, FiHelpCircle } from 'react-icons/fi';
import { useLockContext } from '../context/LockContext';
import './Dashboard.css';


interface Props {
  ninjaId: number;
}

export default function Dashboard({ ninjaId }: Props) {
  const navigate = useNavigate();
  const { setLockStatus } = useLockContext();
  const [ninja, setNinja] = useState<Ninja | null>(null);
  const [unredeemedPurchases, setUnredeemedPurchases] = useState<Purchase[]>([]);
  const [allPurchases, setAllPurchases] = useState<Purchase[]>([]);
  const [progressHistory, setProgressHistory] = useState<ProgressHistory[]>([]);
  const [bigQuestion, setBigQuestion] = useState<BigQuestionResponse | null>(null);
  const [topAchievements, setTopAchievements] = useState<AchievementProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadData();
  }, [ninjaId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [ninjaData, unredeemedData, allData, historyData, achievementsData] = await Promise.all([
        ninjaApi.getById(ninjaId),
        shopApi.getUnredeemedPurchases(ninjaId),
        shopApi.getNinjaPurchases(ninjaId),
        ninjaApi.getProgressHistory(ninjaId),
        achievementApi.getTopAchievements(ninjaId, 3),
      ]);
      setNinja(ninjaData);
      setUnredeemedPurchases(unredeemedData);
      setAllPurchases(allData);
      setProgressHistory(historyData);
      setTopAchievements(achievementsData);

      // Update lock status
      if (ninjaData.isLocked) {
        setLockStatus(true, ninjaData.lockReason || 'Your account is locked. Please get back to work!');
      } else {
        setLockStatus(false, '');
      }

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
    } catch (err: any) {
      if (err.response?.status === 403 || err.message?.includes('Account is locked')) {
        const errorMsg = err.response?.data?.message || err.message || 'Account is locked';
        setLockStatus(true, errorMsg);
      }
      setError('Failed to load data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };


  const getBeltColor = (belt: string) => {
    return belt.toLowerCase();
  };

  const hexToRgba = (hex: string, alpha: number) => {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
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
    return <div className="dashboard-container"><h2>Loading...</h2></div>;
  }

  if (error || !ninja) {
    return (
      <div className="dashboard-container">
        <p className="error">{error || 'Ninja not found'}</p>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="ninja-header" style={{ borderBottomColor: beltTheme.primary }}>
        <div className="ninja-info">
          <div className="ninja-name-row">
            <h1>{ninja.firstName} {ninja.lastName}</h1>
            <div className="progress-inline">
              Level {ninja.currentLevel}, Lesson {ninja.currentLesson}
            </div>
          </div>
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
            <div className={`belt-display belt-${getBeltColor(ninja.currentBeltType)}`} style={{
              border: `3px solid ${beltTheme.primary}`,
              color: '#000000',
              background: 'transparent',
            }}>
              {ninja.currentBeltType} BELT
            </div>
            <button
              onClick={() => navigate('/quiz')}
              style={{
                background: '#000000',
                color: '#ffffff',
                border: bigQuestion && !bigQuestion.hasAnswered ? `3px solid ${beltTheme.primary}` : `2px solid ${beltTheme.primary}`,
                padding: '0.75rem 1.5rem',
                borderRadius: '12px',
                fontSize: '1rem',
                fontWeight: bigQuestion && !bigQuestion.hasAnswered ? 700 : 600,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                boxShadow: bigQuestion && !bigQuestion.hasAnswered
                  ? `0 0 20px ${hexToRgba(beltTheme.primary, 0.5)}, 0 0 40px ${hexToRgba(beltTheme.primary, 0.3)}, 0 0 60px ${hexToRgba(beltTheme.primary, 0.2)}`
                  : 'none',
                animation: bigQuestion && !bigQuestion.hasAnswered ? 'pulse-glow-belt 2s ease-in-out infinite' : 'none',
                '--belt-color': beltTheme.primary,
                '--belt-color-rgba-50': hexToRgba(beltTheme.primary, 0.5),
                '--belt-color-rgba-80': hexToRgba(beltTheme.primary, 0.8),
                '--belt-color-rgba-30': hexToRgba(beltTheme.primary, 0.3),
              } as React.CSSProperties}
            >
              <FiHelpCircle size={bigQuestion && !bigQuestion.hasAnswered ? 20 : 18} />
              {bigQuestion && !bigQuestion.hasAnswered ? 'ANSWER QUESTION!' : 'Quiz'}
            </button>
          </div>
        </div>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <div className="balance-card" style={{
            background: `linear-gradient(135deg, ${beltTheme.primary} 0%, ${beltTheme.primary}dd 100%)`,
            color: beltTheme.secondary
          }}>
            <div className="balance-label">Balance</div>
            <div className="balance-amount">{ninja.buxBalance} Bux</div>
            {ninja.legacyBalance !== undefined && ninja.legacyBalance > 0 && (
              <div className="balance-legacy" style={{ fontSize: '0.9rem', opacity: 0.9, marginTop: '0.25rem' }}>
                {ninja.legacyBalance} Legacy
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Top Achievements */}
      {topAchievements.length > 0 && (
        <div className="achievements-section" style={{ borderColor: beltTheme.primary }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <FiAward size={24} color="#000000" />
            <h2 style={{ margin: 0, color: '#000000' }}>Top Achievements</h2>
          </div>
          <div className="achievements-grid">
            {topAchievements.map((progress) => (
              <div
                key={progress.id}
                className={`achievement-badge rarity-${progress.achievement.rarity}`}
                style={{
                  borderColor: progress.achievement.rarity === 'LEGENDARY' ? '#F59E0B' :
                               progress.achievement.rarity === 'EPIC' ? '#A855F7' :
                               progress.achievement.rarity === 'RARE' ? '#3B82F6' : '#9CA3AF'
                }}
              >
                <div className="achievement-icon-large">{progress.achievement.icon}</div>
                <div className="achievement-details">
                  <h4>{progress.achievement.name}</h4>
                  <p>{progress.achievement.description}</p>
                  {progress.achievement.buxReward > 0 && (
                    <div className="achievement-reward" style={{ color: '#000000' }}>
                      💰 {progress.achievement.buxReward} Bux
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}



      {progressHistory.length > 0 && (
        <div className="progress-history-section">
          <h2>Progress Timeline</h2>
          <div className="progress-timeline">
            {progressHistory.map((entry) => {
              const entryType = entry.earningType === 'LEVEL_UP' ? 'Level Up'
                : entry.earningType === 'QUIZ_REWARD' ? 'Quiz Reward'
                : 'Admin Award';
              const entryColor = entry.earningType === 'LEVEL_UP' ? beltTheme.textColor
                : entry.earningType === 'QUIZ_REWARD' ? '#2563eb'
                : '#16a34a';

              return (
                <div key={entry.id} className="timeline-entry" style={{ borderLeftColor: entryColor }}>
                  <div className="timeline-header">
                    <span className="timeline-type" style={{ color: entryColor }}>{entryType}</span>
                    <span className="timeline-date">
                      {new Date(entry.timestamp).toLocaleDateString()} at {new Date(entry.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div className="timeline-details">
                    <span className="timeline-bux" style={{ color: entryColor }}>+{entry.buxEarned} Bux</span>
                    {entry.earningType === 'LEVEL_UP' && (
                      <span className="timeline-progress">
                        {entry.beltType} Belt - Level {entry.level}, Lesson {entry.lesson}
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {unredeemedPurchases.length > 0 && (
        <div className="purchases-section">
          <h2>Your Unredeemed Purchases</h2>
          <div className="purchases-grid">
            {unredeemedPurchases.map((purchase) => (
              <div key={purchase.id} className="purchase-card" style={{ borderColor: beltTheme.primary }}>
                <h3>{purchase.itemName}</h3>
                <p className="purchase-description">{purchase.itemDescription}</p>
                <p className="purchase-price" style={{ color: beltTheme.primary }}>Paid: {purchase.pricePaid} Bux</p>
                <p className="purchase-date">
                  Purchased: {new Date(purchase.purchaseDate).toLocaleDateString()}
                </p>
                <div className="purchase-status unredeemed">Not Yet Redeemed</div>
              </div>
            ))}
          </div>
          <p className="redeem-note" style={{
            background: beltTheme.primary,
            color: beltTheme.secondary,
            borderColor: beltTheme.primary
          }}>
            Show this to your sensei to redeem your purchases!
          </p>
        </div>
      )}

      {allPurchases.filter(p => p.redeemed).length > 0 && (
        <div className="purchases-section">
          <h2>Past Redeemed Purchases</h2>
          <div className="purchases-grid">
            {allPurchases.filter(p => p.redeemed).map((purchase) => (
              <div key={purchase.id} className="purchase-card redeemed-purchase" style={{ borderColor: beltTheme.primary, opacity: 0.7 }}>
                <h3>{purchase.itemName}</h3>
                <p className="purchase-description">{purchase.itemDescription}</p>
                <p className="purchase-price" style={{ color: beltTheme.primary }}>Paid: {purchase.pricePaid} Bux</p>
                <p className="purchase-date">
                  Purchased: {new Date(purchase.purchaseDate).toLocaleDateString()}
                  {purchase.redeemedDate && (
                    <br />
                  )}
                  {purchase.redeemedDate && `Redeemed: ${new Date(purchase.redeemedDate).toLocaleDateString()}`}
                </p>
                <div className="purchase-status" style={{ background: '#28a745', color: 'white' }}>Redeemed</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {unredeemedPurchases.length === 0 && allPurchases.filter(p => p.redeemed).length === 0 && (
        <div className="no-purchases">
          <p>No purchases yet. Visit the shop to spend your Bux!</p>
        </div>
      )}
    </div>
  );
}
