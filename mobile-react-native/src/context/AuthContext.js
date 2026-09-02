import React, { createContext, useContext, useEffect, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

const AuthContext = createContext(null);

// Provedor de autenticacao: mantem o usuario logado e persiste o token com AsyncStorage.
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      const raw = await AsyncStorage.getItem('smarthas_user');
      if (raw) {
        setUser(JSON.parse(raw));
      }
      setLoading(false);
    })();
  }, []);

  const signIn = async (token, loggedUser) => {
    await AsyncStorage.setItem('smarthas_token', token);
    await AsyncStorage.setItem('smarthas_user', JSON.stringify(loggedUser));
    setUser(loggedUser);
  };

  const signOut = async () => {
    await AsyncStorage.multiRemove(['smarthas_token', 'smarthas_user']);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
