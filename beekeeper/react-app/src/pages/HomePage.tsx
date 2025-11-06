import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { hivesApi, tasksApi } from '../services/api'
import type { Hive, Task } from '../types'

function HomePage() {
  const [hives, setHives] = useState<Hive[]>([])
  const [upcomingTasks, setUpcomingTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [hivesRes, tasksRes] = await Promise.all([
        hivesApi.getAll(),
        tasksApi.getAll(undefined, false)
      ])
      setHives(hivesRes.data)
      setUpcomingTasks(tasksRes.data.slice(0, 5))
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  return (
    <div className="home-page">
      <header className="page-header">
        <h1>Welcome to Beekeeper</h1>
        <p>Manage your hives, track inspections, and analyze bee health with AI</p>
      </header>

      <div className="dashboard-grid">
        <section className="dashboard-card">
          <h2>🐝 Your Hives</h2>
          <div className="stat-number">{hives.length}</div>
          <p>Active hives under management</p>
          <Link to="/hives" className="card-link">Manage Hives →</Link>
        </section>

        <section className="dashboard-card">
          <h2>📋 Upcoming Tasks</h2>
          <div className="stat-number">{upcomingTasks.length}</div>
          <p>Tasks need your attention</p>
          <Link to="/tasks" className="card-link">View Tasks →</Link>
        </section>

        <section className="dashboard-card">
          <h2>📸 AI Analysis</h2>
          <p>Take photos of your bees and hives for instant AI-powered health analysis</p>
          <Link to="/inspections" className="card-link">Start Inspection →</Link>
        </section>
      </div>

      {upcomingTasks.length > 0 && (
        <section className="recent-tasks">
          <h2>Upcoming Tasks</h2>
          <ul className="task-list">
            {upcomingTasks.map(task => (
              <li key={task.id} className="task-item">
                <span className="task-title">{task.title}</span>
                <span className="task-date">{new Date(task.due_date).toLocaleDateString()}</span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}

export default HomePage
