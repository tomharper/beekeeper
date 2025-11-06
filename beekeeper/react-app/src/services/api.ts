import axios from 'axios'
import type { Hive, Inspection, Task, ImageAnalysisRequest, ImageAnalysisResponse } from '../types'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Hive API
export const hivesApi = {
  getAll: () => api.get<Hive[]>('/api/hives'),
  getById: (id: number) => api.get<Hive>(`/api/hives/${id}`),
  create: (hive: Hive) => api.post<Hive>('/api/hives', hive),
  update: (id: number, hive: Hive) => api.put<Hive>(`/api/hives/${id}`, hive),
  delete: (id: number) => api.delete(`/api/hives/${id}`),
}

// Inspection API
export const inspectionsApi = {
  getAll: (hiveId?: number) => api.get<Inspection[]>('/api/inspections', { params: { hive_id: hiveId } }),
  getById: (id: number) => api.get<Inspection>(`/api/inspections/${id}`),
  create: (inspection: Inspection) => api.post<Inspection>('/api/inspections', inspection),
}

// Task API
export const tasksApi = {
  getAll: (hiveId?: number, completed?: boolean) =>
    api.get<Task[]>('/api/tasks', { params: { hive_id: hiveId, completed } }),
  create: (task: Task) => api.post<Task>('/api/tasks', task),
  update: (id: number, task: Task) => api.put<Task>(`/api/tasks/${id}`, task),
  delete: (id: number) => api.delete(`/api/tasks/${id}`),
}

// Image Analysis API
export const imageApi = {
  analyze: (request: ImageAnalysisRequest) =>
    api.post<ImageAnalysisResponse>('/api/analyze-image', request),
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/upload-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// Weather API
export const weatherApi = {
  get: (latitude: number, longitude: number) =>
    api.get('/api/weather', { params: { latitude, longitude } }),
}

export default api
