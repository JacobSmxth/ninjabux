import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { ninjaApi, shopApi, bigQuestionApi, achievementApi } from '../services/api';
import type { Ninja, Purchase, BigQuestionResponse, ProgressHistory, AchievementProgress } from '../types';
import { FiAward, FiHelpCircle, FiDollarSign } from 'react-icons/fi';
import { useLockContext } from '../context/LockContext';
import AchievementIcon from '../components/AchievementIcon';
import { getBeltTheme, defaultBeltTheme } from '../utils/beltTheme';
import { formatBux } from '../utils/format';
import PageLayout from '../components/PageLayout';
import BeltTag from '../components/BeltTag';
import BalanceCard from '../components/BalanceCard';
import MessageBanner from '../components/MessageBanner';
import SectionHeader from '../components/SectionHeader';
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
  }, [ninjaId]);

  useEffect(() => {
    loadData();
  }, [loadData]);


  const beltTheme = ninja ? getBeltTheme(ninja.currentBeltType) : defaultBeltTheme;

  if (loading) {
    return (
      <PageLayout title="Dashboard" containerClass="dashboard-container">
        <h2>Loading...</h2>
      </PageLayout>
    );
  }

  if (error || !ninja) {
    return (
      <PageLayout title="Dashboard" containerClass="dashboard-container">
        <MessageBanner message={error || 'Ninja not found'} type="error" />
      </PageLayout>
    );
  }

  const headerAction = (
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
  );

  return (
    <PageLayout
      title={`${ninja.firstName} ${ninja.lastName}`}
      eyebrow={`Level ${ninja.currentLevel}, Lesson ${ninja.currentLesson}`}
      actions={headerAction}
      containerClass="dashboard-container"
      containerStyle={{
        '--accent': beltTheme.primary,
        '--accent-strong': beltTheme.accent
      } as React.CSSProperties}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <BeltTag beltType={ninja.currentBeltType} label={`${ninja.currentBeltType} Belt`} size="large" />
          <div className="progress-inline">
            Level {ninja.currentLevel}, Lesson {ninja.currentLesson}
          </div>
        </div>
        <BalanceCard balance={ninja.buxBalance} beltType={ninja.currentBeltType} label="Balance" />
      </div>

      {/* Top Achievements */}
      {topAchievements.length > 0 && (
        <div className="achievements-section" style={{ borderColor: beltTheme.primary }}>
          <SectionHeader
            title="Top Achievements"
            subtitle="Highlights from your recent progress"
            action={<FiAward size={24} color={beltTheme.textColor} />}
          />
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
                      <FiDollarSign style={{ marginRight: '0.25rem' }} /> {formatBux(progress.achievement.buxReward)} Bux
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
                    <span className="timeline-bux" style={{ color: entryColor }}>+{formatBux(entry.buxEarned)} Bux</span>
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
                <p className="purchase-price" style={{ color: beltTheme.primary }}>Paid: {formatBux(purchase.pricePaid)} Bux</p>
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
                <p className="purchase-price" style={{ color: beltTheme.primary }}>Paid: {formatBux(purchase.pricePaid)} Bux</p>
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
    </PageLayout>
  );
}
