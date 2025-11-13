import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ninjaApi, shopApi, bigQuestionApi, adminApi, achievementApi } from '../services/api';
import type { Ninja, BeltType, Purchase, ShopItem, BigQuestion, CreateBigQuestionRequest, AdminAuditLog, Admin, CreateAdminByAdminRequest, ChangePasswordRequest, Achievement, AchievementCategory, BadgeRarity, CreateAchievementRequest } from '../types';
import { FiEdit2, FiTrash2, FiPause, FiPlay } from 'react-icons/fi';
import AchievementIcon from '../components/AchievementIcon';
import './AdminDashboard.css';

interface Props {
  onLogout: () => void;
  admin: Admin | null;
}

interface NinjaWithPurchases extends Ninja {
  unredeemedPurchases?: Purchase[];
  allPurchases?: Purchase[];
}

type TabType = 'ninjas' | 'shop' | 'question' | 'achievements' | 'settings';

export default function AdminDashboard({ onLogout, admin }: Props) {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<TabType>('ninjas');
  const [ninjas, setNinjas] = useState<NinjaWithPurchases[]>([]);
  const [shopItems, setShopItems] = useState<ShopItem[]>([]);
  const [questions, setQuestions] = useState<BigQuestion[]>([]);
  const [deletedQuestions, setDeletedQuestions] = useState<BigQuestion[]>([]);
  const [showDeleted, setShowDeleted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [auditLogs, setAuditLogs] = useState<AdminAuditLog[]>([]);

  // Ninja management state
  const [editingNinja, setEditingNinja] = useState<Ninja | null>(null);
  const [isCreatingNinja, setIsCreatingNinja] = useState(false);
  const [selectedNinja, setSelectedNinja] = useState<NinjaWithPurchases | null>(null);
  const [ninjaFormData, setNinjaFormData] = useState({
    firstName: '',
    lastName: '',
    username: '',
    currentBeltType: 'WHITE' as BeltType,
    currentLevel: 0,
    currentLesson: 0,
  });

  // Shop item management state
  const [editingItem, setEditingItem] = useState<ShopItem | null>(null);
  const [isCreatingItem, setIsCreatingItem] = useState(false);
  const [itemFormData, setItemFormData] = useState({
    name: '',
    description: '',
    price: 0,
    category: '',
  });

  // Big Question management state
  const [editingQuestion, setEditingQuestion] = useState<BigQuestion | null>(null);
  const [isCreatingQuestion, setIsCreatingQuestion] = useState(false);
  const [questionFormData, setQuestionFormData] = useState({
    questionDate: new Date().toISOString().split('T')[0],
    questionText: '',
    questionType: 'MULTIPLE_CHOICE' as 'MULTIPLE_CHOICE' | 'SHORT_ANSWER',
    correctAnswer: '',
    correctChoiceIndex: 0,
    choices: ['', '', '', ''],
  });

  // Bux management state
  const [buxNinjaId, setBuxNinjaId] = useState<number | null>(null);
  const [buxAmount, setBuxAmount] = useState(0);

  // Admin management state
  const [isCreatingAdmin, setIsCreatingAdmin] = useState(false);
  const [adminFormData, setAdminFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    currentPassword: '',
  });

  // Password change state
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [passwordFormData, setPasswordFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  // Admin list state
  const [allAdmins, setAllAdmins] = useState<Admin[]>([]);
  const [adminListPassword, setAdminListPassword] = useState('');
  const [showAdminList, setShowAdminList] = useState(false);
  const [deletingAdminId, setDeletingAdminId] = useState<number | null>(null);

  // Achievement management state
  const [achievements, setAchievements] = useState<Achievement[]>([]);
  const [editingAchievement, setEditingAchievement] = useState<Achievement | null>(null);
  const [isCreatingAchievement, setIsCreatingAchievement] = useState(false);
  const [achievementFormData, setAchievementFormData] = useState<CreateAchievementRequest>({
    name: '',
    description: '',
    category: 'PROGRESS' as AchievementCategory,
    rarity: 'COMMON' as BadgeRarity,
    icon: '🎯',
    buxReward: 0,
    manualOnly: false,
    unlockCriteria: '',
    active: true,
    hidden: false,
  });

  useEffect(() => {
    loadData();
    loadAuditLogs();
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      if (activeTab === 'ninjas') {
        await loadNinjas();
      } else if (activeTab === 'shop') {
        await loadShopItems();
      } else if (activeTab === 'question') {
        await loadQuestions();
      } else if (activeTab === 'achievements') {
        await loadAchievements();
      } else if (activeTab === 'settings') {
        if (showAdminList && admin) {
          await loadAllAdmins();
        }
      }
      setError('');
    } catch (err) {
      setError('Failed to load data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadNinjas = async () => {
    const data = await ninjaApi.getAll();
    const ninjasWithPurchases = await Promise.all(
      data.map(async (ninja) => {
        try {
          const unredeemed = await shopApi.getUnredeemedPurchases(ninja.id);
          const all = await shopApi.getNinjaPurchases(ninja.id);
          return { ...ninja, unredeemedPurchases: unredeemed, allPurchases: all };
        } catch (err) {
          return { ...ninja, unredeemedPurchases: [], allPurchases: [] };
        }
      })
    );
    setNinjas(ninjasWithPurchases);
  };

  const loadShopItems = async () => {
    const items = await shopApi.getAllItems();
    setShopItems(items);
  };

  const loadQuestions = async () => {
    const [activeQs, deletedQs] = await Promise.all([
      bigQuestionApi.getAllQuestions(),
      bigQuestionApi.getDeletedQuestions(),
    ]);
    setQuestions(activeQs);
    setDeletedQuestions(deletedQs);
  };

  const loadAchievements = async () => {
    const allAchievements = await achievementApi.getAll();
    setAchievements(allAchievements);
  };

  const loadAuditLogs = async () => {
    try {
      const logs = await adminApi.getAuditLogs(30);
      setAuditLogs(logs);
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    }
  };

  // Ninja management functions
  const handleCreateNinja = () => {
    setIsCreatingNinja(true);
    setEditingNinja(null);
    setNinjaFormData({
      firstName: '',
      lastName: '',
      username: '',
      currentBeltType: 'WHITE',
      currentLevel: 0,
      currentLesson: 0,
    });
  };

  const handleEditNinja = (ninja: Ninja) => {
    setEditingNinja(ninja);
    setIsCreatingNinja(false);
    setNinjaFormData({
      firstName: ninja.firstName,
      lastName: ninja.lastName,
      username: ninja.username,
      currentBeltType: ninja.currentBeltType,
      currentLevel: ninja.currentLevel,
      currentLesson: ninja.currentLesson,
    });
  };

  const handleSubmitNinja = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (isCreatingNinja) {
        await ninjaApi.create({
          firstName: ninjaFormData.firstName,
          lastName: ninjaFormData.lastName,
          username: ninjaFormData.username,
          beltType: ninjaFormData.currentBeltType,
          level: ninjaFormData.currentLevel,
          lesson: ninjaFormData.currentLesson,
        });
      } else if (editingNinja) {
        await ninjaApi.update(editingNinja.id, {
          firstName: ninjaFormData.firstName,
          lastName: ninjaFormData.lastName,
          username: ninjaFormData.username,
          beltType: ninjaFormData.currentBeltType,
          level: ninjaFormData.currentLevel,
          lesson: ninjaFormData.currentLesson,
        });
      }
      setIsCreatingNinja(false);
      setEditingNinja(null);
      loadNinjas();
    } catch (err: any) {
      const errorMessage = err.message || err.response?.data?.message || 'Failed to save ninja';
      alert(errorMessage);
      console.error(err);
    }
  };

  const handleDeleteNinja = async (id: number, name: string) => {
    if (!confirm(`Are you sure you want to delete ${name}?`)) return;
    try {
      await ninjaApi.delete(id);
      loadNinjas();
    } catch (err) {
      alert('Failed to delete ninja');
      console.error(err);
    }
  };

  const handleAwardBux = async () => {
    if (!buxNinjaId || buxAmount <= 0) return;
    try {
      await ninjaApi.awardBux(buxNinjaId, buxAmount);
      setBuxNinjaId(null);
      setBuxAmount(0);
      loadNinjas();
    } catch (err) {
      alert('Failed to award Bux');
      console.error(err);
    }
  };

  const handleDeductBux = async () => {
    if (!buxNinjaId || buxAmount <= 0) return;
    try {
      await ninjaApi.deductBux(buxNinjaId, buxAmount);
      setBuxNinjaId(null);
      setBuxAmount(0);
      loadNinjas();
    } catch (err) {
      alert('Failed to deduct Bux');
      console.error(err);
    }
  };

  // Shop item management functions
  const handleCreateItem = () => {
    setIsCreatingItem(true);
    setEditingItem(null);
    setItemFormData({ name: '', description: '', price: 0, category: '' });
  };

  const handleEditItem = (item: ShopItem) => {
    setEditingItem(item);
    setIsCreatingItem(false);
    setItemFormData({
      name: item.name,
      description: item.description,
      price: item.price,
      category: item.category,
    });
  };

  const handleSubmitItem = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (isCreatingItem) {
        await shopApi.createItem(itemFormData);
      } else if (editingItem) {
        await shopApi.updateItem(editingItem.id, itemFormData);
      }
      setIsCreatingItem(false);
      setEditingItem(null);
      loadShopItems();
    } catch (err) {
      alert('Failed to save item');
      console.error(err);
    }
  };

  const handleDeleteItem = async (id: number, name: string) => {
    if (!confirm(`Are you sure you want to delete "${name}"?`)) return;
    try {
      await shopApi.deleteItem(id);
      loadShopItems();
    } catch (err) {
      alert('Failed to delete item');
      console.error(err);
    }
  };

  const handleToggleItemAvailability = async (id: number, available: boolean) => {
    try {
      await shopApi.updateItemAvailability(id, !available);
      loadShopItems();
    } catch (err) {
      alert('Failed to update availability');
      console.error(err);
    }
  };

  // Big Question management functions
  const handleCreateQuestion = () => {
    setIsCreatingQuestion(true);
    setEditingQuestion(null);
    setQuestionFormData({
      questionDate: new Date().toISOString().split('T')[0],
      questionText: '',
      questionType: 'MULTIPLE_CHOICE',
      correctAnswer: '',
      correctChoiceIndex: 0,
      choices: ['', '', '', ''],
    });
  };

  const handleEditQuestion = (question: BigQuestion) => {
    setEditingQuestion(question);
    setIsCreatingQuestion(false);
    setQuestionFormData({
      questionDate: question.questionDate,
      questionText: question.questionText,
      questionType: question.questionType,
      correctAnswer: question.correctAnswer || '',
      correctChoiceIndex: question.correctChoiceIndex ?? 0,
      choices: question.choices || ['', '', '', ''],
    });
  };

  const handleSubmitQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const data: CreateBigQuestionRequest = {
        questionDate: questionFormData.questionDate,
        questionText: questionFormData.questionText,
        questionType: questionFormData.questionType,
      };

      if (questionFormData.questionType === 'MULTIPLE_CHOICE') {
        data.choices = questionFormData.choices.filter(c => c.trim());
        data.correctChoiceIndex = questionFormData.correctChoiceIndex;
      } else {
        data.correctAnswer = questionFormData.correctAnswer;
      }

      if (isCreatingQuestion) {
        await bigQuestionApi.createQuestion(data);
      } else if (editingQuestion) {
        await bigQuestionApi.updateQuestion(editingQuestion.id, data);
      }
      setIsCreatingQuestion(false);
      setEditingQuestion(null);
      loadQuestions();
    } catch (err) {
      alert('Failed to save question');
      console.error(err);
    }
  };

  const handleDeleteQuestion = async (id: number) => {
    if (!confirm('Are you sure you want to delete this question? You can restore it later.')) return;
    try {
      await bigQuestionApi.deleteQuestion(id);
      loadQuestions();
    } catch (err) {
      alert('Failed to delete question');
      console.error(err);
    }
  };

  const handleRestoreQuestion = async (id: number) => {
    try {
      await bigQuestionApi.restoreQuestion(id);
      loadQuestions();
    } catch (err) {
      alert('Failed to restore question');
      console.error(err);
    }
  };

  const handleRedeemPurchase = async (purchaseId: number, itemName: string) => {
    if (!confirm(`Mark "${itemName}" as redeemed?`)) return;
    try {
      await shopApi.redeemPurchase(purchaseId);
      loadNinjas();
    } catch (err) {
      alert('Failed to redeem purchase');
      console.error(err);
    }
  };

  const handleRefundPurchase = async (purchaseId: number, itemName: string) => {
    if (!confirm(`Refund "${itemName}"?`)) return;
    try {
      await shopApi.refundPurchase(purchaseId);
      loadNinjas();
    } catch (err) {
      alert('Failed to refund purchase');
      console.error(err);
    }
  };

  // Achievement management functions
  const handleCreateAchievement = () => {
    setIsCreatingAchievement(true);
    setEditingAchievement(null);
    setAchievementFormData({
      name: '',
      description: '',
      category: 'PROGRESS',
      rarity: 'COMMON',
      icon: '🎯',
      buxReward: 0,
      manualOnly: false,
      unlockCriteria: '',
      active: true,
      hidden: false,
    });
  };

  const handleEditAchievement = (achievement: Achievement) => {
    setEditingAchievement(achievement);
    setIsCreatingAchievement(false);
    setAchievementFormData({
      name: achievement.name,
      description: achievement.description,
      category: achievement.category,
      rarity: achievement.rarity,
      icon: achievement.icon,
      buxReward: achievement.buxReward,
      manualOnly: achievement.manualOnly,
      unlockCriteria: achievement.unlockCriteria || '',
      active: achievement.active,
      hidden: achievement.hidden,
    });
  };

  const handleSaveAchievement = async () => {
    try {
      if (editingAchievement) {
        await achievementApi.update(editingAchievement.id, achievementFormData);
      } else {
        await achievementApi.create(achievementFormData);
      }
      setIsCreatingAchievement(false);
      setEditingAchievement(null);
      loadAchievements();
    } catch (err: any) {
      alert(err.message || 'Failed to save achievement');
      console.error(err);
    }
  };

  const handleDeleteAchievement = async (id: number) => {
    if (!confirm('Are you sure you want to delete this achievement? This cannot be undone.')) {
      return;
    }
    try {
      await achievementApi.delete(id);
      loadAchievements();
    } catch (err) {
      alert('Failed to delete achievement');
      console.error(err);
    }
  };

  const handleToggleAchievementActive = async (id: number) => {
    try {
      await achievementApi.toggleActive(id);
      loadAchievements();
    } catch (err) {
      alert('Failed to toggle achievement status');
      console.error(err);
    }
  };

  // Admin management functions
  const handleCreateAdmin = () => {
    setIsCreatingAdmin(true);
    setAdminFormData({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      currentPassword: '',
    });
  };

  const handleSubmitAdmin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!admin) return;

    try {
      const data: CreateAdminByAdminRequest = {
        currentAdminUsername: admin.username,
        currentAdminPassword: adminFormData.currentPassword,
        username: adminFormData.username,
        email: adminFormData.email,
        password: adminFormData.password,
        firstName: adminFormData.firstName,
        lastName: adminFormData.lastName,
      };
      await adminApi.createAdmin(data);
      setIsCreatingAdmin(false);
      setAdminFormData({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        currentPassword: '',
      });
      alert('Admin account created successfully!');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to create admin account';
      alert(errorMessage);
      console.error(err);
    }
  };

  const handleChangePassword = () => {
    setIsChangingPassword(true);
    setPasswordFormData({
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    });
  };

  const handleSubmitPasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!admin) return;

    if (passwordFormData.newPassword !== passwordFormData.confirmPassword) {
      alert('New passwords do not match');
      return;
    }

    if (passwordFormData.newPassword.length < 3) {
      alert('New password must be at least 3 characters long');
      return;
    }

    try {
      const data: ChangePasswordRequest = {
        oldPassword: passwordFormData.oldPassword,
        newPassword: passwordFormData.newPassword,
      };
      await adminApi.changePassword(admin.username, data);
      setIsChangingPassword(false);
      setPasswordFormData({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      alert('Password changed successfully!');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to change password';
      alert(errorMessage);
      console.error(err);
    }
  };

  const loadAllAdmins = async () => {
    if (!admin || !adminListPassword) return;
    try {
      const admins = await adminApi.getAllAdmins(admin.username, adminListPassword);
      setAllAdmins(admins);
    } catch (err: any) {
      if (err.response?.status === 401) {
        alert('Invalid password');
        setAdminListPassword('');
        setShowAdminList(false);
      } else {
        alert('Failed to load admin list');
      }
      console.error(err);
    }
  };

  const handleShowAdminList = async () => {
    const password = prompt('Enter your password to view admin accounts:');
    if (!password) return;
    setAdminListPassword(password);
    setShowAdminList(true);
    try {
      if (!admin) return;
      const admins = await adminApi.getAllAdmins(admin.username, password);
      setAllAdmins(admins);
    } catch (err: any) {
      if (err.response?.status === 401) {
        alert('Invalid password');
        setAdminListPassword('');
        setShowAdminList(false);
      } else {
        alert('Failed to load admin list');
      }
      console.error(err);
    }
  };

  const handleDeleteAdmin = async (adminToDelete: Admin) => {
    if (!admin) return;
    
    if (adminToDelete.id === admin.id) {
      alert('You cannot delete your own account');
      return;
    }

    const emailPart = adminToDelete.email ? ` (${adminToDelete.email})` : '';
    if (!confirm(`Are you sure you want to delete admin account "${adminToDelete.username}"${emailPart}?`)) {
      return;
    }

    const password = prompt('Enter your password to confirm deletion:');
    if (!password) return;

    try {
      setDeletingAdminId(adminToDelete.id);
      await adminApi.deleteAdmin(adminToDelete.id, admin.username, password);
      await loadAllAdmins();
      alert('Admin account deleted successfully');
    } catch (err: any) {
      if (err.response?.status === 401) {
        alert('Invalid password');
      } else if (err.response?.status === 400) {
        alert('Cannot delete your own account');
      } else {
        alert('Failed to delete admin account');
      }
      console.error(err);
    } finally {
      setDeletingAdminId(null);
    }
  };

  if (loading && ninjas.length === 0 && shopItems.length === 0 && questions.length === 0) {
    return <div className="admin-loading">Loading...</div>;
  }

  return (
    <div className="admin-container">
      <div className="admin-header">
        <h1>Admin Dashboard</h1>
        <button onClick={onLogout} className="btn-logout">Logout</button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* Audit Log Section - Always Visible */}
      <div className="audit-log-section">
        <div className="audit-header">
          <h3>Recent Activity</h3>
          <span className="audit-count">{auditLogs.length} recent actions</span>
        </div>
        <div className="audit-log-list">
          {auditLogs.slice(0, 5).map((log) => (
            <div key={log.id} className="audit-log-item">
              <div className="audit-info">
                <span className="audit-action">{log.action.replace(/_/g, ' ')}</span>
                <span className="audit-details">{log.details}</span>
                {log.targetNinjaName && (
                  <span className="audit-target">→ {log.targetNinjaName}</span>
                )}
              </div>
              <div className="audit-meta">
                <span className="audit-user">{log.adminUsername}</span>
                <span className="audit-time">
                  {new Date(log.timestamp).toLocaleString()}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="admin-tabs">
        <button
          className={`tab-btn ${activeTab === 'ninjas' ? 'active' : ''}`}
          onClick={() => setActiveTab('ninjas')}
        >
          Manage Ninjas
        </button>
        <button
          className={`tab-btn ${activeTab === 'shop' ? 'active' : ''}`}
          onClick={() => setActiveTab('shop')}
        >
          Manage Shop
        </button>
        <button
          className={`tab-btn ${activeTab === 'question' ? 'active' : ''}`}
          onClick={() => setActiveTab('question')}
        >
          Big Question
        </button>
        <button
          className={`tab-btn ${activeTab === 'achievements' ? 'active' : ''}`}
          onClick={() => setActiveTab('achievements')}
        >
          Achievements
        </button>
        <button
          className={`tab-btn ${activeTab === 'settings' ? 'active' : ''}`}
          onClick={() => setActiveTab('settings')}
        >
          Settings
        </button>
      </div>

      {/* NINJAS TAB */}
      {activeTab === 'ninjas' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>Ninjas ({ninjas.length})</h2>
            <button onClick={handleCreateNinja} className="btn-primary">+ Create Ninja</button>
          </div>

          {(isCreatingNinja || editingNinja) && (
            <div className="form-card">
              <h3>{isCreatingNinja ? 'Create New Ninja' : 'Edit Ninja'}</h3>
              <form onSubmit={handleSubmitNinja}>
                <div className="form-grid">
                  <div className="form-field">
                    <label>First Name</label>
                    <input
                      type="text"
                      value={ninjaFormData.firstName}
                      onChange={(e) => setNinjaFormData({ ...ninjaFormData, firstName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>Last Name</label>
                    <input
                      type="text"
                      value={ninjaFormData.lastName}
                      onChange={(e) => setNinjaFormData({ ...ninjaFormData, lastName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>Username</label>
                    <input
                      type="text"
                      value={ninjaFormData.username}
                      onChange={(e) => setNinjaFormData({ ...ninjaFormData, username: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>Belt</label>
                    <select
                      value={ninjaFormData.currentBeltType}
                      onChange={(e) => setNinjaFormData({ ...ninjaFormData, currentBeltType: e.target.value as BeltType })}
                    >
                      <option value="WHITE">White</option>
                      <option value="YELLOW">Yellow</option>
                      <option value="ORANGE">Orange</option>
                      <option value="GREEN">Green</option>
                      <option value="BLUE">Blue</option>
                      <option value="PURPLE" disabled style={{color: '#999'}}>Purple (Coming Soon)</option>
                      <option value="RED" disabled style={{color: '#999'}}>Red (Coming Soon)</option>
                      <option value="BROWN" disabled style={{color: '#999'}}>Brown (Coming Soon)</option>
                      <option value="BLACK" disabled style={{color: '#999'}}>Black (Coming Soon)</option>
                    </select>
                  </div>
                  <div className="form-field">
                    <label>Level</label>
                    <input
                      type="number"
                      value={ninjaFormData.currentLevel}
                      onChange={(e) => setNinjaFormData({ ...ninjaFormData, currentLevel: parseInt(e.target.value) })}
                      min="0"
                    />
                  </div>
                  <div className="form-field">
                    <label>Lesson</label>
                    <input
                      type="number"
                      value={ninjaFormData.currentLesson}
                      onChange={(e) => setNinjaFormData({ ...ninjaFormData, currentLesson: parseInt(e.target.value) })}
                      min="0"
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn-primary">Save</button>
                  <button type="button" onClick={() => { setIsCreatingNinja(false); setEditingNinja(null); }} className="btn-secondary">Cancel</button>
                </div>
              </form>
            </div>
          )}

          <div className="data-table">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Username</th>
                  <th>Belt</th>
                  <th>Level</th>
                  <th>Lesson</th>
                  <th>Bux</th>
                  <th>Unredeemed</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {ninjas.map((ninja) => (
                  <tr key={ninja.id} onClick={() => navigate(`/admin/ninja/${ninja.id}`)} className="clickable-row">
                    <td>{ninja.firstName} {ninja.lastName}</td>
                    <td>{ninja.username}</td>
                    <td><span className="belt-badge">{ninja.currentBeltType}</span></td>
                    <td>{ninja.currentLevel}</td>
                    <td>{ninja.currentLesson}</td>
                    <td><strong>{ninja.buxBalance}</strong></td>
                    <td>
                      {ninja.unredeemedPurchases && ninja.unredeemedPurchases.length > 0 ? (
                        <span className="badge-danger">{ninja.unredeemedPurchases.length}</span>
                      ) : '-'}
                    </td>
                    <td onClick={(e) => e.stopPropagation()}>
                      <button onClick={() => handleDeleteNinja(ninja.id, `${ninja.firstName} ${ninja.lastName}`)} className="btn-sm btn-delete">Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="form-card" style={{ marginTop: '2rem' }}>
            <h3>Award/Deduct Bux</h3>
            <div className="form-grid">
              <div className="form-field">
                <label>Select Ninja</label>
                <select
                  value={buxNinjaId || ''}
                  onChange={(e) => setBuxNinjaId(Number(e.target.value))}
                >
                  <option value="">-- Select Ninja --</option>
                  {ninjas.map(ninja => (
                    <option key={ninja.id} value={ninja.id}>
                      {ninja.firstName} {ninja.lastName} (Current: {ninja.buxBalance})
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-field">
                <label>Amount</label>
                <input
                  type="number"
                  value={buxAmount}
                  onChange={(e) => setBuxAmount(Number(e.target.value))}
                  min="1"
                />
              </div>
            </div>
            <div className="form-actions">
              <button onClick={handleAwardBux} className="btn-success" disabled={!buxNinjaId || buxAmount <= 0}>
                Award Bux
              </button>
              <button onClick={handleDeductBux} className="btn-warning" disabled={!buxNinjaId || buxAmount <= 0}>
                Deduct Bux
              </button>
            </div>
          </div>
        </div>
      )}

      {/* SHOP TAB */}
      {activeTab === 'shop' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>Shop Items ({shopItems.length})</h2>
            <button onClick={handleCreateItem} className="btn-primary">+ Create Item</button>
          </div>

          {(isCreatingItem || editingItem) && (
            <div className="form-card">
              <h3>{isCreatingItem ? 'Create New Item' : 'Edit Item'}</h3>
              <form onSubmit={handleSubmitItem}>
                <div className="form-grid">
                  <div className="form-field">
                    <label>Item Name</label>
                    <input
                      type="text"
                      value={itemFormData.name}
                      onChange={(e) => setItemFormData({ ...itemFormData, name: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>Price (Bux)</label>
                    <input
                      type="number"
                      value={itemFormData.price}
                      onChange={(e) => setItemFormData({ ...itemFormData, price: Number(e.target.value) })}
                      min="0"
                      required
                    />
                  </div>
                  <div className="form-field full-width">
                    <label>Description</label>
                    <textarea
                      value={itemFormData.description}
                      onChange={(e) => setItemFormData({ ...itemFormData, description: e.target.value })}
                      required
                      rows={3}
                    />
                  </div>
                  <div className="form-field">
                    <label>Category</label>
                    <input
                      type="text"
                      value={itemFormData.category}
                      onChange={(e) => setItemFormData({ ...itemFormData, category: e.target.value })}
                      required
                      placeholder="e.g., snacks, fun, break-time"
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn-primary">Save</button>
                  <button type="button" onClick={() => { setIsCreatingItem(false); setEditingItem(null); }} className="btn-secondary">Cancel</button>
                </div>
              </form>
            </div>
          )}

          <div className="items-grid">
            {shopItems.map((item) => (
              <div key={item.id} className={`item-card ${!item.available ? 'unavailable' : ''}`}>
                <div className="item-card-header">
                  <h3>{item.name}</h3>
                  <span className="item-price">{item.price} Bux</span>
                </div>
                <p className="item-description">{item.description}</p>
                <div className="item-meta">
                  <span className="category-tag">{item.category}</span>
                  <span className={`status-badge ${item.available ? 'available' : 'unavailable'}`}>
                    {item.available ? 'Available' : 'Unavailable'}
                  </span>
                </div>
                <div className="item-actions">
                  <button onClick={() => handleEditItem(item)} className="btn-sm btn-edit">Edit</button>
                  <button
                    onClick={() => handleToggleItemAvailability(item.id, item.available)}
                    className={`btn-sm ${item.available ? 'btn-warning' : 'btn-success'}`}
                  >
                    {item.available ? 'Disable' : 'Enable'}
                  </button>
                  <button onClick={() => handleDeleteItem(item.id, item.name)} className="btn-sm btn-delete">Delete</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* BIG QUESTION TAB */}
      {activeTab === 'question' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>Big Question of the Day</h2>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button onClick={handleCreateQuestion} className="btn-primary">+ Create Question</button>
              {deletedQuestions.length > 0 && (
                <button onClick={() => setShowDeleted(!showDeleted)} className="btn-secondary">
                  {showDeleted ? 'Hide Deleted' : `Show Deleted (${deletedQuestions.length})`}
                </button>
              )}
            </div>
          </div>

          {(isCreatingQuestion || editingQuestion) && (
            <div className="form-card">
              <h3>{isCreatingQuestion ? 'Create New Question' : 'Edit Question'}</h3>
              <form onSubmit={handleSubmitQuestion}>
                <div className="form-grid">
                  <div className="form-field">
                    <label>Date</label>
                    <input
                      type="date"
                      value={questionFormData.questionDate}
                      onChange={(e) => setQuestionFormData({ ...questionFormData, questionDate: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>Question Type</label>
                    <select
                      value={questionFormData.questionType}
                      onChange={(e) => setQuestionFormData({
                        ...questionFormData,
                        questionType: e.target.value as 'MULTIPLE_CHOICE' | 'SHORT_ANSWER'
                      })}
                    >
                      <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                      <option value="SHORT_ANSWER">Short Answer</option>
                    </select>
                  </div>
                  <div className="form-field full-width">
                    <label>Question Text</label>
                    <textarea
                      value={questionFormData.questionText}
                      onChange={(e) => setQuestionFormData({ ...questionFormData, questionText: e.target.value })}
                      required
                      rows={3}
                    />
                  </div>
                  {questionFormData.questionType === 'SHORT_ANSWER' && (
                    <div className="form-field full-width">
                      <label>Correct Answer</label>
                      <input
                        type="text"
                        value={questionFormData.correctAnswer}
                        onChange={(e) => setQuestionFormData({ ...questionFormData, correctAnswer: e.target.value })}
                        required
                      />
                    </div>
                  )}
                  {questionFormData.questionType === 'MULTIPLE_CHOICE' && (
                    <>
                      <div className="form-field">
                        <label>Choice 1</label>
                        <input
                          type="text"
                          value={questionFormData.choices[0]}
                          onChange={(e) => {
                            const newChoices = [...questionFormData.choices];
                            newChoices[0] = e.target.value;
                            setQuestionFormData({ ...questionFormData, choices: newChoices });
                          }}
                        />
                      </div>
                      <div className="form-field">
                        <label>Choice 2</label>
                        <input
                          type="text"
                          value={questionFormData.choices[1]}
                          onChange={(e) => {
                            const newChoices = [...questionFormData.choices];
                            newChoices[1] = e.target.value;
                            setQuestionFormData({ ...questionFormData, choices: newChoices });
                          }}
                        />
                      </div>
                      <div className="form-field">
                        <label>Choice 3</label>
                        <input
                          type="text"
                          value={questionFormData.choices[2]}
                          onChange={(e) => {
                            const newChoices = [...questionFormData.choices];
                            newChoices[2] = e.target.value;
                            setQuestionFormData({ ...questionFormData, choices: newChoices });
                          }}
                        />
                      </div>
                      <div className="form-field">
                        <label>Choice 4</label>
                        <input
                          type="text"
                          value={questionFormData.choices[3]}
                          onChange={(e) => {
                            const newChoices = [...questionFormData.choices];
                            newChoices[3] = e.target.value;
                            setQuestionFormData({ ...questionFormData, choices: newChoices });
                          }}
                        />
                      </div>
                      <div className="form-field full-width">
                        <label>Correct Choice</label>
                        <select
                          value={questionFormData.correctChoiceIndex}
                          onChange={(e) => setQuestionFormData({ ...questionFormData, correctChoiceIndex: Number(e.target.value) })}
                          required
                        >
                          <option value={0}>Choice 1</option>
                          <option value={1}>Choice 2</option>
                          <option value={2}>Choice 3</option>
                          <option value={3}>Choice 4</option>
                        </select>
                      </div>
                    </>
                  )}
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn-primary">Save</button>
                  <button type="button" onClick={() => { setIsCreatingQuestion(false); setEditingQuestion(null); }} className="btn-secondary">Cancel</button>
                </div>
              </form>
            </div>
          )}

          {showDeleted && deletedQuestions.length > 0 && (
            <div className="deleted-questions-section" style={{ marginBottom: '2rem' }}>
              <h3 style={{ color: '#e53e3e', marginBottom: '1rem' }}>Deleted Questions</h3>
              <div className="questions-list">
                {deletedQuestions.map((question) => (
                  <div key={question.id} className="question-card" style={{ opacity: 0.7, borderLeftColor: '#e53e3e' }}>
                    <div className="question-header">
                      <div>
                        <h3>{new Date(question.questionDate).toLocaleDateString()}</h3>
                        <span className={`type-badge ${question.questionType.toLowerCase()}`}>
                          {question.questionType === 'MULTIPLE_CHOICE' ? 'Multiple Choice' : 'Short Answer'}
                        </span>
                      </div>
                      <div className="question-actions">
                        <button onClick={() => handleRestoreQuestion(question.id)} className="btn-sm btn-success">Restore</button>
                      </div>
                    </div>
                    <p className="question-text">{question.questionText}</p>
                    {question.questionType === 'MULTIPLE_CHOICE' && question.choices && (
                      <>
                        <div className="choices-preview">
                          {question.choices.map((choice, idx) => (
                            <div key={idx} className={`choice-item ${idx === question.correctChoiceIndex ? 'correct-choice' : ''}`}>
                              {idx + 1}. {choice}
                            </div>
                          ))}
                        </div>
                        <div className="answer-preview">
                          <strong>Correct:</strong> Choice {(question.correctChoiceIndex ?? 0) + 1}
                        </div>
                      </>
                    )}
                    {question.questionType === 'SHORT_ANSWER' && (
                      <div className="answer-preview">
                        <strong>Answer:</strong> {question.correctAnswer}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="questions-list">
            {questions.map((question) => (
              <div key={question.id} className="question-card">
                <div className="question-header">
                  <div>
                    <h3>{new Date(question.questionDate).toLocaleDateString()}</h3>
                    <span className={`type-badge ${question.questionType.toLowerCase()}`}>
                      {question.questionType === 'MULTIPLE_CHOICE' ? 'Multiple Choice' : 'Short Answer'}
                    </span>
                  </div>
                  <div className="question-actions">
                    <button onClick={() => handleEditQuestion(question)} className="btn-sm btn-edit">Edit</button>
                    <button onClick={() => handleDeleteQuestion(question.id)} className="btn-sm btn-delete">Delete</button>
                  </div>
                </div>
                <p className="question-text">{question.questionText}</p>
                {question.questionType === 'MULTIPLE_CHOICE' && question.choices && (
                  <>
                    <div className="choices-preview">
                      {question.choices.map((choice, idx) => (
                        <div key={idx} className={`choice-item ${idx === question.correctChoiceIndex ? 'correct-choice' : ''}`}>
                          {idx + 1}. {choice}
                        </div>
                      ))}
                    </div>
                    <div className="answer-preview">
                      <strong>Correct:</strong> Choice {(question.correctChoiceIndex ?? 0) + 1}
                    </div>
                  </>
                )}
                {question.questionType === 'SHORT_ANSWER' && (
                  <div className="answer-preview">
                    <strong>Answer:</strong> {question.correctAnswer}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ACHIEVEMENTS TAB */}
      {activeTab === 'achievements' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>Achievements ({achievements.length})</h2>
            <button className="primary-btn" onClick={handleCreateAchievement}>
              Create Achievement
            </button>
          </div>

          {/* Achievement Form */}
          {(isCreatingAchievement || editingAchievement) && (
            <div className="form-card">
              <h3>{editingAchievement ? 'Edit Achievement' : 'Create Achievement'}</h3>
              <div className="form-group">
                <label>Name:</label>
                <input
                  type="text"
                  value={achievementFormData.name}
                  onChange={(e) => setAchievementFormData({ ...achievementFormData, name: e.target.value })}
                  placeholder="Achievement name"
                />
              </div>
              <div className="form-group">
                <label>Description:</label>
                <textarea
                  value={achievementFormData.description}
                  onChange={(e) => setAchievementFormData({ ...achievementFormData, description: e.target.value })}
                  placeholder="Achievement description"
                  rows={3}
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Icon (Emoji):</label>
                  <input
                    type="text"
                    value={achievementFormData.icon}
                    onChange={(e) => setAchievementFormData({ ...achievementFormData, icon: e.target.value })}
                    placeholder="🎯"
                    maxLength={4}
                  />
                </div>
                <div className="form-group">
                  <label>Bux Reward:</label>
                  <input
                    type="number"
                    value={achievementFormData.buxReward}
                    onChange={(e) => setAchievementFormData({ ...achievementFormData, buxReward: parseInt(e.target.value) || 0 })}
                    min="0"
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Category:</label>
                  <select
                    value={achievementFormData.category}
                    onChange={(e) => setAchievementFormData({ ...achievementFormData, category: e.target.value as AchievementCategory })}
                  >
                    <option value="PROGRESS">Progress</option>
                    <option value="QUIZ">Quiz Champion</option>
                    <option value="PURCHASE">Shop Master</option>
                    <option value="STREAK">Consistency</option>
                    <option value="SOCIAL">Social</option>
                    <option value="SPECIAL">Special</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Rarity:</label>
                  <select
                    value={achievementFormData.rarity}
                    onChange={(e) => setAchievementFormData({ ...achievementFormData, rarity: e.target.value as BadgeRarity })}
                  >
                    <option value="COMMON">Common</option>
                    <option value="RARE">Rare</option>
                    <option value="EPIC">Epic</option>
                    <option value="LEGENDARY">Legendary</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Unlock Criteria (JSON):</label>
                <textarea
                  value={achievementFormData.unlockCriteria}
                  onChange={(e) => setAchievementFormData({ ...achievementFormData, unlockCriteria: e.target.value })}
                  placeholder='{"type":"LESSONS_COMPLETED","threshold":10}'
                  rows={2}
                />
                <small>Leave empty for manual-only achievements</small>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={achievementFormData.manualOnly}
                      onChange={(e) => setAchievementFormData({ ...achievementFormData, manualOnly: e.target.checked })}
                    />
                    {' '}Manual Award Only
                  </label>
                </div>
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={achievementFormData.hidden}
                      onChange={(e) => setAchievementFormData({ ...achievementFormData, hidden: e.target.checked })}
                    />
                    {' '}Hidden (Secret Achievement)
                  </label>
                </div>
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={achievementFormData.active}
                      onChange={(e) => setAchievementFormData({ ...achievementFormData, active: e.target.checked })}
                    />
                    {' '}Active
                  </label>
                </div>
              </div>
              <div className="form-actions">
                <button className="primary-btn" onClick={handleSaveAchievement}>
                  {editingAchievement ? 'Update' : 'Create'} Achievement
                </button>
                <button
                  className="secondary-btn"
                  onClick={() => {
                    setIsCreatingAchievement(false);
                    setEditingAchievement(null);
                  }}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}

          {/* Achievement List */}
          <div className="items-grid">
            {achievements.map((achievement) => (
              <div key={achievement.id} className={`achievement-card rarity-${achievement.rarity}`}>
                <div className="item-header">
                  <div className="achievement-header">
                    <span className="achievement-icon">
                      <AchievementIcon icon={achievement.icon} size={24} />
                    </span>
                    <div>
                      <h3 style={{ margin: 0, marginBottom: '0.25rem' }}>{achievement.name}</h3>
                      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                        <span className="badge" style={{
                          background: achievement.rarity === 'LEGENDARY' ? '#F59E0B' :
                                     achievement.rarity === 'EPIC' ? '#A855F7' :
                                     achievement.rarity === 'RARE' ? '#3B82F6' : '#9CA3AF'
                        }}>
                          {achievement.rarity}
                        </span>
                        <span style={{ color: '#666', fontSize: '0.85em' }}>{achievement.category}</span>
                      </div>
                    </div>
                  </div>
                  <div className="item-actions">
                    <button className="icon-btn" onClick={() => handleEditAchievement(achievement)} title="Edit">
                      <FiEdit2 />
                    </button>
                    <button className="icon-btn" onClick={() => handleDeleteAchievement(achievement.id)} title="Delete">
                      <FiTrash2 />
                    </button>
                  </div>
                </div>

                <p style={{ marginBottom: '1rem', color: '#4a5568' }}>{achievement.description}</p>

                <div className="achievement-meta">
                  <div style={{ flex: 1 }}>
                    <strong style={{ color: '#0db88f' }}>💰 {achievement.buxReward} Bux</strong>
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    {achievement.manualOnly && <span className="badge" style={{ background: '#805AD5' }}>Manual Only</span>}
                    {achievement.hidden && <span className="badge" style={{ background: '#718096' }}>🔒 Hidden</span>}
                    {!achievement.active && <span className="badge" style={{ background: '#E53E3E' }}>Inactive</span>}
                  </div>
                </div>

                {achievement.unlockCriteria && (
                  <div className="achievement-criteria">
                    <strong style={{ fontSize: '0.75rem', color: '#718096', textTransform: 'uppercase' }}>
                      Auto-Unlock Criteria:
                    </strong>
                    <div style={{ marginTop: '0.25rem' }}>{achievement.unlockCriteria}</div>
                  </div>
                )}

                <button
                  className={achievement.active ? 'secondary-btn' : 'primary-btn'}
                  onClick={() => handleToggleAchievementActive(achievement.id)}
                  style={{ marginTop: '1rem', width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                >
                  {achievement.active ? <><FiPause /> Deactivate</> : <><FiPlay /> Activate</>}
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SETTINGS TAB */}
      {activeTab === 'settings' && (
        <div className="tab-content">
          <div className="section-header">
            <h2>Settings</h2>
          </div>

          {admin && (
            <div className="form-card">
              <h3>Current Admin Account</h3>
              <div className="form-grid">
                <div className="form-field">
                  <label>Username</label>
                  <input type="text" value={admin.username} disabled />
                </div>
                <div className="form-field">
                  <label>Email</label>
                  <input type="email" value={admin.email || '-'} disabled />
                </div>
                <div className="form-field">
                  <label>First Name</label>
                  <input type="text" value={admin.firstName} disabled />
                </div>
                <div className="form-field">
                  <label>Last Name</label>
                  <input type="text" value={admin.lastName} disabled />
                </div>
                <div className="form-field">
                  <label>Permissions</label>
                  <input
                    type="text"
                    value={admin.canCreateAdmins ? 'Can create admin accounts' : 'Standard admin'}
                    disabled
                  />
                </div>
              </div>
            </div>
          )}

          <div className="form-card">
            <div className="section-header">
              <h3>Change Password</h3>
              {!isChangingPassword && (
                <button onClick={handleChangePassword} className="btn-primary">
                  Change Password
                </button>
              )}
            </div>

            {isChangingPassword && (
              <form onSubmit={handleSubmitPasswordChange}>
                <div className="form-grid">
                  <div className="form-field">
                    <label>Current Password</label>
                    <input
                      type="password"
                      value={passwordFormData.oldPassword}
                      onChange={(e) => setPasswordFormData({ ...passwordFormData, oldPassword: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>New Password</label>
                    <input
                      type="password"
                      value={passwordFormData.newPassword}
                      onChange={(e) => setPasswordFormData({ ...passwordFormData, newPassword: e.target.value })}
                      required
                      minLength={3}
                    />
                  </div>
                  <div className="form-field">
                    <label>Confirm New Password</label>
                    <input
                      type="password"
                      value={passwordFormData.confirmPassword}
                      onChange={(e) => setPasswordFormData({ ...passwordFormData, confirmPassword: e.target.value })}
                      required
                      minLength={3}
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn-primary">Save</button>
                  <button
                    type="button"
                    onClick={() => {
                      setIsChangingPassword(false);
                      setPasswordFormData({ oldPassword: '', newPassword: '', confirmPassword: '' });
                    }}
                    className="btn-secondary"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>

          {admin && admin.canCreateAdmins && (
            <>
              <div className="form-card">
                <div className="section-header">
                  <h3>Admin Accounts</h3>
                  {!showAdminList && (
                    <button onClick={handleShowAdminList} className="btn-primary">
                      List Admin Accounts
                    </button>
                  )}
                </div>

                {showAdminList && (
                  <div>
                    <div className="data-table" style={{ marginTop: '1rem' }}>
                      <table>
                        <thead>
                          <tr>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Name</th>
                            <th>Permissions</th>
                            <th>Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {allAdmins.map((adminItem) => (
                            <tr key={adminItem.id}>
                              <td>{adminItem.username}</td>
                              <td>{adminItem.email || '-'}</td>
                              <td>{adminItem.firstName} {adminItem.lastName}</td>
                              <td>
                                {adminItem.canCreateAdmins ? (
                                  <span className="badge-success">Can create admins</span>
                                ) : (
                                  <span>Standard admin</span>
                                )}
                              </td>
                              <td>
                                {adminItem.id !== admin?.id && (
                                  <button
                                    onClick={() => handleDeleteAdmin(adminItem)}
                                    className="btn-sm btn-delete"
                                    disabled={deletingAdminId === adminItem.id}
                                  >
                                    {deletingAdminId === adminItem.id ? 'Deleting...' : 'Delete'}
                                  </button>
                                )}
                                {adminItem.id === admin?.id && (
                                  <span style={{ color: '#666', fontSize: '0.9em' }}>Current user</span>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                    <div style={{ marginTop: '1rem' }}>
                      <button
                        onClick={() => {
                          setShowAdminList(false);
                          setAdminListPassword('');
                          setAllAdmins([]);
                        }}
                        className="btn-secondary"
                      >
                        Hide Admin List
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="form-card">
                <div className="section-header">
                  <h3>Create Admin Account</h3>
                  {!isCreatingAdmin && (
                    <button onClick={handleCreateAdmin} className="btn-primary">
                      + Create Admin
                    </button>
                  )}
                </div>

              {isCreatingAdmin && (
                <form onSubmit={handleSubmitAdmin}>
                  <div className="form-grid">
                    <div className="form-field">
                      <label>Your Password (for verification)</label>
                      <input
                        type="password"
                        value={adminFormData.currentPassword}
                        onChange={(e) => setAdminFormData({ ...adminFormData, currentPassword: e.target.value })}
                        required
                      />
                    </div>
                    <div className="form-field">
                      <label>Username</label>
                      <input
                        type="text"
                        value={adminFormData.username}
                        onChange={(e) => setAdminFormData({ ...adminFormData, username: e.target.value })}
                        required
                      />
                    </div>
                <div className="form-field">
                  <label>Email (Optional)</label>
                  <input
                    type="email"
                    value={adminFormData.email}
                    onChange={(e) => setAdminFormData({ ...adminFormData, email: e.target.value })}
                  />
                </div>
                    <div className="form-field">
                      <label>First Name</label>
                      <input
                        type="text"
                        value={adminFormData.firstName}
                        onChange={(e) => setAdminFormData({ ...adminFormData, firstName: e.target.value })}
                        required
                      />
                    </div>
                    <div className="form-field">
                      <label>Last Name</label>
                      <input
                        type="text"
                        value={adminFormData.lastName}
                        onChange={(e) => setAdminFormData({ ...adminFormData, lastName: e.target.value })}
                        required
                      />
                    </div>
                    <div className="form-field">
                      <label>Password</label>
                      <input
                        type="password"
                        value={adminFormData.password}
                        onChange={(e) => setAdminFormData({ ...adminFormData, password: e.target.value })}
                        required
                        minLength={3}
                      />
                    </div>
                  </div>
                  <div className="form-actions">
                    <button type="submit" className="btn-primary">Create</button>
                    <button
                      type="button"
                      onClick={() => {
                        setIsCreatingAdmin(false);
                        setAdminFormData({
                          username: '',
                          email: '',
                          password: '',
                          firstName: '',
                          lastName: '',
                          currentPassword: '',
                        });
                      }}
                      className="btn-secondary"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}
              </div>
            </>
          )}
        </div>
      )}

      {/* Ninja Details Modal */}
      {selectedNinja && (
        <div className="modal-overlay" onClick={() => setSelectedNinja(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selectedNinja.firstName} {selectedNinja.lastName}</h2>
              <button onClick={() => setSelectedNinja(null)} className="close-btn">&times;</button>
            </div>
            <div className="modal-body">
              <div className="detail-section">
                <h3>Ninja Information</h3>
                <div className="detail-grid">
                  <div><strong>Username:</strong> {selectedNinja.username}</div>
                  <div><strong>Belt:</strong> {selectedNinja.currentBeltType}</div>
                  <div><strong>Level:</strong> {selectedNinja.currentLevel}</div>
                  <div><strong>Lesson:</strong> {selectedNinja.currentLesson}</div>
                  <div><strong>Total Earned:</strong> {selectedNinja.totalBuxEarned} Bux</div>
                  <div><strong>Total Spent:</strong> {selectedNinja.totalBuxSpent} Bux</div>
                  <div><strong>Current Balance:</strong> {selectedNinja.buxBalance} Bux</div>
                </div>
              </div>

              {selectedNinja.unredeemedPurchases && selectedNinja.unredeemedPurchases.length > 0 && (
                <div className="detail-section">
                  <h3>Unredeemed Purchases</h3>
                  {selectedNinja.unredeemedPurchases.map((purchase) => (
                    <div key={purchase.id} className="purchase-card">
                      <div className="purchase-info">
                        <h4>{purchase.itemName}</h4>
                        <p>{purchase.itemDescription}</p>
                        <small>Purchased: {new Date(purchase.purchaseDate).toLocaleDateString()}</small>
                      </div>
                      <div className="purchase-actions">
                        <button onClick={() => handleRedeemPurchase(purchase.id, purchase.itemName)} className="btn-sm btn-success">
                          Redeem
                        </button>
                        <button onClick={() => handleRefundPurchase(purchase.id, purchase.itemName)} className="btn-sm btn-warning">
                          Refund
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {selectedNinja.allPurchases && selectedNinja.allPurchases.filter(p => p.redeemed).length > 0 && (
                <div className="detail-section">
                  <h3>Redeemed Purchases</h3>
                  {selectedNinja.allPurchases.filter(p => p.redeemed).map((purchase) => (
                    <div key={purchase.id} className="purchase-card redeemed">
                      <div className="purchase-info">
                        <h4>{purchase.itemName}</h4>
                        <p>{purchase.itemDescription}</p>
                        <small>
                          Purchased: {new Date(purchase.purchaseDate).toLocaleDateString()}
                          {purchase.redeemedDate && ` | Redeemed: ${new Date(purchase.redeemedDate).toLocaleDateString()}`}
                        </small>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
