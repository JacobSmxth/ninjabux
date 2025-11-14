import { useState, useEffect } from 'react';
import type { ProgressHistory, ProgressHistoryCorrectionRequest, BeltType } from '../types';
import './ProgressHistoryEditModal.css';

interface Props {
  isOpen: boolean;
  originalEntry: ProgressHistory | null;
  onClose: () => void;
  onSave: (correction: ProgressHistoryCorrectionRequest) => Promise<void>;
}

const BELTS: BeltType[] = ['WHITE', 'YELLOW', 'ORANGE', 'GREEN', 'BLUE', 'PURPLE', 'RED', 'BROWN', 'BLACK'];

export default function ProgressHistoryEditModal({ isOpen, originalEntry, onClose, onSave }: Props) {
  const [formData, setFormData] = useState<ProgressHistoryCorrectionRequest>({
    originalEntryId: 0,
    beltType: 'WHITE',
    level: 1,
    lesson: 1,
    buxDelta: 0,
    legacyDelta: 0,
    notes: '',
    adminUsername: localStorage.getItem('adminUsername') || 'admin',
  });

  useEffect(() => {
    if (originalEntry) {
      setFormData({
        originalEntryId: originalEntry.id,
        beltType: originalEntry.beltType,
        level: originalEntry.level,
        lesson: originalEntry.lesson,
        buxDelta: 0,
        legacyDelta: 0,
        notes: '',
        adminUsername: localStorage.getItem('adminUsername') || 'admin',
      });
    }
  }, [originalEntry]);

  const getErrorMessage = (error: unknown): string | undefined => {
    if (error instanceof Error) {
      return error.message;
    }
    if (typeof error === 'object' && error !== null) {
      const maybeResponse = (error as { response?: { data?: { message?: string } } }).response;
      if (maybeResponse?.data && typeof maybeResponse.data === 'object' && 'message' in maybeResponse.data) {
        const message = (maybeResponse.data as { message?: string }).message;
        return message;
      }
    }
    return undefined;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.notes?.trim()) {
      alert('Please provide a reason/notes for this correction');
      return;
    }
    try {
      await onSave(formData);
      onClose();
    } catch (err: unknown) {
      const message = getErrorMessage(err) || 'Failed to create correction';
      alert(message);
      console.error(err);
    }
  };

  if (!isOpen || !originalEntry) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="progress-history-edit-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Create Progress History Correction</h2>
          <button onClick={onClose} className="btn-close">×</button>
        </div>
        <div className="modal-body">
          <div style={{ marginBottom: '1rem', padding: '1rem', background: '#f3f4f6', borderRadius: '8px' }}>
            <strong>Original Entry:</strong>
            <div style={{ marginTop: '0.5rem' }}>
              {originalEntry.beltType} Belt - Level {originalEntry.level}, Lesson {originalEntry.lesson}
              <br />
              {originalEntry.buxEarned >= 0 ? '+' : ''}{originalEntry.buxEarned} Bux
              {originalEntry.legacyDelta !== undefined && originalEntry.legacyDelta !== 0 && (
                <>, {originalEntry.legacyDelta >= 0 ? '+' : ''}{originalEntry.legacyDelta} Legacy</>
              )}
              <br />
              {new Date(originalEntry.timestamp).toLocaleString()}
            </div>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="form-grid">
              <div className="form-field">
                <label>Belt</label>
                <select
                  value={formData.beltType}
                  onChange={(e) => setFormData({ ...formData, beltType: e.target.value as BeltType })}
                  required
                >
                  {BELTS.map(belt => (
                    <option key={belt} value={belt}>{belt}</option>
                  ))}
                </select>
              </div>
              <div className="form-field">
                <label>Level</label>
                <input
                  type="number"
                  value={formData.level}
                  onChange={(e) => setFormData({ ...formData, level: parseInt(e.target.value) })}
                  min="1"
                  required
                />
              </div>
              <div className="form-field">
                <label>Lesson</label>
                <input
                  type="number"
                  value={formData.lesson}
                  onChange={(e) => setFormData({ ...formData, lesson: parseInt(e.target.value) })}
                  min="1"
                  required
                />
              </div>
              <div className="form-field">
                <label>Bux Delta</label>
                <input
                  type="number"
                  value={formData.buxDelta}
                  onChange={(e) => setFormData({ ...formData, buxDelta: parseInt(e.target.value) || 0 })}
                  placeholder="+/- Bux adjustment"
                />
              </div>
              <div className="form-field">
                <label>Legacy Delta</label>
                <input
                  type="number"
                  value={formData.legacyDelta || ''}
                  onChange={(e) => setFormData({ ...formData, legacyDelta: e.target.value ? parseInt(e.target.value) : undefined })}
                  placeholder="+/- Legacy adjustment"
                />
              </div>
              <div className="form-field" style={{ gridColumn: '1 / -1' }}>
                <label>Notes/Reason *</label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  placeholder="Explain why this correction is needed..."
                  rows={3}
                  required
                />
              </div>
              <div className="form-field">
                <label>Admin Username</label>
                <input
                  type="text"
                  value={formData.adminUsername}
                  onChange={(e) => setFormData({ ...formData, adminUsername: e.target.value })}
                  required
                />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-primary">Create Correction</button>
              <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
