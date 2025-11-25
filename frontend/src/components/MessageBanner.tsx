type MessageType = 'success' | 'error' | 'info' | 'warning';

interface MessageBannerProps {
  message: string;
  type: MessageType;
  onClose?: () => void;
}

/**
 * Reusable message/notification banner.
 * Replaces repeated inline error/success/info message blocks.
 */
export default function MessageBanner({ message, type, onClose }: MessageBannerProps) {
  const styles: Record<MessageType, { bg: string; color: string; border: string }> = {
    success: {
      bg: '#d4edda',
      color: '#155724',
      border: '#28a745',
    },
    error: {
      bg: '#f8d7da',
      color: '#721c24',
      border: '#f5c6cb',
    },
    info: {
      bg: '#d1ecf1',
      color: '#0c5460',
      border: '#bee5eb',
    },
    warning: {
      bg: '#fff3cd',
      color: '#856404',
      border: '#ffeeba',
    },
  };

  const style = styles[type];

  return (
    <div
      style={{
        padding: '1rem',
        borderRadius: '8px',
        marginBottom: '1rem',
        fontWeight: 700,
        textAlign: 'center',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
        background: style.bg,
        color: style.color,
        border: `2px solid ${style.border}`,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <span>{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          style={{
            background: 'transparent',
            border: 'none',
            color: 'inherit',
            cursor: 'pointer',
            fontSize: '1.2rem',
            padding: 0,
            marginLeft: '1rem',
          }}
        >
          ×
        </button>
      )}
    </div>
  );
}
