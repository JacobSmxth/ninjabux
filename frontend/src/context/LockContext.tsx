import { createContext, useContext, useState, useCallback, ReactNode } from 'react';

interface LockContextType {
  isLocked: boolean;
  lockMessage: string;
  setLockStatus: (isLocked: boolean, message: string) => void;
  checkLockStatus: (ninjaId: number) => Promise<void>;
}

const LockContext = createContext<LockContextType | undefined>(undefined);

export function LockProvider({ children }: { children: ReactNode }) {
  const [isLocked, setIsLocked] = useState(false);
  const [lockMessage, setLockMessage] = useState('Your account is locked. Please get back to work!');

  const setLockStatus = useCallback((locked: boolean, message: string) => {
    setIsLocked(locked);
    setLockMessage(message || 'Your account is locked. Please get back to work!');
  }, []);

  const checkLockStatus = useCallback(async (ninjaId: number) => {
    try {
      const { ninjaApi } = await import('../services/api');
      const ninja = await ninjaApi.getById(ninjaId);
      if (ninja.isLocked) {
        setIsLocked(true);
        setLockMessage(ninja.lockReason || 'Your account is locked. Please get back to work!');
      } else {
        setIsLocked(false);
      }
    } catch (err: any) {
      console.error('Failed to check lock status:', err);
      // 403 means locked, handle it
      if (err.response?.status === 403 || err.message?.includes('Account is locked')) {
        const errorMsg = err.response?.data?.message || err.message || 'Your account is locked. Please get back to work!';
        setIsLocked(true);
        setLockMessage(errorMsg);
      } else {
        // on other errors assume unlocked so we don't block legitimate users
        setIsLocked(false);
      }
    }
  }, []);

  return (
    <LockContext.Provider value={{ isLocked, lockMessage, setLockStatus, checkLockStatus }}>
      {children}
    </LockContext.Provider>
  );
}

export function useLockContext() {
  const context = useContext(LockContext);
  if (!context) {
    throw new Error('useLockContext must be used within LockProvider');
  }
  return context;
}

