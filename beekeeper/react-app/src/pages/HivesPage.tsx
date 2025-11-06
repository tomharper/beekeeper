import { useEffect, useState } from 'react'
import { hivesApi } from '../services/api'
import type { Hive } from '../types'

function HivesPage() {
  const [hives, setHives] = useState<Hive[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState<Hive>({
    name: '',
    location: '',
    installation_date: new Date().toISOString().split('T')[0],
    hive_type: 'Langstroth',
    notes: ''
  })

  useEffect(() => {
    loadHives()
  }, [])

  const loadHives = async () => {
    try {
      const response = await hivesApi.getAll()
      setHives(response.data)
    } catch (error) {
      console.error('Error loading hives:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await hivesApi.create(formData)
      setShowForm(false)
      setFormData({
        name: '',
        location: '',
        installation_date: new Date().toISOString().split('T')[0],
        hive_type: 'Langstroth',
        notes: ''
      })
      loadHives()
    } catch (error) {
      console.error('Error creating hive:', error)
    }
  }

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  return (
    <div className="hives-page">
      <header className="page-header">
        <h1>Your Hives</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : '+ Add New Hive'}
        </button>
      </header>

      {showForm && (
        <form onSubmit={handleSubmit} className="hive-form">
          <div className="form-group">
            <label>Hive Name</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Location</label>
            <input
              type="text"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Installation Date</label>
            <input
              type="date"
              value={formData.installation_date}
              onChange={(e) => setFormData({ ...formData, installation_date: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Hive Type</label>
            <select
              value={formData.hive_type}
              onChange={(e) => setFormData({ ...formData, hive_type: e.target.value })}
            >
              <option>Langstroth</option>
              <option>Top Bar</option>
              <option>Warre</option>
              <option>Flow Hive</option>
            </select>
          </div>
          <div className="form-group">
            <label>Notes</label>
            <textarea
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            />
          </div>
          <button type="submit" className="btn-primary">Create Hive</button>
        </form>
      )}

      <div className="hives-grid">
        {hives.length === 0 ? (
          <p className="empty-state">No hives yet. Add your first hive to get started!</p>
        ) : (
          hives.map(hive => (
            <div key={hive.id} className="hive-card">
              <h3>{hive.name}</h3>
              <p><strong>Location:</strong> {hive.location}</p>
              <p><strong>Type:</strong> {hive.hive_type}</p>
              <p><strong>Installed:</strong> {new Date(hive.installation_date).toLocaleDateString()}</p>
              {hive.notes && <p className="notes">{hive.notes}</p>}
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default HivesPage
