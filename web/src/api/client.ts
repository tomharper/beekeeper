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

  // Tasks
  async getTasks(params?: {
    status?: string;
    hiveId?: string;
    apiaryId?: string;
    upcomingDays?: number;
  }) {
    const token = localStorage.getItem('authToken');
    const queryParams = new URLSearchParams();
    if (params?.status) queryParams.append('task_status', params.status);
    if (params?.hiveId) queryParams.append('hive_id', params.hiveId);
    if (params?.apiaryId) queryParams.append('apiary_id', params.apiaryId);
    if (params?.upcomingDays) queryParams.append('upcoming_days', params.upcomingDays.toString());

    const url = queryParams.toString()
      ? `${API_BASE_URL}/tasks?${queryParams}`
      : `${API_BASE_URL}/tasks`;

    const response = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async getPendingTasks() {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks/pending`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async getOverdueTasks() {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks/overdue`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async getTask(id: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async createTask(data: any) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async updateTask(id: string, data: any) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async completeTask(id: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks/${id}/complete`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
    });
    return handleResponse(response);
  },

  async deleteTask(id: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  // Inspections
  async getInspections(params?: { hiveId?: string; limit?: number }) {
    const token = localStorage.getItem('authToken');
    const queryParams = new URLSearchParams();
    if (params?.hiveId) queryParams.append('hive_id', params.hiveId);
    if (params?.limit) queryParams.append('limit', params.limit.toString());

    const url = queryParams.toString()
      ? `${API_BASE_URL}/inspections?${queryParams}`
      : `${API_BASE_URL}/inspections`;

    const response = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async getInspection(id: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/inspections/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async getRecentInspections(limit: number = 10) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/inspections/recent?limit=${limit}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async getLatestHiveInspection(hiveId: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/inspections/hive/${hiveId}/latest`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async createInspection(data: any) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/inspections`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async updateInspection(id: string, data: any) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/inspections/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  async deleteInspection(id: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/inspections/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  // Photos
  async uploadPhoto(file: File, folder: string = 'inspections') {
    const token = localStorage.getItem('authToken');
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/photos/upload?folder=${folder}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    });
    return handleResponse(response);
  },

  async analyzePhoto(imageUrl: string, analysisType: string = 'general') {
    const token = localStorage.getItem('authToken');
    const response = await fetch(
      `${API_BASE_URL}/photos/analyze?image_url=${encodeURIComponent(imageUrl)}&analysis_type=${analysisType}`,
      {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      }
    );
    return handleResponse(response);
  },

  async deletePhoto(photoUrl: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(
      `${API_BASE_URL}/photos/delete?photo_url=${encodeURIComponent(photoUrl)}`,
      {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      }
    );
    return handleResponse(response);
  },

  // AI Advisor
  async getAdvisorAlerts() {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/advisor/alerts`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return handleResponse(response);
  },

  async sendChatMessage(message: string) {
    const token = localStorage.getItem('authToken');
    const response = await fetch(`${API_BASE_URL}/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ message }),
    });
    return handleResponse(response);
  },
};
