import type { IconType } from 'react-icons';

interface EmptyStateProps {
  icon: IconType;
  title: string;
  hint?: string;
  action?: React.ReactNode;
  iconColor?: string;
  iconSize?: number;
}

/**
 * Standardized empty state display for "no items", "no results", etc.
 * Replaces repeated empty state blocks across pages.
 */
export default function EmptyState({
  icon: Icon,
  title,
  hint,
  action,
  iconColor = 'var(--accent)',
  iconSize = 48,
}: EmptyStateProps) {
  return (
    <div
      style={{
        textAlign: 'center',
        padding: '3rem 2rem',
        color: 'var(--text-secondary)',
      }}
    >
      <Icon
        size={iconSize}
        color={iconColor}
        style={{ marginBottom: '1rem', opacity: 0.6 }}
      />
      <h3
        style={{
          margin: '1rem 0 0.5rem 0',
          fontSize: '1.25rem',
          fontWeight: 700,
          color: 'var(--text)',
        }}
      >
        {title}
      </h3>
      {hint && (
        <p style={{ margin: '0.5rem 0 1.5rem 0', fontSize: '0.95rem' }}>
          {hint}
        </p>
      )}
      {action && <div>{action}</div>}
    </div>
  );
}
