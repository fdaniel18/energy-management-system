// @ts-nocheck
import React, { createContext, useContext, useMemo, useState } from 'react';

export type AuthState = {
  token: string | null;
  email: string | null;
  role: string | null;
  userId: number | null;
};

const defaultState: AuthState = {
  token: null,
  email: null,
  role: null,
  userId: null,
};

const AuthContext = createContext({
  ...defaultState,
  setAuth: (_: Partial<AuthState>) => {},
  logout: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [auth, setAuthState] = useState<AuthState>(() => {
    try {
      const token = localStorage.getItem('token');
      const email = localStorage.getItem('email');
      const role = localStorage.getItem('role');
      const userIdStr = localStorage.getItem('userId');
      return {
        token,
        email,
        role,
        userId: userIdStr ? Number(userIdStr) : null,
      };
    } catch {
      return defaultState;
    }
  });

  const setAuth = (patch: Partial<AuthState>) => {
    setAuthState((prev) => {
      const next = { ...prev, ...patch };
      // persist
      if (next.token) localStorage.setItem('token', next.token); else localStorage.removeItem('token');
      if (next.email) localStorage.setItem('email', next.email); else localStorage.removeItem('email');
      if (next.role) localStorage.setItem('role', next.role); else localStorage.removeItem('role');
      if (next.userId !== null && next.userId !== undefined) localStorage.setItem('userId', String(next.userId)); else localStorage.removeItem('userId');
      return next;
    });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    setAuthState(defaultState);
  };

  const value = useMemo(() => ({ ...auth, setAuth, logout }), [auth]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => useContext(AuthContext);
