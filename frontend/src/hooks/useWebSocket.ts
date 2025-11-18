import { useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useToastContext } from '../context/ToastContext';

interface NotificationMessage {
  type: string;
  title?: string;
  message: string;
  ninjaId?: number;
  timestamp: string;
  data?: Record<string, unknown>;
}

export function useWebSocket(ninjaId: number | null, onLockStatusChange?: (isLocked: boolean, message: string) => void) {
  const clientRef = useRef<Client | null>(null);
  const { showToast, lessonComplete, levelUp, achievement, announcement, ninjaLevelUp, ninjaBeltUp } = useToastContext();

  const toastFunctionsRef = useRef({ showToast, lessonComplete, levelUp, achievement, announcement, ninjaLevelUp, ninjaBeltUp });

  useEffect(() => {
    toastFunctionsRef.current = { showToast, lessonComplete, levelUp, achievement, announcement, ninjaLevelUp, ninjaBeltUp };
  }, [showToast, lessonComplete, levelUp, achievement, announcement, ninjaLevelUp, ninjaBeltUp]);

  useEffect(() => {
    const connect = () => {
      try {
        const wsUrl = import.meta.env.DEV && window.location.hostname === 'localhost'
          ? `${window.location.protocol}//${window.location.host}/ws`
          : `${window.location.protocol}//${window.location.hostname}:8080/ws`;

        const client = new Client({
            webSocketFactory: () => new SockJS(wsUrl) as WebSocket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
            if (ninjaId) {
              const ninjaTopic = `/topic/ninja/${ninjaId}`;
              client.subscribe(ninjaTopic, (message) => {
                try {
                  const notification: NotificationMessage = JSON.parse(message.body);
                  handleNotification(notification);
                } catch (error) {
                  console.error('Failed to parse notification:', error);
                }
              });
            }

            client.subscribe('/topic/announcements', (message) => {
              try {
                const notification: NotificationMessage = JSON.parse(message.body);
                handleNotification(notification);
              } catch (error) {
                console.error('Failed to parse announcement:', error);
              }
            });
          },
          onStompError: (frame) => {
            console.error('STOMP error:', frame);
          },
          onWebSocketError: (error) => {
            console.error('WebSocket error:', error);
          },
          onDisconnect: () => {
            console.log('WebSocket disconnected');
          }
        });

        client.activate();
        clientRef.current = client;
      } catch (error) {
        console.error('Failed to connect WebSocket:', error);
      }
    };

    const handleNotification = (notification: NotificationMessage) => {
      // skip broadcast notification if its for the current ninja (they already got their personal one)
      if ((notification.type === 'NINJA_LEVEL_UP' || notification.type === 'NINJA_BELT_UP')) {
        if (ninjaId !== null && ninjaId !== undefined && notification.ninjaId === ninjaId) {
          return;
        }
      }

      const typeMap: Record<string, 'lesson-complete' | 'level-up' | 'achievement' | 'announcement' | 'ninja-level-up' | 'ninja-belt-up' | 'info' | 'error'> = {
        'LESSON_COMPLETE': 'lesson-complete',
        'LEVEL_UP': 'level-up',
        'ACHIEVEMENT': 'achievement',
        'ANNOUNCEMENT': 'announcement',
        'NINJA_LEVEL_UP': 'ninja-level-up',
        'NINJA_BELT_UP': 'ninja-belt-up',
        'INFO': 'info',
        'ERROR': 'error'
      };

      // Handle lock/unlock notifications
      if (notification.type === 'ACCOUNT_LOCKED') {
        if (onLockStatusChange) {
          onLockStatusChange(true, notification.message || 'Your account has been locked');
        }
        return; // Don't show toast for lock, use overlay instead
      }

      if (notification.type === 'ACCOUNT_UNLOCKED') {
        if (onLockStatusChange) {
          onLockStatusChange(false, '');
        }
        toastFunctionsRef.current.showToast(notification.message, 'info', notification.title);
        return;
      }

      const toastType = typeMap[notification.type] || 'info';

      // store announcements in local storage for offline mode
      if (notification.type === 'ANNOUNCEMENT') {
        try {
          const stored = localStorage.getItem('offlineAnnouncements');
          const announcements = stored ? JSON.parse(stored) : [];
          announcements.push({
            message: notification.message,
            title: notification.title || 'Announcement',
            timestamp: Date.now()
          });
          const recent = announcements.slice(-10);
          localStorage.setItem('offlineAnnouncements', JSON.stringify(recent));
        } catch (err) {
          console.error('Failed to store announcement:', err);
        }
      }

      switch (toastType) {
        case 'lesson-complete':
          toastFunctionsRef.current.lessonComplete(notification.message, notification.title);
          break;
        case 'level-up':
          toastFunctionsRef.current.levelUp(notification.message, notification.title);
          break;
        case 'achievement':
          toastFunctionsRef.current.achievement(notification.message, notification.title);
          break;
        case 'announcement':
          toastFunctionsRef.current.announcement(notification.message, notification.title);
          break;
        case 'ninja-level-up':
          toastFunctionsRef.current.ninjaLevelUp(notification.message, notification.title);
          break;
        case 'ninja-belt-up':
          toastFunctionsRef.current.ninjaBeltUp(notification.message, notification.title);
          break;
        default:
          toastFunctionsRef.current.showToast(notification.message, 'info', notification.title);
      }
    };

    connect();

    // Cleanup on unmount
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [ninjaId, onLockStatusChange]);
}
