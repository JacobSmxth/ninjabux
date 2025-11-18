import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ninjaApi, shopApi, achievementApi } from '../services/api';
import type { Ninja, Purchase, BeltType, ProgressHistory, AchievementProgress, ProgressHistoryCorrectionRequest } from '../types';
import { beltOrder, getMaxLessonsForLevel, getMaxLevelsForBelt } from '../utils/ninjaProgress';
import ConfirmationModal from '../components/ConfirmationModal';
import ProgressHistoryEditModal from '../components/ProgressHistoryEditModal';
import AchievementIcon from '../components/AchievementIcon';
import './NinjaDetail.css';
import { formatBux } from '../utils/format';

export default function NinjaDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [ninja, setNinja] = useState<Ninja | null>(null);
  const [progressHistory, setProgressHistory] = useState<ProgressHistory[]>([]);
  const [unredeemedPurchases, setUnredeemedPurchases] = useState<Purchase[]>([]);
  const [allPurchases, setAllPurchases] = useState<Purchase[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditing, setIsEditing] = useState(false);

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    currentBeltType: 'WHITE' as BeltType,
    currentLevel: 0,
    currentLesson: 0,
    adminNote: '',
  });

  const [buxAmount, setBuxAmount] = useState(0);
  const [buxNotes, setBuxNotes] = useState('');
  const [showBuxForm, setShowBuxForm] = useState(false);
  const [showProgressHistory, setShowProgressHistory] = useState(false);
  const [achievements, setAchievements] = useState<AchievementProgress[]>([]);
  const [editingHistoryEntry, setEditingHistoryEntry] = useState<ProgressHistory | null>(null);

  const [confirmationModal, setConfirmationModal] = useState<{
    isOpen: boolean;
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    variant?: 'danger' | 'warning' | 'info';
    onConfirm: () => void;
  }>({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: () => {},
  });

  useEffect(() => {
    loadNinjaData();
  }, [id]);

  const loadNinjaData = async () => {
    if (!id) return;
    try {
      setLoading(true);
      // admin check because they see hidden achievements
      const isAdmin = !!localStorage.getItem('adminToken') || !!localStorage.getItem('adminUsername');
      
      const [ninjaData, historyData, unredeemedData, allPurchasesData, achievementsData] = await Promise.all([
        ninjaApi.getById(parseInt(id)),
        ninjaApi.getProgressHistory(parseInt(id)),
        shopApi.getUnredeemedPurchases(parseInt(id)),
        shopApi.getNinjaPurchases(parseInt(id)),
        achievementApi.getNinjaAchievements(parseInt(id), isAdmin).catch(() => []),
      ]);
      
      // filter hidden achievements unless admin because mystery
      const filteredAchievements = isAdmin 
        ? achievementsData 
        : achievementsData.filter(ap => !ap.achievement.hidden || ap.unlocked);
      // jackson serialization needs this annotation or java booleans break
      setNinja(ninjaData);
      setProgressHistory(historyData);
      setUnredeemedPurchases(unredeemedData);
      setAllPurchases(allPurchasesData);
      setAchievements(filteredAchievements);
      setFormData({
        firstName: ninjaData.firstName,
        lastName: ninjaData.lastName,
        username: ninjaData.username,
        currentBeltType: ninjaData.currentBeltType,
        currentLevel: ninjaData.currentLevel,
        currentLesson: ninjaData.currentLesson,
        adminNote: ninjaData.adminNote || '',
      });
      setError('');
    } catch (err: any) {
      setError('Failed to load ninja data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleLessonUp = async () => {
    if (!ninja) return;
    
    try {
      // figure out max lessons because belt system is complicated
      const maxLessonsForLevel = getMaxLessonsForLevel(ninja.currentBeltType, ninja.currentLevel);
      const maxLevelsForBelt = getMaxLevelsForBelt(ninja.currentBeltType);
      const currentBeltIndex = beltOrder.indexOf(ninja.currentBeltType);
      
      let newBelt = ninja.currentBeltType;
      let newLevel = ninja.currentLevel;
      let newLesson = ninja.currentLesson + 1;
      
      // increment lesson if we can
      if (newLesson <= maxLessonsForLevel) {
        // stay in same level
      } else if (newLevel < maxLevelsForBelt) {
        // move to next level, reset lesson
        newLevel = ninja.currentLevel + 1;
        newLesson = 1;
      } else if (currentBeltIndex < beltOrder.length - 1) {
        // move to next belt, reset everything
        newBelt = beltOrder[currentBeltIndex + 1];
        newLevel = 1;
        newLesson = 1;
      } else {
        alert('Already at maximum progression!');
        return;
      }
      
      await ninjaApi.updateProgress(ninja.id, {
        beltType: newBelt,
        level: newLevel,
        lesson: newLesson,
      });
      await loadNinjaData();
    } catch (err: any) {
      const errorMessage = err.message || err.response?.data?.message || 'Failed to progress';
      alert(errorMessage);
      console.error(err);
    }
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ninja) return;
    try {
      await ninjaApi.update(ninja.id, {
        firstName: formData.firstName,
        lastName: formData.lastName,
        username: formData.username,
        beltType: formData.currentBeltType,
        level: formData.currentLevel,
        lesson: formData.currentLesson,
        adminNote: formData.adminNote || undefined,
      });
      setIsEditing(false);
      await loadNinjaData();
    } catch (err: any) {
      const errorMessage = err.message || err.response?.data?.message || 'Failed to save changes';
      alert(errorMessage);
      console.error(err);
    }
  };

  const handleAwardBux = async () => {
    if (!ninja || buxAmount <= 0) return;
    try {
      await ninjaApi.awardBux(ninja.id, buxAmount, buxNotes || undefined);
      setBuxAmount(0);
      setBuxNotes('');
      setShowBuxForm(false);
      await loadNinjaData();
    } catch (err) {
      alert('Failed to award Bux');
      console.error(err);
    }
  };

  const handleDeductBux = async () => {
    if (!ninja || buxAmount <= 0) return;
    try {
      await ninjaApi.deductBux(ninja.id, buxAmount, buxNotes || undefined);
      setBuxAmount(0);
      setBuxNotes('');
      setShowBuxForm(false);
      await loadNinjaData();
    } catch (err) {
      alert('Failed to deduct Bux');
      console.error(err);
    }
  };

  const handleRedeemPurchase = async (purchaseId: number, itemName: string) => {
    setConfirmationModal({
      isOpen: true,
      title: 'Mark as Redeemed',
      message: `Mark "${itemName}" as redeemed?`,
      confirmText: 'Mark Redeemed',
      cancelText: 'Cancel',
      variant: 'info',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await shopApi.redeemPurchase(purchaseId);
          await loadNinjaData();
        } catch (err) {
          alert('Failed to redeem purchase');
          console.error(err);
        }
      },
    });
  };

  const handleRefundPurchase = async (purchaseId: number, itemName: string) => {
    setConfirmationModal({
      isOpen: true,
      title: 'Refund Purchase',
      message: `Refund "${itemName}"? This will return the Bux to the ninja.`,
      confirmText: 'Refund',
      cancelText: 'Cancel',
      variant: 'warning',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await shopApi.refundPurchase(purchaseId);
          await loadNinjaData();
        } catch (err) {
          alert('Failed to refund purchase');
          console.error(err);
        }
      },
    });
  };


  const handleSaveCorrection = async (correction: ProgressHistoryCorrectionRequest) => {
    if (!id) return;
    await ninjaApi.createProgressHistoryCorrection(parseInt(id), correction);
    await loadNinjaData();
  };

  if (loading) {
    return <div className="ninja-detail-loading">Loading ninja details...</div>;
  }

  if (error || !ninja) {
    return (
      <div className="ninja-detail-error">
        <p>{error || 'Ninja not found'}</p>
        <button onClick={() => navigate('/admin')} className="btn-primary">
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="ninja-detail-container">
      {/* Confirmation Modal */}
      <ConfirmationModal
        isOpen={confirmationModal.isOpen}
        title={confirmationModal.title}
        message={confirmationModal.message}
        confirmText={confirmationModal.confirmText}
        cancelText={confirmationModal.cancelText}
        variant={confirmationModal.variant}
        onConfirm={confirmationModal.onConfirm}
        onCancel={() => setConfirmationModal({ ...confirmationModal, isOpen: false })}
      />
      {/* Progress History Edit Modal */}
      <ProgressHistoryEditModal
        isOpen={editingHistoryEntry !== null}
        originalEntry={editingHistoryEntry}
        onClose={() => setEditingHistoryEntry(null)}
        onSave={handleSaveCorrection}
      />
      <div className="ninja-detail-header">
        <button onClick={() => navigate('/admin')} className="btn-back">
          ← Back to Dashboard
        </button>
        <h1>
          {ninja.firstName} {ninja.lastName}
        </h1>
        <button onClick={() => setIsEditing(!isEditing)} className="btn-secondary">
          {isEditing ? 'Cancel Edit' : 'Edit Ninja'}
        </button>
      </div>

      {isEditing ? (
        <div className="ninja-edit-form">
          <form onSubmit={handleSaveEdit}>
            <div className="form-grid">
              <div className="form-field">
                <label>First Name</label>
                <input
                  type="text"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  required
                />
              </div>
              <div className="form-field">
                <label>Last Name</label>
                <input
                  type="text"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  required
                />
              </div>
              <div className="form-field">
                <label>Username</label>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  required
                />
              </div>
              <div className="form-field">
                <label>Belt</label>
                <select
                  value={formData.currentBeltType}
                  onChange={(e) => setFormData({ ...formData, currentBeltType: e.target.value as BeltType })}
                >
                  <option value="WHITE">White</option>
                  <option value="YELLOW">Yellow</option>
                  <option value="ORANGE">Orange</option>
                  <option value="GREEN">Green</option>
                  <option value="BLUE">Blue</option>
                  <option value="PURPLE">Purple</option>
                  <option value="RED">Red</option>
                  <option value="BROWN">Brown</option>
                  <option value="BLACK">Black</option>
                </select>
              </div>
              <div className="form-field">
                <label>Level</label>
                <input
                  type="number"
                  value={formData.currentLevel}
                  onChange={(e) => setFormData({ ...formData, currentLevel: parseInt(e.target.value) })}
                  min="0"
                />
              </div>
              <div className="form-field">
                <label>Lesson</label>
                <input
                  type="number"
                  value={formData.currentLesson}
                  onChange={(e) => setFormData({ ...formData, currentLesson: parseInt(e.target.value) })}
                  min="0"
                />
              </div>
              <div className="form-field" style={{ gridColumn: '1 / -1' }}>
                <label>Admin Note</label>
                <textarea
                  value={formData.adminNote}
                  onChange={(e) => setFormData({ ...formData, adminNote: e.target.value })}
                  rows={3}
                  placeholder="Internal admin notes about this ninja (optional)"
                />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-primary">Save Changes</button>
              <button type="button" onClick={() => setIsEditing(false)} className="btn-secondary">
                Cancel
              </button>
            </div>
          </form>
        </div>
      ) : (
        <>
          {/* Quick Progress Actions */}
          <div className="quick-actions-card">
            <h2>Quick Progress</h2>
            <div className="quick-actions-grid" style={{ gridTemplateColumns: '1fr' }}>
              <button onClick={handleLessonUp} className="btn-quick-action btn-lesson" style={{ width: '100%' }}>
                Lesson Up →
                <span className="current-value">
                  {ninja.currentBeltType} Belt - Level {ninja.currentLevel}, Lesson {ninja.currentLesson}
                </span>
              </button>
            </div>
          </div>

          {/* Ninja Info Card */}
          <div className="ninja-info-card">
            <h2>Ninja Information</h2>
            <div className="info-grid">
              <div className="info-item">
                <span className="info-label">Username:</span>
                <span className="info-value">{ninja.username}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Belt:</span>
                <span className="info-value belt-badge-large">{ninja.currentBeltType}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Level:</span>
                <span className="info-value">{ninja.currentLevel}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Lesson:</span>
                <span className="info-value">{ninja.currentLesson}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Total Earned:</span>
                <span className="info-value">{formatBux(ninja.totalBuxEarned)} Bux</span>
              </div>
              <div className="info-item">
                <span className="info-label">Total Spent:</span>
                <span className="info-value">{formatBux(ninja.totalBuxSpent)} Bux</span>
              </div>
              <div className="info-item highlight">
                <span className="info-label">Current Balance:</span>
                <span className="info-value balance">{formatBux(ninja.buxBalance)} Bux</span>
              </div>
              {ninja.legacyBalance !== undefined && ninja.legacyBalance > 0 && (
                <div className="info-item highlight" style={{ background: '#fef3c7', border: '2px solid #f59e0b' }}>
                  <span className="info-label" style={{ color: '#92400e', fontWeight: 600 }}>Legacy Points:</span>
                  <span className="info-value" style={{ color: '#92400e', fontWeight: 700 }}>{ninja.legacyBalance} Legacy</span>
                </div>
              )}
              <div className="info-item">
                <span className="info-label">Questions Answered:</span>
                <span className="info-value">{ninja.totalQuestionsAnswered}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Questions Correct:</span>
                <span className="info-value">{ninja.totalQuestionsCorrect}</span>
              </div>
              {ninja.suggestionsBanned && (
                <div className="info-item" style={{ gridColumn: '1 / -1', background: '#fee2e2', padding: '0.75rem', borderRadius: '8px', border: '1px solid #dc2626' }}>
                  <span className="info-label" style={{ color: '#dc2626', fontWeight: 600 }}>Suggestions Banned</span>
                  <span className="info-value" style={{ color: '#dc2626' }}>This ninja cannot suggest questions</span>
                </div>
              )}
              {ninja.isLocked && (
                <div className="info-item" style={{ gridColumn: '1 / -1', background: '#fee2e2', padding: '0.75rem', borderRadius: '8px', border: '1px solid #dc2626' }}>
                  <span className="info-label" style={{ color: '#dc2626', fontWeight: 600 }}>Account Locked</span>
                  <span className="info-value" style={{ color: '#dc2626' }}>
                    {ninja.lockReason || 'Account is locked'}
                    {ninja.lockedAt && (
                      <div style={{ fontSize: '0.875rem', marginTop: '0.25rem', opacity: 0.8 }}>
                        Locked at: {new Date(ninja.lockedAt).toLocaleString()}
                      </div>
                    )}
                  </span>
                </div>
              )}
              {ninja.adminNote && (
                <div className="info-item" style={{ gridColumn: '1 / -1', background: '#eff6ff', padding: '0.75rem', borderRadius: '8px', border: '1px solid #3b82f6' }}>
                  <span className="info-label" style={{ color: '#1e40af', fontWeight: 600 }}>Admin Note:</span>
                  <span className="info-value" style={{ color: '#1e40af', whiteSpace: 'pre-wrap' }}>
                    {ninja.adminNote}
                  </span>
                </div>
              )}
            </div>

            <div className="bux-management" style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
              {!showBuxForm ? (
                <button onClick={() => setShowBuxForm(true)} className="btn-secondary">
                  Manage Bux
                </button>
              ) : (
                <div className="bux-form" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <input
                    type="number"
                    value={buxAmount}
                    onChange={(e) => setBuxAmount(parseInt(e.target.value) || 0)}
                    placeholder="Amount"
                    min="1"
                  />
                  <input
                    type="text"
                    value={buxNotes}
                    onChange={(e) => setBuxNotes(e.target.value)}
                    placeholder="Notes (optional)"
                  />
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button onClick={handleAwardBux} className="btn-success" disabled={buxAmount <= 0}>
                      Award
                    </button>
                    <button onClick={handleDeductBux} className="btn-warning" disabled={buxAmount <= 0}>
                      Deduct
                    </button>
                    <button onClick={() => {
                      setShowBuxForm(false);
                      setBuxAmount(0);
                      setBuxNotes('');
                    }} className="btn-secondary">
                      Cancel
                    </button>
                  </div>
                </div>
              )}
              
              {ninja && (
                <button 
                  onClick={async () => {
                    if (!ninja) return;
                    const isBanned = ninja.suggestionsBanned || false;
                    setConfirmationModal({
                      isOpen: true,
                      title: isBanned ? 'Unban from Suggestions' : 'Ban from Suggestions',
                      message: isBanned 
                        ? 'Allow this ninja to suggest questions again?'
                        : 'Ban this ninja from suggesting questions?',
                      confirmText: isBanned ? 'Unban' : 'Ban',
                      cancelText: 'Cancel',
                      variant: isBanned ? 'info' : 'warning',
                      onConfirm: async () => {
                        setConfirmationModal({ ...confirmationModal, isOpen: false });
                        try {
                          const newBannedState = !isBanned;
                          await ninjaApi.banSuggestions(ninja.id, newBannedState);
                          setNinja(prev => prev ? { ...prev, suggestionsBanned: newBannedState } : null);
                          await loadNinjaData();
                        } catch (err: any) {
                          alert(err.response?.data?.message || `Failed to ${isBanned ? 'unban' : 'ban'} ninja from suggestions`);
                          console.error(err);
                        }
                      },
                    });
                  }}
                  className={(ninja.suggestionsBanned || false) ? 'btn-success' : 'btn-warning'}
                  style={{ marginLeft: 'auto' }}
                >
                  {(ninja.suggestionsBanned || false) ? 'Unban Suggestions' : 'Ban Suggestions'}
                </button>
              )}
              
              {ninja && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginLeft: 'auto' }}>
                  <label style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '0.5rem', 
                    cursor: 'pointer',
                    fontWeight: 500,
                    fontSize: '0.95rem'
                  }}>
                    <span style={{ color: ninja.isLocked ? '#dc2626' : '#6b7280' }}>
                      {ninja.isLocked ? 'Locked' : 'Unlocked'}
                    </span>
                    <div
                      onClick={async (e) => {
                        e.preventDefault();
                        if (!ninja) return;
                        const isLocked = ninja.isLocked || false;
                        
                        if (isLocked) {
                          // Unlock - use confirmation modal
                          setConfirmationModal({
                            isOpen: true,
                            title: 'Unlock Account',
                            message: `Unlock ${ninja.firstName} ${ninja.lastName}'s account?`,
                            confirmText: 'Unlock',
                            cancelText: 'Cancel',
                            variant: 'info',
                            onConfirm: async () => {
                              setConfirmationModal({ ...confirmationModal, isOpen: false });
                              try {
                                const updatedNinja = await ninjaApi.unlockAccount(ninja.id);
                                // Update state with the response from server
                                setNinja(updatedNinja);
                                // Reload other data
                                const [historyData, unredeemedData, allPurchasesData] = await Promise.all([
                                  ninjaApi.getProgressHistory(parseInt(id!)),
                                  shopApi.getUnredeemedPurchases(parseInt(id!)),
                                  shopApi.getNinjaPurchases(parseInt(id!)),
                                ]);
                                setProgressHistory(historyData);
                                setUnredeemedPurchases(unredeemedData);
                                setAllPurchases(allPurchasesData);
                              } catch (err: any) {
                                alert(err.response?.data?.message || err.message || 'Failed to unlock account');
                                console.error(err);
                                // Reload to get current state
                                loadNinjaData();
                              }
                            },
                          });
                        } else {
                          // Lock - use confirmation modal with prompt for reason
                          const reason = prompt(`Enter reason for locking ${ninja.firstName} ${ninja.lastName} (optional):`);
                          if (reason === null) return; // User cancelled
                          
                          setConfirmationModal({
                            isOpen: true,
                            title: 'Lock Account',
                            message: `Lock ${ninja.firstName} ${ninja.lastName}'s account?${reason ? `\n\nReason: ${reason}` : ''}`,
                            confirmText: 'Lock',
                            cancelText: 'Cancel',
                            variant: 'warning',
                            onConfirm: async () => {
                              setConfirmationModal({ ...confirmationModal, isOpen: false });
                              try {
                                const updatedNinja = await ninjaApi.lockAccount(ninja.id, reason || undefined);
                                // Update state with the response from server
                                setNinja(updatedNinja);
                                // Reload other data
                                const [historyData, unredeemedData, allPurchasesData] = await Promise.all([
                                  ninjaApi.getProgressHistory(parseInt(id!)),
                                  shopApi.getUnredeemedPurchases(parseInt(id!)),
                                  shopApi.getNinjaPurchases(parseInt(id!)),
                                ]);
                                setProgressHistory(historyData);
                                setUnredeemedPurchases(unredeemedData);
                                setAllPurchases(allPurchasesData);
                              } catch (err: any) {
                                alert(err.response?.data?.message || err.message || 'Failed to lock account');
                                console.error(err);
                                // Reload to get current state
                                loadNinjaData();
                              }
                            },
                          });
                        }
                      }}
                      style={{
                        position: 'relative',
                        width: '48px',
                        height: '24px',
                        borderRadius: '12px',
                        backgroundColor: ninja.isLocked ? '#dc2626' : '#d1d5db',
                        cursor: 'pointer',
                        transition: 'background-color 0.2s',
                        border: '2px solid',
                        borderColor: ninja.isLocked ? '#dc2626' : '#d1d5db',
                      }}
                    >
                      <div
                        style={{
                          position: 'absolute',
                          top: '2px',
                          left: ninja.isLocked ? '26px' : '2px',
                          width: '16px',
                          height: '16px',
                          borderRadius: '50%',
                          backgroundColor: '#ffffff',
                          transition: 'left 0.2s',
                          boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                        }}
                      />
                    </div>
                  </label>
                </div>
              )}
              
            </div>
          </div>

          {/* Unredeemed Purchases */}
          {unredeemedPurchases.length > 0 && (
            <div className="purchases-card">
              <h2>Unredeemed Purchases ({unredeemedPurchases.length})</h2>
              <div className="purchases-list">
                {unredeemedPurchases.map((purchase) => (
                  <div key={purchase.id} className="purchase-item unredeemed">
                    <div className="purchase-info">
                      <h3>{purchase.itemName}</h3>
                      <p>{purchase.itemDescription}</p>
                      <small>Purchased: {new Date(purchase.purchaseDate).toLocaleDateString()}</small>
                    </div>
                    <div className="purchase-actions">
                      <button
                        onClick={() => handleRedeemPurchase(purchase.id, purchase.itemName)}
                        className="btn-sm btn-success"
                      >
                        Redeem
                      </button>
                      <button
                        onClick={() => handleRefundPurchase(purchase.id, purchase.itemName)}
                        className="btn-sm btn-warning"
                      >
                        Refund
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* All Purchases History */}
          {allPurchases.length > 0 && (
            <div className="purchases-card">
              <h2>Purchase History ({allPurchases.length})</h2>
              <div className="purchases-list">
                {allPurchases.map((purchase) => (
                  <div key={purchase.id} className={`purchase-item ${purchase.redeemed ? 'redeemed' : ''}`}>
                    <div className="purchase-info">
                      <h3>{purchase.itemName}</h3>
                      <p>{purchase.itemDescription}</p>
                      <small>
                        Purchased: {new Date(purchase.purchaseDate).toLocaleDateString()}
                        {purchase.redeemed && purchase.redeemedDate && (
                          <> | Redeemed: {new Date(purchase.redeemedDate).toLocaleDateString()}</>
                        )}
                      </small>
                    </div>
                    <div className="purchase-meta">
                      <span className="purchase-price">{formatBux(purchase.pricePaid)} Bux</span>
                      {purchase.redeemed && <span className="redeemed-badge">✓ Redeemed</span>}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Achievements Section */}
          <div className="achievements-card" style={{ marginTop: '2rem', padding: '1.5rem', background: '#f8f9fa', borderRadius: '12px' }}>
            <h2>Achievements</h2>
            {achievements.length > 0 ? (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
                {achievements.map((progress) => {
                  const unlocked = progress.unlocked;
                  
                  return (
                    <div
                      key={progress.id}
                      style={{
                        padding: '1rem',
                        background: unlocked ? 'white' : '#e5e7eb',
                        borderRadius: '8px',
                        border: `2px solid ${unlocked ? '#3b82f6' : '#9ca3af'}`,
                        opacity: unlocked ? 1 : 0.7,
                      }}
                    >
                      <div style={{ fontSize: '2rem', textAlign: 'center', marginBottom: '0.5rem' }}>
                        <AchievementIcon icon={progress.achievement.icon} size={32} />
                      </div>
                      <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1rem', fontWeight: 600 }}>
                        {progress.achievement.name}
                      </h3>
                      <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem', color: '#666' }}>
                        {progress.achievement.description}
                      </p>
                      {unlocked ? (
                        <div style={{ fontSize: '0.875rem', color: '#059669', fontWeight: 600 }}>
                          ✓ Unlocked {progress.unlockedAt && `(${new Date(progress.unlockedAt).toLocaleDateString()})`}
                        </div>
                      ) : (
                        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          Progress: {progress.progressValue}%
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <p style={{ marginTop: '1rem', color: '#666' }}>No achievements yet</p>
            )}
          </div>

          {/* Progress History */}
          <div className="history-card">
            <div className="history-card-header">
              <h2>Progress History ({progressHistory.length})</h2>
              <button
                onClick={() => setShowProgressHistory(!showProgressHistory)}
                className="btn-toggle"
              >
                {showProgressHistory ? 'Hide' : 'Show'}
              </button>
            </div>
            {showProgressHistory && (
              <>
                {progressHistory.length > 0 ? (
                  <div className="history-list">
                    {progressHistory.map((history) => {
                      const corrections = progressHistory.filter(h => h.correctionToId === history.id);
                      return (
                        <div key={history.id} className="history-item">
                          <div className="history-main">
                            <div className="history-progress">
                              {history.isCorrection && (
                                <span className="history-correction-indicator">
                                  CORRECTION
                                </span>
                              )}
                              <span className="belt-badge">{history.beltType}</span>
                              <span className="progress-detail">
                                Level {history.level} - Lesson {history.lesson}
                              </span>
                              {history.correctionToId && (
                                <span className="history-correction-ref">
                                  (Correction to #{history.correctionToId})
                                </span>
                              )}
                            </div>
                            <div className="history-earning">
                              <span className="earning-type">{history.earningType.replace(/_/g, ' ')}</span>
                              {history.buxEarned !== 0 && (
                                <span className="bux-earned" style={{ color: history.buxEarned >= 0 ? '#059669' : '#dc2626' }}>
                                {history.buxEarned >= 0 ? '+' : ''}{formatBux(Math.abs(history.buxEarned))} Bux
                                </span>
                              )}
                              {history.legacyDelta !== undefined && history.legacyDelta !== 0 && (
                                <span className="legacy-earned" style={{ color: history.legacyDelta >= 0 ? '#f59e0b' : '#dc2626' }}>
                                  {history.legacyDelta >= 0 ? '+' : ''}{history.legacyDelta} Legacy
                                </span>
                              )}
                            </div>
                            <div className="history-time">
                              {new Date(history.timestamp).toLocaleString()}
                              {history.adminUsername && (
                                <span className="admin-username">
                                  by {history.adminUsername}
                                </span>
                              )}
                            </div>
                            {history.notes && (
                              <div className="history-notes">
                                {history.notes}
                              </div>
                            )}
                            {!history.isCorrection && (
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setEditingHistoryEntry(history);
                                }}
                                className="btn-sm btn-secondary"
                                style={{ marginTop: '0.5rem', alignSelf: 'flex-start' }}
                              >
                                Edit
                              </button>
                            )}
                          </div>
                          {corrections.length > 0 && (
                            <div className="history-corrections">
                              {corrections.map(correction => (
                                <div key={correction.id} className="history-correction-item">
                                  <strong>Correction:</strong> {correction.buxEarned >= 0 ? '+' : ''}{formatBux(Math.abs(correction.buxEarned))} Bux
                                  {correction.legacyDelta !== undefined && correction.legacyDelta !== 0 && (
                                    <span>, {correction.legacyDelta >= 0 ? '+' : ''}{correction.legacyDelta} Legacy</span>
                                  )}
                                  {correction.notes && ` - ${correction.notes}`}
                                  {correction.adminUsername && ` (by ${correction.adminUsername})`}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <p className="empty-state">No progress history yet</p>
                )}
              </>
            )}
          </div>
        </>
      )}
    </div>
  );
}
