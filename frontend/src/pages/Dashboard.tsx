import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { ninjaApi, shopApi, bigQuestionApi, achievementApi } from '../services/api';
import type { Ninja, Purchase, BigQuestionResponse, ProgressHistory, AchievementProgress } from '../types';
import { FiAward, FiHelpCircle, FiDollarSign } from 'react-icons/fi';
import { useLockContext } from '../context/LockContext';
import AchievementIcon from '../components/AchievementIcon';
import { getBeltTheme, defaultBeltTheme } from '../utils/beltTheme';
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

  const loadData = useCallback(async () => {
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
        setBigQuestion(questionData ?? null);
      } catch (questionError) {
        console.error('Error loading question:', questionError);
        setBigQuestion(null);
      }
    } catch (error) {
      if (error instanceof Error && ('response' in error ? (error as any).response?.status === 403 : error.message.includes('Account is locked'))) {
        const errorMsg = (error as any)?.response?.data?.message || error.message || 'Account is locked';
        setLockStatus(true, errorMsg);
      }
      setError('Failed to load data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [ninjaId, setLockStatus]);

  useEffect(() => {
    loadData();
  }, [loadData]);


  const getBeltColor = (belt: string) => {
    return belt.toLowerCase();
  };

  const beltTheme = ninja ? getBeltTheme(ninja.currentBeltType) : defaultBeltTheme;

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
                border: '1px solid rgba(15, 23, 42, 0.15)',
                padding: '0.75rem 1.5rem',
                borderRadius: '12px',
                fontSize: '1rem',
                fontWeight: bigQuestion && !bigQuestion.hasAnswered ? 700 : 600,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                boxShadow: '0 0 18px rgba(107, 114, 128, 0.35)',
                animation: bigQuestion && !bigQuestion.hasAnswered ? 'pulse-glow-belt 2s ease-in-out infinite' : 'none',
                '--belt-color': '#737373',
                '--belt-color-rgba-50': 'rgba(115, 115, 115, 0.5)',
                '--belt-color-rgba-80': 'rgba(115, 115, 115, 0.8)',
                '--belt-color-rgba-30': 'rgba(115, 115, 115, 0.3)',
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
                <div className="achievement-icon-large">
                  <AchievementIcon icon={progress.achievement.icon} size={36} />
                </div>
                <div className="achievement-details">
                  <h4>{progress.achievement.name}</h4>
                  <p>{progress.achievement.description}</p>
                  {progress.achievement.buxReward > 0 && (
                    <div className="achievement-reward" style={{ color: '#000000' }}>
                      <FiDollarSign style={{ marginRight: '0.25rem' }} /> {progress.achievement.buxReward} Bux
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
