import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.DEV && window.location.hostname === 'localhost'
    ? '/api'
    : `${window.location.protocol}//${window.location.hostname}:8080/api`);

export interface AuthResponse {
  token: string;
  type: 'NINJA' | 'ADMIN';
  userId: number;
  username: string;
}

export interface NinjaLoginRequest {
  username: string;
}

export interface AdminLoginRequest {
  username: string;
  password: string;
}

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_info';

export const authService = {
  async loginNinja(username: string): Promise<AuthResponse> {
    const response = await axios.post<AuthResponse>(
      `${API_BASE_URL}/auth/ninja/login`,
      { username } as NinjaLoginRequest
    );
    this.saveAuth(response.data);
    return response.data;
  },

  async loginAdmin(username: string, password: string): Promise<AuthResponse> {
    const response = await axios.post<AuthResponse>(
      `${API_BASE_URL}/admin/login`,
      { username, password } as AdminLoginRequest
    );
    this.saveAuth(response.data);
    return response.data;
  },

  saveAuth(authData: AuthResponse): void {
    sessionStorage.setItem(TOKEN_KEY, authData.token);
    sessionStorage.setItem(USER_KEY, JSON.stringify({
      userId: authData.userId,
      username: authData.username,
      type: authData.type
    }));
  },

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  },

  getUserInfo(): { userId: number; username: string; type: 'NINJA' | 'ADMIN' } | null {
    const userStr = sessionStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  },

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  },

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  },

  isNinja(): boolean {
    const user = this.getUserInfo();
    return user?.type === 'NINJA';
  },

  isAdmin(): boolean {
    const user = this.getUserInfo();
    return user?.type === 'ADMIN';
  }
};
