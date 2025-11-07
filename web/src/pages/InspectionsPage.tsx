import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Plus,
  Calendar,
  Eye,
  Bug,
  AlertCircle,
  CheckCircle,
  Loader,
  X,
  Droplets,
  Thermometer,
} from 'lucide-react';
import { apiClient } from '../api/client';
import {
  Inspection,
  BroodPattern,
  ColonyTemperament,
  ColonyPopulation,
  InspectionHealthStatus,
  ResourceLevel,
  QueenCellStatus,
} from '../types';
import { formatDistanceToNow, format } from 'date-fns';

export default function InspectionsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const hiveId = searchParams.get('hiveId');

  const [inspections, setInspections] = useState<Inspection[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedHiveId, setSelectedHiveId] = useState(hiveId || '');

  useEffect(() => {
    loadInspections();
  }, [hiveId]);

  const loadInspections = async () => {
    try {
      setLoading(true);
      setError(null);

      let inspectionsData;
      if (hiveId) {
        inspectionsData = await apiClient.getInspections({ hiveId });
      } else {
        inspectionsData = await apiClient.getInspections();
      }

      const parsedInspections = (inspectionsData as any[]).map((inspection: any) => ({
        ...inspection,
        inspectionDate: new Date(inspection.inspectionDate),
        nextInspectionDate: inspection.nextInspectionDate
          ? new Date(inspection.nextInspectionDate)
          : undefined,
        createdAt: new Date(inspection.createdAt),
        updatedAt: new Date(inspection.updatedAt),
      }));

      setInspections(parsedInspections);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load inspections');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteInspection = async (inspectionId: string) => {
    if (!confirm('Are you sure you want to delete this inspection?')) return;

    try {
      await apiClient.deleteInspection(inspectionId);
      loadInspections();
    } catch (err) {
      console.error('Failed to delete inspection:', err);
    }
  };

  const getHealthStatusColor = (status: InspectionHealthStatus) => {
    switch (status) {
      case InspectionHealthStatus.EXCELLENT:
      case InspectionHealthStatus.HEALTHY:
        return 'text-status-healthy';
      case InspectionHealthStatus.CONCERNING:
        return 'text-status-warning';
      case InspectionHealthStatus.NEEDS_ATTENTION:
      case InspectionHealthStatus.CRITICAL:
        return 'text-status-alert';
      default:
        return 'text-text-secondary';
    }
  };

  const getPopulationColor = (pop: ColonyPopulation) => {
    if (pop === ColonyPopulation.STRONG || pop === ColonyPopulation.VERY_STRONG) {
      return 'text-status-healthy';
    }
    if (pop === ColonyPopulation.MEDIUM) {
      return 'text-beekeeper-gold';
    }
    return 'text-status-alert';
  };

  const formatEnum = (value: string) => {
    return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (l) => l.toUpperCase());
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center">
        <Loader className="w-8 h-8 text-beekeeper-gold animate-spin" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-background-dark flex items-center justify-center p-4">
        <div className="bg-status-alert/20 border border-status-alert/50 rounded-xl p-6 max-w-md">
          <div className="flex items-center gap-2 mb-4">
            <AlertCircle className="w-6 h-6 text-status-alert" />
            <p className="text-text-primary font-semibold">{error}</p>
          </div>
          <button
            onClick={loadInspections}
            className="w-full bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background-dark pb-20">
      {/* Header */}
      <div className="bg-card-dark border-b border-card-border p-4">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-text-primary">Hive Inspections</h1>
            {hiveId && (
              <p className="text-sm text-text-secondary mt-1">
                Filtered by hive
              </p>
            )}
          </div>
          <button
            onClick={() => setShowCreateModal(true)}
            className="bg-beekeeper-gold text-black p-2 rounded-full hover:bg-beekeeper-gold/90 transition"
          >
            <Plus className="w-6 h-6" />
          </button>
        </div>
      </div>

      {/* Inspections List */}
      <div className="max-w-6xl mx-auto p-4 space-y-4">
        {inspections.length === 0 ? (
          <div className="bg-card-dark border border-card-border rounded-xl p-8 text-center">
            <Eye className="w-12 h-12 text-text-secondary mx-auto mb-4" />
            <p className="text-text-secondary">No inspections found</p>
            <button
              onClick={() => setShowCreateModal(true)}
              className="mt-4 bg-beekeeper-gold text-black px-6 py-2 rounded-lg font-medium"
            >
              Create Your First Inspection
            </button>
          </div>
        ) : (
          inspections.map((inspection) => (
            <div
              key={inspection.id}
              className="bg-card-dark border border-card-border rounded-xl overflow-hidden hover:shadow-lg transition"
            >
              <div className="p-4">
                {/* Header */}
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <div className="flex items-center gap-2">
                      <Calendar className="w-5 h-5 text-beekeeper-gold" />
                      <h3 className="text-text-primary font-semibold">
                        {format(inspection.inspectionDate, 'MMM dd, yyyy')}
                      </h3>
                      <span className="text-text-secondary text-sm">
                        ({formatDistanceToNow(inspection.inspectionDate, { addSuffix: true })})
                      </span>
                    </div>
                    {inspection.durationMinutes && (
                      <p className="text-sm text-text-secondary mt-1">
                        Duration: {inspection.durationMinutes} minutes
                      </p>
                    )}
                  </div>
                  <button
                    onClick={() => handleDeleteInspection(inspection.id)}
                    className="p-2 text-status-alert hover:bg-status-alert/20 rounded-lg transition"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>

                {/* Key Observations Grid */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-3">
                  <div className="bg-background-dark rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <CheckCircle className={`w-4 h-4 ${getHealthStatusColor(inspection.healthStatus)}`} />
                      <span className="text-xs text-text-secondary">Health</span>
                    </div>
                    <p className={`text-sm font-medium ${getHealthStatusColor(inspection.healthStatus)}`}>
                      {formatEnum(inspection.healthStatus)}
                    </p>
                  </div>

                  <div className="bg-background-dark rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <Eye className={`w-4 h-4 ${getPopulationColor(inspection.population)}`} />
                      <span className="text-xs text-text-secondary">Population</span>
                    </div>
                    <p className={`text-sm font-medium ${getPopulationColor(inspection.population)}`}>
                      {formatEnum(inspection.population)}
                    </p>
                  </div>

                  <div className="bg-background-dark rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-lg">👑</span>
                      <span className="text-xs text-text-secondary">Queen</span>
                    </div>
                    <p className={`text-sm font-medium ${inspection.queenSeen ? 'text-status-healthy' : 'text-status-alert'}`}>
                      {inspection.queenSeen ? 'Seen' : 'Not Seen'}
                    </p>
                  </div>

                  <div className="bg-background-dark rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <Droplets className="w-4 h-4 text-beekeeper-gold" />
                      <span className="text-xs text-text-secondary">Honey</span>
                    </div>
                    <p className="text-sm font-medium text-text-primary">
                      {formatEnum(inspection.honeyStores)}
                    </p>
                  </div>
                </div>

                {/* Brood & Temperament */}
                <div className="flex flex-wrap gap-2 mb-3">
                  <span className="px-2 py-1 bg-beekeeper-gold/20 text-beekeeper-gold text-xs rounded">
                    Brood: {formatEnum(inspection.broodPattern)}
                  </span>
                  <span className="px-2 py-1 bg-blue-500/20 text-blue-400 text-xs rounded">
                    {formatEnum(inspection.temperament)}
                  </span>
                  {inspection.varroaMitesDetected && (
                    <span className="px-2 py-1 bg-status-alert/20 text-status-alert text-xs rounded flex items-center gap-1">
                      <Bug className="w-3 h-3" />
                      Varroa Detected
                    </span>
                  )}
                  {inspection.feedingDone && (
                    <span className="px-2 py-1 bg-status-healthy/20 text-status-healthy text-xs rounded">
                      Fed
                    </span>
                  )}
                  {inspection.treatmentApplied && (
                    <span className="px-2 py-1 bg-status-warning/20 text-status-warning text-xs rounded">
                      Treatment Applied
                    </span>
                  )}
                </div>

                {/* Weather */}
                {inspection.weatherTemp && (
                  <div className="flex items-center gap-2 text-sm text-text-secondary mb-2">
                    <Thermometer className="w-4 h-4" />
                    <span>{inspection.weatherTemp}°F</span>
                    {inspection.weatherConditions && (
                      <span className="ml-2">{inspection.weatherConditions}</span>
                    )}
                  </div>
                )}

                {/* Notes */}
                {inspection.notes && (
                  <div className="mt-3 p-3 bg-background-dark rounded-lg">
                    <p className="text-sm text-text-secondary">{inspection.notes}</p>
                  </div>
                )}

                {/* Next Inspection */}
                {inspection.nextInspectionDate && (
                  <div className="mt-3 flex items-center gap-2 text-sm text-beekeeper-gold">
                    <Calendar className="w-4 h-4" />
                    <span>
                      Next: {format(inspection.nextInspectionDate, 'MMM dd, yyyy')}
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Create Modal Placeholder */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-50">
          <div className="bg-card-dark border border-card-border rounded-xl max-w-2xl w-full p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-text-primary">Create Inspection</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-text-secondary hover:text-text-primary"
              >
                <X className="w-6 h-6" />
              </button>
            </div>
            <div className="text-center py-8">
              <p className="text-text-secondary mb-4">
                Inspection form coming soon! This will include:
              </p>
              <ul className="text-left max-w-md mx-auto text-text-secondary space-y-2">
                <li>• Queen observations</li>
                <li>• Brood pattern assessment</li>
                <li>• Colony temperament & population</li>
                <li>• Health & pest detection</li>
                <li>• Resource levels (honey, pollen)</li>
                <li>• Actions taken & notes</li>
                <li>• Photo upload for AI analysis</li>
              </ul>
              <button
                onClick={() => setShowCreateModal(false)}
                className="mt-6 bg-beekeeper-gold text-black px-6 py-2 rounded-lg font-medium"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Bottom Navigation */}
      <div className="fixed bottom-0 left-0 right-0 bg-card-dark border-t border-card-border p-4">
        <div className="max-w-6xl mx-auto flex justify-around">
          <button
            onClick={() => navigate('/')}
            className="flex flex-col items-center gap-1 text-text-secondary hover:text-text-primary transition"
          >
            <Calendar className="w-6 h-6" />
            <span className="text-xs">Apiaries</span>
          </button>
          <button
            onClick={() => navigate('/tasks')}
            className="flex flex-col items-center gap-1 text-text-secondary hover:text-text-primary transition"
          >
            <CheckCircle className="w-6 h-6" />
            <span className="text-xs">Tasks</span>
          </button>
          <button className="flex flex-col items-center gap-1 text-beekeeper-gold">
            <Eye className="w-6 h-6" />
            <span className="text-xs">Inspections</span>
          </button>
          <button
            onClick={() => navigate('/advisor')}
            className="flex flex-col items-center gap-1 text-text-secondary hover:text-text-primary transition"
          >
            <AlertCircle className="w-6 h-6" />
            <span className="text-xs">Advisor</span>
          </button>
        </div>
      </div>
    </div>
  );
}
