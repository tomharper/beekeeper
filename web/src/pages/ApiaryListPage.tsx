import { Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Menu, RefreshCw, Plus, CheckCircle, AlertTriangle, XCircle, ChevronRight, Settings, Loader, User } from 'lucide-react';
import { apiClient } from '../api/client';
import { Apiary, ApiaryStatus } from '../types';
import BottomNav from '../components/BottomNav';

export default function ApiaryListPage() {
  const [apiaries, setApiaries] = useState<Apiary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadApiaries = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiClient.getApiaries();
      setApiaries(data as Apiary[]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load apiaries');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadApiaries();
  }, []);

  const getStatusIcon = (status: ApiaryStatus) => {
    switch (status) {
      case ApiaryStatus.HEALTHY:
        return <CheckCircle className="w-5 h-5 text-status-healthy" />;
      case ApiaryStatus.WARNING:
        return <AlertTriangle className="w-5 h-5 text-status-warning" />;
      case ApiaryStatus.ALERT:
        return <XCircle className="w-5 h-5 text-status-alert" />;
    }
  };

  const getStatusBgColor = (status: ApiaryStatus) => {
    switch (status) {
      case ApiaryStatus.HEALTHY:
        return 'bg-status-healthy/20';
      case ApiaryStatus.WARNING:
        return 'bg-status-warning/20';
      case ApiaryStatus.ALERT:
        return 'bg-status-alert/20';
    }
  };

  return (
    <div className="min-h-screen bg-background-dark">
      {/* Top Bar */}
      <div className="bg-beekeeper-green-dark px-4 py-4 flex items-center justify-between">
        <button className="p-2">
          <Menu className="w-6 h-6 text-text-primary" />
        </button>
        <h1 className="text-xl font-bold text-text-primary">My Apiaries</h1>
        <div className="flex items-center gap-2">
          <button className="p-2" onClick={loadApiaries} disabled={loading}>
            <RefreshCw className={`w-6 h-6 text-text-primary ${loading ? 'animate-spin' : ''}`} />
          </button>
          <Link to="/profile" className="p-2">
            <User className="w-6 h-6 text-text-primary" />
          </Link>
        </div>
      </div>

      {/* Content */}
      <div className="p-4 space-y-4">
        <h2 className="text-sm text-text-secondary mb-2">Apiary Sites Overview</h2>

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center py-20">
            <Loader className="w-8 h-8 text-beekeeper-gold animate-spin" />
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="bg-status-alert/20 border border-status-alert/50 rounded-xl p-4">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-status-alert" />
              <p className="text-text-primary">{error}</p>
            </div>
            <button
              onClick={loadApiaries}
              className="mt-3 bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium"
            >
              Try Again
            </button>
          </div>
        )}

        {/* Apiary Cards */}
        {!loading && !error && (
          <div className="space-y-3">
            {apiaries.map((apiary) => (
              <Link
                key={apiary.id}
                to={`/apiary/${apiary.id}`}
                className="block bg-card-background rounded-xl p-4 hover:bg-opacity-90 transition-all"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-text-primary mb-1">
                      {apiary.name}
                    </h3>
                    <p className="text-text-secondary text-sm mb-2">{apiary.location}</p>
                    <div className="flex items-center text-text-secondary text-sm">
                      <Settings className="w-4 h-4 mr-1" />
                      <span>{apiary.hiveCount} Hives</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    {/* Status Indicator */}
                    <div className={`w-8 h-8 rounded-full ${getStatusBgColor(apiary.status)} flex items-center justify-center`}>
                      {getStatusIcon(apiary.status)}
                    </div>

                    {/* Arrow */}
                    <ChevronRight className="w-6 h-6 text-text-secondary" />
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* FAB */}
      <button className="fixed bottom-24 right-6 w-14 h-14 bg-beekeeper-gold rounded-full flex items-center justify-center shadow-lg hover:bg-beekeeper-gold-dark transition-colors">
        <Plus className="w-6 h-6 text-black" />
      </button>

      <BottomNav />
    </div>
  );
}
