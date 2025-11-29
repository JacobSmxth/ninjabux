import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import '../pages/Login.css';

type AuthRole = 'NINJA' | 'ADMIN';

interface AuthCardProps {
  role: AuthRole;
  onSwitchToAdmin?: () => void;
}

export default function AuthCard({ role, onSwitchToAdmin }: AuthCardProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginNinja, loginAdmin } = useAuth();
  const navigate = useNavigate();

  const isAdmin = role === 'ADMIN';
  const title = isAdmin ? 'Admin' : 'NinjaBux';
  const subtitle = isAdmin ? 'Code Sensei Login' : 'Student Login';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const trimmedUsername = username.trim();

    if (isAdmin) {
      if (!trimmedUsername || !password.trim()) {
        setError('Please enter both username and password');
        return;
      }
    } else {
      if (!trimmedUsername) {
        setError('Please enter your username');
        return;
      }
    }

    try {
      setLoading(true);
      if (isAdmin) {
        await loginAdmin(trimmedUsername, password);
        navigate('/admin');
      } else {
        await loginNinja(trimmedUsername);
        navigate('/dashboard');
      }
    } catch (error) {
      const err = error as { response?: { status?: number }; message?: string };
      if (err?.response?.status === 401) {
        setError(
          isAdmin
            ? 'Invalid username or password'
            : `No ninja found with username: ${trimmedUsername} or account is locked`
        );
      } else {
        setError(
          isAdmin
            ? 'Failed to connect to server'
            : 'Failed to connect to server. Make sure the backend is running!'
        );
      }
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" className="login-logo" />
          <h1 className="login-title">{title}</h1>
          <p className="login-subtitle">{subtitle}</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="username"
              className="form-input"
              autoFocus
              disabled={loading}
            />
          </div>

          {isAdmin && (
            <div className="form-group">
              <input
                type="password"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="password"
                className="form-input"
                disabled={loading}
              />
            </div>
          )}

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        {!isAdmin && (
          <div className="login-footer">
            <p>Don't have an account? Ask your Code Sensei!</p>
            <button
              type="button"
              onClick={onSwitchToAdmin}
              style={{
                background: 'transparent',
                border: 'none',
                color: '#fcfdff',
                cursor: 'pointer',
                textDecoration: 'underline',
                marginTop: '1rem',
                fontSize: '0.9rem',
              }}
            >
              Admin Login
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
