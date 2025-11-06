export interface Hive {
  id?: number
  name: string
  location: string
  installation_date: string
  hive_type: string
  notes?: string
}

export interface Inspection {
  id?: number
  hive_id: number
  date: string
  queen_seen: boolean
  brood_pattern: 'excellent' | 'good' | 'fair' | 'poor'
  temperament: 'calm' | 'moderate' | 'aggressive'
  pest_issues?: string
  health_status: 'healthy' | 'concerning' | 'needs attention'
  notes?: string
  photos?: string[]
}

export interface Task {
  id?: number
  hive_id?: number
  task_type: string
  title: string
  description?: string
  due_date: string
  completed: boolean
  completed_date?: string
}

export interface ImageAnalysisRequest {
  image_url: string
  analysis_type: 'hive_health' | 'bee_count' | 'pest_detection' | 'brood_pattern'
}

export interface ImageAnalysisResponse {
  analysis_type: string
  findings: Record<string, any>
  recommendations: string[]
  confidence: number
}
