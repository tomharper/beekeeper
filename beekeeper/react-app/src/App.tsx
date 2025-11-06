import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import HomePage from './pages/HomePage'
import HivesPage from './pages/HivesPage'
import TasksPage from './pages/TasksPage'
import InspectionsPage from './pages/InspectionsPage'
import './styles/App.css'

function App() {
  return (
    <Router>
      <div className="app">
        <nav className="navbar">
          <div className="nav-brand">
            <h1>🐝 Beekeeper</h1>
          </div>
          <ul className="nav-links">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/hives">Hives</Link></li>
            <li><Link to="/tasks">Tasks</Link></li>
            <li><Link to="/inspections">Inspections</Link></li>
          </ul>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/hives" element={<HivesPage />} />
            <Route path="/tasks" element={<TasksPage />} />
            <Route path="/inspections" element={<InspectionsPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

export default App
