import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect, useCallback } from 'react';
import Login from './pages/Login';
import AdminLogin from './pages/AdminLogin';
import AdminDashboard from './pages/AdminDashboard';
import NinjaDetail from './pages/NinjaDetail';
import Dashboard from './pages/Dashboard';
import Shop from './pages/Shop';
import Leaderboard from './pages/Leaderboard';
import AchievementGallery from './pages/AchievementGallery';
import Quiz from './pages/Quiz';
import Toast from './components/Toast';
import { useToastContext } from './context/ToastContext';
import { useWebSocket } from './hooks/useWebSocket';
import { ToastProvider } from './context/ToastContext';
import { LockProvider, useLockContext } from './context/LockContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { FiLock } from 'react-icons/fi';
import './App.css';

function ToastHost() {
  const { toasts, removeToast } = useToastContext();

  if (toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          message={toast.message}
          type={toast.type}
          title={toast.title}
          duration={toast.duration}
          onClose={() => removeToast(toast.id)}
        />
      ))}
    </div>
  );
}

function AppContent() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isLocked, lockMessage, setLockStatus, checkLockStatus } = useLockContext();
  const { isAuthenticated, userId, logout: authLogout, userType } = useAuth();

  const handleLockStatusChange = useCallback((locked: boolean, message: string) => {
    setLockStatus(locked, message);
  }, [setLockStatus]);

  useEffect(() => {
    if (userId && userType === 'NINJA') {
      checkLockStatus(userId);
      const interval = setInterval(() => {
        checkLockStatus(userId);
      }, 5000);
      return () => clearInterval(interval);
    } else {
      setLockStatus(false, '');
    }
  }, [userId, userType, location.pathname, checkLockStatus, setLockStatus]);

  const handleLogout = () => {
    authLogout();
  };

  const switchToAdmin = () => {
    navigate('/admin-login', { replace: true });
  };

  useWebSocket(userType === 'NINJA' ? userId : null, handleLockStatusChange);

  if (!isAuthenticated && (location.pathname === '/' || location.pathname === '/admin-login')) {
    return (
      <>
        <ToastHost />
        <Routes>
          <Route path="/" element={<Login onSwitchToAdmin={switchToAdmin} />} />
          <Route path="/admin-login" element={<AdminLogin />} />
        </Routes>
      </>
    );
  }

  if (!isAuthenticated) {
    return (
      <>
        <ToastHost />
        <Login onSwitchToAdmin={switchToAdmin} />
      </>
    );
  }

  if (userType === 'ADMIN') {
    return (
      <>
        <ToastHost />
        <Routes>
          <Route path="/admin" element={
            <ProtectedRoute requireAdmin>
              <AdminDashboard onLogout={handleLogout} />
            </ProtectedRoute>
          } />
          <Route path="/admin/ninja/:id" element={
            <ProtectedRoute requireAdmin>
              <NinjaDetail />
            </ProtectedRoute>
          } />
          <Route path="*" element={
            <ProtectedRoute requireAdmin>
              <AdminDashboard onLogout={handleLogout} />
            </ProtectedRoute>
          } />
        </Routes>
      </>
    );
  }

  return (
    <div className="app">
      <ToastHost />

      {/* Lock Overlay - covers entire screen */}
      {isLocked && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.95)',
          zIndex: 9999,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexDirection: 'column',
          color: '#fff',
          fontSize: '1.5rem',
          textAlign: 'center',
          padding: '2rem',
          pointerEvents: 'all',
          cursor: 'not-allowed'
        }}>
          <FiLock size={64} style={{ marginBottom: '1rem', color: '#ef4444' }} />
          <h2 style={{ marginBottom: '1rem', fontSize: '2rem', fontWeight: 600 }}>Account Locked</h2>
                 <p style={{ fontSize: '1.25rem', maxWidth: '600px', lineHeight: '1.6' }}>
                   {lockMessage}
                 </p>
        </div>
      )}

      <nav className="navbar" style={{ opacity: isLocked ? 0.3 : 1, pointerEvents: isLocked ? 'none' : 'auto' }}>
        <div className="nav-container">
          <h1 className="app-title">
            <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" className="app-logo" />
            NinjaBux
          </h1>
          <div className="nav-links">
            <Link to="/" className="nav-link">Dashboard</Link>
            <Link to="/achievements" className="nav-link">Achievements</Link>
            <Link to="/shop" className="nav-link">Shop</Link>
            <Link to="/leaderboard" className="nav-link">Leaderboard</Link>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </div>
        </div>
      </nav>
      <main className="main-content" style={{ opacity: isLocked ? 0.3 : 1, pointerEvents: isLocked ? 'none' : 'auto' }}>
        <Routes>
          <Route path="/" element={
            <ProtectedRoute>
              <Dashboard ninjaId={userId!} />
            </ProtectedRoute>
          } />
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <Dashboard ninjaId={userId!} />
            </ProtectedRoute>
          } />
          <Route path="/achievements" element={
            <ProtectedRoute>
              <AchievementGallery ninjaId={userId!} />
            </ProtectedRoute>
          } />
          <Route path="/shop" element={
            <ProtectedRoute>
              <Shop ninjaId={userId!} />
            </ProtectedRoute>
          } />
          <Route path="/leaderboard" element={
            <ProtectedRoute>
              <Leaderboard />
            </ProtectedRoute>
          } />
          <Route path="/quiz" element={
            <ProtectedRoute>
              <Quiz ninjaId={userId!} />
            </ProtectedRoute>
          } />
          <Route path="*" element={
            <ProtectedRoute>
              <Dashboard ninjaId={userId!} />
            </ProtectedRoute>
          } />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <ToastProvider>
          <LockProvider>
            <AppContent />
          </LockProvider>
        </ToastProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
