import { useState, useEffect } from 'react';
import { ninjaApi } from '../services/api';
import type { Ninja } from '../types';
import './NinjaSelector.css';

interface Props {
  onSelectNinja: (id: number) => void;
}

export default function NinjaSelector({ onSelectNinja }: Props) {
  const [ninjas, setNinjas] = useState<Ninja[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadNinjas();
  }, []);

  const loadNinjas = async () => {
    try {
      setLoading(true);
      const data = await ninjaApi.getAll();
      setNinjas(data);
      setError('');
    } catch (err) {
      setError('Failed to load ninjas. Make sure the backend is running!');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="selector-container">
        <div className="selector-card">
          <h1>Loading...</h1>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="selector-container">
        <div className="selector-card">
          <h1>NinjaBux</h1>
          <p className="error">{error}</p>
          <button onClick={loadNinjas} className="retry-btn">Retry</button>
        </div>
      </div>
    );
  }

  return (
    <div className="selector-container">
      <div className="selector-card">
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" style={{ height: '60px', width: 'auto' }} />
        </div>
        <h1 className="selector-title">Select Your Ninja</h1>
        {ninjas.length === 0 ? (
          <div className="no-ninjas">
            <p>No ninjas found. Create one first!</p>
            <p className="help-text">
              Use the API to create a ninja:<br />
              POST http://localhost:8080/api/ninjas
            </p>
          </div>
        ) : (
          <div className="ninja-grid">
            {ninjas.map((ninja) => (
              <div
                key={ninja.id}
                className="ninja-card"
                onClick={() => onSelectNinja(ninja.id)}
              >
                <div className={`belt-badge belt-${ninja.currentBeltType.toLowerCase()}`}>
                  {ninja.currentBeltType}
                </div>
                <h2>{ninja.firstName} {ninja.lastName}</h2>
                <p className="ninja-balance">{ninja.buxBalance.toFixed(2)} Bux</p>
                <p className="ninja-progress">
                  Level {ninja.currentLevel} • Lesson {ninja.currentLesson}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
