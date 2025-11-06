import { FiAlertTriangle, FiX } from 'react-icons/fi';
import './ConfirmationModal.css';

interface ConfirmationModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  variant?: 'danger' | 'warning' | 'info';
}

export default function ConfirmationModal({
  isOpen,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  onConfirm,
  onCancel,
  variant = 'warning'
}: ConfirmationModalProps) {
  if (!isOpen) return null;

  return (
    <div className="confirmation-modal-overlay" onClick={onCancel}>
      <div className={`confirmation-modal confirmation-modal-${variant}`} onClick={(e) => e.stopPropagation()}>
        <div className="confirmation-modal-header">
          <div className="confirmation-modal-icon">
            <FiAlertTriangle size={24} />
          </div>
          <h3 className="confirmation-modal-title">{title}</h3>
          <button className="confirmation-modal-close" onClick={onCancel}>
            <FiX size={20} />
          </button>
        </div>
        <div className="confirmation-modal-body">
          <p>{message}</p>
        </div>
        <div className="confirmation-modal-footer">
          <button className="confirmation-modal-btn confirmation-modal-btn-cancel" onClick={onCancel}>
            {cancelText}
          </button>
          <button className={`confirmation-modal-btn confirmation-modal-btn-confirm confirmation-modal-btn-${variant}`} onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

