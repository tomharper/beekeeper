import { useParams, Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { ArrowLeft, Settings as SettingsIcon, Plus, AlertTriangle, Loader } from 'lucide-react';
import { apiClient } from '../api/client';
import BottomNav from '../components/BottomNav';
import { Apiary, Hive, Alert, HiveStatus } from '../types';
import { formatInspectionDate } from '../utils/dateUtils';

export default function ApiaryDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [apiary, setApiary] = useState<Apiary | null>(null);
  const [hives, setHives] = useState<Hive[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAlert, setShowAlert] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      if (!id) return;

      try {
        setLoading(true);
        setError(null);

        const [apiaryData, hivesData, alertsData] = await Promise.all([
          apiClient.getApiary(id),
          apiClient.getHives(id),
          apiClient.getActiveAlerts(),
        ]);

        setApiary(apiaryData as Apiary);
        setHives(hivesData as Hive[]);
        setAlerts(alertsData as Alert[]);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [id]);

  const handleDismissAlert = async (alertId: string) => {
    try {
      await apiClient.dismissAlert(alertId);
      setShowAlert(false);
    } catch (err) {
      console.error('Failed to dismiss alert:', err);
    }
  };

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

  if (loading) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center">
        <Loader className="w-8 h-8 text-beekeeper-gold animate-spin" />
      </div>
    );
  }

  if (error || !apiary) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center p-4">
        <div className="bg-status-alert/20 border border-status-alert/50 rounded-xl p-6 max-w-md">
          <div className="flex items-center gap-2 mb-4">
            <AlertTriangle className="w-6 h-6 text-status-alert" />
            <p className="text-text-primary font-semibold">
              {error || 'Apiary not found'}
            </p>
          </div>
          <button
            onClick={() => navigate('/')}
            className="w-full bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium"
          >
            Back to Apiaries
          </button>
        </div>
      </div>
    );
  }

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
                  onClick={() => handleDismissAlert(alerts[0].id)}
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
                  Inspected: {formatInspectionDate(new Date(hive.lastInspected))}
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

      <BottomNav />
    </div>
  );
}
