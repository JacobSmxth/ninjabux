import type { IconType } from 'react-icons';
import {
  FiAward,
  FiTarget,
  FiStar,
  FiTrendingUp,
  FiZap,
  FiClock,
  FiDollarSign,
  FiCheck,
  FiBook,
  FiShoppingBag,
  FiUsers,
  FiHelpCircle,
} from 'react-icons/fi';

const NAMED_ICON_MAP: Record<string, IconType> = {
  target: FiTarget,
  bullseye: FiTarget,
  accuracy: FiTarget,
  star: FiStar,
  shine: FiStar,
  sparkle: FiStar,
  trend: FiTrendingUp,
  trending: FiTrendingUp,
  progress: FiTrendingUp,
  zap: FiZap,
  lightning: FiZap,
  bolt: FiZap,
  energy: FiZap,
  clock: FiClock,
  time: FiClock,
  timer: FiClock,
  speed: FiClock,
  award: FiAward,
  trophy: FiAward,
  medal: FiAward,
  ribbon: FiAward,
  badge: FiAward,
  book: FiBook,
  study: FiBook,
  knowledge: FiBook,
  dollar: FiDollarSign,
  money: FiDollarSign,
  bux: FiDollarSign,
  coin: FiDollarSign,
  cash: FiDollarSign,
  shopping: FiShoppingBag,
  store: FiShoppingBag,
  bag: FiShoppingBag,
  partner: FiUsers,
  teamwork: FiUsers,
  collab: FiUsers,
  check: FiCheck,
  success: FiCheck,
  complete: FiCheck,
  verify: FiCheck,
  light: FiZap,
  idea: FiZap,
  question: FiHelpCircle,
};

const EMOJI_ICON_MAP: Record<string, IconType> = {
  '🎯': FiTarget,
  '📚': FiBook,
  '💯': FiTrendingUp,
  '🥋': FiAward,
  '🟠': FiStar,
  '🟢': FiStar,
  '🔵': FiStar,
  '❓': FiHelpCircle,
  '💎': FiStar,
  '💰': FiDollarSign,
  '💸': FiDollarSign,
  '🏆': FiAward,
  '🛍️': FiShoppingBag,
  '🤝': FiUsers,
  '💡': FiZap,
  '👑': FiAward,
  '🔥': FiZap,
  '⚡': FiZap,
  '⚡️': FiZap,
  '⭐': FiStar,
  '🌟': FiStar,
  '✅': FiCheck,
  '☑️': FiCheck,
  '🎖️': FiAward,
  '🥇': FiAward,
  '🥈': FiAward,
  '🥉': FiAward,
};

const sanitize = (value?: string | null) => value?.trim().toLowerCase().replace(/[\s_-]+/g, '') ?? '';

const resolveIcon = (iconName?: string | null): IconType => {
  if (!iconName) return FiAward;

  if (EMOJI_ICON_MAP[iconName]) {
    return EMOJI_ICON_MAP[iconName];
  }

  const normalized = sanitize(iconName);
  if (!normalized) return FiAward;

  if (NAMED_ICON_MAP[normalized]) {
    return NAMED_ICON_MAP[normalized];
  }

  // Support strings like "FiTarget" by trimming the prefix
  if (normalized.startsWith('fi')) {
    const trimmed = normalized.slice(2);
    if (NAMED_ICON_MAP[trimmed]) {
      return NAMED_ICON_MAP[trimmed];
    }
  }

  return FiAward;
};

interface AchievementIconProps {
  icon?: string | null;
  size?: number;
  className?: string;
}

export default function AchievementIcon({ icon, size = 16, className }: AchievementIconProps) {
  const IconComponent = resolveIcon(icon);
  return <IconComponent size={size} className={className} />;
}
