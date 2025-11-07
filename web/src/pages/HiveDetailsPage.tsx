import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MoreVertical, Plus, CheckCircle, Heart, Smile, Star, AlertTriangle, MapPin, FileText } from 'lucide-react';
import { getHiveById, getApiaryById, getRecommendationsForHive } from '../data/mockData';
import { ColonyStrength, QueenStatus, Temperament, HoneyStores, RecommendationType } from '../types';
import { useState } from 'react';

export default function HiveDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const hive = id ? getHiveById(id) : undefined;
  const apiary = hive ? getApiaryById(hive.apiaryId) : undefined;
  const recommendations = id ? getRecommendationsForHive(id) : [];
  const [selectedTab, setSelectedTab] = useState(0);

  if (!hive) {
    return <div className="min-h-screen bg-background-dark flex items-center justify-center text-text-primary">Hive not found</div>;
  }

  const getStatusColor = (value: ColonyStrength | QueenStatus | Temperament | HoneyStores) => {
    if (value === ColonyStrength.STRONG || value === QueenStatus.LAYING || value === Temperament.CALM || value === HoneyStores.FULL || value === HoneyStores.ADEQUATE) {
      return 'bg-status-healthy';
    }
    if (value === ColonyStrength.MODERATE || value === QueenStatus.NOT_LAYING || value === Temperament.MODERATE || value === HoneyStores.LOW) {
      return 'bg-status-warning';
    }
    return 'bg-status-alert';
  };

  const getRecommendationColors = (type: RecommendationType) => {
    switch (type) {
      case RecommendationType.POSITIVE:
        return { bg: 'bg-status-healthy/15', border: 'border-status-healthy/50', icon: 'text-status-healthy' };
      case RecommendationType.WARNING:
        return { bg: 'bg-status-warning/15', border: 'border-status-warning/50', icon: 'text-status-warning' };
      case RecommendationType.ACTION_REQUIRED:
        return { bg: 'bg-status-alert/15', border: 'border-status-alert/50', icon: 'text-status-alert' };
      default:
        return { bg: 'bg-beekeeper-gold/15', border: 'border-beekeeper-gold/50', icon: 'text-beekeeper-gold' };
    }
  };

  const formatValue = (value: string) => {
    return value.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  return (
    <div className="min-h-screen bg-background-dark pb-6">
      {/* Top Bar */}
      <div className="bg-beekeeper-green-dark px-4 py-4 flex items-center justify-between">
        <button onClick={() => navigate(-1)} className="p-2">
          <ArrowLeft className="w-6 h-6 text-text-primary" />
        </button>
        <div className="flex-1 text-center">
          <h1 className="text-xl font-bold text-text-primary">{hive.name}</h1>
          <p className="text-sm text-text-secondary">{apiary?.name}</p>
        </div>
        <button className="p-2">
          <MoreVertical className="w-6 h-6 text-text-primary" />
        </button>
      </div>

      {/* Tabs */}
      <div className="bg-beekeeper-green-dark flex">
        <button
          onClick={() => setSelectedTab(0)}
          className={`flex-1 py-3 text-sm font-medium ${
            selectedTab === 0 ? 'text-beekeeper-gold border-b-2 border-beekeeper-gold' : 'text-text-secondary'
          }`}
        >
          Overview
        </button>
        <button
          onClick={() => setSelectedTab(1)}
          className={`flex-1 py-3 text-sm font-medium ${
            selectedTab === 1 ? 'text-beekeeper-gold border-b-2 border-beekeeper-gold' : 'text-text-secondary'
          }`}
        >
          Logbook
        </button>
        <button
          onClick={() => setSelectedTab(2)}
          className={`flex-1 py-3 text-sm font-medium ${
            selectedTab === 2 ? 'text-beekeeper-gold border-b-2 border-beekeeper-gold' : 'text-text-secondary'
          }`}
        >
          Photos
        </button>
      </div>

      {/* Content */}
      {selectedTab === 0 && (
        <div className="p-4 space-y-4">
          {/* Current Status */}
          <h2 className="text-xl font-bold text-text-primary">Current Status</h2>

          <div className="grid grid-cols-2 gap-3">
            {/* Colony Strength */}
            <div className="bg-card-background rounded-xl p-3">
              <div className="flex items-center gap-1 mb-2">
                <CheckCircle className="w-4 h-4 text-text-secondary" />
                <span className="text-xs text-text-secondary">Colony Strength</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-text-primary">{formatValue(hive.colonyStrength)}</span>
                <div className={`w-2 h-2 rounded ${getStatusColor(hive.colonyStrength)}`} />
              </div>
            </div>

            {/* Queen Status */}
            <div className="bg-card-background rounded-xl p-3">
              <div className="flex items-center gap-1 mb-2">
                <Heart className="w-4 h-4 text-text-secondary" />
                <span className="text-xs text-text-secondary">Queen Status</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-text-primary">{formatValue(hive.queenStatus)}</span>
                <div className={`w-2 h-2 rounded ${getStatusColor(hive.queenStatus)}`} />
              </div>
            </div>

            {/* Temperament */}
            <div className="bg-card-background rounded-xl p-3">
              <div className="flex items-center gap-1 mb-2">
                <Smile className="w-4 h-4 text-text-secondary" />
                <span className="text-xs text-text-secondary">Temperament</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-text-primary">{formatValue(hive.temperament)}</span>
                <div className={`w-2 h-2 rounded ${getStatusColor(hive.temperament)}`} />
              </div>
            </div>

            {/* Honey Stores */}
            <div className="bg-card-background rounded-xl p-3">
              <div className="flex items-center gap-1 mb-2">
                <Star className="w-4 h-4 text-text-secondary" />
                <span className="text-xs text-text-secondary">Honey Stores</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-text-primary">{formatValue(hive.honeyStores)}</span>
                <div className={`w-2 h-2 rounded ${getStatusColor(hive.honeyStores)}`} />
              </div>
            </div>
          </div>

          {/* AI Recommendations */}
          {recommendations.length > 0 && (
            <>
              <h2 className="text-xl font-bold text-text-primary mt-6">AI Recommendations</h2>
              <div className="space-y-3">
                {recommendations.map((rec) => {
                  const colors = getRecommendationColors(rec.type);
                  return (
                    <div key={rec.id} className={`${colors.bg} border ${colors.border} rounded-xl p-4`}>
                      <div className="flex gap-3">
                        {rec.type === RecommendationType.POSITIVE ? (
                          <CheckCircle className={`w-6 h-6 ${colors.icon} flex-shrink-0`} />
                        ) : (
                          <AlertTriangle className={`w-6 h-6 ${colors.icon} flex-shrink-0`} />
                        )}
                        <div>
                          <h3 className="font-semibold text-text-primary mb-1">{rec.title}</h3>
                          <p className="text-sm text-text-secondary">{rec.description}</p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </>
          )}
        </div>
      )}

      {selectedTab === 1 && (
        <div className="flex-1 flex items-center justify-center py-20">
          <div className="text-center">
            <FileText className="w-12 h-12 text-text-secondary mx-auto mb-2" />
            <p className="text-text-secondary">No logbook entries yet</p>
          </div>
        </div>
      )}

      {selectedTab === 2 && (
        <div className="flex-1 flex items-center justify-center py-20">
          <div className="text-center">
            <MapPin className="w-12 h-12 text-text-secondary mx-auto mb-2" />
            <p className="text-text-secondary">No photos yet</p>
          </div>
        </div>
      )}

      {/* FAB */}
      <button className="fixed bottom-6 right-6 w-14 h-14 bg-beekeeper-gold rounded-full flex items-center justify-center shadow-lg hover:bg-beekeeper-gold-dark transition-colors">
        <Plus className="w-6 h-6 text-black" />
      </button>
    </div>
  );
}
