import { Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { Menu, RefreshCw, Plus, CheckCircle, AlertTriangle, XCircle, ChevronRight, Settings, Loader, User, MapPin, X } from 'lucide-react';
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

  // --- Create apiary (location captured from the device, "set it at the spot") ---
  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState<{
    name: string;
    location: string;
    latitude: number | null;
    longitude: number | null;
  }>({ name: '', location: '', latitude: null, longitude: null });
  const [gettingLocation, setGettingLocation] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const resetForm = () => {
    setForm({ name: '', location: '', latitude: null, longitude: null });
    setFormError(null);
  };

  const handleUseLocation = () => {
    if (!('geolocation' in navigator)) {
      setFormError('Geolocation is not available in this browser.');
      return;
    }
    setGettingLocation(true);
    setFormError(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        setForm((f) => ({
          ...f,
          latitude,
          longitude,
          // If no label was typed, use the coords so the required field is filled.
          location: f.location.trim() || `${latitude.toFixed(5)}, ${longitude.toFixed(5)}`,
        }));
        setGettingLocation(false);
      },
      (err) => {
        setFormError(err.message || 'Could not get your location.');
        setGettingLocation(false);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  const handleCreate = async () => {
    if (!form.name.trim() || !form.location.trim()) {
      setFormError('Name and location are required.');
      return;
    }
    try {
      setCreating(true);
      await apiClient.createApiary({
        name: form.name.trim(),
        location: form.location.trim(),
        latitude: form.latitude ?? undefined,
        longitude: form.longitude ?? undefined,
      });
      setShowCreate(false);
      resetForm();
      loadApiaries();
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to create apiary.');
    } finally {
      setCreating(false);
    }
  };

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
      <button
        onClick={() => { resetForm(); setShowCreate(true); }}
        className="fixed bottom-24 right-6 w-14 h-14 bg-beekeeper-gold rounded-full flex items-center justify-center shadow-lg hover:bg-beekeeper-gold-dark transition-colors"
      >
        <Plus className="w-6 h-6 text-black" />
      </button>

      {/* Create Apiary Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-50">
          <div className="bg-card-dark border border-card-border rounded-xl max-w-md w-full">
            <div className="flex items-center justify-between p-5 border-b border-card-border">
              <h2 className="text-xl font-bold text-text-primary">New Apiary</h2>
              <button
                onClick={() => { setShowCreate(false); resetForm(); }}
                className="text-text-secondary hover:text-text-primary"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="p-5 space-y-4">
              <div>
                <label className="block text-sm font-medium text-text-primary mb-2">Name *</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="e.g. Home apiary"
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-text-primary mb-2">Location *</label>
                <input
                  type="text"
                  value={form.location}
                  onChange={(e) => setForm({ ...form, location: e.target.value })}
                  placeholder="e.g. Back field — or use current location"
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                />
              </div>

              {/* Set coordinates from the device — "set it at the spot" */}
              <button
                onClick={handleUseLocation}
                disabled={gettingLocation}
                className="w-full flex items-center justify-center gap-2 bg-beekeeper-green-light text-text-primary px-4 py-2 rounded-lg font-medium hover:bg-beekeeper-green-light/80 transition"
              >
                <MapPin className="w-5 h-5 text-beekeeper-gold" />
                {gettingLocation ? 'Getting location…' : 'Use current location'}
              </button>
              {form.latitude != null && form.longitude != null && (
                <p className="text-sm text-status-healthy flex items-center gap-1">
                  <MapPin className="w-4 h-4" />
                  Captured: {form.latitude.toFixed(5)}, {form.longitude.toFixed(5)}
                </p>
              )}
              {formError && <p className="text-sm text-status-alert">{formError}</p>}

              <div className="flex gap-3 pt-2">
                <button
                  onClick={handleCreate}
                  disabled={creating}
                  className="flex-1 bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium hover:bg-beekeeper-gold/90 transition"
                >
                  {creating ? 'Creating…' : 'Create Apiary'}
                </button>
                <button
                  onClick={() => { setShowCreate(false); resetForm(); }}
                  className="px-4 py-2 bg-card-border text-text-primary rounded-lg font-medium"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <BottomNav />
    </div>
  );
}
