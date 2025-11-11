import { useState, useEffect } from 'react';
import { ninjaApi } from '../services/api';
import type { LeaderboardResponse, LeaderboardEntry, BadgeRarity } from '../types';
import { FiStar, FiTarget, FiTrendingUp, FiZap, FiClock, FiAward, FiDollarSign, FiCheck, FiChevronDown, FiChevronUp } from 'react-icons/fi';
import './Leaderboard.css';

type TimePeriod = 'daily' | 'week' | 'month' | 'lifetime';
type LeaderboardSection = 'earners' | 'spenders' | 'improved' | 'quiz' | 'streak';

export default function Leaderboard() {
  const [leaderboard, setLeaderboard] = useState<LeaderboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [period, setPeriod] = useState<TimePeriod>('week');
  const [expandedSection, setExpandedSection] = useState<LeaderboardSection | null>('earners');

  useEffect(() => {
    loadLeaderboard();
  }, [period]);

  const loadLeaderboard = async () => {
    try {
      setLoading(true);
      const data = await ninjaApi.getLeaderboard(10, period);
      setLeaderboard(data);
      setError('');
    } catch (err) {
      setError('Failed to load leaderboard');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const getAchievementIcon = (iconName: string) => {
    if (!iconName) return <FiAward size={16} />;

    // check if it's an emoji using unicode ranges
    const emojiRegex = /[\u{1F300}-\u{1F9FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]|[\u{2B00}-\u{2BFF}]/u;
    if (emojiRegex.test(iconName)) {
      return <span style={{ fontSize: '18px', lineHeight: 1 }}>{iconName}</span>;
    }

    // fallback to react icons for old achievements
    const icons: Record<string, any> = {
      'target': FiTarget,
      'star': FiStar,
      'trending': FiTrendingUp,
      'zap': FiZap,
      'lightning': FiZap,
      'clock': FiClock,
      'award': FiAward,
      'dollar': FiDollarSign,
      'check': FiCheck,
    };
    const Icon = icons[iconName?.toLowerCase()] || FiAward;
    return <Icon size={16} />;
  };

  const getRarityGlowClass = (rarity: BadgeRarity) => {
    return `badge-glow rarity-${rarity}`;
  };

  const toggleSection = (section: LeaderboardSection) => {
    setExpandedSection(expandedSection === section ? null : section);
  };

  const LeaderboardEntryComponent = ({
    entry,
    value,
    valueLabel,
    isTop = false,
    topIcon = null
  }: {
    entry: LeaderboardEntry;
    value: string | number;
    valueLabel?: string;
    isTop?: boolean;
    topIcon?: string | null;
  }) => {
    const fullName = `${entry.firstName.charAt(0).toUpperCase() + entry.firstName.slice(1).toLowerCase()} ${entry.lastName.charAt(0).toUpperCase() + entry.lastName.slice(1).toLowerCase()}`;

    return (
      <div className={`leaderboard-entry ${isTop ? 'top-1' : ''}`}>
        <div className="rank">#{entry.rank}</div>
        <div className="ninja-info">
          <div className="ninja-name-row">
            <div className="ninja-name" title={fullName}>
              {fullName}
            </div>
            {entry.leaderboardBadge && (
              <div className="ninja-badges">
                <div
                  className={`leaderboard-badge ${getRarityGlowClass(entry.leaderboardBadge.achievement.rarity)}`}
                  title={entry.leaderboardBadge.achievement.name}
                >
                  {getAchievementIcon(entry.leaderboardBadge.achievement.icon)}
                </div>
              </div>
            )}
          </div>
          <div className="ninja-belt">{entry.currentBeltType} Belt</div>
        </div>
        <div className="bux-amount">
          {isTop && topIcon && <span className="badge">{topIcon}</span>}
          {typeof value === 'number' && valueLabel === '$' ? `$${value}` : value}
          {valueLabel && valueLabel !== '$' && <span className="value-label">{valueLabel}</span>}
        </div>
      </div>
    );
  };

  if (loading) {
    return <div className="leaderboard-container">Loading...</div>;
  }

  if (error) {
    return <div className="leaderboard-container error">{error}</div>;
  }

  return (
    <div className="leaderboard-container">
      <h1 className="leaderboard-title">Leaderboard</h1>

      {leaderboard?.message && (
        <div style={{ padding: '1rem', background: '#fef3c7', border: '2px solid #f59e0b', borderRadius: '8px', marginBottom: '1rem' }}>
          {leaderboard.message}
        </div>
      )}

      <div className="period-tabs">
        <button
          className={`period-tab ${period === 'daily' ? 'active' : ''}`}
          onClick={() => setPeriod('daily')}
        >
          Today
        </button>
        <button
          className={`period-tab ${period === 'week' ? 'active' : ''}`}
          onClick={() => setPeriod('week')}
        >
          This Week
        </button>
        <button
          className={`period-tab ${period === 'month' ? 'active' : ''}`}
          onClick={() => setPeriod('month')}
        >
          This Month
        </button>
        <button
          className={`period-tab ${period === 'lifetime' ? 'active' : ''}`}
          onClick={() => setPeriod('lifetime')}
        >
          All Time
        </button>
      </div>

      <div className="leaderboard-sections-wrapper">
        {/* Main content area - expanded section */}
        <div className="leaderboard-main">
          {expandedSection === 'earners' && (
            <div className="leaderboard-section expanded">
              <div className="section-header" onClick={() => toggleSection('earners')}>
                <h2>Top Earners <span className="section-subtitle">(Most Bux Earned)</span></h2>
                <button className="expand-toggle">
                  <FiChevronUp size={20} />
                </button>
              </div>
              <div className="leaderboard-list">
                {leaderboard?.topEarners.length === 0 ? (
                  <div className="empty-leaderboard">
                    <p>{leaderboard?.message || 'No one has earned any Bux yet!'}</p>
                    <p className="empty-subtitle">Be the first to earn Bux by completing lessons and leveling up.</p>
                  </div>
                ) : (
                  leaderboard?.topEarners.map((entry: LeaderboardEntry) => (
                    <LeaderboardEntryComponent
                      key={entry.ninjaId}
                      entry={entry}
                      value={entry.totalBuxEarned}
                      valueLabel="$"
                      isTop={entry.isTopEarner}
                      topIcon="👑"
                    />
                  ))
                )}
              </div>
            </div>
          )}

          {expandedSection === 'spenders' && (
            <div className="leaderboard-section expanded">
              <div className="section-header" onClick={() => toggleSection('spenders')}>
                <h2>Top Spenders <span className="section-subtitle">(Most Bux Spent)</span></h2>
                <button className="expand-toggle">
                  <FiChevronUp size={20} />
                </button>
              </div>
              <div className="leaderboard-list">
                {leaderboard?.topSpenders.length === 0 ? (
                  <div className="empty-leaderboard">
                    <p>{leaderboard?.message || 'No one has spent any Bux yet!'}</p>
                    <p className="empty-subtitle">Visit the shop to purchase awesome rewards with your Bux.</p>
                  </div>
                ) : (
                  leaderboard?.topSpenders.map((entry: LeaderboardEntry) => (
                    <LeaderboardEntryComponent
                      key={entry.ninjaId}
                      entry={entry}
                      value={entry.totalBuxSpent}
                      valueLabel="$"
                      isTop={entry.isTopSpender}
                      topIcon="🏆"
                    />
                  ))
                )}
              </div>
            </div>
          )}

          {expandedSection === 'improved' && (
            <div className="leaderboard-section expanded">
              <div className="section-header" onClick={() => toggleSection('improved')}>
                <h2>Most Improved <span className="section-subtitle">(Lessons Advanced)</span></h2>
                <button className="expand-toggle">
                  <FiChevronUp size={20} />
                </button>
              </div>
              <div className="leaderboard-list">
                {leaderboard?.mostImproved && leaderboard.mostImproved.length > 0 ? (
                  leaderboard.mostImproved.map((entry: LeaderboardEntry) => (
                    <LeaderboardEntryComponent
                      key={entry.ninjaId}
                      entry={entry}
                      value={entry.totalBuxEarned}
                      valueLabel="lessons"
                      isTop={false}
                      topIcon={null}
                    />
                  ))
                ) : (
                  <div className="empty-leaderboard">
                    <p>{leaderboard?.message || 'No users qualify for this leaderboard'}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {expandedSection === 'quiz' && (
            <div className="leaderboard-section expanded">
              <div className="section-header" onClick={() => toggleSection('quiz')}>
                <h2>Quiz Champions <span className="section-subtitle">(Correct Answers)</span></h2>
                <button className="expand-toggle">
                  <FiChevronUp size={20} />
                </button>
              </div>
              <div className="leaderboard-list">
                {leaderboard?.quizChampions && leaderboard.quizChampions.length > 0 ? (
                  leaderboard.quizChampions.map((entry: LeaderboardEntry) => (
                    <LeaderboardEntryComponent
                      key={entry.ninjaId}
                      entry={entry}
                      value={entry.totalBuxEarned}
                      valueLabel="correct"
                      isTop={false}
                      topIcon={null}
                    />
                  ))
                ) : (
                  <div className="empty-leaderboard">
                    <p>{leaderboard?.message || 'No users qualify for this leaderboard'}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {expandedSection === 'streak' && (
            <div className="leaderboard-section expanded">
              <div className="section-header" onClick={() => toggleSection('streak')}>
                <h2>Streak Leaders <span className="section-subtitle">(Consecutive Sessions)</span></h2>
                <button className="expand-toggle">
                  <FiChevronUp size={20} />
                </button>
              </div>
              <div className="leaderboard-list">
                {leaderboard?.streakLeaders && leaderboard.streakLeaders.length > 0 ? (
                  leaderboard.streakLeaders.map((entry: LeaderboardEntry) => (
                    <LeaderboardEntryComponent
                      key={entry.ninjaId}
                      entry={entry}
                      value={entry.totalBuxEarned}
                      valueLabel="days"
                      isTop={false}
                      topIcon={null}
                    />
                  ))
                ) : (
                  <div className="empty-leaderboard">
                    <p>{leaderboard?.message || 'No users qualify for this leaderboard'}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Default view when nothing is expanded - show grid */}
          {!expandedSection && (
            <div className="leaderboard-sections">
              <div className="leaderboard-section collapsed">
                <div className="section-header" onClick={() => toggleSection('earners')}>
                  <h2>Top Earners <span className="section-subtitle">(Most Bux Earned)</span></h2>
                  <button className="expand-toggle">
                    <FiChevronDown size={20} />
                  </button>
                </div>
              </div>

              <div className="leaderboard-section collapsed">
                <div className="section-header" onClick={() => toggleSection('spenders')}>
                  <h2>Top Spenders <span className="section-subtitle">(Most Bux Spent)</span></h2>
                  <button className="expand-toggle">
                    <FiChevronDown size={20} />
                  </button>
                </div>
              </div>

              {(period === 'week' || period === 'daily') && (
                <>
                  <div className="leaderboard-section collapsed">
                    <div className="section-header" onClick={() => toggleSection('improved')}>
                      <h2>Most Improved <span className="section-subtitle">(Lessons Advanced)</span></h2>
                      <button className="expand-toggle">
                        <FiChevronDown size={20} />
                      </button>
                    </div>
                  </div>

                  <div className="leaderboard-section collapsed">
                    <div className="section-header" onClick={() => toggleSection('quiz')}>
                      <h2>Quiz Champions <span className="section-subtitle">(Correct Answers)</span></h2>
                      <button className="expand-toggle">
                        <FiChevronDown size={20} />
                      </button>
                    </div>
                  </div>

                  <div className="leaderboard-section collapsed">
                    <div className="section-header" onClick={() => toggleSection('streak')}>
                      <h2>Streak Leaders <span className="section-subtitle">(Consecutive Sessions)</span></h2>
                      <button className="expand-toggle">
                        <FiChevronDown size={20} />
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          )}
        </div>

        {/* Sidebar - collapsed sections as buttons */}
        {expandedSection && (
          <div className="leaderboard-sidebar">
            {expandedSection !== 'earners' && (
              <div className="leaderboard-section collapsed sidebar-button" onClick={() => toggleSection('earners')}>
                <div className="section-header">
                  <h2>Top Earners</h2>
                </div>
              </div>
            )}

            {expandedSection !== 'spenders' && (
              <div className="leaderboard-section collapsed sidebar-button" onClick={() => toggleSection('spenders')}>
                <div className="section-header">
                  <h2>Top Spenders</h2>
                </div>
              </div>
            )}

            {(period === 'week' || period === 'daily') && (
              <>
                {expandedSection !== 'improved' && (
                  <div className="leaderboard-section collapsed sidebar-button" onClick={() => toggleSection('improved')}>
                    <div className="section-header">
                      <h2>Most Improved</h2>
                    </div>
                  </div>
                )}

                {expandedSection !== 'quiz' && (
                  <div className="leaderboard-section collapsed sidebar-button" onClick={() => toggleSection('quiz')}>
                    <div className="section-header">
                      <h2>Quiz Champions</h2>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
