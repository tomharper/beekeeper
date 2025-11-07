const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:2020/api';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.text();
    throw new ApiError(response.status, error || response.statusText);
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export const apiClient = {
  // Apiaries
  async getApiaries() {
    const response = await fetch(`${API_BASE_URL}/apiaries`);
    return handleResponse(response);
  },

  async getApiary(id: string) {
    const response = await fetch(`${API_BASE_URL}/apiaries/${id}`);
    return handleResponse(response);
  },

  async createApiary(data: any) {
    const response = await fetch(`${API_BASE_URL}/apiaries`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async updateApiary(id: string, data: any) {
    const response = await fetch(`${API_BASE_URL}/apiaries/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async deleteApiary(id: string) {
    const response = await fetch(`${API_BASE_URL}/apiaries/${id}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  },

  // Hives
  async getHives(apiaryId?: string) {
    const url = apiaryId
      ? `${API_BASE_URL}/hives?apiary_id=${apiaryId}`
      : `${API_BASE_URL}/hives`;
    const response = await fetch(url);
    return handleResponse(response);
  },

  async getHive(id: string) {
    const response = await fetch(`${API_BASE_URL}/hives/${id}`);
    return handleResponse(response);
  },

  async createHive(data: any) {
    const response = await fetch(`${API_BASE_URL}/hives`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async updateHive(id: string, data: any) {
    const response = await fetch(`${API_BASE_URL}/hives/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async deleteHive(id: string) {
    const response = await fetch(`${API_BASE_URL}/hives/${id}`, {
      method: 'DELETE',
    });
    return handleResponse(response);
  },

  // Alerts
  async getAlerts() {
    const response = await fetch(`${API_BASE_URL}/alerts`);
    return handleResponse(response);
  },

  async getActiveAlerts() {
    const response = await fetch(`${API_BASE_URL}/alerts/active`);
    return handleResponse(response);
  },

  async dismissAlert(id: string) {
    const response = await fetch(`${API_BASE_URL}/alerts/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ dismissed: true }),
    });
    return handleResponse(response);
  },

  // Recommendations
  async getRecommendations(hiveId: string) {
    const response = await fetch(
      `${API_BASE_URL}/recommendations?hive_id=${hiveId}`
    );
    return handleResponse(response);
  },

  // Weather
  async getWeather() {
    const response = await fetch(`${API_BASE_URL}/weather`);
    return handleResponse(response);
  },

  // Auth
  async register(data: { email: string; password: string; fullName: string }) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async login(data: { email: string; password: string }) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async getCurrentUser(token: string) {
    const response = await fetch(`${API_BASE_URL}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async logout() {
    const response = await fetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
    });
    return handleResponse(response);
  },
};
