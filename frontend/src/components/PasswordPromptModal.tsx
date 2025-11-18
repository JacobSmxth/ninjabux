import { useEffect, useState } from 'react';
import { FiLock } from 'react-icons/fi';
import './PasswordPromptModal.css';

interface PasswordPromptModalProps {
  isOpen: boolean;
  title: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  onSubmit: (password: string) => Promise<void> | void;
  onClose: () => void;
}

export default function PasswordPromptModal({
  isOpen,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  onSubmit,
  onClose
}: PasswordPromptModalProps) {
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isOpen) {
      setPassword('');
      setError(null);
      setSubmitting(false);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!password.trim()) {
      setError('Password is required');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await onSubmit(password);
      onClose();
    } catch (err) {
      const message = err instanceof Error && err.message ? err.message : 'Failed to verify password';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!submitting) {
      onClose();
    }
  };

  return (
    <div className="password-prompt-overlay" onClick={handleClose}>
      <div className="password-prompt-modal" onClick={(e) => e.stopPropagation()}>
        <div className="password-prompt-header">
          <div className="password-prompt-icon">
            <FiLock size={18} />
          </div>
          <h3>{title}</h3>
        </div>
        {message && <p className="password-prompt-message">{message}</p>}
        <form onSubmit={handleSubmit}>
          <label htmlFor="admin-password-input">Admin Password</label>
          <input
            id="admin-password-input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter password"
            autoFocus
            disabled={submitting}
          />
          {error && <div className="password-prompt-error">{error}</div>}
          <div className="password-prompt-actions">
            <button type="button" className="password-prompt-btn password-prompt-btn-secondary" onClick={handleClose} disabled={submitting}>
              {cancelText}
            </button>
            <button type="submit" className="password-prompt-btn password-prompt-btn-primary" disabled={submitting}>
              {submitting ? 'Verifying...' : confirmText}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
