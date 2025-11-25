import React from 'react';

interface PageLayoutProps {
  title: string;
  eyebrow?: string;
  actions?: React.ReactNode;
  children: React.ReactNode;
  containerClass?: string;
  containerStyle?: React.CSSProperties;
}

/**
 * Standardized page layout wrapper.
 * Provides consistent padding, header styling, and token-based appearance.
 * Uses belt tier tokens (--surface, --card, --text, --accent) for automatic theming.
 */
export default function PageLayout({
  title,
  eyebrow,
  actions,
  children,
  containerClass = '',
  containerStyle
}: PageLayoutProps) {
  return (
    <div className={`page-container ${containerClass}`} style={containerStyle}>
      <div className="page-header">
        <div>
          {eyebrow && <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>{eyebrow}</p>}
          <h1 className="page-title">{title}</h1>
        </div>
        {actions && <div>{actions}</div>}
      </div>
      <div style={{ position: 'relative', zIndex: 1 }}>
        {children}
      </div>
    </div>
  );
}
