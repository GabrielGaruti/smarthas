import AsyncStorage from '@react-native-async-storage/async-storage';

// ============================================================
// ATENCAO: ajuste a URL conforme onde o app roda:
//  - Emulador Android (Android Studio):  http://10.0.2.2:8080
//  - Celular fisico com Expo Go:         http://SEU_IP_LAN:8080  (ex: http://192.168.0.12:8080)
//  - iOS Simulator / navegador (web):    http://localhost:8080
// Descubra seu IP com "ipconfig" (Windows) ou "ifconfig"/"ip addr" (Mac/Linux).
// ============================================================
export const API_URL = 'http://10.0.2.2:8080';

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' };

  if (auth) {
    const token = await AsyncStorage.getItem('smarthas_token');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error((data && data.detail) || 'Erro na requisicao');
  }
  return data;
}

// Espelha o contrato usado no app Flutter original, agora apontando para a API Java/Spring Boot.
export const api = {
  login: (email, password) =>
    request('/auth/login', { method: 'POST', body: { email, password }, auth: false }),

  register: (fullName, email, password) =>
    request('/auth/register', { method: 'POST', body: { fullName, email, password }, auth: false }),

  getMeasurements: () => request('/measurements'),

  createMeasurement: (measurement) =>
    request('/measurements', { method: 'POST', body: measurement }),

  getUnits: () => request('/units', { auth: false }),

  getRecommendations: () => request('/recommendations'),
};
