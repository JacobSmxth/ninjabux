import { useState, useEffect, useCallback } from 'react';
import { achievementApi } from '../services/api';
import type { AchievementProgress, AchievementCategory } from '../types';
import { FiAward, FiLock, FiFilter, FiDollarSign } from 'react-icons/fi';
import { useToast } from '../hooks/useToast';
import AchievementIcon from '../components/AchievementIcon';
import './AchievementGallery.css';

interface Props {
  ninjaId: number;
}

const categoryLabels: Record<AchievementCategory, string> = {
  PROGRESS: 'Progress',
  QUIZ: 'Quiz Master',
  PURCHASE: 'Big Spender',
  STREAK: 'Streaks',
  SOCIAL: 'Social',
  SPECIAL: 'Special'
};

export default function AchievementGallery({ ninjaId }: Props) {
  const { success, error: showError } = useToast();
  const [achievements, setAchievements] = useState<AchievementProgress[]>([]);
  const [filteredAchievements, setFilteredAchievements] = useState<AchievementProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<AchievementCategory | 'ALL'>('ALL');
  const [showUnlockedOnly, setShowUnlockedOnly] = useState(false);

  const extractErrorMessage = (error: unknown): string | undefined => {
    if (error instanceof Error && !('response' in error)) {
      return error.message;
    }
    if (typeof error === 'object' && error !== null && 'response' in error) {
      const response = (error as { response?: { data?: { message?: string } } }).response;
      return response?.data?.message;
    }
    return undefined;
  };

  const loadAchievements = useCallback(async () => {
    try {
      setLoading(true);
      // admin can see hidden achievements, regular users can't
      const isAdmin = !!localStorage.getItem('adminToken') || !!localStorage.getItem('adminUsername');
      const data = await achievementApi.getNinjaAchievements(ninjaId, isAdmin);
      
      // hide locked achievements unless you're admin
      const filtered = isAdmin 
        ? data 
        : data.filter(ap => !ap.achievement.hidden || ap.unlocked);
      
      setAchievements(filtered);
      setError('');
    } catch (error) {
      const message = extractErrorMessage(error) || 'Failed to load achievements';
      setError(message);
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [ninjaId]);

  useEffect(() => {
    loadAchievements();
  }, [loadAchievements]);

  const filterAchievements = useCallback(() => {
    let filtered = [...achievements];

    // filter by category if one is selected
    if (selectedCategory !== 'ALL') {
      filtered = filtered.filter(a => a.achievement.category === selectedCategory);
    }

    // filter by unlocked status if toggle is on
    if (showUnlockedOnly) {
      filtered = filtered.filter(a => a.unlocked);
    }

    // unlocked first, locked second, because that's what users care about
    filtered.sort((a, b) => {
      if (a.unlocked && !b.unlocked) return -1;
      if (!a.unlocked && b.unlocked) return 1;
      return 0;
    });

    setFilteredAchievements(filtered);
  }, [achievements, selectedCategory, showUnlockedOnly]);

  useEffect(() => {
    filterAchievements();
  }, [filterAchievements]);

  const getStats = () => {
    const total = achievements.length;
    const unlocked = achievements.filter(a => a.unlocked).length;
    const totalBux = achievements
      .filter(a => a.unlocked)
      .reduce((sum, a) => sum + a.achievement.buxReward, 0);

    return { total, unlocked, totalBux };
  };

  const stats = getStats();

  if (loading) {
    return (
      <div className="gallery-container">
        <h2>Loading achievements...</h2>
      </div>
    );
  }

  if (error) {
    return (
      <div className="gallery-container">
        <p className="error">{error}</p>
      </div>
    );
  }

  return (
    <div className="gallery-container">
      <div className="gallery-header">
        <div className="header-content">
          <FiAward size={48} color="#E31E24" />
          <div>
            <h1>Achievement Gallery</h1>
            <p className="gallery-subtitle">Collect them all and earn Bux!</p>
          </div>
        </div>
        <div className="stats-cards">
          <div className="stat-card">
            <div className="stat-value">{stats.unlocked}/{stats.total}</div>
            <div className="stat-label">Unlocked</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.totalBux}</div>
            <div className="stat-label">Bux Earned</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{Math.round((stats.unlocked / stats.total) * 100)}%</div>
            <div className="stat-label">Complete</div>
          </div>
        </div>
      </div>

      <div className="gallery-filters">
        <div className="filter-section">
          <FiFilter size={20} />
          <span className="filter-label">Category:</span>
          <select
            className="filter-dropdown"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value as AchievementCategory | 'ALL')}
          >
            <option value="ALL">All Categories</option>
            {(Object.keys(categoryLabels) as AchievementCategory[]).map(category => (
              <option key={category} value={category}>
                {categoryLabels[category]}
              </option>
            ))}
          </select>
        </div>
        <label className="toggle-label">
          <input
            type="checkbox"
            checked={showUnlockedOnly}
            onChange={(e) => setShowUnlockedOnly(e.target.checked)}
          />
          <span>Show unlocked only</span>
        </label>
      </div>

      <div className="achievements-gallery-grid">
        {filteredAchievements.map((progress) => (
          <div
            key={progress.id}
            className={`gallery-achievement-card rarity-${progress.achievement.rarity} ${!progress.unlocked ? 'locked' : ''}`}
          >
            {!progress.unlocked && (
              <div className="locked-overlay">
                <FiLock size={32} />
              </div>
            )}
            <div className={`achievement-icon-gallery ${!progress.unlocked ? 'locked-icon' : ''}`}>
              <AchievementIcon icon={progress.achievement.icon} size={48} />
            </div>
            <div className="achievement-info">
              <div className="achievement-category-badge">{categoryLabels[progress.achievement.category]}</div>
              <h3>{progress.achievement.name}</h3>
              <p className="achievement-desc">{progress.achievement.description}</p>
              <div className="achievement-footer">
                {progress.achievement.buxReward > 0 && (
                  <div className="achievement-reward-badge">
                    <FiDollarSign style={{ marginRight: '0.25rem' }} /> {progress.achievement.buxReward} Bux
                  </div>
                )}
                <div className={`rarity-badge rarity-${progress.achievement.rarity}`}>
                  {progress.achievement.rarity}
                </div>
              </div>
              {progress.unlocked && progress.unlockedAt && (
                <div className="unlocked-date">
                  Unlocked: {new Date(progress.unlockedAt).toLocaleDateString()}
                </div>
              )}
              {progress.manuallyAwarded && (
                <div className="manual-award-badge">
                  Awarded by Sensei
                </div>
              )}
              {progress.unlocked && (
                <button
                  onClick={async () => {
                    // check for true explicitly because javascript equality is a lie
                    if (progress.isLeaderboardBadge === true) return; // already selected, don't do anything
                    try {
                      await achievementApi.setLeaderboardBadge(ninjaId, progress.id);
                      await loadAchievements();
                      success('Badge selected for leaderboard!');
                    } catch (error) {
                      const errorMsg = extractErrorMessage(error) || 'Failed to set badge';
                      showError(errorMsg);
                    }
                  }}
                  style={{
                    marginTop: '0.5rem',
                    padding: '0.5rem 1rem',
                    background: progress.isLeaderboardBadge === true ? '#e5e7eb' : 'transparent',
                    color: progress.isLeaderboardBadge === true ? '#9ca3af' : '#666',
                    border: `2px solid ${progress.isLeaderboardBadge === true ? '#d1d5db' : '#ddd'}`,
                    borderRadius: '8px',
                    fontSize: '0.875rem',
                    fontWeight: 600,
                    cursor: progress.isLeaderboardBadge === true ? 'not-allowed' : 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    width: '100%',
                    justifyContent: 'center',
                    opacity: progress.isLeaderboardBadge === true ? 0.6 : 1,
                  }}
                  title={progress.isLeaderboardBadge === true ? 'This badge is shown on leaderboard' : 'Select this badge for leaderboard'}
                  disabled={progress.isLeaderboardBadge === true}
                >
                  {progress.isLeaderboardBadge === true ? 'Leaderboard Badge' : 'Set as Leaderboard Badge'}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {filteredAchievements.length === 0 && (
        <div className="no-achievements">
          <FiAward size={64} color="#ccc" />
          <p>No achievements found with the selected filters.</p>
        </div>
      )}
    </div>
  );
}
