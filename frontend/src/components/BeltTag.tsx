import { getBeltTheme } from '../utils/beltTheme';

interface BeltTagProps {
  beltType: string;
  label?: string;
  size?: 'small' | 'medium' | 'large';
}

/**
 * Reusable belt badge component.
 * Displays belt name with themed color and styling.
 * Used across Dashboard, Leaderboard, ninja cards.
 */
export default function BeltTag({ beltType, label, size = 'medium' }: BeltTagProps) {
  const theme = getBeltTheme(beltType);

  const sizeMap = {
    small: { padding: '0.25rem 0.75rem', fontSize: '0.75rem' },
    medium: { padding: '0.5rem 1rem', fontSize: '0.9rem' },
    large: { padding: '0.75rem 1.5rem', fontSize: '1rem' },
  };

  const style = sizeMap[size];

  return (
    <span
      style={{
        display: 'inline-block',
        background: theme.primary,
        color: theme.primary === '#ffffff' ? '#000000' : '#ffffff',
        padding: style.padding,
        borderRadius: '8px',
        fontSize: style.fontSize,
        fontWeight: 700,
        textTransform: 'uppercase',
        letterSpacing: '0.3px',
        border: `2px solid ${theme.primary}`,
      }}
    >
      {label || `${beltType} Belt`}
    </span>
  );
}
