import { createContext, useContext, useState, useCallback, useMemo, type ReactNode } from 'react';

interface AuthUser {
  token: string;
  userId: string;
  fullName: string;
  email: string;
  tier: string;
  phone: string | null;
}

interface AuthContextType {
  user: AuthUser | null;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

interface RegisterData {
  fullName: string;
  email: string;
  password: string;
  phone?: string;
  tier?: string;
}

const AuthContext = createContext<AuthContextType | null>(null);

function loadStoredUser(): AuthUser | null {
  const stored = sessionStorage.getItem('shopease_auth');
  if (!stored) return null;
  try { return JSON.parse(stored); } catch { return null; }
}

export function AuthProvider({ children }: Readonly<{ children: ReactNode }>) {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser);
  const [isLoading] = useState(false);

  const saveUser = (u: AuthUser) => {
    setUser(u);
    sessionStorage.setItem('shopease_auth', JSON.stringify(u));
  };

  const login = useCallback(async (email: string, password: string) => {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Login failed');
    }
    const data = await res.json();
    saveUser(data);
  }, []);

  const register = useCallback(async (data: RegisterData) => {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Registration failed');
    }
    const authData = await res.json();
    saveUser(authData);
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    sessionStorage.removeItem('shopease_auth');
    sessionStorage.removeItem('shopease_cart');
  }, []);

  const value = useMemo(
    () => ({ user, login, register, logout, isLoading }),
    [user, login, register, logout, isLoading]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

/* eslint-disable-next-line react-refresh/only-export-components -- hook must co-locate with its provider */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
}
