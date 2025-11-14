import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { ninjaApi, shopApi, bigQuestionApi, adminApi, achievementApi, analyticsApi, ledgerApi } from '../services/api';
import type { Ninja, Purchase, ShopItem, BigQuestion, CreateBigQuestionRequest, AdminAuditLog, Admin, CreateAdminByAdminRequest, ChangePasswordRequest, Achievement, AchievementCategory, BadgeRarity, CreateAchievementRequest, AwardAchievementRequest, AnalyticsSnapshot, LedgerTransaction } from '../types';
import { FiEdit2, FiTrash2, FiPause, FiPlay, FiUsers, FiShoppingBag, FiHelpCircle, FiAward, FiSettings, FiSearch, FiPlus, FiDollarSign, FiShoppingCart, FiTarget, FiTrendingUp, FiClock, FiLock } from 'react-icons/fi';
import { useToastContext } from '../context/ToastContext';
import Toast from '../components/Toast';
import ConfirmationModal from '../components/ConfirmationModal';
import NinjaFormModal, { type NinjaFormValues } from '../components/NinjaFormModal';
import AchievementIcon from '../components/AchievementIcon';
import { INITIAL_NINJA_PROGRESS, normalizeProgress } from '../utils/ninjaProgress';
import './AdminDashboard.css';

interface Props {
  onLogout: () => void;
  admin: Admin | null;
}

interface NinjaWithPurchases extends Ninja {
  unredeemedPurchases?: Purchase[];
  allPurchases?: Purchase[];
}

interface PendingSuggestion extends BigQuestion {
  question?: BigQuestion;
  ninjaName?: string;
  suggestionsBanned?: boolean;
}
interface QuestionFormState {
  questionDate: string;
  questionText: string;
  questionType: CreateBigQuestionRequest['questionType'];
  correctAnswer: string;
  correctChoiceIndex: number;
  choices: [string, string, string, string];
}

type TabType = 'activity' | 'ninjas' | 'shop' | 'question' | 'achievements' | 'analytics' | 'settings';
type OverviewSubTab = 'activity' | 'ledger' | 'announcement';

const getErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof Error && !('response' in error)) {
    return error.message || fallback;
  }
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return fallback;
};

const getErrorStatus = (error: unknown): number | undefined => {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    return (error as { response?: { status?: number } }).response?.status;
  }
  return undefined;
};

const normalizeChoices = (choices?: string[]): QuestionFormState['choices'] => ([
  choices?.[0] ?? '',
  choices?.[1] ?? '',
  choices?.[2] ?? '',
  choices?.[3] ?? '',
]);

// suggestion review component for pending questions
function SuggestionReviewList({
  suggestions,
  onReject,
  onBan
}: {
  suggestions: PendingSuggestion[];
  onReject: (id: number, reason: string) => void;
  onBan: (ninjaId: number, banned: boolean) => void;
}) {
  const [rejectStates, setRejectStates] = useState<Map<number, { showInput: boolean; reason: string }>>(new Map());

  const handleRejectClick = (id: number) => {
    setRejectStates(new Map(rejectStates.set(id, { showInput: true, reason: '' })));
  };

  const handleRejectConfirm = (id: number) => {
    const state = rejectStates.get(id);
    onReject(id, state?.reason || '');
    const newStates = new Map(rejectStates);
    newStates.delete(id);
    setRejectStates(newStates);
  };

  const handleRejectCancel = (id: number) => {
    const newStates = new Map(rejectStates);
    newStates.delete(id);
    setRejectStates(newStates);
  };

  if (suggestions.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', background: '#f8f9fa', borderRadius: '12px', marginBottom: '2rem' }}>
        <FiHelpCircle size={48} color="#ccc" style={{ marginBottom: '1rem' }} />
        <p style={{ color: '#666' }}>No pending suggestions</p>
      </div>
    );
  }

  return (
    <div className="suggestions-section" style={{ marginBottom: '2rem', padding: '1.5rem', background: '#f8f9fa', borderRadius: '12px', border: '2px solid #3B82F6' }}>
      <h3 style={{ color: '#3B82F6', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <FiHelpCircle /> Pending Suggestions ({suggestions.length})
      </h3>
      <div className="questions-list">
        {suggestions.map((suggestion) => {
          const question = suggestion.question || suggestion;
          const questionId = question.id || suggestion.id;
          const rejectState = rejectStates.get(questionId);
          const showRejectInput = rejectState?.showInput || false;
          const rejectReason = rejectState?.reason || '';

          const ninjaName = suggestion.ninjaName || (question.suggestedByNinjaId ? `(ID: ${question.suggestedByNinjaId})` : '');
          const suggestionsBanned = suggestion.suggestionsBanned ?? false;

          return (
            <div key={questionId} className="question-card" style={{ borderLeftColor: '#3B82F6', background: 'white' }}>
              <div className="question-header">
                <div>
                  <h3>Suggested by {ninjaName || 'Student'}</h3>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                    <span className="type-badge pending" style={{ background: '#3B82F6', color: 'white' }}>
                      PENDING
                    </span>
                    {suggestionsBanned && (
                      <span className="type-badge" style={{ background: '#dc2626', color: 'white', fontSize: '0.75rem' }}>
                        BANNED
                      </span>
                    )}
                  </div>
                </div>
                <div className="question-actions" style={{ display: 'flex', gap: '0.5rem' }}>
                  {!showRejectInput ? (
                    <>
                      {question.suggestedByNinjaId && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            onBan(question.suggestedByNinjaId!, !suggestionsBanned);
                          }}
                          className={`btn btn-sm ${suggestionsBanned ? 'btn-success' : 'btn-warning'}`}
                          title={suggestionsBanned ? 'Unban ninja from suggesting questions' : 'Ban ninja from suggesting questions'}
                        >
                          {suggestionsBanned ? 'Unban' : 'Ban'}
                        </button>
                      )}
                      <button
                        onClick={() => handleRejectClick(questionId)}
                        className="btn btn-sm btn-danger"
                      >
                        <FiTrash2 /> Reject
                      </button>
                    </>
                  ) : (
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flex: 1 }}>
                      <input
                        type="text"
                        placeholder="Rejection reason (required)"
                        value={rejectReason}
                        onChange={(e) => {
                          const newStates = new Map(rejectStates);
                          newStates.set(questionId, { showInput: true, reason: e.target.value });
                          setRejectStates(newStates);
                        }}
                        style={{ padding: '0.5rem', borderRadius: '4px', border: '1px solid #ddd', fontSize: '0.875rem', flex: 1 }}
                        required
                      />
                      <button
                        onClick={() => handleRejectConfirm(questionId)}
                        className="btn btn-sm btn-danger"
                        disabled={!rejectReason.trim()}
                      >
                        Confirm
                      </button>
                      <button
                        onClick={() => handleRejectCancel(questionId)}
                        className="btn btn-sm btn-secondary"
                      >
                        Cancel
                      </button>
                    </div>
                  )}
                </div>
              </div>
              <p className="question-text">{question.questionText}</p>
              {question.questionType === 'MULTIPLE_CHOICE' && question.choices && (
                <>
                  <div className="choices-preview">
                    {question.choices.map((choice: string, idx: number) => (
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
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default function AdminDashboard({ onLogout, admin }: Props) {
  const navigate = useNavigate();
  const { toasts, removeToast, success, error: showError } = useToastContext();

  // remember which tab admin was on so it persists across reloads
  const [activeTab, setActiveTab] = useState<TabType>(() => {
    const savedTab = localStorage.getItem('adminActiveTab');
    return (savedTab as TabType) || 'activity';
  });

  // save tab preference whenever it changes
  useEffect(() => {
    localStorage.setItem('adminActiveTab', activeTab);
  }, [activeTab]);
  const [ninjas, setNinjas] = useState<NinjaWithPurchases[]>([]);
  const [shopItems, setShopItems] = useState<ShopItem[]>([]);
  const [questions, setQuestions] = useState<BigQuestion[]>([]);
  const [pastQuestions, setPastQuestions] = useState<BigQuestion[]>([]);
  const [showPast, setShowPast] = useState(false);
  const [pendingSuggestions, setPendingSuggestions] = useState<PendingSuggestion[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [loading, setLoading] = useState(true);
  const [auditLogs, setAuditLogs] = useState<AdminAuditLog[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  // pagination state because loading everything breaks the browser
  const [ninjaPage, setNinjaPage] = useState(0);
  const [ninjaPageSize, setNinjaPageSize] = useState(25);
  const [ninjaSort, setNinjaSort] = useState<string>('name');
  const [ninjaSortDirection, setNinjaSortDirection] = useState<string>('ASC');
  const [ninjaBeltFilter, setNinjaBeltFilter] = useState<string>('');
  const [ninjaLockedFilter, setNinjaLockedFilter] = useState<boolean | undefined>(undefined);
  const [ninjaTotalPages, setNinjaTotalPages] = useState(0);
  const [ninjaTotalElements, setNinjaTotalElements] = useState(0);
  const [showFiltersAndSort, setShowFiltersAndSort] = useState(false);

  const [editingNinja, setEditingNinja] = useState<Ninja | null>(null);
  const [isCreatingNinja, setIsCreatingNinja] = useState(false);
  const [selectedNinja, setSelectedNinja] = useState<NinjaWithPurchases | null>(null);
  const createEmptyNinjaForm = (): NinjaFormValues => ({
    firstName: '',
    lastName: '',
    username: '',
    beltType: INITIAL_NINJA_PROGRESS.beltType,
    level: INITIAL_NINJA_PROGRESS.level,
    lesson: INITIAL_NINJA_PROGRESS.lesson,
  });
  const [ninjaFormInitialValues, setNinjaFormInitialValues] = useState<NinjaFormValues>(createEmptyNinjaForm());
  const [ninjaFormSubmitting, setNinjaFormSubmitting] = useState(false);

  const [editingItem, setEditingItem] = useState<ShopItem | null>(null);
  const [isCreatingItem, setIsCreatingItem] = useState(false);
  const [itemFormData, setItemFormData] = useState({
    name: '',
    description: '',
    price: 0,
    category: '',
  });

  const [editingQuestion, setEditingQuestion] = useState<BigQuestion | null>(null);
  const [isCreatingQuestion, setIsCreatingQuestion] = useState(false);
  const [selectedSuggestionId, setSelectedSuggestionId] = useState<number | null>(null);
  const createEmptyQuestionForm = (): QuestionFormState => ({
    questionDate: new Date().toISOString().split('T')[0],
    questionText: '',
    questionType: 'MULTIPLE_CHOICE',
    correctAnswer: '',
    correctChoiceIndex: 0,
    choices: normalizeChoices(),
  });

  const [questionFormData, setQuestionFormData] = useState<QuestionFormState>(createEmptyQuestionForm());

  const [isCreatingAdmin, setIsCreatingAdmin] = useState(false);
  const [adminFormData, setAdminFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    currentPassword: '',
  });

  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [passwordFormData, setPasswordFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [allAdmins, setAllAdmins] = useState<Admin[]>([]);
  const [adminListPassword, setAdminListPassword] = useState('');
  const [showAdminList, setShowAdminList] = useState(false);
  const [deletingAdminId, setDeletingAdminId] = useState<number | null>(null);

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

  const [assigningAchievement, setAssigningAchievement] = useState(false);
  const [selectedAchievementId, setSelectedAchievementId] = useState<number | null>(null);
  const [selectedNinjaForAchievement, setSelectedNinjaForAchievement] = useState<number | null>(null);

  const [analytics, setAnalytics] = useState<AnalyticsSnapshot | null>(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [rebuildingLeaderboard, setRebuildingLeaderboard] = useState(false);

  const [announcementTitle, setAnnouncementTitle] = useState('');
  const [announcementMessage, setAnnouncementMessage] = useState('');
  const [sendingAnnouncement, setSendingAnnouncement] = useState(false);

  const [overviewSubTab, setOverviewSubTab] = useState<OverviewSubTab>('activity');
  const [ledgerTransactions, setLedgerTransactions] = useState<LedgerTransaction[]>([]);
  const [ledgerLoading, setLedgerLoading] = useState(false);

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

  const loadNinjas = useCallback(async () => {
    try {
      const data = await ninjaApi.getAllPaginated(
        ninjaPage,
        ninjaPageSize,
        ninjaSort,
        ninjaSortDirection,
        searchQuery || undefined,
        ninjaBeltFilter || undefined,
        ninjaLockedFilter
      );
      const ninjasWithPurchases = await Promise.all(
        data.content.map(async (ninja) => {
          try {
            const unredeemed = await shopApi.getUnredeemedPurchases(ninja.id);
            const all = await shopApi.getNinjaPurchases(ninja.id);
            return { ...ninja, unredeemedPurchases: unredeemed, allPurchases: all };
          } catch {
            return { ...ninja, unredeemedPurchases: [], allPurchases: [] };
          }
        })
      );
      setNinjas(ninjasWithPurchases);
      setNinjaTotalPages(data.totalPages);
      setNinjaTotalElements(data.totalElements);
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to load ninjas'));
      console.error(error);
    }
  }, [ninjaPage, ninjaPageSize, ninjaSort, ninjaSortDirection, searchQuery, ninjaBeltFilter, ninjaLockedFilter, showError]);

  const loadShopItems = useCallback(async () => {
    const items = await shopApi.getAllItems();
    setShopItems(items);
  }, []);

  const loadQuestions = useCallback(async () => {
    const questionsData = await bigQuestionApi.getAllQuestions();
    setQuestions(questionsData);
  }, []);

  const loadPastQuestions = useCallback(async () => {
    try {
      const pastData = await bigQuestionApi.getPastQuestions();
      setPastQuestions(pastData);
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to load past questions'));
      console.error(error);
    }
  }, [showError]);

  const loadPendingSuggestions = useCallback(async () => {
    try {
      const suggestions = await bigQuestionApi.getPendingSuggestions();
      setPendingSuggestions(suggestions as PendingSuggestion[]);
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to load pending suggestions'));
      console.error(error);
    }
  }, [showError]);

  const loadAchievements = useCallback(async () => {
    const allAchievements = await achievementApi.getAll();
    setAchievements(allAchievements);
  }, []);

  const loadAuditLogs = useCallback(async () => {
    try {
      const logs = await adminApi.getAuditLogs(30);
      setAuditLogs(logs);
    } catch (error) {
      console.error('Failed to load audit logs:', error);
    }
  }, []);

  const loadLedgerTransactions = useCallback(async () => {
    try {
      setLedgerLoading(true);
      const transactions = await ledgerApi.getAllLedgerTransactions(100);
      setLedgerTransactions(transactions);
    } catch (error) {
      console.error('Failed to load ledger transactions:', error);
      showError('Failed to load ledger transactions');
    } finally {
      setLedgerLoading(false);
    }
  }, [showError]);

  const loadAnalytics = useCallback(async () => {
    try {
      setAnalyticsLoading(true);
      const data = await analyticsApi.getAnalytics();
      setAnalytics(data);
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to load analytics'));
      console.error(error);
    } finally {
      setAnalyticsLoading(false);
    }
  }, [showError]);

  const loadAllAdmins = useCallback(async () => {
    if (!admin || !adminListPassword) return;
    try {
      const admins = await adminApi.getAllAdmins(admin.username, adminListPassword);
      setAllAdmins(admins);
    } catch (error) {
      if (getErrorStatus(error) === 401) {
        showError('Invalid password');
        setAdminListPassword('');
        setShowAdminList(false);
      } else {
        showError(getErrorMessage(error, 'Failed to load admin list'));
      }
      console.error(error);
    }
  }, [admin, adminListPassword, showError]);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      if (activeTab === 'activity' || activeTab === 'ninjas') {
        await loadNinjas();
      }
      if (activeTab === 'activity' || activeTab === 'shop') {
        await loadShopItems();
      }
      if (activeTab === 'question') {
        await loadQuestions();
        await loadPendingSuggestions();
      }
      if (activeTab === 'achievements') {
        await loadAchievements();
      }
      if (activeTab === 'analytics') {
        await loadAnalytics();
      }
      if (activeTab === 'settings') {
        if (showAdminList && admin) {
          await loadAllAdmins();
        }
      }
    } catch (err) {
      showError('Failed to load data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [activeTab, admin, loadAchievements, loadAllAdmins, loadAnalytics, loadNinjas, loadPendingSuggestions, loadQuestions, loadShopItems, showAdminList, showError]);

  useEffect(() => {
    loadData();
    loadAuditLogs();
  }, [loadData, loadAuditLogs]);

  // reload when filters change, otherwise stale data confuses admins
  useEffect(() => {
    if (activeTab === 'activity' || activeTab === 'ninjas') {
      loadNinjas();
    }
  }, [activeTab, loadNinjas]);
  const handleUseSuggestion = (suggestion: BigQuestion) => {
    // Auto-fill form with suggestion data
    const today = new Date().toISOString().split('T')[0];
    const mondayDate = getMondayOfWeek(today);

    // Fill in what we can from the suggestion
    const choices = suggestion.choices && suggestion.choices.length > 0
      ? [...suggestion.choices, '', '', '', ''].slice(0, 4) // Pad to 4 choices
      : ['', '', '', ''];

    setSelectedSuggestionId(suggestion.id);

    if (!isCreatingQuestion && !editingQuestion) {
      setIsCreatingQuestion(true);
      setEditingQuestion(null);
      if (activeTab !== 'question') {
        setActiveTab('question');
      }
    }

    setQuestionFormData({
      questionDate: mondayDate,
      questionText: suggestion.questionText || '',
      questionType: 'MULTIPLE_CHOICE',
      correctAnswer: '',
      correctChoiceIndex: suggestion.correctChoiceIndex ?? 0,
      choices: choices,
    });
  };

  const handleRejectSuggestion = async (questionId: number, reason: string) => {
    if (!reason || !reason.trim()) {
      showError('Please provide a rejection reason');
      return;
    }

    try {
      await bigQuestionApi.rejectQuestion(questionId, {
        adminUsername: admin?.username || '',
        reason: reason.trim()
      });
      await loadPendingSuggestions();
      success('Question rejected and ninja notified');
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to reject question'));
      console.error(error);
    }
  };

  const handleBanSuggestions = async (ninjaId: number, banned: boolean) => {
    setConfirmationModal({
      isOpen: true,
      title: banned ? 'Ban Ninja from Suggestions' : 'Unban Ninja from Suggestions',
      message: banned
        ? 'Are you sure you want to ban this ninja from suggesting questions?'
        : 'Are you sure you want to unban this ninja from suggesting questions?',
      confirmText: banned ? 'Ban' : 'Unban',
      cancelText: 'Cancel',
      variant: banned ? 'warning' : 'info',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          const updatedNinja = await ninjaApi.banSuggestions(ninjaId, banned);
          success(banned ? 'Ninja banned from suggestions' : 'Ninja unbanned from suggestions');
          await loadPendingSuggestions();
          await loadNinjas();
          if (selectedNinja && selectedNinja.id === ninjaId) {
            setSelectedNinja(prev => prev ? { ...prev, suggestionsBanned: updatedNinja.suggestionsBanned } : null);
          }
        } catch (error) {
          showError(getErrorMessage(error, `Failed to ${banned ? 'ban' : 'unban'} ninja`));
          console.error(error);
        }
      },
    });
  };
  useEffect(() => {
    if (activeTab === 'activity' && overviewSubTab === 'ledger') {
      loadLedgerTransactions();
    }
  }, [activeTab, overviewSubTab, loadLedgerTransactions]);

  const handleCreateNinja = () => {
    setIsCreatingNinja(true);
    setEditingNinja(null);
    setNinjaFormInitialValues(createEmptyNinjaForm());
  };

  const handleEditNinja = (ninja: Ninja) => {
    setEditingNinja(ninja);
    setIsCreatingNinja(false);
    setNinjaFormInitialValues({
      firstName: ninja.firstName,
      lastName: ninja.lastName,
      username: ninja.username,
      beltType: ninja.currentBeltType,
      level: ninja.currentLevel || INITIAL_NINJA_PROGRESS.level,
      lesson: ninja.currentLesson || INITIAL_NINJA_PROGRESS.lesson,
    });
  };

  const closeNinjaForm = () => {
    setIsCreatingNinja(false);
    setEditingNinja(null);
  };

  const handleNinjaFormSubmit = async (values: NinjaFormValues) => {
    const trimmed = {
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      username: values.username.trim(),
      beltType: values.beltType,
      level: values.level,
      lesson: values.lesson,
    };

    if (!trimmed.firstName || !trimmed.lastName || !trimmed.username) {
      showError('Please complete all required fields');
      return;
    }

    const normalized = normalizeProgress(trimmed.beltType, trimmed.level, trimmed.lesson);

    try {
      setNinjaFormSubmitting(true);
      if (isCreatingNinja) {
        await ninjaApi.create({
          firstName: trimmed.firstName,
          lastName: trimmed.lastName,
          username: trimmed.username,
          beltType: normalized.beltType,
          level: normalized.level,
          lesson: normalized.lesson,
        });
        success('Ninja created successfully!');
      } else if (editingNinja) {
        await ninjaApi.update(editingNinja.id, {
          firstName: trimmed.firstName,
          lastName: trimmed.lastName,
          username: trimmed.username,
          beltType: normalized.beltType,
          level: normalized.level,
          lesson: normalized.lesson,
        });
        success('Ninja updated successfully!');
      } else {
        showError('No ninja selected for editing');
        return;
      }
      closeNinjaForm();
      await loadNinjas();
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to save ninja'));
      console.error(error);
    } finally {
      setNinjaFormSubmitting(false);
    }
  };


  const handleDeleteNinja = async (id: number, name: string) => {
    setConfirmationModal({
      isOpen: true,
      title: 'Delete Ninja',
      message: `Are you sure you want to delete ${name}? This action cannot be undone.`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'danger',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await ninjaApi.delete(id);
          success(`${name} deleted successfully`);
          loadNinjas();
        } catch (error) {
          showError(getErrorMessage(error, 'Failed to delete ninja'));
          console.error('Delete ninja error:', error);
        }
      },
    });
  };

  // shop item management functions
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
        success('Shop item created successfully!');
      } else if (editingItem) {
        await shopApi.updateItem(editingItem.id, itemFormData);
        success('Shop item updated successfully!');
      }
      setIsCreatingItem(false);
      setEditingItem(null);
      loadShopItems();
    } catch (err) {
      showError('Failed to save item');
      console.error(err);
    }
  };

  const handleDeleteItem = async (id: number, name: string) => {
    setConfirmationModal({
      isOpen: true,
      title: 'Delete Shop Item',
      message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'danger',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await shopApi.deleteItem(id);
          success(`${name} deleted successfully`);
          loadShopItems();
        } catch (err) {
          showError('Failed to delete item');
          console.error(err);
        }
      },
    });
  };

  const handleToggleItemAvailability = async (id: number, available: boolean) => {
    try {
      await shopApi.updateItemAvailability(id, !available);
      success(`Item ${!available ? 'enabled' : 'disabled'} successfully`);
      loadShopItems();
    } catch (err) {
      showError('Failed to update availability');
      console.error(err);
    }
  };

  // big question management functions
  // get monday because questions are weekly and start on monday
  const getMondayOfWeek = (dateString: string): string => {
    const date = new Date(dateString);
    const day = date.getDay();
    // sunday is 0, monday is 1, etc. math to get back to monday
    const diff = day === 0 ? -6 : -(day - 1);
    const monday = new Date(date);
    monday.setDate(date.getDate() + diff);
    return monday.toISOString().split('T')[0];
  };

  const handleCreateQuestion = () => {
    setIsCreatingQuestion(true);
    setEditingQuestion(null);
    setSelectedSuggestionId(null);
    const today = new Date().toISOString().split('T')[0];
    setQuestionFormData({
      ...createEmptyQuestionForm(),
      questionDate: getMondayOfWeek(today),
    });
  };

  const handleEditQuestion = (question: BigQuestion) => {
    setEditingQuestion(question);
    setIsCreatingQuestion(false);
    setQuestionFormData({
      questionDate: getMondayOfWeek(question.questionDate),
      questionText: question.questionText,
      questionType: question.questionType ?? 'MULTIPLE_CHOICE',
      correctAnswer: question.correctAnswer || '',
      correctChoiceIndex: question.correctChoiceIndex ?? 0,
      choices: normalizeChoices(question.choices),
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
        // if this came from a suggestion, include the id so it gets approved
        if (selectedSuggestionId) {
          data.suggestionId = selectedSuggestionId;
        }
        await bigQuestionApi.createQuestion(data);

        if (selectedSuggestionId) {
          await loadPendingSuggestions();
          success('Question created and suggestion approved!');
          setSelectedSuggestionId(null);
        } else {
          success('Question saved successfully!');
        }
      } else if (editingQuestion) {
        await bigQuestionApi.updateQuestion(editingQuestion.id, data);
        success('Question updated successfully!');
      }

      setIsCreatingQuestion(false);
      setEditingQuestion(null);
      loadQuestions();
    } catch (err) {
      showError('Failed to save question');
      console.error(err);
    }
  };

  const handleDeleteQuestion = async (id: number) => {
    setConfirmationModal({
      isOpen: true,
      title: 'Delete Question',
      message: 'Are you sure you want to permanently delete this question? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'warning',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await bigQuestionApi.deleteQuestion(id);
          success('Question deleted permanently');
          loadQuestions();
          if (showPast) {
            loadPastQuestions();
          }
        } catch (err) {
          showError('Failed to delete question');
          console.error(err);
        }
      },
    });
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
          success(`${itemName} marked as redeemed`);
          loadNinjas();
        } catch (err) {
          showError('Failed to redeem purchase');
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
          success(`${itemName} refunded successfully`);
          loadNinjas();
        } catch (err) {
          showError('Failed to refund purchase');
          console.error(err);
        }
      },
    });
  };

  // achievement management functions
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
      success('Achievement saved successfully!');
      loadAchievements();
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to save achievement'));
      console.error(error);
    }
  };

  const handleDeleteAchievement = async (id: number) => {
    setConfirmationModal({
      isOpen: true,
      title: 'Delete Achievement',
      message: 'Are you sure you want to delete this achievement? This cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'danger',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        try {
          await achievementApi.delete(id);
          success('Achievement deleted successfully');
          loadAchievements();
        } catch (error) {
          showError(getErrorMessage(error, 'Failed to delete achievement'));
          console.error(error);
        }
      },
    });
  };

  const handleToggleAchievementActive = async (id: number) => {
    try {
      await achievementApi.toggleActive(id);
      success('Achievement status updated');
      loadAchievements();
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to toggle achievement status'));
      console.error(error);
    }
  };

  // achievement assignment functions
  const handleAssignAchievement = async () => {
    if (!selectedAchievementId || !selectedNinjaForAchievement) {
      showError('Please select both a ninja and an achievement');
      return;
    }

    try {
      const data: AwardAchievementRequest = {
        ninjaId: selectedNinjaForAchievement,
        achievementId: selectedAchievementId,
      };
      await achievementApi.awardAchievement(data);
      success('Achievement awarded successfully!');
      setAssigningAchievement(false);
      setSelectedAchievementId(null);
      setSelectedNinjaForAchievement(null);
    } catch (error) {
      if (getErrorStatus(error) === 409) {
        showError('Ninja already has this achievement');
      } else {
        showError(getErrorMessage(error, 'Failed to award achievement'));
      }
      console.error(error);
    }
  };

  // admin management functions
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
      success('Admin account created successfully!');
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to create admin account'));
      console.error(error);
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
      showError('New passwords do not match');
      return;
    }

    if (passwordFormData.newPassword.length < 3) {
      showError('New password must be at least 3 characters long');
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
      success('Password changed successfully!');
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to change password'));
      console.error(error);
    }
  };

  const handleRebuildLeaderboard = async () => {
    try {
      setRebuildingLeaderboard(true);
      await ninjaApi.rebuildLeaderboard();
      success('Leaderboard rebuilt successfully');
    } catch (error) {
      showError(getErrorMessage(error, 'Failed to rebuild leaderboard'));
      console.error(error);
    } finally {
      setRebuildingLeaderboard(false);
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
    } catch (error) {
      if (getErrorStatus(error) === 401) {
        showError('Invalid password');
        setAdminListPassword('');
        setShowAdminList(false);
      } else {
        showError(getErrorMessage(error, 'Failed to load admin list'));
      }
      console.error(error);
    }
  };

  const handleDeleteAdmin = async (adminToDelete: Admin) => {
    if (!admin) return;

    if (adminToDelete.id === admin.id) {
      showError('You cannot delete your own account');
      return;
    }

    const emailPart = adminToDelete.email ? ` (${adminToDelete.email})` : '';
    setConfirmationModal({
      isOpen: true,
      title: 'Delete Admin Account',
      message: `Are you sure you want to delete admin account "${adminToDelete.username}"${emailPart}? This action cannot be undone.`,
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'danger',
      onConfirm: async () => {
        setConfirmationModal({ ...confirmationModal, isOpen: false });
        const password = prompt('Enter your password to confirm deletion:');
        if (!password) return;

        try {
          setDeletingAdminId(adminToDelete.id);
          await adminApi.deleteAdmin(adminToDelete.id, admin.username, password);
          await loadAllAdmins();
          success('Admin account deleted successfully');
        } catch (error) {
          const status = getErrorStatus(error);
          if (status === 401) {
            showError('Invalid password');
          } else if (status === 400) {
            showError('Cannot delete your own account');
          } else {
            showError(getErrorMessage(error, 'Failed to delete admin account'));
          }
          console.error(error);
        } finally {
          setDeletingAdminId(null);
        }
      },
    });
  };

  // Calculate stats
  const totalBuxInCirculation = ninjas.reduce((sum, n) => sum + n.buxBalance, 0);
  const unredeemedCount = ninjas.reduce((sum, n) => sum + (n.unredeemedPurchases?.length || 0), 0);
  const filteredNinjas = ninjas.filter(n =>
    `${n.firstName} ${n.lastName} ${n.username}`.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const stalledNinjas = analytics?.stallDetection?.stalledNinjas ?? [];
  const mostPopularItems = analytics?.itemPopularity?.mostPopularItems ?? [];
  const leastPopularItems = analytics?.itemPopularity?.leastPopularItems ?? [];

  // reset to first page when filters change, otherwise you're on page 5 of 0 results
  useEffect(() => {
    if (ninjaPage > 0) {
      setNinjaPage(0);
    }
  }, [searchQuery, ninjaBeltFilter, ninjaLockedFilter, ninjaPage]);

  if (loading && ninjas.length === 0 && shopItems.length === 0 && questions.length === 0) {
    return (
      <div className="admin-container">
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  return (
    <div className="admin-container">
      {/* Toast Container - AdminDashboard uses its own toast instance for success/error messages */}
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

      {/* Merged Header + Navigation */}
      <div className="admin-header">
        <div className="admin-header-top">
          <h1>Admin Dashboard</h1>
          <button onClick={onLogout} className="btn btn-secondary">Logout</button>
        </div>
        <div className="admin-tabs">
          <button
            className={`tab-btn ${activeTab === 'activity' ? 'active' : ''}`}
            onClick={() => setActiveTab('activity')}
          >
            <FiDollarSign />
            Overview
          </button>
          <button
            className={`tab-btn ${activeTab === 'ninjas' ? 'active' : ''}`}
            onClick={() => setActiveTab('ninjas')}
          >
            <FiUsers />
            Ninjas
          </button>
          <button
            className={`tab-btn ${activeTab === 'shop' ? 'active' : ''}`}
            onClick={() => setActiveTab('shop')}
          >
            <FiShoppingBag />
            Shop
          </button>
          <button
            className={`tab-btn ${activeTab === 'question' ? 'active' : ''}`}
            onClick={() => setActiveTab('question')}
          >
            <FiHelpCircle />
            Questions
          </button>
          <button
            className={`tab-btn ${activeTab === 'achievements' ? 'active' : ''}`}
            onClick={() => setActiveTab('achievements')}
          >
            <FiAward />
            Achievements
          </button>
          <button
            className={`tab-btn ${activeTab === 'analytics' ? 'active' : ''}`}
            onClick={() => setActiveTab('analytics')}
          >
            <FiTrendingUp />
            Analytics
          </button>
          <button
            className={`tab-btn ${activeTab === 'settings' ? 'active' : ''}`}
            onClick={() => setActiveTab('settings')}
          >
            <FiSettings />
            Settings
          </button>
        </div>
      </div>

      <div className="admin-content">

      {/* ACTIVITY/OVERVIEW TAB */}
      {activeTab === 'activity' && (
        <>
          {/* Stats Cards - Only show when data is loaded */}
          {!loading && (
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-card-header">
                  <div className="stat-card-icon">
                    <FiUsers />
                  </div>
                </div>
                <div className="stat-card-value">{ninjas.length}</div>
                <div className="stat-card-label">Total Ninjas</div>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <div className="stat-card-icon">
                    <FiDollarSign />
                  </div>
                </div>
                <div className="stat-card-value">{totalBuxInCirculation}</div>
                <div className="stat-card-label">Bux in Circulation</div>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <div className="stat-card-icon">
                    <FiShoppingCart />
                  </div>
                </div>
                <div className="stat-card-value">{unredeemedCount}</div>
                <div className="stat-card-label">Pending Redemptions</div>
              </div>

              <div className="stat-card">
                <div className="stat-card-header">
                  <div className="stat-card-icon">
                    <FiShoppingBag />
                  </div>
                </div>
                <div className="stat-card-value">{shopItems.filter(i => i.available).length}</div>
                <div className="stat-card-label">Active Shop Items</div>
              </div>
            </div>
          )}

          {/* Overview Sub-Tabs */}
          <div style={{ marginBottom: '2rem', borderBottom: '2px solid var(--gray-200)' }}>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button
                className={`tab-btn ${overviewSubTab === 'activity' ? 'active' : ''}`}
                onClick={() => setOverviewSubTab('activity')}
                style={{ borderBottom: overviewSubTab === 'activity' ? '3px solid var(--primary)' : '3px solid transparent' }}
              >
                Admin Activity
              </button>
              <button
                className={`tab-btn ${overviewSubTab === 'ledger' ? 'active' : ''}`}
                onClick={() => setOverviewSubTab('ledger')}
                style={{ borderBottom: overviewSubTab === 'ledger' ? '3px solid var(--primary)' : '3px solid transparent' }}
              >
                Ledger
              </button>
              <button
                className={`tab-btn ${overviewSubTab === 'announcement' ? 'active' : ''}`}
                onClick={() => setOverviewSubTab('announcement')}
                style={{ borderBottom: overviewSubTab === 'announcement' ? '3px solid var(--primary)' : '3px solid transparent' }}
              >
                Announcement
              </button>
            </div>
          </div>

          {/* Sub-tab Content */}
          {overviewSubTab === 'activity' && (
            <div className="activity-feed">
              <div className="activity-header">
                <h3>Recent Activity</h3>
                <span className="activity-count">{auditLogs.length} recent actions</span>
              </div>
              <div className="activity-timeline">
                {auditLogs.map((log) => (
                  <div key={log.id} className="activity-item">
                    <div className="activity-dot"></div>
                    <div className="activity-content">
                      <div className="activity-action">{log.action.replace(/_/g, ' ')}</div>
                      <div className="activity-details">{log.details}</div>
                      <div className="activity-meta">
                        <span>{log.adminUsername}</span>
                        <span>{new Date(log.timestamp).toLocaleString()}</span>
                        {log.targetNinjaName && <span>→ {log.targetNinjaName}</span>}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {overviewSubTab === 'ledger' && (
            <div>
              <div className="section-header">
                <h2>Ledger Transactions</h2>
                <button onClick={loadLedgerTransactions} className="btn btn-secondary" disabled={ledgerLoading}>
                  {ledgerLoading ? 'Loading...' : 'Refresh'}
                </button>
              </div>
              {ledgerLoading ? (
                <div className="loading-message">Loading ledger transactions...</div>
              ) : (
                <div className="data-table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Ninja</th>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Source</th>
                        <th>Note</th>
                      </tr>
                    </thead>
                    <tbody>
                      {ledgerTransactions.length === 0 ? (
                        <tr>
                          <td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>
                            No ledger transactions found
                          </td>
                        </tr>
                      ) : (
                        ledgerTransactions.map((txn) => (
                          <tr key={txn.id}>
                            <td>{new Date(txn.createdAt).toLocaleString()}</td>
                            <td>{txn.ninjaFirstName} {txn.ninjaLastName}</td>
                            <td><span className="badge badge-gray">{txn.type}</span></td>
                            <td style={{ color: txn.amount > 0 ? '#10b981' : '#ef4444', fontWeight: 600 }}>
                              {txn.amount > 0 ? '+' : ''}{(txn.amount)} Bux
                            </td>
                            <td>{txn.sourceType}</td>
                            <td>{txn.note || '-'}</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {overviewSubTab === 'announcement' && (
            <div className="announcement-section" style={{ background: 'white', padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--gray-200)' }}>
              <h3 style={{ marginBottom: '1rem', fontSize: '1.25rem', fontWeight: 600 }}>Send Announcement</h3>
              <form onSubmit={async (e) => {
                e.preventDefault();
                if (!announcementTitle.trim() || !announcementMessage.trim()) {
                  showError('Please fill in both title and message');
                  return;
                }
                if (!admin) {
                  showError('Admin not found');
                  return;
                }
                setSendingAnnouncement(true);
                try {
                  const adminPassword = prompt('Enter your admin password to send announcement:');
                  if (!adminPassword) {
                    setSendingAnnouncement(false);
                    return;
                  }
                  await adminApi.sendAnnouncement(announcementTitle, announcementMessage, admin.username, adminPassword);
                  success('Announcement sent to all users!');
                  setAnnouncementTitle('');
                  setAnnouncementMessage('');
                  await loadAuditLogs();
                } catch (error) {
                  showError(getErrorMessage(error, 'Failed to send announcement'));
                } finally {
                  setSendingAnnouncement(false);
                }
              }}>
                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Title</label>
                  <input
                    type="text"
                    value={announcementTitle}
                    onChange={(e) => setAnnouncementTitle(e.target.value)}
                    placeholder="Announcement title"
                    style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--gray-300)', fontSize: '0.9375rem' }}
                    disabled={sendingAnnouncement}
                  />
                </div>
                <div style={{ marginBottom: '1rem' }}>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Message</label>
                  <textarea
                    value={announcementMessage}
                    onChange={(e) => setAnnouncementMessage(e.target.value)}
                    placeholder="Announcement message"
                    rows={3}
                    style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--gray-300)', fontSize: '0.9375rem', fontFamily: 'inherit', resize: 'vertical' }}
                    disabled={sendingAnnouncement}
                  />
                </div>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={sendingAnnouncement || !announcementTitle.trim() || !announcementMessage.trim()}
                  style={{ opacity: (sendingAnnouncement || !announcementTitle.trim() || !announcementMessage.trim()) ? 0.6 : 1 }}
                >
                  {sendingAnnouncement ? 'Sending...' : 'Send Announcement'}
                </button>
              </form>
            </div>
          )}
        </>
      )}

      {/* NINJAS TAB */}
      {activeTab === 'ninjas' && (
        <>
          <div className="section-header">
            <h2>Ninjas ({filteredNinjas.length})</h2>
            <button onClick={handleCreateNinja} className="btn btn-primary">
              <FiPlus /> Create Ninja
            </button>
          </div>

          {(isCreatingNinja || editingNinja) && (
            <NinjaFormModal
              isOpen={Boolean(isCreatingNinja || editingNinja)}
              mode={isCreatingNinja ? 'create' : 'edit'}
              initialValues={ninjaFormInitialValues}
              isSubmitting={ninjaFormSubmitting}
              onClose={closeNinjaForm}
              onSubmit={handleNinjaFormSubmit}
            />
          )}

          {/* Filters and Sort - Collapsible */}
          {showFiltersAndSort && (
            <div className="form-section" style={{ marginBottom: '1rem' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                <div className="form-field">
                  <label>Belt Filter</label>
                  <select
                    value={ninjaBeltFilter}
                    onChange={(e) => setNinjaBeltFilter(e.target.value)}
                  >
                    <option value="">All Belts</option>
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
                  <label>Locked Filter</label>
                  <select
                    value={ninjaLockedFilter === undefined ? '' : ninjaLockedFilter.toString()}
                    onChange={(e) => {
                      const value = e.target.value;
                      setNinjaLockedFilter(value === '' ? undefined : value === 'true');
                    }}
                  >
                    <option value="">All</option>
                    <option value="true">Locked</option>
                    <option value="false">Unlocked</option>
                  </select>
                </div>
                <div className="form-field">
                  <label>Sort By</label>
                  <select
                    value={ninjaSort}
                    onChange={(e) => setNinjaSort(e.target.value)}
                  >
                    <option value="name">Name</option>
                    <option value="belt">Belt</option>
                    <option value="bux">Bux</option>
                    <option value="legacy">Legacy</option>
                    <option value="locked">Locked</option>
                  </select>
                </div>
                <div className="form-field">
                  <label>Sort Direction</label>
                  <select
                    value={ninjaSortDirection}
                    onChange={(e) => setNinjaSortDirection(e.target.value)}
                  >
                    <option value="ASC">Ascending</option>
                    <option value="DESC">Descending</option>
                  </select>
                </div>
                <div className="form-field">
                  <label>Page Size</label>
                  <select
                    value={ninjaPageSize}
                    onChange={(e) => {
                      setNinjaPageSize(Number(e.target.value));
                      setNinjaPage(0);
                    }}
                  >
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                  </select>
                </div>
              </div>
            </div>
          )}

          <div className="data-table-container">
            <div className="table-header">
              <h2>All Ninjas ({ninjaTotalElements})</h2>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <div className="table-search">
                  <FiSearch className="table-search-icon" />
                  <input
                    type="text"
                    placeholder="Search ninjas..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
                <button
                  onClick={() => setShowFiltersAndSort(!showFiltersAndSort)}
                  className="btn btn-secondary"
                  style={{ padding: '0.5rem 1rem', whiteSpace: 'nowrap' }}
                >
                  {showFiltersAndSort ? 'Hide' : 'Show'} Sort & Filters
                </button>
              </div>
            </div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Username</th>
                  <th>Belt</th>
                  <th>Level</th>
                  <th>Lesson</th>
                  <th>Bux</th>
                  <th>Unredeemed</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {filteredNinjas.map((ninja) => (
                  <tr key={ninja.id} onClick={() => navigate(`/admin/ninja/${ninja.id}`)}>
                    <td>{ninja.firstName} {ninja.lastName}</td>
                    <td>{ninja.username}</td>
                    <td><span className="badge badge-gray">{ninja.currentBeltType}</span></td>
                    <td>{ninja.currentLevel}</td>
                    <td>{ninja.currentLesson}</td>
                    <td><strong>{ninja.buxBalance.toFixed(2)}</strong></td>
                    <td>
                      {ninja.unredeemedPurchases && ninja.unredeemedPurchases.length > 0 ? (
                        <span className="badge badge-danger">{ninja.unredeemedPurchases.length}</span>
                      ) : '-'}
                    </td>
                    <td onClick={(e) => e.stopPropagation()} className="row-actions">
                      <button
                        onClick={() => handleEditNinja(ninja)}
                        className="btn-icon"
                        title="Edit"
                      >
                        <FiEdit2 />
                      </button>
                      <button
                        onClick={() => handleDeleteNinja(ninja.id, `${ninja.firstName} ${ninja.lastName}`)}
                        className="btn-icon"
                        title="Delete"
                      >
                        <FiTrash2 />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {/* Pagination Controls - Only show if there are more items than page size */}
            {ninjaTotalElements > ninjaPageSize && (
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', padding: '1rem' }}>
                <div>
                  Showing {ninjaPage * ninjaPageSize + 1} - {Math.min((ninjaPage + 1) * ninjaPageSize, ninjaTotalElements)} of {ninjaTotalElements}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    className="btn btn-secondary"
                    onClick={() => setNinjaPage(0)}
                    disabled={ninjaPage === 0}
                  >
                    First
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => setNinjaPage(ninjaPage - 1)}
                    disabled={ninjaPage === 0}
                  >
                    Previous
                  </button>
                  <span style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center' }}>
                    Page {ninjaPage + 1} of {ninjaTotalPages || 1}
                  </span>
                  <button
                    className="btn btn-secondary"
                    onClick={() => setNinjaPage(ninjaPage + 1)}
                    disabled={ninjaPage >= ninjaTotalPages - 1}
                  >
                    Next
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => setNinjaPage(ninjaTotalPages - 1)}
                    disabled={ninjaPage >= ninjaTotalPages - 1}
                  >
                    Last
                  </button>
                </div>
              </div>
            )}
          </div>
        </>
      )}

      {/* SHOP TAB */}
      {activeTab === 'shop' && (
        <>
          <div className="section-header">
            <h2>Shop Items ({shopItems.length})</h2>
            <button onClick={handleCreateItem} className="btn btn-primary">
              <FiPlus /> Create Item
            </button>
          </div>

          {(isCreatingItem || editingItem) && (
            <div className="modal-backdrop" onClick={() => { setIsCreatingItem(false); setEditingItem(null); }}>
              <div className="form-section modal" onClick={(e) => e.stopPropagation()}>
                <div className="form-section-header">
                  <h3>{isCreatingItem ? 'Create New Item' : 'Edit Item'}</h3>
                </div>
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
                    <button type="submit" className="btn btn-primary">Save</button>
                    <button type="button" onClick={() => { setIsCreatingItem(false); setEditingItem(null); }} className="btn btn-secondary">Cancel</button>
                  </div>
                </form>
              </div>
            </div>
          )}

          <div className="card-grid">
            {shopItems.map((item) => (
              <div key={item.id} className="card">
                <div className="card-header">
                  <div>
                    <div className="card-title">{item.name}</div>
                    <div className="card-subtitle">{item.category}</div>
                  </div>
                  <div className="card-actions">
                    <button onClick={() => handleEditItem(item)} className="btn-icon" title="Edit">
                      <FiEdit2 />
                    </button>
                    <button onClick={() => handleDeleteItem(item.id, item.name)} className="btn-icon" title="Delete">
                      <FiTrash2 />
                    </button>
                  </div>
                </div>
                <p style={{ margin: '1rem 0', color: 'var(--admin-text-secondary)' }}>{item.description}</p>
                <div className="card-footer">
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                    <strong style={{ fontSize: '1.25rem' }}>{item.price} Bux</strong>
                    {item.available ? (
                      <span className="badge badge-success">Available</span>
                    ) : (
                      <span className="badge badge-gray">Unavailable</span>
                    )}
                  </div>
                  <button
                    onClick={() => handleToggleItemAvailability(item.id, item.available)}
                    className={`btn btn-sm ${item.available ? 'btn-secondary' : 'btn-success'}`}
                  >
                    {item.available ? 'Disable' : 'Enable'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {/* BIG QUESTION TAB */}
      {activeTab === 'question' && (
        <>
          <div className="section-header">
            <h2>Question of the Week</h2>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button onClick={handleCreateQuestion} className="btn btn-primary">
                <FiPlus /> Create Question
              </button>
              {pendingSuggestions.length > 0 && (
                <button onClick={() => setShowSuggestions(!showSuggestions)} className="btn btn-secondary">
                  {showSuggestions ? 'Hide Suggestions' : `View Suggestions (${pendingSuggestions.length})`}
                </button>
              )}
              <button onClick={() => {
                setShowPast(!showPast);
                if (!showPast && pastQuestions.length === 0) {
                  loadPastQuestions();
                }
              }} className="btn btn-secondary">
                {showPast ? 'Hide Past Questions' : 'Show Past Questions'}
              </button>
            </div>
          </div>

          {/* Pending Suggestions Section */}
          {showSuggestions && !isCreatingQuestion && !editingQuestion && <SuggestionReviewList
            suggestions={pendingSuggestions}
            onReject={handleRejectSuggestion}
            onBan={handleBanSuggestions}
          />}


          {(isCreatingQuestion || editingQuestion) && (
            <div className="modal-backdrop" onClick={() => { setIsCreatingQuestion(false); setEditingQuestion(null); setSelectedSuggestionId(null); }}>
              <div style={{ display: 'flex', gap: '1rem', width: '100%', maxWidth: '1400px', margin: '0 auto' }}>
                {/* Suggestions Sidebar */}
                {pendingSuggestions.length > 0 && (
                  <div
                    onClick={(e) => e.stopPropagation()}
                    style={{ width: '300px', background: 'white', borderRadius: '8px', padding: '1rem', maxHeight: '80vh', overflowY: 'auto', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
                  >
                    <h4 style={{ marginBottom: '1rem', color: '#3B82F6' }}>Suggestions ({pendingSuggestions.length})</h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      {pendingSuggestions.map((suggestion) => {
                        const question = suggestion.question || suggestion;
                        const ninjaName = suggestion.ninjaName || (question.suggestedByNinjaId ? `(ID: ${question.suggestedByNinjaId})` : '');
                        const isSelected = selectedSuggestionId === question.id;

                        return (
                          <div
                            key={question.id}
                            onClick={(e) => {
                              e.stopPropagation();
                              handleUseSuggestion(question);
                            }}
                            style={{
                              padding: '0.75rem',
                              border: `2px solid ${isSelected ? '#3B82F6' : '#e5e7eb'}`,
                              borderRadius: '6px',
                              cursor: 'pointer',
                              background: isSelected ? '#eff6ff' : 'white',
                              transition: 'all 0.2s'
                            }}
                            onMouseEnter={(e) => {
                              if (!isSelected) e.currentTarget.style.borderColor = '#9ca3af';
                            }}
                            onMouseLeave={(e) => {
                              if (!isSelected) e.currentTarget.style.borderColor = '#e5e7eb';
                            }}
                          >
                            <div style={{ fontSize: '0.75rem', color: '#666', marginBottom: '0.25rem' }}>
                              By {ninjaName}
                            </div>
                            <div style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                              {question.questionText?.substring(0, 60)}
                              {question.questionText && question.questionText.length > 60 ? '...' : ''}
                            </div>
                            {isSelected && (
                              <div style={{ fontSize: '0.75rem', color: '#3B82F6', marginTop: '0.25rem' }}>
                                ✓ Selected
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Form */}
                <div className="form-section modal" onClick={(e) => e.stopPropagation()} style={{ flex: 1 }}>
                  <div className="form-section-header">
                    <h3>{isCreatingQuestion ? 'Create New Question' : 'Edit Question'}</h3>
                  </div>
                  <form onSubmit={handleSubmitQuestion}>
                <div className="form-grid">
                  <div className="form-field">
                    <label>Date</label>
                    <input
                      type="date"
                      value={questionFormData.questionDate}
                      onChange={(e) => {
                        const selectedDate = e.target.value;
                        const mondayDate = getMondayOfWeek(selectedDate);
                        setQuestionFormData({ ...questionFormData, questionDate: mondayDate });
                      }}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label>Question Type</label>
                    <select
                      value={questionFormData.questionType}
                      onChange={(e) => setQuestionFormData({
                        ...questionFormData,
                        questionType: e.target.value as 'MULTIPLE_CHOICE'
                      })}
                      disabled
                    >
                      <option value="MULTIPLE_CHOICE">Multiple Choice</option>
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
                    <button type="submit" className="btn btn-primary">Save</button>
                    <button type="button" onClick={() => { setIsCreatingQuestion(false); setEditingQuestion(null); setSelectedSuggestionId(null); }} className="btn btn-secondary">Cancel</button>
                  </div>
                </form>
                </div>
              </div>
            </div>
          )}

          {showPast && (
            <div className="past-questions-section" style={{ marginBottom: '2rem' }}>
              <h3 style={{ color: '#666', marginBottom: '1rem' }}>Past Questions</h3>
              {pastQuestions.length === 0 ? (
                <div style={{ padding: '2rem', textAlign: 'center', background: '#f8f9fa', borderRadius: '12px' }}>
                  <p style={{ color: '#666' }}>No past questions found</p>
                </div>
              ) : (
                <div className="questions-list">
                  {pastQuestions.map((question) => (
                    <div key={question.id} className="question-card" style={{ borderLeftColor: '#9ca3af' }}>
                      <div className="question-header">
                        <div>
                          <h3>{new Date(question.questionDate).toLocaleDateString()}</h3>
                          <span className={`type-badge ${question.questionType.toLowerCase()}`}>
                            Multiple Choice
                          </span>
                        </div>
                        <div className="question-actions">
                          <button onClick={() => handleDeleteQuestion(question.id)} className="btn-icon" title="Delete">
                            <FiTrash2 />
                          </button>
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
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          <div className="questions-list">
            {questions.length === 0 ? (
              <div style={{ padding: '3rem', textAlign: 'center', background: '#f8f9fa', borderRadius: '12px', border: '2px dashed #d1d5db' }}>
                <FiHelpCircle size={48} color="#9ca3af" style={{ marginBottom: '1rem' }} />
                <p style={{ color: '#666', fontSize: '1rem', margin: 0 }}>No questions yet. Create your first question to get started!</p>
              </div>
            ) : (
              questions.map((question) => (
                <div key={question.id} className="question-card">
                  <div className="question-header">
                    <div>
                      <h3>{new Date(question.questionDate).toLocaleDateString()}</h3>
                      <span className={`type-badge ${question.questionType.toLowerCase()}`}>
                        Multiple Choice
                      </span>
                    </div>
                    <div className="question-actions">
                      <button onClick={() => handleEditQuestion(question)} className="btn-icon" title="Edit">
                        <FiEdit2 />
                      </button>
                      <button onClick={() => handleDeleteQuestion(question.id)} className="btn-icon" title="Delete">
                        <FiTrash2 />
                      </button>
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
                </div>
              ))
            )}
          </div>
        </>
      )}

      {/* ACHIEVEMENTS TAB */}
      {activeTab === 'achievements' && (
        <>
          <div className="section-header">
            <h2>Achievements ({achievements.length})</h2>
            <button className="btn btn-primary" onClick={handleCreateAchievement}>
              <FiPlus /> Create Achievement
            </button>
          </div>

          {/* Achievement Form */}
          {(isCreatingAchievement || editingAchievement) && (
            <div className="modal-backdrop" onClick={() => { setIsCreatingAchievement(false); setEditingAchievement(null); }}>
              <div className="form-section modal" onClick={(e) => e.stopPropagation()}>
                <div className="form-section-header">
                  <h3>{editingAchievement ? 'Edit Achievement' : 'Create Achievement'}</h3>
                </div>
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
                  <label>Icon:</label>
                  <select
                    value={achievementFormData.icon}
                    onChange={(e) => setAchievementFormData({ ...achievementFormData, icon: e.target.value })}
                  >
                    <option value="🎯">🎯 Target</option>
                    <option value="📚">📚 Book</option>
                    <option value="⭐">⭐ Star</option>
                    <option value="💯">💯 Hundred</option>
                    <option value="🥋">🥋 Karate</option>
                    <option value="🟠">🟠 Orange</option>
                    <option value="🟢">🟢 Green</option>
                    <option value="🔵">🔵 Blue</option>
                    <option value="❓">❓ Question</option>
                    <option value="💎">💎 Diamond</option>
                    <option value="💰">💰 Money</option>
                    <option value="💸">💸 Flying Money</option>
                    <option value="🏆">🏆 Trophy</option>
                    <option value="🛍️">🛍️ Shopping</option>
                    <option value="🤝">🤝 Handshake</option>
                    <option value="💡">💡 Light Bulb</option>
                  </select>
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
                  <button className="btn btn-primary" onClick={handleSaveAchievement}>
                    {editingAchievement ? 'Update' : 'Create'} Achievement
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={() => {
                      setIsCreatingAchievement(false);
                      setEditingAchievement(null);
                    }}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Assign Achievement Section */}
          <div className="form-section" style={{ marginBottom: '2rem' }}>
            <div className="form-section-header">
              <h3>Assign Achievement to Ninja</h3>
              {!assigningAchievement && (
                <button className="btn btn-primary" onClick={() => setAssigningAchievement(true)}>
                  <FiAward /> Assign Achievement
                </button>
              )}
            </div>
            {assigningAchievement && (
              <div className="modal-backdrop" onClick={() => { setAssigningAchievement(false); setSelectedAchievementId(null); setSelectedNinjaForAchievement(null); }}>
                <div className="form-section modal" onClick={(e) => e.stopPropagation()}>
                  <div className="form-section-header">
                    <h3>Assign Achievement to Ninja</h3>
                  </div>
                  <div className="form-grid">
                <div className="form-field">
                  <label>Select Ninja</label>
                  <select
                    value={selectedNinjaForAchievement || ''}
                    onChange={(e) => setSelectedNinjaForAchievement(Number(e.target.value))}
                  >
                    <option value="">-- Select Ninja --</option>
                    {ninjas.map(ninja => (
                      <option key={ninja.id} value={ninja.id}>
                        {ninja.firstName} {ninja.lastName} ({ninja.username})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-field">
                  <label>Select Achievement</label>
                  <select
                    value={selectedAchievementId || ''}
                    onChange={(e) => setSelectedAchievementId(Number(e.target.value))}
                  >
                    <option value="">-- Select Achievement --</option>
                    {achievements.filter(a => a.active).map(achievement => (
                      <option key={achievement.id} value={achievement.id}>
                        {achievement.name} ({achievement.rarity})
                      </option>
                    ))}
                    </select>
                  </div>
                  </div>
                  <div className="form-actions">
                    <button
                      className="btn btn-primary"
                      onClick={handleAssignAchievement}
                      disabled={!selectedAchievementId || !selectedNinjaForAchievement}
                    >
                      <FiAward /> Award Achievement
                    </button>
                    <button
                      className="btn btn-secondary"
                      onClick={() => {
                        setAssigningAchievement(false);
                        setSelectedAchievementId(null);
                        setSelectedNinjaForAchievement(null);
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Achievement List */}
          <div className="card-grid">
            {achievements.map((achievement) => (
              <div key={achievement.id} className="card">
                <div className="card-header">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <div style={{
                      width: '48px',
                      height: '48px',
                      borderRadius: '8px',
                      background: 'var(--gray-100)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '1.5rem',
                      color: 'var(--gray-600)',
                      flexShrink: 0
                    }}>
                      <AchievementIcon icon={achievement.icon} size={24} />
                    </div>
                    <div style={{ minWidth: 0 }}>
                      <h3 className="card-title" style={{ fontSize: '1rem', marginBottom: '0.25rem' }}>
                        {achievement.name}
                      </h3>
                      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                        <span className={`rarity-badge rarity-${achievement.rarity}`}>
                          {achievement.rarity}
                        </span>
                        <span style={{ color: 'var(--gray-500)', fontSize: '0.8125rem' }}>
                          {achievement.category}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="card-actions">
                    <button className="btn-icon" onClick={() => handleEditAchievement(achievement)} title="Edit">
                      <FiEdit2 />
                    </button>
                    <button className="btn-icon" onClick={() => handleDeleteAchievement(achievement.id)} title="Delete">
                      <FiTrash2 />
                    </button>
                  </div>
                </div>

                <p style={{ margin: '1rem 0', color: 'var(--gray-600)', fontSize: '0.875rem', lineHeight: '1.5' }}>
                  {achievement.description}
                </p>

                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
                  <span className="badge badge-success">
                    <FiDollarSign size={12} /> {achievement.buxReward} Bux
                  </span>
                  {achievement.manualOnly && (
                    <span className="badge badge-warning">Manual Only</span>
                  )}
                  {achievement.hidden && (
                    <span className="badge badge-gray">
                      <FiLock size={12} /> Hidden
                    </span>
                  )}
                  {!achievement.active && (
                    <span className="badge badge-danger">Inactive</span>
                  )}
                </div>

                {achievement.unlockCriteria && (
                  <div style={{
                    background: 'var(--gray-50)',
                    border: '1px solid var(--gray-200)',
                    borderRadius: '6px',
                    padding: '0.75rem',
                    marginBottom: '1rem'
                  }}>
                    <div style={{
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'var(--gray-500)',
                      textTransform: 'uppercase',
                      marginBottom: '0.5rem'
                    }}>
                      Auto-Unlock Criteria
                    </div>
                    <pre style={{
                      fontSize: '0.75rem',
                      color: 'var(--gray-700)',
                      margin: 0,
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      fontFamily: 'monospace',
                      background: 'white',
                      padding: '0.5rem',
                      borderRadius: '4px',
                      border: '1px solid var(--gray-200)'
                    }}>
                      {achievement.unlockCriteria}
                    </pre>
                  </div>
                )}

                <div style={{ display: 'flex', gap: '0.5rem', flexDirection: 'column' }}>
                  <button
                    className={achievement.active ? 'btn btn-secondary' : 'btn btn-primary'}
                    onClick={() => handleToggleAchievementActive(achievement.id)}
                    style={{ width: '100%', justifyContent: 'center' }}
                  >
                    {achievement.active ? <><FiPause /> Deactivate</> : <><FiPlay /> Activate</>}
                  </button>
                  {achievement.active && (
                    <button
                      className="btn btn-sm"
                      onClick={() => {
                        setAssigningAchievement(true);
                        setSelectedAchievementId(achievement.id);
                      }}
                      style={{ width: '100%', justifyContent: 'center' }}
                    >
                      <FiAward size={14} /> Quick Assign
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {/* ANALYTICS TAB */}
      {activeTab === 'analytics' && (
        <>
          <div className="section-header">
            <h2>Analytics Dashboard</h2>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button onClick={handleRebuildLeaderboard} className="btn btn-warning" disabled={rebuildingLeaderboard}>
                {rebuildingLeaderboard ? 'Rebuilding...' : 'Rebuild Leaderboard'}
              </button>
              <button onClick={loadAnalytics} className="btn btn-secondary" disabled={analyticsLoading}>
                {analyticsLoading ? 'Loading...' : 'Refresh'}
              </button>
            </div>
          </div>

          {analyticsLoading ? (
            <div className="loading-message">Loading analytics...</div>
          ) : analytics ? (
            <div className="analytics-dashboard">
              {/* Stall Detection */}
              {analytics.stallDetection && (
                <div className="analytics-section">
                  <h3><FiClock /> Stall Detection</h3>
                  {stalledNinjas.length > 0 ? (
                    <div className="stall-alerts">
                      <h4>Stalled Ninjas (No progress in 7+ days)</h4>
                      <div className="stall-list">
                        {stalledNinjas.slice(0, 10).map((stall) => (
                          <div key={stall.ninjaId} className="stall-item">
                            <span className="stall-name">{stall.ninjaName}</span>
                            <span className="stall-days">{stall.daysStalled} days</span>
                            <button
                              onClick={() => navigate(`/ninja/${stall.ninjaId}`)}
                              className="btn btn-sm btn-primary"
                            >
                              View
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>
                  ) : (
                    <p className="no-stalls">No stalled ninjas detected! 🎉</p>
                  )}
                </div>
              )}

              {/* Economy Health */}
              {analytics.economyHealth && (
                <div className="analytics-section">
                  <h3><FiDollarSign /> Economy Health</h3>
                  <div className="analytics-grid">
                    <div className="metric-card">
                      <div className="metric-label">Total Bux in Circulation</div>
                      <div className="metric-value">{analytics.economyHealth.totalBuxInCirculation?.toLocaleString() || 0}</div>
                    </div>
                    <div className="metric-card">
                      <div className="metric-label">Total Earned</div>
                      <div className="metric-value">{analytics.economyHealth.totalBuxEarned?.toLocaleString() || 0}</div>
                    </div>
                    <div className="metric-card">
                      <div className="metric-label">Total Spent</div>
                      <div className="metric-value">{analytics.economyHealth.totalBuxSpent?.toLocaleString() || 0}</div>
                    </div>
                    <div className="metric-card">
                      <div className="metric-label">Spend/Earn Ratio</div>
                      <div className="metric-value">
                        {(analytics.economyHealth.spendEarnRatio * 100 || 0).toFixed(1)}%
                      </div>
                    </div>
                  </div>
                  {analytics.economyHealth.balanceDistribution && (
                    <div className="balance-distribution">
                      <h4>Balance Distribution</h4>
                      <div className="distribution-grid">
                        <div className="dist-item">
                          <span className="dist-label">Zero Balance:</span>
                          <span className="dist-value">{analytics.economyHealth.balanceDistribution.zeroBalance || 0}</span>
                        </div>
                        <div className="dist-item">
                          <span className="dist-label">Low (1-50):</span>
                          <span className="dist-value">{analytics.economyHealth.balanceDistribution.lowBalance || 0}</span>
                        </div>
                        <div className="dist-item">
                          <span className="dist-label">Medium (51-200):</span>
                          <span className="dist-value">{analytics.economyHealth.balanceDistribution.mediumBalance || 0}</span>
                        </div>
                        <div className="dist-item">
                          <span className="dist-label">High (201-500):</span>
                          <span className="dist-value">{analytics.economyHealth.balanceDistribution.highBalance || 0}</span>
                        </div>
                        <div className="dist-item">
                          <span className="dist-label">Very High (500+):</span>
                          <span className="dist-value">{analytics.economyHealth.balanceDistribution.veryHighBalance || 0}</span>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Engagement Metrics */}
              {analytics.engagement && (
                <div className="analytics-section">
                  <h3><FiTarget /> Engagement Metrics</h3>
                  {analytics.engagement.quizMetrics && (
                    <div className="metrics-subsection">
                      <h4>Quiz Metrics</h4>
                      <div className="analytics-grid">
                        <div className="metric-card">
                          <div className="metric-label">Total Questions</div>
                          <div className="metric-value">{analytics.engagement.quizMetrics.totalQuestions || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Total Answers</div>
                          <div className="metric-value">{analytics.engagement.quizMetrics.totalAnswers || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Average Accuracy</div>
                          <div className="metric-value">
                            {(analytics.engagement.quizMetrics.averageAccuracy || 0).toFixed(1)}%
                          </div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Participants This Week</div>
                          <div className="metric-value">{analytics.engagement.quizMetrics.participantsThisWeek || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Participation Rate</div>
                          <div className="metric-value">
                            {(analytics.engagement.quizMetrics.participationRate || 0).toFixed(1)}%
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                  {analytics.engagement.shopMetrics && (
                    <div className="metrics-subsection">
                      <h4>Shop Metrics</h4>
                      <div className="analytics-grid">
                        <div className="metric-card">
                          <div className="metric-label">Total Purchases</div>
                          <div className="metric-value">{analytics.engagement.shopMetrics.totalPurchases || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Purchases This Week</div>
                          <div className="metric-value">{analytics.engagement.shopMetrics.totalPurchasesThisWeek || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Repeat Purchase Rate</div>
                          <div className="metric-value">
                            {(analytics.engagement.shopMetrics.repeatPurchaseRate || 0).toFixed(1)}%
                          </div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Avg Purchase Value</div>
                          <div className="metric-value">{analytics.engagement.shopMetrics.averagePurchaseValue || 0} Bux</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Unique Shoppers (This Week)</div>
                          <div className="metric-value">{analytics.engagement.shopMetrics.uniqueShoppersThisWeek || 0}</div>
                        </div>
                      </div>
                    </div>
                  )}
                  {analytics.engagement.achievementMetrics && (
                    <div className="metrics-subsection">
                      <h4>Achievement Metrics</h4>
                      <div className="analytics-grid">
                        <div className="metric-card">
                          <div className="metric-label">Total Unlocked</div>
                          <div className="metric-value">{analytics.engagement.achievementMetrics.totalUnlocked || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Unlocked This Week</div>
                          <div className="metric-value">{analytics.engagement.achievementMetrics.unlockedThisWeek || 0}</div>
                        </div>
                        <div className="metric-card">
                          <div className="metric-label">Avg Achievements per Ninja</div>
                          <div className="metric-value">
                            {(analytics.engagement.achievementMetrics.averageAchievementsPerNinja || 0).toFixed(1)}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* Item Popularity */}
              {analytics.itemPopularity && (
                <div className="analytics-section">
                  <h3><FiShoppingBag /> Item Popularity</h3>
                  {mostPopularItems.length > 0 && (
                    <div className="popularity-section">
                      <h4>Most Popular Items</h4>
                      <div className="popular-items-list">
                        {mostPopularItems.slice(0, 10).map((item) => (
                          <div key={item.itemId} className="popular-item-card">
                            <div className="item-name">{item.itemName}</div>
                            <div className="item-stats">
                              <span>Purchases: {item.purchaseCount}</span>
                              <span>Revenue: {item.revenue} Bux</span>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                  {leastPopularItems.length > 0 && (
                    <div className="popularity-section">
                      <h4>Least Popular Items</h4>
                      <div className="popular-items-list">
                        {leastPopularItems.slice(0, 10).map((item) => (
                          <div key={item.itemId} className="popular-item-card">
                            <div className="item-name">{item.itemName}</div>
                            <div className="item-stats">
                              <span>Purchases: {item.purchaseCount}</span>
                              <span>Revenue: {item.revenue} Bux</span>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="no-data">No analytics data available. Click "Refresh" to load.</div>
          )}
        </>
      )}

      {/* SETTINGS TAB */}
      {activeTab === 'settings' && (
        <>
          <div className="section-header">
            <h2>Settings</h2>
          </div>

          {admin && (
            <div className="form-section">
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

          <div className="form-section">
            <div className="form-section-header">
              <h3>Change Password</h3>
              {!isChangingPassword && (
                <button onClick={handleChangePassword} className="btn btn-primary">
                  Change Password
                </button>
              )}
            </div>

            {isChangingPassword && (
              <div className="modal-backdrop" onClick={() => { setIsChangingPassword(false); setPasswordFormData({ oldPassword: '', newPassword: '', confirmPassword: '' }); }}>
                <div className="form-section modal" onClick={(e) => e.stopPropagation()}>
                  <div className="form-section-header">
                    <h3>Change Password</h3>
                  </div>
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
                      <button type="submit" className="btn btn-primary">Save</button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsChangingPassword(false);
                          setPasswordFormData({ oldPassword: '', newPassword: '', confirmPassword: '' });
                        }}
                        className="btn btn-secondary"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </div>

          {admin && admin.canCreateAdmins && (
            <>
              <div className="form-section">
                <div className="form-section-header">
                  <h3>Admin Accounts</h3>
                  {!showAdminList && (
                    <button onClick={handleShowAdminList} className="btn btn-primary">
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
                        className="btn btn-secondary"
                      >
                        Hide Admin List
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="form-section">
                <div className="form-section-header">
                  <h3>Create Admin Account</h3>
                  {!isCreatingAdmin && (
                    <button onClick={handleCreateAdmin} className="btn btn-primary">
                      <FiPlus /> Create Admin
                    </button>
                  )}
                </div>

              {isCreatingAdmin && (
                <div className="modal-backdrop" onClick={() => { setIsCreatingAdmin(false); setAdminFormData({ username: '', email: '', password: '', firstName: '', lastName: '', currentPassword: '' }); }}>
                  <div className="form-section modal" onClick={(e) => e.stopPropagation()}>
                    <div className="form-section-header">
                      <h3>Create Admin Account</h3>
                    </div>
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
                      <button type="submit" className="btn btn-primary">Create Admin</button>
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
                        className="btn btn-secondary"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              </div>
              )}
            </div>
            </>
          )}
        </>
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
                  <div><strong>Current Balance:</strong> {selectedNinja.buxBalance.toFixed(2)} Bux</div>
                  {selectedNinja.suggestionsBanned && (
                    <div style={{ gridColumn: '1 / -1', background: '#fee2e2', padding: '0.75rem', borderRadius: '8px', border: '1px solid #dc2626' }}>
                      <strong style={{ color: '#dc2626', fontWeight: 600 }}>Suggestions Banned</strong>
                      <span style={{ color: '#dc2626', display: 'block', marginTop: '0.25rem' }}>This ninja cannot suggest questions</span>
                    </div>
                  )}
                  {selectedNinja.isLocked && (
                    <div style={{ gridColumn: '1 / -1', background: '#fee2e2', padding: '0.75rem', borderRadius: '8px', border: '1px solid #dc2626' }}>
                      <strong style={{ color: '#dc2626', fontWeight: 600 }}>Account Locked</strong>
                      <span style={{ color: '#dc2626', display: 'block', marginTop: '0.25rem' }}>
                        {selectedNinja.lockReason || 'Account is locked'}
                      </span>
                    </div>
                  )}
                </div>
                <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <button
                    onClick={() => {
                      const isBanned = selectedNinja.suggestionsBanned || false;
                      handleBanSuggestions(selectedNinja.id, !isBanned);
                    }}
                    className={`btn ${selectedNinja.suggestionsBanned ? 'btn-success' : 'btn-warning'}`}
                  >
                    {selectedNinja.suggestionsBanned ? 'Unban Suggestions' : 'Ban Suggestions'}
                  </button>
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
                        <button onClick={() => handleRedeemPurchase(purchase.id, purchase.itemName)} className="btn btn-sm btn-success">
                          Redeem
                        </button>
                        <button onClick={() => handleRefundPurchase(purchase.id, purchase.itemName)} className="btn btn-sm btn-warning">
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
    </div>
  );
}
