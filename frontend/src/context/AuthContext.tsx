import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService } from '../services/authService';
import { useNavigate } from 'react-router-dom';

interface AuthContextType {
  isAuthenticated: boolean;
  userType: 'NINJA' | 'ADMIN' | null;
  userId: number | null;
  username: string | null;
  loginNinja: (username: string) => Promise<void>;
  loginAdmin: (username: string, password: string) => Promise<void>;
  logout: () => void;
  resetInactivityTimer: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const INACTIVITY_TIMEOUT = 15 * 60 * 1000;

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(authService.isAuthenticated());
  const [userInfo, setUserInfo] = useState(authService.getUserInfo());
  const navigate = useNavigate();
  const [lastActivity, setLastActivity] = useState(Date.now());

  const resetInactivityTimer = () => {
    setLastActivity(Date.now());
  };

  const logout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUserInfo(null);
    navigate('/');
  };

  useEffect(() => {
    const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];

    const handleActivity = () => {
      resetInactivityTimer();
    };

    events.forEach(event => {
      document.addEventListener(event, handleActivity);
    });

    return () => {
      events.forEach(event => {
        document.removeEventListener(event, handleActivity);
      });
    };
  }, []);

  useEffect(() => {
    if (!isAuthenticated) return;

    const interval = setInterval(() => {
      const now = Date.now();
      const timeSinceLastActivity = now - lastActivity;

      if (timeSinceLastActivity >= INACTIVITY_TIMEOUT) {
        logout();
      }
    }, 60000);

    return () => clearInterval(interval);
  }, [isAuthenticated, lastActivity]);

  const loginNinja = async (username: string) => {
    const response = await authService.loginNinja(username);
    setIsAuthenticated(true);
    setUserInfo({
      userId: response.userId,
      username: response.username,
      type: response.type
    });
    resetInactivityTimer();
  };

  const loginAdmin = async (username: string, password: string) => {
    const response = await authService.loginAdmin(username, password);
    setIsAuthenticated(true);
    setUserInfo({
      userId: response.userId,
      username: response.username,
      type: response.type
    });
    resetInactivityTimer();
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        userType: userInfo?.type || null,
        userId: userInfo?.userId || null,
        username: userInfo?.username || null,
        loginNinja,
        loginAdmin,
        logout,
        resetInactivityTimer
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
