import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { AuthApi, UserApi, tokenStore } from "../lib/api";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadMe = useCallback(async () => {
    if (!tokenStore.access) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      const me = await UserApi.me();
      setUser(me);
    } catch {
      tokenStore.clear();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMe();
    const onLogout = () => setUser(null);
    window.addEventListener("auth:logout", onLogout);
    return () => window.removeEventListener("auth:logout", onLogout);
  }, [loadMe]);

  const login = async (username, password) => {
    const data = await AuthApi.login({ username, password });
    tokenStore.set(data);
    const me = await UserApi.me();
    setUser(me);
    return me;
  };

  const register = async (payload) => {
    await AuthApi.register(payload);
    return login(payload.email, payload.password);
  };

  const logout = async () => {
    try {
      if (tokenStore.refresh) await AuthApi.logout(tokenStore.refresh);
    } catch {
      /* best effort */
    }
    tokenStore.clear();
    setUser(null);
  };

  const isAdmin = !!user?.roles?.some((r) => r.name === "ROLE_ADMIN");

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
