import { Menu, Bell, CheckCircle, Sparkles, Calendar, AlertTriangle, Info, Thermometer, Droplets, Wind, Sun } from 'lucide-react';
import { weather, getActiveAlerts } from '../data/mockData';

export default function AIAdvisorPage() {
  const alerts = getActiveAlerts();

  return (
    <div className="min-h-screen bg-background-dark pb-6">
      {/* Top Bar */}
      <div className="bg-beekeeper-green-dark px-4 py-4 flex items-center justify-between">
        <button className="p-2">
          <Menu className="w-6 h-6 text-text-primary" />
        </button>
        <h1 className="text-xl font-bold text-text-primary">AI Expert Advisor</h1>
        <button className="p-2">
          <Bell className="w-6 h-6 text-text-primary" />
        </button>
      </div>

      {/* Content */}
      <div className="p-4 space-y-6">
        {/* Weather */}
        <div>
          <p className="text-sm text-text-secondary mb-3">Weather for: Sunny Meadow Apiary</p>

          <div className="bg-card-background rounded-xl p-4">
            <div className="flex items-center justify-between mb-3">
              <div>
                <h3 className="font-semibold text-text-primary mb-1">Good conditions for hive work.</h3>
                <p className="text-sm text-text-secondary">{weather.description}</p>
              </div>
              <Sun className="w-8 h-8 text-beekeeper-gold" />
            </div>

            {/* Weather Metrics */}
            <div className="flex items-center justify-around pt-3 border-t border-beekeeper-green-light">
              <div className="text-center">
                <Thermometer className="w-5 h-5 text-text-secondary mx-auto mb-1" />
                <p className="font-bold text-text-primary">{weather.temperature}°F</p>
              </div>
              <div className="text-center">
                <Droplets className="w-5 h-5 text-text-secondary mx-auto mb-1" />
                <p className="font-bold text-text-primary">{weather.humidity}%</p>
              </div>
              <div className="text-center">
                <Wind className="w-5 h-5 text-text-secondary mx-auto mb-1" />
                <p className="font-bold text-text-primary">{weather.windSpeed} mph</p>
              </div>
            </div>
          </div>
        </div>

        {/* Today's AI Advice */}
        <div>
          <h2 className="text-xl font-bold text-text-primary mb-3">Today's AI Advice</h2>

          <div className="space-y-3">
            {/* Positive Advice */}
            <div className="bg-status-healthy/15 border border-status-healthy/50 rounded-xl p-4">
              <div className="flex gap-3">
                <div className="w-10 h-10 bg-status-healthy/20 rounded-full flex items-center justify-center flex-shrink-0">
                  <CheckCircle className="w-6 h-6 text-status-healthy" />
                </div>
                <div>
                  <h3 className="font-semibold text-text-primary mb-1">
                    Ideal day for a full hive inspection.
                  </h3>
                  <p className="text-sm text-text-secondary">
                    Weather conditions are perfect. Check for queen health, brood pattern, and food stores.
                  </p>
                </div>
              </div>
            </div>

            {/* Info Advice */}
            <div className="bg-beekeeper-gold/15 border border-beekeeper-gold/50 rounded-xl p-4">
              <div className="flex gap-3">
                <div className="w-10 h-10 bg-beekeeper-gold/20 rounded-full flex items-center justify-center flex-shrink-0">
                  <Sparkles className="w-6 h-6 text-beekeeper-gold" />
                </div>
                <div>
                  <h3 className="font-semibold text-text-primary mb-1">
                    High nectar flow expected.
                  </h3>
                  <p className="text-sm text-text-secondary">
                    Check for super space to avoid swarming. Add a new super to hives 01 and 03.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Upcoming Alerts & Reminders */}
        <div>
          <h2 className="text-xl font-bold text-text-primary mb-3">Upcoming Alerts & Reminders</h2>

          <div className="space-y-3">
            {/* Swarm Season */}
            <div className="bg-beekeeper-gold/15 rounded-xl p-4">
              <div className="flex gap-3 items-center">
                <div className="w-10 h-10 bg-beekeeper-gold/20 rounded-full flex items-center justify-center flex-shrink-0">
                  <Calendar className="w-6 h-6 text-beekeeper-gold" />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-text-primary">Swarm Season Begins</h3>
                  <p className="text-sm text-text-secondary">In 2 weeks</p>
                </div>
              </div>
            </div>

            {/* Mite Treatment */}
            <div className="bg-status-warning/15 rounded-xl p-4">
              <div className="flex gap-3 items-center">
                <div className="w-10 h-10 bg-status-warning/20 rounded-full flex items-center justify-center flex-shrink-0">
                  <AlertTriangle className="w-6 h-6 text-status-warning" />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-text-primary">Mite treatment due</h3>
                  <p className="text-sm text-text-secondary">For Hive 02</p>
                </div>
              </div>
            </div>

            {/* Humidity Warning */}
            <div className="bg-blue-500/15 rounded-xl p-4">
              <div className="flex gap-3 items-center">
                <div className="w-10 h-10 bg-blue-500/20 rounded-full flex items-center justify-center flex-shrink-0">
                  <Info className="w-6 h-6 text-blue-400" />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold text-text-primary">High humidity overnight</h3>
                  <p className="text-sm text-text-secondary">Ensure hive ventilation</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
