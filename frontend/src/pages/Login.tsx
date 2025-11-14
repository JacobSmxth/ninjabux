import { useState } from 'react';
import { ninjaApi } from '../services/api';
import './Login.css';

interface Props {
  onLogin: (id: number) => void;
  onSwitchToAdmin: () => void;
}

export default function Login({ onLogin, onSwitchToAdmin }: Props) {
  const [username, setUsername] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const trimmedUsername = username.trim();
    if (!trimmedUsername) {
      setError('Please enter your username');
      return;
    }

    try {
      setLoading(true);
      const ninja = await ninjaApi.loginByUsername(trimmedUsername);
      onLogin(ninja.id);
    } catch (error) {
      if (error instanceof Error && 'response' in error && (error as any).response?.status === 404) {
        setError(`No ninja found with username: ${trimmedUsername}`);
      } else if ((error as any)?.response?.status === 403 || (error as Error).message?.includes('Account is locked')) {
        const responseMessage = (error as any)?.response?.data?.message;
        const errorMsg = responseMessage || (error as Error).message || 'Your account is locked. Please get back to work!';
        setError(errorMsg);
      } else {
        setError('Failed to connect to server. Make sure the backend is running!');
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
          <h1 className="login-title">NinjaBux</h1>
          <p className="login-subtitle">Student Login</p>
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

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

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
      </div>
    </div>
  );
}
