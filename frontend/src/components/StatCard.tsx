import React from 'react';

interface StatCardProps {
  label: string;
  value: string | number;
  unit?: string;
  icon?: React.ReactNode;
  highlight?: boolean;
  onClick?: () => void;
}

/**
 * Reusable stat card for displaying key metrics.
 * Used across Dashboard, Leaderboard, Analytics pages.
 */
export default function StatCard({
  label,
  value,
  unit,
  icon,
  highlight = false,
  onClick,
}: StatCardProps) {
  return (
    <div
      className="card"
      onClick={onClick}
      style={{
        cursor: onClick ? 'pointer' : 'default',
        borderColor: highlight ? 'var(--accent)' : undefined,
        borderWidth: highlight ? '2px' : '1px',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.3px', fontWeight: 600 }}>
          {label}
        </span>
        {icon && <span style={{ color: 'var(--accent)' }}>{icon}</span>}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.25rem' }}>
        <span style={{ fontSize: '2rem', fontWeight: 900, color: 'var(--text)' }}>
          {value}
        </span>
        {unit && (
          <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
            {unit}
          </span>
        )}
      </div>
    </div>
  );
}
