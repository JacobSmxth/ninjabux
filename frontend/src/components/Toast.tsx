import { useEffect } from 'react';
import { FiCheckCircle, FiAlertCircle, FiInfo, FiX, FiAward, FiTrendingUp, FiZap, FiBell } from 'react-icons/fi';
import confetti from 'canvas-confetti';
import './Toast.css';

export type ToastType = 'success' | 'error' | 'info' | 'lesson-complete' | 'level-up' | 'achievement' | 'announcement' | 'ninja-level-up' | 'ninja-belt-up';

export interface ToastProps {
  message: string;
  type: ToastType;
  onClose: () => void;
  duration?: number;
  title?: string;
}

export default function Toast({ message, type, onClose, duration = 4000, title }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose]);

  useEffect(() => {
    if (type === 'ninja-level-up' || type === 'ninja-belt-up') {
      const duration = 2000;
      const animationEnd = Date.now() + duration;
      const defaults = { startVelocity: 30, spread: 360, ticks: 60, zIndex: 9999 };

      function randomInRange(min: number, max: number) {
        return Math.random() * (max - min) + min;
      }

      const interval: any = setInterval(function() {
        const timeLeft = animationEnd - Date.now();

        if (timeLeft <= 0) {
          return clearInterval(interval);
        }

        const particleCount = 50 * (timeLeft / duration);
        
        confetti({
          ...defaults,
          particleCount,
          origin: { x: randomInRange(0.1, 0.3), y: Math.random() - 0.2 }
        });
        confetti({
          ...defaults,
          particleCount,
          origin: { x: randomInRange(0.7, 0.9), y: Math.random() - 0.2 }
        });
      }, 250);

      return () => {
        if (interval) {
          clearInterval(interval);
        }
      };
    }
  }, [type]);

  const icons = {
    success: <FiCheckCircle size={22} />,
    error: <FiAlertCircle size={22} />,
    info: <FiInfo size={22} />,
    'lesson-complete': <FiCheckCircle size={22} />,
    'level-up': <FiTrendingUp size={22} />,
    'achievement': <FiAward size={22} />,
    'announcement': <FiBell size={22} />,
    'ninja-level-up': <FiTrendingUp size={22} />,
    'ninja-belt-up': <FiAward size={22} />
  };

  return (
    <div className={`toast toast-${type}`}>
      <div className="toast-icon">{icons[type]}</div>
      <div className="toast-content">
        {title && <div className="toast-title">{title}</div>}
        <div className="toast-message">{message}</div>
      </div>
      <button className="toast-close" onClick={onClose}>
        <FiX size={18} />
      </button>
    </div>
  );
}
