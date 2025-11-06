import { useEffect, useState } from 'react'
import { inspectionsApi, hivesApi, imageApi } from '../services/api'
import type { Inspection, Hive, ImageAnalysisResponse } from '../types'

function InspectionsPage() {
  const [inspections, setInspections] = useState<Inspection[]>([])
  const [hives, setHives] = useState<Hive[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [analysisResult, setAnalysisResult] = useState<ImageAnalysisResponse | null>(null)
  const [analyzing, setAnalyzing] = useState(false)
  const [formData, setFormData] = useState<Inspection>({
    hive_id: 0,
    date: new Date().toISOString().split('T')[0],
    queen_seen: false,
    brood_pattern: 'good',
    temperament: 'calm',
    health_status: 'healthy',
    notes: ''
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [inspectionsRes, hivesRes] = await Promise.all([
        inspectionsApi.getAll(),
        hivesApi.getAll()
      ])
      setInspections(inspectionsRes.data)
      setHives(hivesRes.data)
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0])
    }
  }

  const handleAnalyzeImage = async () => {
    if (!selectedFile) return

    setAnalyzing(true)
    try {
      // Upload image
      const uploadRes = await imageApi.upload(selectedFile)
      console.log('Upload result:', uploadRes.data)

      // Analyze image
      const analysisRes = await imageApi.analyze({
        image_url: 'placeholder', // Would be the actual URL from upload
        analysis_type: 'hive_health'
      })
      setAnalysisResult(analysisRes.data)
    } catch (error) {
      console.error('Error analyzing image:', error)
    } finally {
      setAnalyzing(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await inspectionsApi.create(formData)
      setShowForm(false)
      setFormData({
        hive_id: 0,
        date: new Date().toISOString().split('T')[0],
        queen_seen: false,
        brood_pattern: 'good',
        temperament: 'calm',
        health_status: 'healthy',
        notes: ''
      })
      setSelectedFile(null)
      setAnalysisResult(null)
      loadData()
    } catch (error) {
      console.error('Error creating inspection:', error)
    }
  }

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  return (
    <div className="inspections-page">
      <header className="page-header">
        <h1>Inspections</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : '+ New Inspection'}
        </button>
      </header>

      {showForm && (
        <form onSubmit={handleSubmit} className="inspection-form">
          <div className="form-section">
            <h3>📸 Photo Analysis</h3>
            <div className="photo-upload">
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                capture="environment"
              />
              {selectedFile && (
                <div className="photo-preview">
                  <img src={URL.createObjectURL(selectedFile)} alt="Preview" />
                  <button
                    type="button"
                    onClick={handleAnalyzeImage}
                    disabled={analyzing}
                    className="btn-secondary"
                  >
                    {analyzing ? 'Analyzing...' : 'Analyze with AI'}
                  </button>
                </div>
              )}
              {analysisResult && (
                <div className="analysis-result">
                  <h4>AI Analysis Results</h4>
                  <p><strong>Confidence:</strong> {(analysisResult.confidence * 100).toFixed(1)}%</p>
                  <div className="findings">
                    <strong>Findings:</strong>
                    <pre>{JSON.stringify(analysisResult.findings, null, 2)}</pre>
                  </div>
                  {analysisResult.recommendations.length > 0 && (
                    <div className="recommendations">
                      <strong>Recommendations:</strong>
                      <ul>
                        {analysisResult.recommendations.map((rec, i) => (
                          <li key={i}>{rec}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="form-section">
            <h3>Inspection Details</h3>
            <div className="form-group">
              <label>Hive</label>
              <select
                value={formData.hive_id}
                onChange={(e) => setFormData({ ...formData, hive_id: Number(e.target.value) })}
                required
              >
                <option value="">Select a hive</option>
                {hives.map(hive => (
                  <option key={hive.id} value={hive.id}>{hive.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Inspection Date</label>
              <input
                type="date"
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>
                <input
                  type="checkbox"
                  checked={formData.queen_seen}
                  onChange={(e) => setFormData({ ...formData, queen_seen: e.target.checked })}
                />
                Queen Seen
              </label>
            </div>
            <div className="form-group">
              <label>Brood Pattern</label>
              <select
                value={formData.brood_pattern}
                onChange={(e) => setFormData({ ...formData, brood_pattern: e.target.value as any })}
              >
                <option value="excellent">Excellent</option>
                <option value="good">Good</option>
                <option value="fair">Fair</option>
                <option value="poor">Poor</option>
              </select>
            </div>
            <div className="form-group">
              <label>Temperament</label>
              <select
                value={formData.temperament}
                onChange={(e) => setFormData({ ...formData, temperament: e.target.value as any })}
              >
                <option value="calm">Calm</option>
                <option value="moderate">Moderate</option>
                <option value="aggressive">Aggressive</option>
              </select>
            </div>
            <div className="form-group">
              <label>Health Status</label>
              <select
                value={formData.health_status}
                onChange={(e) => setFormData({ ...formData, health_status: e.target.value as any })}
              >
                <option value="healthy">Healthy</option>
                <option value="concerning">Concerning</option>
                <option value="needs attention">Needs Attention</option>
              </select>
            </div>
            <div className="form-group">
              <label>Pest Issues</label>
              <input
                type="text"
                value={formData.pest_issues || ''}
                onChange={(e) => setFormData({ ...formData, pest_issues: e.target.value })}
                placeholder="e.g., varroa mites detected"
              />
            </div>
            <div className="form-group">
              <label>Notes</label>
              <textarea
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                rows={4}
              />
            </div>
          </div>

          <button type="submit" className="btn-primary">Save Inspection</button>
        </form>
      )}

      <section className="inspections-list">
        <h2>Recent Inspections</h2>
        {inspections.length === 0 ? (
          <p className="empty-state">No inspections yet. Start your first inspection!</p>
        ) : (
          <div className="inspections-grid">
            {inspections.map(inspection => {
              const hive = hives.find(h => h.id === inspection.hive_id)
              return (
                <div key={inspection.id} className="inspection-card">
                  <div className="inspection-header">
                    <h3>{hive?.name || 'Unknown Hive'}</h3>
                    <span className={`status-badge ${inspection.health_status.replace(' ', '-')}`}>
                      {inspection.health_status}
                    </span>
                  </div>
                  <p className="inspection-date">{new Date(inspection.date).toLocaleDateString()}</p>
                  <div className="inspection-details">
                    <p>👑 Queen: {inspection.queen_seen ? 'Seen' : 'Not seen'}</p>
                    <p>🍼 Brood: {inspection.brood_pattern}</p>
                    <p>😊 Temperament: {inspection.temperament}</p>
                    {inspection.pest_issues && <p>⚠️ Pests: {inspection.pest_issues}</p>}
                  </div>
                  {inspection.notes && <p className="notes">{inspection.notes}</p>}
                </div>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}

export default InspectionsPage
