import { useState, useCallback, useEffect } from 'react';
import type { ToastType } from '../components/Toast';

interface ToastItem {
  id: number;
  message: string;
  type: ToastType;
  title?: string;
  duration?: number;
}

export function useToast() {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const showToast = useCallback((message: string, type: ToastType = 'info', title?: string, duration?: number) => {
    const id = Date.now() + Math.random();
    setToasts(prev => {
      const newToasts = [...prev, { id, message, type, title, duration }];
      return newToasts;
    });
  }, []);

  const removeToast = useCallback((id: number) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  }, []);

  const success = useCallback((message: string, title?: string) => showToast(message, 'success', title), [showToast]);
  const error = useCallback((message: string, title?: string) => showToast(message, 'error', title), [showToast]);
  const info = useCallback((message: string, title?: string) => showToast(message, 'info', title), [showToast]);
  const lessonComplete = useCallback((message: string, title?: string) => showToast(message, 'lesson-complete', title, 5000), [showToast]);
  const levelUp = useCallback((message: string, title?: string) => showToast(message, 'level-up', title, 6000), [showToast]);
  const achievement = useCallback((message: string, title?: string) => showToast(message, 'achievement', title, 6000), [showToast]);
  const announcement = useCallback((message: string, title?: string) => showToast(message, 'announcement', title, 5000), [showToast]);
  const ninjaLevelUp = useCallback((message: string, title?: string) => showToast(message, 'ninja-level-up', title, 5000), [showToast]);
  const ninjaBeltUp = useCallback((message: string, title?: string) => showToast(message, 'ninja-belt-up', title, 5000), [showToast]);

  useEffect(() => {
    const storedAnnouncements = localStorage.getItem('offlineAnnouncements');
    if (storedAnnouncements) {
      try {
        const announcements = JSON.parse(storedAnnouncements);
        announcements.forEach((ann: { message: string; title?: string; timestamp: number }) => {
          if (Date.now() - ann.timestamp < 24 * 60 * 60 * 1000) {
            showToast(ann.message, 'announcement', ann.title || 'Announcement', 5000);
          }
        });
        localStorage.removeItem('offlineAnnouncements');
      } catch (err) {
        console.error('Failed to load offline announcements:', err);
      }
    }
  }, [showToast]);

  return {
    toasts,
    showToast,
    removeToast,
    success,
    error,
    info,
    lessonComplete,
    levelUp,
    achievement,
    announcement,
    ninjaLevelUp,
    ninjaBeltUp
  };
}
