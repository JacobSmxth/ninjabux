import { useEffect } from 'react';
import { useLockContext } from '../context/LockContext';

/**
 * Hook to set up automatic lock status polling for a ninja user.
 * Only activates when a ninja is authenticated.
 *
 * @param ninjaId - The ID of the ninja to poll (null if not a ninja or not authenticated)
 * @param pollingInterval - How often to check lock status in milliseconds (default: 5000ms)
 */
export function useLockPolling(ninjaId: number | null, pollingInterval: number = 5000) {
  const { checkLockStatus, setLockStatus } = useLockContext();

  useEffect(() => {
    if (!ninjaId) {
      setLockStatus(false, '');
      return;
    }

    // Check immediately on mount/change
    checkLockStatus(ninjaId);

    // Set up polling interval
    const interval = setInterval(() => {
      checkLockStatus(ninjaId);
    }, pollingInterval);

    return () => clearInterval(interval);
  }, [ninjaId, pollingInterval, checkLockStatus, setLockStatus]);
}
