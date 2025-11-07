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
  Upload,
  Image as ImageIcon,
  Sparkles,
} from 'lucide-react';
import { apiClient } from '../api/client';
import BottomNav from '../components/BottomNav';
import {
  Inspection,
  BroodPattern,
  ColonyTemperament,
  ColonyPopulation,
  InspectionHealthStatus,
  ResourceLevel,
  QueenCellStatus,
  Hive,
} from '../types';
import { formatDistanceToNow, format } from 'date-fns';

export default function InspectionsPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const hiveId = searchParams.get('hiveId');

  const [inspections, setInspections] = useState<Inspection[]>([]);
  const [hives, setHives] = useState<Hive[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedHiveId, setSelectedHiveId] = useState(hiveId || '');

  // Form state
  const [formData, setFormData] = useState({
    hiveId: hiveId || '',
    inspectionDate: new Date().toISOString().split('T')[0],
    durationMinutes: 30,
    queenSeen: false,
    queenMarked: false,
    queenCells: QueenCellStatus.NONE,
    broodPattern: BroodPattern.GOOD,
    population: ColonyPopulation.MEDIUM,
    temperament: ColonyTemperament.CALM,
    healthStatus: InspectionHealthStatus.HEALTHY,
    honeyStores: ResourceLevel.MEDIUM,
    pollenStores: ResourceLevel.MEDIUM,
    varroaMitesDetected: false,
    otherPestsDetected: false,
    feedingDone: false,
    treatmentApplied: false,
    weatherTemp: '',
    weatherConditions: '',
    notes: '',
    nextInspectionDate: '',
  });

  // Photo state
  const [uploadedPhotos, setUploadedPhotos] = useState<string[]>([]);
  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const [analyzingPhoto, setAnalyzingPhoto] = useState(false);
  const [aiAnalysis, setAiAnalysis] = useState<any>(null);

  useEffect(() => {
    loadInspections();
    loadHives();
  }, [hiveId]);

  const loadHives = async () => {
    try {
      const hivesData = await apiClient.getHives();
      setHives(hivesData as Hive[]);
    } catch (err) {
      console.error('Failed to load hives:', err);
    }
  };

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

  const handlePhotoUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploadingPhoto(true);
      const result: any = await apiClient.uploadPhoto(file);
      setUploadedPhotos([...uploadedPhotos, result.url]);
    } catch (err) {
      console.error('Failed to upload photo:', err);
      alert('Failed to upload photo. Please try again.');
    } finally {
      setUploadingPhoto(false);
    }
  };

  const handleAnalyzePhoto = async (photoUrl: string, analysisType: string = 'general') => {
    try {
      setAnalyzingPhoto(true);
      const result: any = await apiClient.analyzePhoto(photoUrl, analysisType);
      setAiAnalysis(result.analysis);
    } catch (err) {
      console.error('Failed to analyze photo:', err);
      alert('Failed to analyze photo. Please try again.');
    } finally {
      setAnalyzingPhoto(false);
    }
  };

  const handleRemovePhoto = async (photoUrl: string) => {
    try {
      await apiClient.deletePhoto(photoUrl);
      setUploadedPhotos(uploadedPhotos.filter((url) => url !== photoUrl));
    } catch (err) {
      console.error('Failed to delete photo:', err);
    }
  };

  const handleSubmitInspection = async () => {
    if (!formData.hiveId) {
      alert('Please select a hive');
      return;
    }

    try {
      const inspectionData = {
        ...formData,
        inspectionDate: new Date(formData.inspectionDate).toISOString(),
        nextInspectionDate: formData.nextInspectionDate
          ? new Date(formData.nextInspectionDate).toISOString()
          : undefined,
        weatherTemp: formData.weatherTemp ? parseFloat(formData.weatherTemp) : undefined,
        photos: uploadedPhotos,
        aiAnalysis: aiAnalysis,
      };

      await apiClient.createInspection(inspectionData);
      setShowCreateModal(false);
      resetForm();
      loadInspections();
    } catch (err) {
      console.error('Failed to create inspection:', err);
      alert('Failed to create inspection. Please try again.');
    }
  };

  const resetForm = () => {
    setFormData({
      hiveId: hiveId || '',
      inspectionDate: new Date().toISOString().split('T')[0],
      durationMinutes: 30,
      queenSeen: false,
      queenMarked: false,
      queenCells: QueenCellStatus.NONE,
      broodPattern: BroodPattern.GOOD,
      population: ColonyPopulation.MEDIUM,
      temperament: ColonyTemperament.CALM,
      healthStatus: InspectionHealthStatus.HEALTHY,
      honeyStores: ResourceLevel.MEDIUM,
      pollenStores: ResourceLevel.MEDIUM,
      varroaMitesDetected: false,
      otherPestsDetected: false,
      feedingDone: false,
      treatmentApplied: false,
      weatherTemp: '',
      weatherConditions: '',
      notes: '',
      nextInspectionDate: '',
    });
    setUploadedPhotos([]);
    setAiAnalysis(null);
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

      {/* Create Inspection Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-50 overflow-y-auto">
          <div className="bg-card-dark border border-card-border rounded-xl max-w-4xl w-full my-8">
            <div className="sticky top-0 bg-card-dark border-b border-card-border p-6 flex items-center justify-between">
              <h2 className="text-2xl font-bold text-text-primary">Create Inspection</h2>
              <button
                onClick={() => {
                  setShowCreateModal(false);
                  resetForm();
                }}
                className="text-text-secondary hover:text-text-primary"
              >
                <X className="w-6 h-6" />
              </button>
            </div>

            <div className="p-6 space-y-6">
              {/* Basic Info */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-text-primary mb-2">
                    Hive *
                  </label>
                  <select
                    value={formData.hiveId}
                    onChange={(e) => setFormData({ ...formData, hiveId: e.target.value })}
                    className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                  >
                    <option value="">Select a hive</option>
                    {hives.map((hive) => (
                      <option key={hive.id} value={hive.id}>
                        {hive.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-2">
                    Inspection Date *
                  </label>
                  <input
                    type="date"
                    value={formData.inspectionDate}
                    onChange={(e) => setFormData({ ...formData, inspectionDate: e.target.value })}
                    className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-2">
                    Duration (minutes)
                  </label>
                  <input
                    type="number"
                    value={formData.durationMinutes}
                    onChange={(e) =>
                      setFormData({ ...formData, durationMinutes: parseInt(e.target.value) || 0 })
                    }
                    className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-text-primary mb-2">
                    Next Inspection Date
                  </label>
                  <input
                    type="date"
                    value={formData.nextInspectionDate}
                    onChange={(e) =>
                      setFormData({ ...formData, nextInspectionDate: e.target.value })
                    }
                    className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                  />
                </div>
              </div>

              {/* Queen Observations */}
              <div className="border border-card-border rounded-lg p-4">
                <h3 className="text-lg font-semibold text-text-primary mb-4">Queen Observations</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.queenSeen}
                      onChange={(e) => setFormData({ ...formData, queenSeen: e.target.checked })}
                      className="w-5 h-5"
                    />
                    <span className="text-text-primary">Queen Seen</span>
                  </label>

                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.queenMarked}
                      onChange={(e) => setFormData({ ...formData, queenMarked: e.target.checked })}
                      className="w-5 h-5"
                    />
                    <span className="text-text-primary">Queen Marked</span>
                  </label>

                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Queen Cells
                    </label>
                    <select
                      value={formData.queenCells}
                      onChange={(e) =>
                        setFormData({ ...formData, queenCells: e.target.value as QueenCellStatus })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(QueenCellStatus).map((status) => (
                        <option key={status} value={status}>
                          {formatEnum(status)}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              {/* Colony Assessment */}
              <div className="border border-card-border rounded-lg p-4">
                <h3 className="text-lg font-semibold text-text-primary mb-4">Colony Assessment</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Brood Pattern
                    </label>
                    <select
                      value={formData.broodPattern}
                      onChange={(e) =>
                        setFormData({ ...formData, broodPattern: e.target.value as BroodPattern })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(BroodPattern).map((pattern) => (
                        <option key={pattern} value={pattern}>
                          {formatEnum(pattern)}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Population
                    </label>
                    <select
                      value={formData.population}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          population: e.target.value as ColonyPopulation,
                        })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(ColonyPopulation).map((pop) => (
                        <option key={pop} value={pop}>
                          {formatEnum(pop)}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Temperament
                    </label>
                    <select
                      value={formData.temperament}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          temperament: e.target.value as ColonyTemperament,
                        })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(ColonyTemperament).map((temp) => (
                        <option key={temp} value={temp}>
                          {formatEnum(temp)}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Health Status
                    </label>
                    <select
                      value={formData.healthStatus}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          healthStatus: e.target.value as InspectionHealthStatus,
                        })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(InspectionHealthStatus).map((status) => (
                        <option key={status} value={status}>
                          {formatEnum(status)}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              {/* Resources */}
              <div className="border border-card-border rounded-lg p-4">
                <h3 className="text-lg font-semibold text-text-primary mb-4">Resource Levels</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Honey Stores
                    </label>
                    <select
                      value={formData.honeyStores}
                      onChange={(e) =>
                        setFormData({ ...formData, honeyStores: e.target.value as ResourceLevel })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(ResourceLevel).map((level) => (
                        <option key={level} value={level}>
                          {formatEnum(level)}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Pollen Stores
                    </label>
                    <select
                      value={formData.pollenStores}
                      onChange={(e) =>
                        setFormData({ ...formData, pollenStores: e.target.value as ResourceLevel })
                      }
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    >
                      {Object.values(ResourceLevel).map((level) => (
                        <option key={level} value={level}>
                          {formatEnum(level)}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              {/* Pests & Actions */}
              <div className="border border-card-border rounded-lg p-4">
                <h3 className="text-lg font-semibold text-text-primary mb-4">Pests & Actions</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.varroaMitesDetected}
                      onChange={(e) =>
                        setFormData({ ...formData, varroaMitesDetected: e.target.checked })
                      }
                      className="w-5 h-5"
                    />
                    <span className="text-text-primary">Varroa Mites</span>
                  </label>

                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.otherPestsDetected}
                      onChange={(e) =>
                        setFormData({ ...formData, otherPestsDetected: e.target.checked })
                      }
                      className="w-5 h-5"
                    />
                    <span className="text-text-primary">Other Pests</span>
                  </label>

                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.feedingDone}
                      onChange={(e) =>
                        setFormData({ ...formData, feedingDone: e.target.checked })
                      }
                      className="w-5 h-5"
                    />
                    <span className="text-text-primary">Feeding Done</span>
                  </label>

                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.treatmentApplied}
                      onChange={(e) =>
                        setFormData({ ...formData, treatmentApplied: e.target.checked })
                      }
                      className="w-5 h-5"
                    />
                    <span className="text-text-primary">Treatment Applied</span>
                  </label>
                </div>
              </div>

              {/* Weather */}
              <div className="border border-card-border rounded-lg p-4">
                <h3 className="text-lg font-semibold text-text-primary mb-4">Weather Conditions</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Temperature (°F)
                    </label>
                    <input
                      type="number"
                      value={formData.weatherTemp}
                      onChange={(e) => setFormData({ ...formData, weatherTemp: e.target.value })}
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-text-primary mb-2">
                      Conditions
                    </label>
                    <input
                      type="text"
                      value={formData.weatherConditions}
                      onChange={(e) =>
                        setFormData({ ...formData, weatherConditions: e.target.value })
                      }
                      placeholder="e.g., Sunny, Cloudy, Rainy"
                      className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                    />
                  </div>
                </div>
              </div>

              {/* Photo Upload & AI Analysis */}
              <div className="border border-card-border rounded-lg p-4">
                <h3 className="text-lg font-semibold text-text-primary mb-4 flex items-center gap-2">
                  <ImageIcon className="w-5 h-5" />
                  Photos & AI Analysis
                </h3>

                {/* Upload Button */}
                <div className="mb-4">
                  <label className="cursor-pointer inline-flex items-center gap-2 bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium hover:bg-beekeeper-gold/90 transition">
                    <Upload className="w-5 h-5" />
                    {uploadingPhoto ? 'Uploading...' : 'Upload Photo'}
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handlePhotoUpload}
                      disabled={uploadingPhoto}
                      className="hidden"
                    />
                  </label>
                </div>

                {/* Uploaded Photos */}
                {uploadedPhotos.length > 0 && (
                  <div className="space-y-3">
                    {uploadedPhotos.map((photoUrl, index) => (
                      <div
                        key={index}
                        className="bg-background-dark rounded-lg p-3 flex items-center gap-3"
                      >
                        <img
                          src={photoUrl}
                          alt={`Inspection photo ${index + 1}`}
                          className="w-20 h-20 object-cover rounded"
                        />
                        <div className="flex-1">
                          <p className="text-sm text-text-secondary">Photo {index + 1}</p>
                          <div className="flex gap-2 mt-2">
                            <button
                              onClick={() => handleAnalyzePhoto(photoUrl, 'general')}
                              disabled={analyzingPhoto}
                              className="text-sm bg-beekeeper-gold/20 text-beekeeper-gold px-3 py-1 rounded hover:bg-beekeeper-gold/30 transition flex items-center gap-1"
                            >
                              <Sparkles className="w-4 h-4" />
                              {analyzingPhoto ? 'Analyzing...' : 'Analyze'}
                            </button>
                            <button
                              onClick={() => handleRemovePhoto(photoUrl)}
                              className="text-sm bg-status-alert/20 text-status-alert px-3 py-1 rounded hover:bg-status-alert/30 transition"
                            >
                              Remove
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* AI Analysis Results */}
                {aiAnalysis && (
                  <div className="mt-4 bg-beekeeper-gold/10 border border-beekeeper-gold/30 rounded-lg p-4">
                    <h4 className="font-semibold text-beekeeper-gold mb-3 flex items-center gap-2">
                      <Sparkles className="w-5 h-5" />
                      AI Analysis Results
                    </h4>
                    <div className="space-y-3">
                      {aiAnalysis.findings && aiAnalysis.findings.length > 0 && (
                        <div>
                          <p className="text-sm font-medium text-text-primary mb-1">Findings:</p>
                          <ul className="text-sm text-text-secondary space-y-1">
                            {aiAnalysis.findings.map((finding: string, idx: number) => (
                              <li key={idx}>• {finding}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                      {aiAnalysis.recommendations && aiAnalysis.recommendations.length > 0 && (
                        <div>
                          <p className="text-sm font-medium text-text-primary mb-1">
                            Recommendations:
                          </p>
                          <ul className="text-sm text-text-secondary space-y-1">
                            {aiAnalysis.recommendations.map((rec: string, idx: number) => (
                              <li key={idx}>• {rec}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                      {aiAnalysis.full_analysis && (
                        <details className="mt-2">
                          <summary className="text-sm font-medium text-text-primary cursor-pointer">
                            Full Analysis
                          </summary>
                          <p className="text-sm text-text-secondary mt-2 whitespace-pre-wrap">
                            {aiAnalysis.full_analysis}
                          </p>
                        </details>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* Notes */}
              <div>
                <label className="block text-sm font-medium text-text-primary mb-2">Notes</label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  placeholder="Additional observations or notes..."
                  rows={4}
                  className="w-full bg-background-dark border border-card-border rounded-lg px-4 py-2 text-text-primary"
                />
              </div>

              {/* Submit Buttons */}
              <div className="flex gap-3 pt-4">
                <button
                  onClick={handleSubmitInspection}
                  className="flex-1 bg-beekeeper-gold text-black px-6 py-3 rounded-lg font-medium hover:bg-beekeeper-gold/90 transition"
                >
                  Create Inspection
                </button>
                <button
                  onClick={() => {
                    setShowCreateModal(false);
                    resetForm();
                  }}
                  className="px-6 py-3 bg-card-border text-text-primary rounded-lg font-medium hover:bg-card-border/80 transition"
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
