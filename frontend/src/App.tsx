import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
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
import type { Admin } from './types';
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

  const [selectedNinjaId, setSelectedNinjaId] = useState<number | null>(() => {
    const saved = localStorage.getItem('selectedNinjaId');
    return saved ? parseInt(saved) : null;
  });

  const [admin, setAdmin] = useState<Admin | null>(() => {
    const saved = localStorage.getItem('admin');
    return saved ? JSON.parse(saved) : null;
  });

  const handleLockStatusChange = (locked: boolean, message: string) => {
    setLockStatus(locked, message);
  };

  useEffect(() => {
    // check lock status on login and route changes
    if (selectedNinjaId) {
      checkLockStatus(selectedNinjaId);
      // also poll periodically because external changes happen
      const interval = setInterval(() => {
        checkLockStatus(selectedNinjaId);
      }, 5000); // check every 5 seconds
      return () => clearInterval(interval);
    } else {
      setLockStatus(false, '');
    }
  }, [selectedNinjaId, location.pathname, checkLockStatus, setLockStatus]);

  useEffect(() => {
    if (selectedNinjaId) {
      localStorage.setItem('selectedNinjaId', selectedNinjaId.toString());
      if (location.pathname !== '/' && !location.pathname.startsWith('/shop') && !location.pathname.startsWith('/leaderboard') && !location.pathname.startsWith('/achievements') && !location.pathname.startsWith('/quiz')) {
        navigate('/', { replace: true });
      }
    } else {
      localStorage.removeItem('selectedNinjaId');
    }
  }, [selectedNinjaId]);

  useEffect(() => {
    if (admin) {
      localStorage.setItem('admin', JSON.stringify(admin));
      if (!location.pathname.startsWith('/admin') && location.pathname !== '/admin-login') {
        navigate('/admin', { replace: true });
      }
    } else {
      localStorage.removeItem('admin');
    }
  }, [admin]);

  const handleLogout = () => {
    setSelectedNinjaId(null);
    localStorage.removeItem('selectedNinjaId');
    navigate('/', { replace: true });
  };

  const handleAdminLogout = () => {
    setAdmin(null);
    localStorage.removeItem('admin');
    navigate('/admin-login', { replace: true });
  };

  const switchToAdmin = () => {
    setSelectedNinjaId(null);
    navigate('/admin-login', { replace: true });
  };

  useWebSocket(selectedNinjaId, handleLockStatusChange);

  const isAdminRoute = location.pathname.startsWith('/admin') || location.pathname.startsWith('/admin-login');

  if (isAdminRoute) {
    if (!admin) {
      return (
        <>
          <ToastHost />
          <Routes>
            <Route path="/admin" element={<AdminLogin onLogin={setAdmin} />} />
            <Route path="/admin-login" element={<AdminLogin onLogin={setAdmin} />} />
            <Route path="*" element={<AdminLogin onLogin={setAdmin} />} />
          </Routes>
        </>
      );
    }
    return (
      <>
        <ToastHost />
        <Routes>
          <Route path="/admin" element={<AdminDashboard onLogout={handleAdminLogout} admin={admin} />} />
          <Route path="/admin/ninja/:id" element={<NinjaDetail />} />
          <Route path="*" element={<AdminDashboard onLogout={handleAdminLogout} admin={admin} />} />
        </Routes>
      </>
    );
  }

  if (!selectedNinjaId) {
    return (
      <>
        <ToastHost />
        <Login onLogin={setSelectedNinjaId} onSwitchToAdmin={switchToAdmin} />
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
          <Route path="/" element={<Dashboard ninjaId={selectedNinjaId} />} />
          <Route path="/achievements" element={<AchievementGallery ninjaId={selectedNinjaId} />} />
          <Route path="/shop" element={<Shop ninjaId={selectedNinjaId} />} />
          <Route path="/leaderboard" element={<Leaderboard />} />
          <Route path="/quiz" element={<Quiz ninjaId={selectedNinjaId} />} />
          <Route path="*" element={<Dashboard ninjaId={selectedNinjaId} />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <ToastProvider>
        <LockProvider>
          <AppContent />
        </LockProvider>
      </ToastProvider>
    </Router>
  );
}

export default App;
