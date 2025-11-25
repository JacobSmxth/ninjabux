import React from 'react';

interface SectionHeaderProps {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}

/**
 * Standardized section header for dividing page content.
 * Used for repeated h2 + subtitle + optional action patterns.
 */
export default function SectionHeader({ title, subtitle, action }: SectionHeaderProps) {
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'flex-start',
      marginBottom: '1.5rem',
      paddingBottom: '1rem',
      borderBottom: '2px solid var(--border)',
    }}>
      <div>
        <h2 style={{
          margin: '0 0 0.5rem 0',
          fontSize: '1.5rem',
          fontWeight: 700,
          color: 'var(--text)',
          textTransform: 'uppercase',
          letterSpacing: '0.5px',
        }}>
          {title}
        </h2>
        {subtitle && (
          <p style={{
            margin: 0,
            fontSize: '0.9rem',
            color: 'var(--text-secondary)',
          }}>
            {subtitle}
          </p>
        )}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
}
