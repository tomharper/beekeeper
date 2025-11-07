import { useState, useEffect } from 'react';
import { Menu, Bell, CheckCircle, Sparkles, Calendar, AlertTriangle, Info, Thermometer, Droplets, Wind, Sun, Cloud, CloudRain, CloudSnow, Loader } from 'lucide-react';
import { apiClient } from '../api/client';
import { Weather, Alert, AlertSeverity, WeatherCondition } from '../types';

export default function AIAdvisorPage() {
  const [weather, setWeather] = useState<Weather | null>(null);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        const [weatherData, alertsData] = await Promise.all([
          apiClient.getWeather(),
          apiClient.getActiveAlerts(),
        ]);

        setWeather(weatherData as Weather);
        setAlerts(alertsData as Alert[]);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const getWeatherIcon = (condition: WeatherCondition) => {
    switch (condition) {
      case WeatherCondition.SUNNY:
        return <Sun className="w-8 h-8 text-beekeeper-gold" />;
      case WeatherCondition.PARTLY_CLOUDY:
        return <Cloud className="w-8 h-8 text-text-secondary" />;
      case WeatherCondition.CLOUDY:
        return <Cloud className="w-8 h-8 text-text-secondary" />;
      case WeatherCondition.RAINY:
        return <CloudRain className="w-8 h-8 text-blue-400" />;
      case WeatherCondition.STORMY:
        return <CloudSnow className="w-8 h-8 text-status-alert" />;
      default:
        return <Sun className="w-8 h-8 text-beekeeper-gold" />;
    }
  };

  const getAlertColors = (severity: AlertSeverity) => {
    switch (severity) {
      case AlertSeverity.INFO:
        return { bg: 'bg-blue-500/15', iconBg: 'bg-blue-500/20', iconColor: 'text-blue-400' };
      case AlertSeverity.WARNING:
        return { bg: 'bg-status-warning/15', iconBg: 'bg-status-warning/20', iconColor: 'text-status-warning' };
      case AlertSeverity.CRITICAL:
        return { bg: 'bg-status-alert/15', iconBg: 'bg-status-alert/20', iconColor: 'text-status-alert' };
      default:
        return { bg: 'bg-beekeeper-gold/15', iconBg: 'bg-beekeeper-gold/20', iconColor: 'text-beekeeper-gold' };
    }
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
            <AlertTriangle className="w-6 h-6 text-status-alert" />
            <p className="text-text-primary font-semibold">{error}</p>
          </div>
          <button
            onClick={() => window.location.reload()}
            className="w-full bg-beekeeper-gold text-black px-4 py-2 rounded-lg font-medium"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

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
        {weather && (
          <div>
            <p className="text-sm text-text-secondary mb-3">Weather for: Sunny Meadow Apiary</p>

            <div className="bg-card-background rounded-xl p-4">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <h3 className="font-semibold text-text-primary mb-1">
                    {weather.condition === WeatherCondition.SUNNY ? 'Good conditions for hive work.' :
                     weather.condition === WeatherCondition.RAINY ? 'Not ideal for hive inspections.' :
                     weather.condition === WeatherCondition.STORMY ? 'Avoid hive work today.' :
                     'Moderate conditions for hive work.'}
                  </h3>
                  <p className="text-sm text-text-secondary">{weather.description}</p>
                </div>
                {getWeatherIcon(weather.condition)}
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
        )}

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
        {alerts.length > 0 && (
          <div>
            <h2 className="text-xl font-bold text-text-primary mb-3">Upcoming Alerts & Reminders</h2>

            <div className="space-y-3">
              {alerts.map((alert) => {
                const colors = getAlertColors(alert.severity);
                const Icon = alert.severity === AlertSeverity.CRITICAL ? AlertTriangle :
                            alert.severity === AlertSeverity.WARNING ? Calendar : Info;

                return (
                  <div key={alert.id} className={`${colors.bg} rounded-xl p-4`}>
                    <div className="flex gap-3 items-center">
                      <div className={`w-10 h-10 ${colors.iconBg} rounded-full flex items-center justify-center flex-shrink-0`}>
                        <Icon className={`w-6 h-6 ${colors.iconColor}`} />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-text-primary">{alert.title}</h3>
                        <p className="text-sm text-text-secondary">{alert.message}</p>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
