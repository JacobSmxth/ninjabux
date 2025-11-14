import { useState, useEffect } from 'react';
import { ninjaApi, shopApi } from '../services/api';
import type { Ninja, ShopItem, Purchase } from '../types';
import ConfirmationModal from '../components/ConfirmationModal';
import { useLockContext } from '../context/LockContext';
import { getBeltTheme, defaultBeltTheme } from '../utils/beltTheme';
import './Shop.css';

interface Props {
  ninjaId: number;
}

export default function Shop({ ninjaId }: Props) {
  const { setLockStatus } = useLockContext();
  const [ninja, setNinja] = useState<Ninja | null>(null);
  const [items, setItems] = useState<ShopItem[]>([]);
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [purchaseMessage, setPurchaseMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);
  const [tooltipItemId, setTooltipItemId] = useState<number | null>(null);
  
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
    loadData();
  }, [ninjaId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [ninjaData, itemsData, purchasesData] = await Promise.all([
        ninjaApi.getById(ninjaId),
        shopApi.getAvailableItems(),
        shopApi.getNinjaPurchases(ninjaId),
      ]);
      setNinja(ninjaData);
      setItems(itemsData);
      setPurchases(purchasesData);
      
      // update lock status so overlay shows if needed
      if (ninjaData.isLocked) {
        setLockStatus(true, ninjaData.lockReason || 'Your account is locked. Please get back to work!');
      } else {
        setLockStatus(false, '');
      }
      
      setError('');
    } catch (err: any) {
      if (err.response?.status === 403 || err.message?.includes('Account is locked')) {
        const errorMsg = err.response?.data?.message || err.message || 'Account is locked';
        setLockStatus(true, errorMsg);
      }
      setError('Failed to load shop data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handlePurchase = async (itemId: number, itemName: string, price: number) => {
    if (!ninja) return;

    if (ninja.buxBalance < price) {
      setPurchaseMessage({ type: 'error', text: `Not enough Bux! You need ${price} but only have ${ninja.buxBalance.toFixed(2)}.` });
      setTimeout(() => setPurchaseMessage(null), 5000);
      return;
    }

    setConfirmationModal({
      isOpen: true,
      title: 'Purchase Item',
      message: `Purchase ${itemName} for ${price} Bux?`,
      confirmText: 'Purchase',
      cancelText: 'Cancel',
      variant: 'info',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await shopApi.purchaseItem({ ninjaId, itemId });
          setPurchaseMessage({ type: 'success', text: `Successfully purchased ${itemName}!` });
          setTimeout(() => setPurchaseMessage(null), 5000);

          await loadData();
        } catch (err: any) {
          if (err.response?.status === 403 || err.message?.includes('Account is locked')) {
            const errorMsg = err.response?.data?.message || err.message || 'Account is locked';
            setLockStatus(true, errorMsg);
            setPurchaseMessage({ type: 'error', text: errorMsg });
            setTimeout(() => setPurchaseMessage(null), 5000);
            // reload to get updated lock status
            await loadData();
          } else {
            const errorMsg = err.response?.data?.message || 'Failed to purchase item';
            setPurchaseMessage({ type: 'error', text: errorMsg });
            setTimeout(() => setPurchaseMessage(null), 5000);
          }
          console.error(err);
        }
      },
    });
  };

  const getPurchaseRestrictions = (item: ShopItem) => {
    const restrictions: string[] = [];
    
    if (item.maxPerStudent) {
      const count = purchases.filter(p => p.itemId === item.id).length;
      if (count >= item.maxPerStudent) {
        restrictions.push(`Max ${item.maxPerStudent} per student (${count}/${item.maxPerStudent})`);
      }
    }
    
    if (item.maxPerDay) {
      const today = new Date().toDateString();
      const todayCount = purchases.filter(p => 
        p.itemId === item.id && new Date(p.purchaseDate).toDateString() === today
      ).length;
      if (todayCount >= item.maxPerDay) {
        restrictions.push(`Max ${item.maxPerDay} per day (${todayCount}/${item.maxPerDay})`);
      }
    }
    
    if (item.maxActiveAtOnce) {
      const activeCount = purchases.filter(p => 
        p.itemId === item.id && !p.redeemed
      ).length;
      if (activeCount >= item.maxActiveAtOnce) {
        restrictions.push(`Max ${item.maxActiveAtOnce} active at once (${activeCount}/${item.maxActiveAtOnce})`);
      }
    }
    
    if (item.restrictedCategories) {
      restrictions.push(`Restricted for: ${item.restrictedCategories}`);
    }
    
    return restrictions;
  };

  const canPurchaseItem = (item: ShopItem): { canPurchase: boolean; reason?: string } => {
    if (!ninja) return { canPurchase: false, reason: 'Not logged in' };
    
    if (ninja.buxBalance < item.price) {
      return { canPurchase: false, reason: `Not enough Bux! You need ${item.price} but only have ${ninja.buxBalance.toFixed(2)}.` };
    }
    
    const restrictions = getPurchaseRestrictions(item);
    if (restrictions.length > 0) {
      return { canPurchase: false, reason: restrictions.join(' | ') };
    }
    
    return { canPurchase: true };
  };

  const getRestrictionBadges = (item: ShopItem) => {
    const badges: string[] = [];
    
    if (item.maxPerStudent) {
      badges.push(`Max ${item.maxPerStudent} per student`);
    }
    if (item.maxPerDay) {
      badges.push(`Max ${item.maxPerDay} per day`);
    }
    if (item.maxLifetime) {
      badges.push(`Max ${item.maxLifetime} lifetime`);
    }
    if (item.maxActiveAtOnce) {
      badges.push(`Max ${item.maxActiveAtOnce} active at once`);
    }
    if (item.restrictedCategories) {
      badges.push(`Restricted: ${item.restrictedCategories}`);
    }
    
    return badges;
  };

  const hexToRgba = (hex: string, alpha: number) => {
    const clean = hex.replace('#', '');
    const bigint = parseInt(clean, 16);
    const r = (bigint >> 16) & 255;
    const g = (bigint >> 8) & 255;
    const b = bigint & 255;
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };

  const beltTheme = ninja ? getBeltTheme(ninja.currentBeltType) : defaultBeltTheme;
  const accentShadow = hexToRgba(beltTheme.accent, 0.35);
  const accentBorder = hexToRgba(beltTheme.accent, 0.45);

  if (loading) {
    return <div className="shop-container"><h2>Loading shop...</h2></div>;
  }

  if (error || !ninja) {
    return (
      <div className="shop-container">
        <p className="error">{error || 'Failed to load'}</p>
      </div>
    );
  }

  return (
    <div className="shop-container">
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
      <div className="shop-header" style={{ borderBottomColor: beltTheme.accent }}>
        <h1>NinjaBux Shop</h1>
        <div className="balance-display" style={{
          background: `linear-gradient(135deg, ${beltTheme.primary} 0%, ${hexToRgba(beltTheme.accent, 0.35)} 100%)`,
          borderColor: beltTheme.accent,
          boxShadow: `0 12px 28px ${accentShadow}`
        }}>
          <span className="balance-label" style={{ color: beltTheme.secondary }}>Your Balance:</span>
          <span className="balance-amount" style={{ color: beltTheme.secondary }}>{ninja.buxBalance.toFixed(2)} Bux</span>
          {ninja.legacyBalance !== undefined && ninja.legacyBalance > 0 && (
            <span className="balance-legacy" style={{ 
              color: beltTheme.secondary, 
              fontSize: '0.85rem', 
              opacity: 0.9, 
              marginLeft: '0.5rem' 
            }}>
              ({ninja.legacyBalance} Legacy)
            </span>
          )}
        </div>
      </div>

      {purchaseMessage && (
        <div className={`message-banner ${purchaseMessage.type}`}>
          {purchaseMessage.text}
        </div>
      )}

      <div className="shop-items-list">
        {items.map((item) => {
          const { canPurchase, reason } = canPurchaseItem(item);
          const canAfford = ninja.buxBalance >= item.price;
          const restrictionBadges = getRestrictionBadges(item);
          const showTooltip = tooltipItemId === item.id && !canPurchase;
          
          return (
            <div 
              key={item.id} 
              className={`shop-item-row ${!canPurchase ? 'restricted' : ''} ${!canAfford ? 'unaffordable' : ''}`}
            >
              <div className="item-info">
                <div className="item-header">
                  <h3>{item.name}</h3>
                  {restrictionBadges.length > 0 && (
                    <div className="restriction-badges">
                      {restrictionBadges.map((badge, idx) => (
                        <span key={idx} className="restriction-badge">{badge}</span>
                      ))}
                    </div>
                  )}
                </div>
                <p className="item-description">{item.description}</p>
              </div>
              <div className="item-actions">
                <span className="item-price" style={{ color: beltTheme.accent }}>{item.price} Bux</span>
                <div className="purchase-button-wrapper">
                  <button
                    className={`purchase-btn ${!canPurchase ? 'disabled' : ''}`}
                    onClick={() => canPurchase && handlePurchase(item.id, item.name, item.price)}
                    disabled={!canPurchase}
                    onMouseEnter={() => !canPurchase && setTooltipItemId(item.id)}
                    onMouseLeave={() => setTooltipItemId(null)}
                    style={canPurchase ? {
                      background: beltTheme.accent,
                      color: beltTheme.accentText,
                      boxShadow: `0 10px 22px ${accentShadow}`
                    } : {
                      borderColor: accentBorder,
                      color: beltTheme.textColor
                    }}
                  >
                    {canPurchase ? 'Purchase' : canAfford ? 'Limit Reached' : 'Not Enough Bux'}
                  </button>
                  {showTooltip && reason && (
                    <div className="purchase-tooltip">
                      {reason}
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {items.length === 0 && (
        <div className="no-items">
          <p>No items available.</p>
        </div>
      )}
    </div>
  );
}
