import AchievementIcon from './AchievementIcon';
import type { Achievement } from '../types';

interface AchievementCardProps {
  achievement: Achievement;
  onClick?: () => void;
  size?: 'small' | 'medium' | 'large';
}

/**
 * Reusable achievement/badge card for displaying earned or available badges.
 * Used across AchievementGallery and Dashboard achievement sections.
 */
export default function AchievementCard({
  achievement,
  onClick,
  size = 'medium',
}: AchievementCardProps) {
  const sizeMap = {
    small: { card: '6rem', icon: 3 },
    medium: { card: '8rem', icon: 4 },
    large: { card: '10rem', icon: 5 },
  };

  const dimensions = sizeMap[size];

  return (
    <div
      className="card"
      onClick={onClick}
      style={{
        cursor: onClick ? 'pointer' : 'default',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        width: dimensions.card,
        height: dimensions.card,
        padding: '0.75rem',
      }}
    >
      <AchievementIcon
        badgeName={achievement.name}
        badgeRarity={achievement.rarity}
        size={dimensions.icon}
      />
      <p
        style={{
          margin: '0.5rem 0 0 0',
          fontSize: '0.75rem',
          fontWeight: 700,
          textAlign: 'center',
          color: 'var(--text-secondary)',
          textTransform: 'uppercase',
          letterSpacing: '0.2px',
        }}
      >
        {achievement.name}
      </p>
    </div>
  );
}
