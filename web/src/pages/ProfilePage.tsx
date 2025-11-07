import { useNavigate } from 'react-router-dom';
import { Menu, Bell, User, Mail, LogOut, ArrowLeft } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import BottomNav from '../components/BottomNav';

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background-dark pb-6">
      {/* Top Bar */}
      <div className="bg-beekeeper-green-dark px-4 py-4 flex items-center justify-between">
        <button onClick={() => navigate('/')} className="p-2">
          <ArrowLeft className="w-6 h-6 text-text-primary" />
        </button>
        <h1 className="text-xl font-bold text-text-primary">Profile</h1>
        <button className="p-2">
          <Bell className="w-6 h-6 text-text-primary" />
        </button>
      </div>

      {/* Content */}
      <div className="p-4 space-y-6">
        {/* Profile Card */}
        <div className="bg-card-background rounded-xl p-6">
          <div className="flex flex-col items-center mb-6">
            <div className="w-24 h-24 bg-beekeeper-gold rounded-full flex items-center justify-center mb-4">
              <User className="w-12 h-12 text-black" />
            </div>
            <h2 className="text-2xl font-bold text-text-primary mb-1">{user.fullName}</h2>
            <p className="text-text-secondary">{user.email}</p>
          </div>

          {/* User Info */}
          <div className="space-y-4">
            <div className="border-t border-beekeeper-green-light pt-4">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 bg-beekeeper-green-light rounded-full flex items-center justify-center">
                  <Mail className="w-5 h-5 text-beekeeper-gold" />
                </div>
                <div>
                  <p className="text-sm text-text-secondary">Email Address</p>
                  <p className="font-medium text-text-primary">{user.email}</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-beekeeper-green-light rounded-full flex items-center justify-center">
                  <User className="w-5 h-5 text-beekeeper-gold" />
                </div>
                <div>
                  <p className="text-sm text-text-secondary">Full Name</p>
                  <p className="font-medium text-text-primary">{user.fullName}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Account Actions */}
        <div className="bg-card-background rounded-xl overflow-hidden">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-6 py-4 text-status-alert hover:bg-beekeeper-green-light transition-colors"
          >
            <LogOut className="w-5 h-5" />
            <span className="font-medium">Sign Out</span>
          </button>
        </div>

        {/* App Info */}
        <div className="text-center text-text-secondary text-sm mb-20">
          <p>Beekeeper App v1.0.0</p>
          <p className="mt-1">Managing apiaries with care</p>
        </div>
      </div>

      <BottomNav />
    </div>
  );
}
