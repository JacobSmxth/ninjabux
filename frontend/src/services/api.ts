import axios, { type AxiosError } from 'axios';
import type { Ninja, ShopItem, Purchase, CreateNinjaRequest, UpdateProgressRequest, PurchaseRequest, Admin, AdminLoginRequest, UpdateNinjaRequest, LeaderboardResponse, BigQuestion, BigQuestionResponse, CreateBigQuestionRequest, AnswerBigQuestionRequest, SuggestQuestionRequest, CreateShopItemRequest, ProgressHistory, ProgressHistoryCorrectionRequest, AdminAuditLog, CreateAdminByAdminRequest, ChangePasswordRequest, Achievement, AchievementProgress, CreateAchievementRequest, AwardAchievementRequest, AchievementCategory, PaginatedNinjaResponse, AnalyticsSnapshot, LedgerTransaction } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 
  (import.meta.env.DEV && window.location.hostname === 'localhost' 
    ? '/api'  // Use proxy in dev when on localhost
    : `${window.location.protocol}//${window.location.hostname}:8080/api`); // Use direct connection when on network or production

console.log('API Base URL:', API_BASE_URL);

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

const isAxiosError = (error: unknown): error is AxiosError => axios.isAxiosError(error);

const throwWithResponseMessage = (error: AxiosError, fallback: string): never => {
  const responseMessage = (error.response?.data as { message?: string } | undefined)?.message;
  const message = responseMessage || fallback;
  const customError = new Error(message);
  (customError as AxiosError).response = error.response;
  throw customError;
};

interface BackendError extends Error {
  response?: AxiosError['response'];
  status?: number;
}

api.interceptors.request.use(
  (config) => {
    const skipLogging = config.url?.includes('/big-question/week/');
    if (!skipLogging) {
      console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
    }
    return config;
  },
  (error) => {
    console.error('API Request Error:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    const skipLogging = response.config.url?.includes('/big-question/week/');
    if (!skipLogging) {
      console.log(`API Response: ${response.status} ${response.config.url}`);
    }
    return response;
  },
  (error) => {
    const isExpected404 = error.response?.status === 404 &&
      error.config?.url?.includes('/big-question/week/');

    // chatgpt told me this would work. it still logs errors sometimes.
    if (!isExpected404) {
      console.error('API Error:', error);
      console.error('Error Message:', error.message);
      console.error('Error Code:', error.code);
      console.error('Error Response:', error.response);
      console.error('Error Data:', error.response?.data);
      console.error('Request URL:', error.config?.url);
      console.error('Request Method:', error.config?.method);
    }

    if (!error.response) {
      if (error.code === 'ECONNABORTED') {
        console.error('Request timeout - server may be down or slow');
      } else if (error.code === 'ERR_NETWORK') {
        console.error('Network error - cannot connect to server');
      } else {
        console.error('Network error - check if backend is running on port 8080');
      }
    }

    if (error.response?.data?.message) {
      const backendError: BackendError = new Error(error.response.data.message);
      backendError.response = error.response;
      backendError.status = error.response.status;
      throw backendError;
    }
    throw error;
  }
);

// Ninja API
export const ninjaApi = {
  getAll: async (): Promise<Ninja[]> => {
    const response = await api.get<Ninja[]>('/ninjas');
    return response.data;
  },

  getAllPaginated: async (
    page: number = 0,
    size: number = 25,
    sort: string = 'name',
    direction: string = 'ASC',
    name?: string,
    belt?: string,
    locked?: boolean
  ): Promise<PaginatedNinjaResponse> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sort', sort);
    params.append('direction', direction);
    if (name) params.append('name', name);
    if (belt) params.append('belt', belt);
    if (locked !== undefined) params.append('locked', locked.toString());
    const response = await api.get<PaginatedNinjaResponse>(`/ninjas?${params.toString()}`);
    return response.data;
  },

  getById: async (id: number): Promise<Ninja> => {
    try {
      const response = await api.get<Ninja>(`/ninjas/${id}`);
      return response.data;
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 403) {
        throwWithResponseMessage(error, 'Account is locked');
      }
      throw error;
    }
  },

  loginByUsername: async (username: string): Promise<Ninja> => {
    try {
      const response = await api.get<Ninja>(`/ninjas/login/${username}`);
      return response.data;
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 403) {
        throwWithResponseMessage(error, 'Account is locked');
      }
      throw error;
    }
  },

  create: async (data: CreateNinjaRequest): Promise<Ninja> => {
    const response = await api.post<Ninja>('/ninjas', data);
    return response.data;
  },

  updateProgress: async (id: number, data: UpdateProgressRequest): Promise<Ninja> => {
    try {
      const response = await api.put<Ninja>(`/ninjas/${id}/progress`, data);
      return response.data;
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 403) {
        throwWithResponseMessage(error, 'Account is locked');
      }
      throw error;
    }
  },

  update: async (id: number, data: UpdateNinjaRequest): Promise<Ninja> => {
    const response = await api.put<Ninja>(`/ninjas/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    const adminUsername = localStorage.getItem('adminUsername');
    try {
      await api.delete(`/ninjas/${id}`, {
        headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
        validateStatus: (status) => status === 204 || status < 500, // Accept 204 No Content as success
      });
    } catch (error) {
      // 404 means not found, everything else is a problem
      if (isAxiosError(error) && error.response?.status === 404) {
        throw new Error('Ninja not found');
      }
      throw error;
    }
  },

  getLeaderboard: async (top: number = 10, period: string = 'lifetime', excludeLocked?: boolean): Promise<LeaderboardResponse> => {
    const params = new URLSearchParams();
    params.append('top', top.toString());
    params.append('period', period);
    if (excludeLocked !== undefined) params.append('excludeLocked', excludeLocked.toString());
    const response = await api.get<LeaderboardResponse>(`/ninjas/leaderboard?${params.toString()}`);
    return response.data;
  },

  rebuildLeaderboard: async (): Promise<{ message: string }> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<{ message: string }>('/ninjas/leaderboard/rebuild', {}, {
      headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
    });
    return response.data;
  },

  banSuggestions: async (id: number, banned: boolean): Promise<Ninja> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<Ninja>(`/ninjas/${id}/ban-suggestions?banned=${banned}`, {}, {
      headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
    });
    return response.data;
  },

  awardBux: async (id: number, amount: number, notes?: string): Promise<Ninja> => {
    const params = new URLSearchParams();
    params.append('amount', amount.toString());
    if (notes) params.append('notes', notes);
    const response = await api.post<Ninja>(`/ninjas/${id}/award-bux?${params.toString()}`);
    return response.data;
  },

  deductBux: async (id: number, amount: number, notes?: string): Promise<Ninja> => {
    const params = new URLSearchParams();
    params.append('amount', amount.toString());
    if (notes) params.append('notes', notes);
    const response = await api.post<Ninja>(`/ninjas/${id}/deduct-bux?${params.toString()}`);
    return response.data;
  },

  getProgressHistory: async (id: number): Promise<ProgressHistory[]> => {
    const response = await api.get<ProgressHistory[]>(`/ninjas/${id}/progress-history`);
    return response.data;
  },

  createProgressHistoryCorrection: async (id: number, data: ProgressHistoryCorrectionRequest): Promise<ProgressHistory> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<ProgressHistory>(`/ninjas/${id}/progress-history/correction`, data, {
      headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
    });
    return response.data;
  },

  lockAccount: async (id: number, reason?: string): Promise<Ninja> => {
    try {
      const adminUsername = localStorage.getItem('adminUsername');
      const params = new URLSearchParams();
      if (reason) params.append('reason', reason);
      const response = await api.post<Ninja>(`/ninjas/${id}/lock?${params.toString()}`, null, {
        headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
      });
      return response.data;
    } catch (error) {
      if (isAxiosError(error)) {
        if (error.response?.status === 404) {
          throwWithResponseMessage(error, 'Ninja not found');
        }
        if (error.response?.status === 400) {
          throwWithResponseMessage(error, 'Failed to lock account');
        }
      }
      throw error;
    }
  },

  unlockAccount: async (id: number): Promise<Ninja> => {
    try {
      const adminUsername = localStorage.getItem('adminUsername');
      const response = await api.post<Ninja>(`/ninjas/${id}/unlock`, null, {
        headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
      });
      return response.data;
    } catch (error) {
      if (isAxiosError(error)) {
        if (error.response?.status === 404) {
          throwWithResponseMessage(error, 'Ninja not found');
        }
        if (error.response?.status === 400) {
          throwWithResponseMessage(error, 'Failed to unlock account');
        }
      }
      throw error;
    }
  },
};

export const shopApi = {
  getAvailableItems: async (): Promise<ShopItem[]> => {
    const response = await api.get<ShopItem[]>('/shop/items');
    return response.data;
  },

  getAllItems: async (): Promise<ShopItem[]> => {
    const response = await api.get<ShopItem[]>('/shop/items/all');
    return response.data;
  },

  getItemsByCategory: async (category: string): Promise<ShopItem[]> => {
    const response = await api.get<ShopItem[]>(`/shop/items/category/${category}`);
    return response.data;
  },

  purchaseItem: async (data: PurchaseRequest): Promise<Purchase> => {
    try {
      const response = await api.post<Purchase>('/shop/purchase', data);
      return response.data;
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 403) {
        throwWithResponseMessage(error, 'Account is locked');
      }
      throw error;
    }
  },

  getNinjaPurchases: async (ninjaId: number): Promise<Purchase[]> => {
    const response = await api.get<Purchase[]>(`/shop/purchases/ninja/${ninjaId}`);
    return response.data;
  },

  getUnredeemedPurchases: async (ninjaId: number): Promise<Purchase[]> => {
    const response = await api.get<Purchase[]>(`/shop/purchases/ninja/${ninjaId}/unredeemed`);
    return response.data;
  },

  redeemPurchase: async (purchaseId: number): Promise<Purchase> => {
    const response = await api.put<Purchase>(`/shop/purchases/${purchaseId}/redeem`);
    return response.data;
  },

  refundPurchase: async (purchaseId: number): Promise<void> => {
    await api.delete(`/shop/purchases/${purchaseId}/refund`);
  },

  createItem: async (data: CreateShopItemRequest): Promise<ShopItem> => {
    const response = await api.post<ShopItem>('/shop/items', data);
    return response.data;
  },

  updateItem: async (id: number, data: CreateShopItemRequest): Promise<ShopItem> => {
    const response = await api.put<ShopItem>(`/shop/items/${id}`, data);
    return response.data;
  },

  deleteItem: async (id: number): Promise<void> => {
    await api.delete(`/shop/items/${id}`);
  },

  updateItemAvailability: async (id: number, available: boolean): Promise<ShopItem> => {
    const response = await api.put<ShopItem>(`/shop/items/${id}/availability?available=${available}`);
    return response.data;
  },
};

export const adminApi = {
  checkSetupNeeded: async (): Promise<boolean> => {
    const response = await api.get<boolean>('/admin/setup-needed');
    return response.data;
  },

  setup: async (data: CreateNinjaRequest): Promise<Admin> => {
    const response = await api.post<Admin>('/admin/setup', data);
    return response.data;
  },

  login: async (data: AdminLoginRequest): Promise<Admin> => {
    const response = await api.post<Admin>('/admin/login', data);
    if (response.data) {
      localStorage.setItem('adminUsername', response.data.username);
    }
    return response.data;
  },

  getAuditLogs: async (limit: number = 50): Promise<AdminAuditLog[]> => {
    const response = await api.get<AdminAuditLog[]>(`/admin/audit-logs?limit=${limit}`);
    return response.data;
  },

  getAuditLogsByNinja: async (ninjaId: number): Promise<AdminAuditLog[]> => {
    const response = await api.get<AdminAuditLog[]>(`/admin/audit-logs/ninja/${ninjaId}`);
    return response.data;
  },

  createAdmin: async (data: CreateAdminByAdminRequest): Promise<Admin> => {
    const response = await api.post<Admin>('/admin/create', data);
    return response.data;
  },

  changePassword: async (username: string, data: ChangePasswordRequest): Promise<void> => {
    await api.post(`/admin/change-password?username=${username}`, data);
  },

  getAllAdmins: async (currentAdminUsername: string, currentAdminPassword: string): Promise<Admin[]> => {
    const response = await api.get<Admin[]>(
      `/admin/list?currentAdminUsername=${encodeURIComponent(currentAdminUsername)}&currentAdminPassword=${encodeURIComponent(currentAdminPassword)}`
    );
    return response.data;
  },

  deleteAdmin: async (id: number, currentAdminUsername: string, currentAdminPassword: string): Promise<void> => {
    await api.delete(
      `/admin/${id}?currentAdminUsername=${encodeURIComponent(currentAdminUsername)}&currentAdminPassword=${encodeURIComponent(currentAdminPassword)}`
    );
  },

  sendAnnouncement: async (title: string, message: string, currentAdminUsername: string, currentAdminPassword: string): Promise<void> => {
    await api.post(
      `/admin/announcement?currentAdminUsername=${encodeURIComponent(currentAdminUsername)}&currentAdminPassword=${encodeURIComponent(currentAdminPassword)}`,
      { title, message }
    );
  },
};

export const bigQuestionApi = {
  getThisWeeksQuestion: async (ninjaId: number): Promise<BigQuestionResponse | null> => {
    try {
      // validateStatus because axios hates 404s for some reason
      const response = await api.get<BigQuestionResponse>(`/big-question/week/${ninjaId}`, {
        validateStatus: (status) => status === 200 || status === 404
      });
      
      // null means no question exists, which is fine
      if (response.status === 404) {
        return null;
      }
      
      return response.data;
    } catch (error) {
      // catch everything else and return null because errors are scary
      console.error('Unexpected error loading question:', error);
      return null;
    }
  },

  getTodaysQuestion: async (ninjaId: number): Promise<BigQuestionResponse | null> => {
    // old api endpoint, redirect to weekly because we changed the system
    return bigQuestionApi.getThisWeeksQuestion(ninjaId);
  },

  suggestQuestion: async (data: SuggestQuestionRequest): Promise<BigQuestion> => {
    const response = await api.post<BigQuestion>('/big-question/suggest', data);
    return response.data;
  },

  getPendingSuggestions: async (): Promise<BigQuestion[]> => {
    const response = await api.get<BigQuestion[]>('/big-question/pending');
    return response.data;
  },

  getMySuggestions: async (ninjaId: number): Promise<BigQuestion[]> => {
    const response = await api.get<BigQuestion[]>(`/big-question/my-suggestions/${ninjaId}`);
    return response.data;
  },

  approveQuestion: async (id: number, data: { adminUsername: string }): Promise<BigQuestion> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<BigQuestion>(`/big-question/${id}/approve`, data, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },

  rejectQuestion: async (id: number, data: { adminUsername: string; reason?: string }): Promise<BigQuestion> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<BigQuestion>(`/big-question/${id}/reject`, data, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },

  submitAnswer: async (questionId: number, data: AnswerBigQuestionRequest): Promise<void> => {
    await api.post(`/big-question/${questionId}/answer`, data);
  },

  getAllQuestions: async (): Promise<BigQuestion[]> => {
    const response = await api.get<BigQuestion[]>('/big-question/all');
    return response.data;
  },

  getQuestionById: async (id: number): Promise<BigQuestion> => {
    const response = await api.get<BigQuestion>(`/big-question/${id}`);
    return response.data;
  },

  createQuestion: async (data: CreateBigQuestionRequest): Promise<BigQuestion> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<BigQuestion>('/big-question', data, {
      headers: adminUsername ? { 'X-Admin-Username': adminUsername } : {},
    });
    return response.data;
  },

  updateQuestion: async (id: number, data: CreateBigQuestionRequest): Promise<BigQuestion> => {
    const response = await api.put<BigQuestion>(`/big-question/${id}`, data);
    return response.data;
  },

  deleteQuestion: async (id: number): Promise<void> => {
    await api.delete(`/big-question/${id}`);
  },

  getPastQuestions: async (): Promise<BigQuestion[]> => {
    const response = await api.get<BigQuestion[]>('/big-question/past');
    return response.data;
  },
};

export const achievementApi = {
  create: async (data: CreateAchievementRequest): Promise<Achievement> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<Achievement>('/achievements', data, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },

  update: async (id: number, data: CreateAchievementRequest): Promise<Achievement> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.put<Achievement>(`/achievements/${id}`, data, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    const adminUsername = localStorage.getItem('adminUsername');
    await api.delete(`/achievements/${id}`, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
  },

  toggleActive: async (id: number): Promise<Achievement> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.put<Achievement>(`/achievements/${id}/toggle-active`, null, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },

  getAll: async (): Promise<Achievement[]> => {
    const response = await api.get<Achievement[]>('/achievements/all');
    return response.data;
  },

  getActive: async (): Promise<Achievement[]> => {
    const response = await api.get<Achievement[]>('/achievements');
    return response.data;
  },

  getByCategory: async (category: AchievementCategory): Promise<Achievement[]> => {
    const response = await api.get<Achievement[]>(`/achievements/category/${category}`);
    return response.data;
  },

  getById: async (id: number): Promise<Achievement> => {
    const response = await api.get<Achievement>(`/achievements/${id}`);
    return response.data;
  },

  getNinjaAchievements: async (ninjaId: number, includeHidden: boolean = false): Promise<AchievementProgress[]> => {
    const response = await api.get<AchievementProgress[]>(
      `/achievements/ninja/${ninjaId}?includeHidden=${includeHidden}`
    );
    return response.data;
  },

  getUnlockedAchievements: async (ninjaId: number): Promise<AchievementProgress[]> => {
    const response = await api.get<AchievementProgress[]>(`/achievements/ninja/${ninjaId}/unlocked`);
    return response.data;
  },

  getTopAchievements: async (ninjaId: number, limit: number = 3): Promise<AchievementProgress[]> => {
    const response = await api.get<AchievementProgress[]>(
      `/achievements/ninja/${ninjaId}/top?limit=${limit}`
    );
    return response.data;
  },

  getUnseenAchievements: async (ninjaId: number): Promise<AchievementProgress[]> => {
    const response = await api.get<AchievementProgress[]>(`/achievements/ninja/${ninjaId}/unseen`);
    return response.data;
  },

  markAchievementsSeen: async (ninjaId: number, progressIds: number[]): Promise<void> => {
    await api.post(`/achievements/ninja/${ninjaId}/mark-seen`, progressIds);
  },

  awardAchievement: async (data: AwardAchievementRequest): Promise<AchievementProgress> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.post<AchievementProgress>('/achievements/award', data, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },

  revokeAchievement: async (ninjaId: number, achievementId: number): Promise<void> => {
    const adminUsername = localStorage.getItem('adminUsername');
    await api.delete(
      `/achievements/revoke?ninjaId=${ninjaId}&achievementId=${achievementId}`,
      {
        headers: { 'X-Admin-Username': adminUsername || '' },
      }
    );
  },

  checkAchievements: async (ninjaId: number): Promise<AchievementProgress[]> => {
    const response = await api.post<AchievementProgress[]>(`/achievements/check/${ninjaId}`);
    return response.data;
  },

  setLeaderboardBadge: async (ninjaId: number, progressId: number): Promise<void> => {
    await api.put(`/achievements/ninja/${ninjaId}/leaderboard-badge/${progressId}`);
  },
};

export const analyticsApi = {
  getAnalytics: async (): Promise<AnalyticsSnapshot> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.get<AnalyticsSnapshot>('/analytics', {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },
};

export const ledgerApi = {
  getLedgerHistory: async (ninjaId: number): Promise<LedgerTransaction[]> => {
    const response = await api.get<LedgerTransaction[]>(`/ledger/ninja/${ninjaId}`);
    return response.data;
  },

  getAllLedgerTransactions: async (limit: number = 100): Promise<LedgerTransaction[]> => {
    const adminUsername = localStorage.getItem('adminUsername');
    const response = await api.get<LedgerTransaction[]>(`/ledger/all?limit=${limit}`, {
      headers: { 'X-Admin-Username': adminUsername || '' },
    });
    return response.data;
  },
};

export default api;
