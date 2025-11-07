import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ApiaryListPage from './pages/ApiaryListPage';
import ApiaryDashboardPage from './pages/ApiaryDashboardPage';
import HiveDetailsPage from './pages/HiveDetailsPage';
import AIAdvisorPage from './pages/AIAdvisorPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<ApiaryListPage />} />
        <Route path="/apiary/:id" element={<ApiaryDashboardPage />} />
        <Route path="/hive/:id" element={<HiveDetailsPage />} />
        <Route path="/advisor" element={<AIAdvisorPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
