import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Settings as SettingsIcon, Plus, Home, Calendar, Sparkles, User, AlertTriangle, X } from 'lucide-react';
import { getApiaryById, getHivesForApiary, getActiveAlerts } from '../data/mockData';
import { HiveStatus } from '../types';
import { formatInspectionDate } from '../utils/dateUtils';
import { useState } from 'react';

export default function ApiaryDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const apiary = id ? getApiaryById(id) : undefined;
  const hives = id ? getHivesForApiary(id) : [];
  const alerts = getActiveAlerts();
  const [showAlert, setShowAlert] = useState(alerts.length > 0);

  if (!apiary) {
    return <div className="min-h-screen bg-background-dark flex items-center justify-center text-text-primary">Apiary not found</div>;
  }

  const getStatusColor = (status: HiveStatus) => {
    switch (status) {
      case HiveStatus.STRONG:
        return 'bg-status-healthy/20 text-status-healthy';
      case HiveStatus.ALERT:
        return 'bg-status-alert/20 text-status-alert';
      case HiveStatus.NEEDS_INSPECTION:
        return 'bg-status-warning/20 text-status-warning';
      case HiveStatus.WEAK:
        return 'bg-status-alert/20 text-status-alert';
    }
  };

  const getStatusText = (status: HiveStatus) => {
    switch (status) {
      case HiveStatus.STRONG:
        return 'Strong';
      case HiveStatus.ALERT:
        return 'Alert';
      case HiveStatus.NEEDS_INSPECTION:
        return 'Needs Inspection';
      case HiveStatus.WEAK:
        return 'Weak';
    }
  };

  return (
    <div className="min-h-screen bg-background-dark pb-20">
      {/* Top Bar */}
      <div className="bg-beekeeper-green-dark px-4 py-4 flex items-center justify-between">
        <button onClick={() => navigate(-1)} className="p-2">
          <ArrowLeft className="w-6 h-6 text-text-primary" />
        </button>
        <h1 className="text-xl font-bold text-text-primary">{apiary.name}</h1>
        <button className="p-2">
          <SettingsIcon className="w-6 h-6 text-text-primary" />
        </button>
      </div>

      {/* Content */}
      <div className="p-4 space-y-4">
        {/* Alert Banner */}
        {showAlert && alerts.length > 0 && (
          <div className="bg-status-warning/15 border border-status-warning/50 rounded-xl p-4">
            <div className="flex items-start gap-3">
              <AlertTriangle className="w-6 h-6 text-status-warning flex-shrink-0 mt-0.5" />
              <div className="flex-1">
                <h3 className="font-semibold text-text-primary mb-1">{alerts[0].title}</h3>
                <p className="text-sm text-text-secondary mb-3">{alerts[0].message}</p>
                <button
                  onClick={() => setShowAlert(false)}
                  className="w-full bg-beekeeper-gold/30 text-beekeeper-gold py-2 px-4 rounded-lg font-medium"
                >
                  Dismiss
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Hive Grid */}
        <div className="grid grid-cols-2 gap-3">
          {hives.map((hive) => (
            <Link
              key={hive.id}
              to={`/hive/${hive.id}`}
              className="bg-card-background rounded-xl overflow-hidden hover:bg-opacity-90 transition-all"
            >
              {/* Hive Image */}
              <div className="h-32 bg-beekeeper-green-medium flex items-center justify-center">
                <SettingsIcon className="w-12 h-12 text-text-secondary opacity-30" />
              </div>

              {/* Hive Info */}
              <div className="p-3">
                <h3 className="font-semibold text-text-primary mb-1">{hive.name}</h3>
                <div className={`inline-block px-2 py-1 rounded text-xs font-medium mb-2 ${getStatusColor(hive.status)}`}>
                  {getStatusText(hive.status)}
                </div>
                <p className="text-xs text-text-secondary">
                  Inspected: {formatInspectionDate(hive.lastInspected)}
                </p>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* FAB */}
      <button className="fixed bottom-24 right-6 w-14 h-14 bg-beekeeper-gold rounded-full flex items-center justify-center shadow-lg hover:bg-beekeeper-gold-dark transition-colors">
        <Plus className="w-6 h-6 text-black" />
      </button>

      {/* Bottom Navigation */}
      <div className="fixed bottom-0 left-0 right-0 bg-beekeeper-green-dark border-t border-beekeeper-green-light">
        <div className="flex items-center justify-around py-2">
          <button className="flex flex-col items-center gap-1 px-4 py-2">
            <Home className="w-6 h-6 text-beekeeper-gold" />
            <span className="text-xs text-beekeeper-gold">Dashboard</span>
          </button>
          <button className="flex flex-col items-center gap-1 px-4 py-2">
            <Calendar className="w-6 h-6 text-text-secondary" />
            <span className="text-xs text-text-secondary">Tasks</span>
          </button>
          <button
            onClick={() => navigate('/advisor')}
            className="flex flex-col items-center gap-1 px-4 py-2"
          >
            <Sparkles className="w-6 h-6 text-text-secondary" />
            <span className="text-xs text-text-secondary">Advisor</span>
          </button>
          <button className="flex flex-col items-center gap-1 px-4 py-2">
            <User className="w-6 h-6 text-text-secondary" />
            <span className="text-xs text-text-secondary">Profile</span>
          </button>
        </div>
      </div>
    </div>
  );
}
