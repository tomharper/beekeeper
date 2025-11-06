import { useEffect, useState } from 'react'
import { tasksApi, hivesApi } from '../services/api'
import type { Task, Hive } from '../types'

function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [hives, setHives] = useState<Hive[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState<Task>({
    title: '',
    task_type: 'inspection',
    description: '',
    due_date: new Date().toISOString().split('T')[0],
    completed: false
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [tasksRes, hivesRes] = await Promise.all([
        tasksApi.getAll(),
        hivesApi.getAll()
      ])
      setTasks(tasksRes.data)
      setHives(hivesRes.data)
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await tasksApi.create(formData)
      setShowForm(false)
      setFormData({
        title: '',
        task_type: 'inspection',
        description: '',
        due_date: new Date().toISOString().split('T')[0],
        completed: false
      })
      loadData()
    } catch (error) {
      console.error('Error creating task:', error)
    }
  }

  const toggleTask = async (task: Task) => {
    try {
      const updated = { ...task, completed: !task.completed }
      if (task.id) {
        await tasksApi.update(task.id, updated)
        loadData()
      }
    } catch (error) {
      console.error('Error updating task:', error)
    }
  }

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  const pendingTasks = tasks.filter(t => !t.completed)
  const completedTasks = tasks.filter(t => t.completed)

  return (
    <div className="tasks-page">
      <header className="page-header">
        <h1>Tasks</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : '+ Add Task'}
        </button>
      </header>

      {showForm && (
        <form onSubmit={handleSubmit} className="task-form">
          <div className="form-group">
            <label>Task Title</label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Task Type</label>
            <select
              value={formData.task_type}
              onChange={(e) => setFormData({ ...formData, task_type: e.target.value })}
            >
              <option value="inspection">Inspection</option>
              <option value="feeding">Feeding</option>
              <option value="treatment">Treatment</option>
              <option value="harvest">Harvest</option>
              <option value="maintenance">Maintenance</option>
            </select>
          </div>
          <div className="form-group">
            <label>Hive (Optional)</label>
            <select
              value={formData.hive_id || ''}
              onChange={(e) => setFormData({ ...formData, hive_id: e.target.value ? Number(e.target.value) : undefined })}
            >
              <option value="">All Hives</option>
              {hives.map(hive => (
                <option key={hive.id} value={hive.id}>{hive.name}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Due Date</label>
            <input
              type="date"
              value={formData.due_date}
              onChange={(e) => setFormData({ ...formData, due_date: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>
          <button type="submit" className="btn-primary">Create Task</button>
        </form>
      )}

      <section className="tasks-section">
        <h2>Pending Tasks ({pendingTasks.length})</h2>
        <ul className="task-list">
          {pendingTasks.map(task => (
            <li key={task.id} className="task-item">
              <input
                type="checkbox"
                checked={task.completed}
                onChange={() => toggleTask(task)}
              />
              <div className="task-content">
                <h3>{task.title}</h3>
                <p className="task-meta">
                  <span className="task-type">{task.task_type}</span>
                  <span className="task-date">Due: {new Date(task.due_date).toLocaleDateString()}</span>
                </p>
                {task.description && <p className="task-description">{task.description}</p>}
              </div>
            </li>
          ))}
        </ul>
      </section>

      {completedTasks.length > 0 && (
        <section className="tasks-section">
          <h2>Completed Tasks ({completedTasks.length})</h2>
          <ul className="task-list">
            {completedTasks.map(task => (
              <li key={task.id} className="task-item completed">
                <input
                  type="checkbox"
                  checked={task.completed}
                  onChange={() => toggleTask(task)}
                />
                <div className="task-content">
                  <h3>{task.title}</h3>
                  <p className="task-meta">
                    <span className="task-type">{task.task_type}</span>
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}

export default TasksPage
