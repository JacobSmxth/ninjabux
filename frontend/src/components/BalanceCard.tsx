import { FiDollarSign } from 'react-icons/fi';
import { getBeltTheme } from '../utils/beltTheme';
import { formatBux } from '../utils/format';

interface BalanceCardProps {
  balance: number;
  beltType: string;
  label?: string;
}

/**
 * Reusable balance/currency display card.
 * Applies belt theme colors for consistent styling.
 * Used in Dashboard and Shop pages.
 */
export default function BalanceCard({
  balance,
  beltType,
  label = 'Bux Balance',
}: BalanceCardProps) {
  const theme = getBeltTheme(beltType);

  return (
    <div
      className="card"
      style={{
        borderColor: theme.primary,
        borderWidth: '2px',
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
        padding: '1rem 1.8rem',
      }}
    >
      <div style={{ color: theme.primary }}>
        <FiDollarSign size={32} />
      </div>
      <div>
        <p
          style={{
            margin: 0,
            fontSize: '0.85rem',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            color: 'var(--text-secondary)',
          }}
        >
          {label}
        </p>
        <p
          style={{
            margin: '0.25rem 0 0 0',
            fontSize: '2rem',
            fontWeight: 900,
            color: theme.primary,
          }}
        >
          {formatBux(balance)}
        </p>
      </div>
    </div>
  );
}
