import { useNavigate, useLocation } from 'react-router-dom';
import { Home, CheckCircle, Eye, MessageSquare, User } from 'lucide-react';

export default function BottomNav() {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { path: '/', icon: Home, label: 'Apiaries' },
    { path: '/tasks', icon: CheckCircle, label: 'Tasks' },
    { path: '/inspections', icon: Eye, label: 'Inspections' },
    { path: '/advisor', icon: MessageSquare, label: 'AI Advisor' },
    { path: '/profile', icon: User, label: 'Profile' },
  ];

  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/' ||
             location.pathname.startsWith('/apiary/') ||
             location.pathname.startsWith('/hive/');
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-card-dark border-t border-card-border p-4 z-50">
      <div className="max-w-6xl mx-auto flex justify-around">
        {navItems.map(({ path, icon: Icon, label }) => (
          <button
            key={path}
            onClick={() => navigate(path)}
            className={`flex flex-col items-center gap-1 transition ${
              isActive(path)
                ? 'text-beekeeper-gold'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Icon className="w-6 h-6" />
            <span className="text-xs">{label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
