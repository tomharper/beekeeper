import {
  Apiary,
  ApiaryStatus,
  Hive,
  HiveStatus,
  ColonyStrength,
  QueenStatus,
  Temperament,
  HoneyStores,
  Alert,
  AlertType,
  AlertSeverity,
  Recommendation,
  RecommendationType,
  Priority,
  Weather,
  WeatherCondition,
} from '../types';

export const apiaries: Apiary[] = [
  {
    id: '1',
    name: 'Backyard Garden',
    location: 'Sunnyvale, CA',
    latitude: 37.3688,
    longitude: -122.0363,
    hiveCount: 5,
    status: ApiaryStatus.HEALTHY,
  },
  {
    id: '2',
    name: 'Hillside Meadow',
    location: '45.123, -122.456',
    latitude: 45.123,
    longitude: -122.456,
    hiveCount: 8,
    status: ApiaryStatus.WARNING,
  },
  {
    id: '3',
    name: 'Riverbend Apiary',
    location: 'Cloverdale, OR',
    latitude: 45.2271,
    longitude: -123.4023,
    hiveCount: 3,
    status: ApiaryStatus.ALERT,
  },
];

export const hives: Hive[] = [
  // Backyard Garden hives
  {
    id: 'h1',
    name: 'Hive A-01',
    apiaryId: '1',
    status: HiveStatus.STRONG,
    lastInspected: new Date(2024, 10, 3, 14, 30),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.STRONG,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.CALM,
    honeyStores: HoneyStores.FULL,
  },
  {
    id: 'h2',
    name: 'Hive A-02',
    apiaryId: '1',
    status: HiveStatus.NEEDS_INSPECTION,
    lastInspected: new Date(2024, 9, 23, 10, 15),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.MODERATE,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.MODERATE,
    honeyStores: HoneyStores.ADEQUATE,
  },
  {
    id: 'h3',
    name: 'Hive B-01',
    apiaryId: '1',
    status: HiveStatus.STRONG,
    lastInspected: new Date(2024, 10, 1, 9, 0),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.STRONG,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.CALM,
    honeyStores: HoneyStores.FULL,
  },
  {
    id: 'h4',
    name: 'Hive B-02',
    apiaryId: '1',
    status: HiveStatus.ALERT,
    lastInspected: new Date(2024, 10, 4, 15, 45),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.WEAK,
    queenStatus: QueenStatus.NOT_LAYING,
    temperament: Temperament.DEFENSIVE,
    honeyStores: HoneyStores.LOW,
  },
  // Hillside Meadow hives
  {
    id: 'h5',
    name: 'Hive 01',
    apiaryId: '2',
    status: HiveStatus.STRONG,
    lastInspected: new Date(2024, 10, 5, 11, 0),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.STRONG,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.CALM,
    honeyStores: HoneyStores.FULL,
  },
  {
    id: 'h6',
    name: 'Hive 02',
    apiaryId: '2',
    status: HiveStatus.STRONG,
    lastInspected: new Date(2024, 10, 4, 13, 30),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.STRONG,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.CALM,
    honeyStores: HoneyStores.ADEQUATE,
  },
  {
    id: 'h7',
    name: 'Hive 03',
    apiaryId: '2',
    status: HiveStatus.NEEDS_INSPECTION,
    lastInspected: new Date(2024, 9, 28, 16, 0),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.MODERATE,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.MODERATE,
    honeyStores: HoneyStores.ADEQUATE,
  },
  {
    id: 'h8',
    name: 'Hive 04',
    apiaryId: '2',
    status: HiveStatus.ALERT,
    lastInspected: new Date(2024, 10, 2, 14, 0),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.MODERATE,
    queenStatus: QueenStatus.LAYING,
    temperament: Temperament.CALM,
    honeyStores: HoneyStores.ADEQUATE,
  },
  // Riverbend hives
  {
    id: 'h9',
    name: 'Hive 01',
    apiaryId: '3',
    status: HiveStatus.WEAK,
    lastInspected: new Date(2024, 9, 30, 10, 0),
    imageUrl: undefined,
    colonyStrength: ColonyStrength.WEAK,
    queenStatus: QueenStatus.MISSING,
    temperament: Temperament.DEFENSIVE,
    honeyStores: HoneyStores.LOW,
  },
];

export const alerts: Alert[] = [
  {
    id: 'a1',
    type: AlertType.SWARM_WARNING,
    title: 'Swarm Warning',
    message: 'AI Alert: High Swarm Probability in this area. Check hives A-01 and C-04.',
    severity: AlertSeverity.WARNING,
    timestamp: new Date(2024, 10, 6, 8, 0),
    hiveIds: ['h1', 'h4'],
    dismissed: false,
  },
  {
    id: 'a2',
    type: AlertType.TREATMENT_DUE,
    title: 'Mite treatment due',
    message: 'For Hive 02',
    severity: AlertSeverity.WARNING,
    timestamp: new Date(2024, 10, 6, 0, 0),
    hiveIds: ['h2'],
    dismissed: false,
  },
  {
    id: 'a3',
    type: AlertType.WEATHER_WARNING,
    title: 'High humidity overnight',
    message: 'Ensure hive ventilation',
    severity: AlertSeverity.INFO,
    timestamp: new Date(2024, 10, 6, 18, 0),
    dismissed: false,
  },
];

export const recommendations: Record<string, Recommendation[]> = {
  h1: [
    {
      id: 'r1',
      hiveId: 'h1',
      type: RecommendationType.POSITIVE,
      title: 'Hive is Thriving',
      description:
        'Excellent honey stores and strong population. Consider adding a super soon to prevent swarming.',
      priority: Priority.LOW,
    },
    {
      id: 'r2',
      hiveId: 'h1',
      type: RecommendationType.WARNING,
      title: 'Monitor Varroa Mites',
      description:
        "Your last mite count was 3 weeks ago. It's recommended to perform a new count within the next 7 days.",
      priority: Priority.MEDIUM,
    },
  ],
  h4: [
    {
      id: 'r3',
      hiveId: 'h4',
      type: RecommendationType.ACTION_REQUIRED,
      title: 'Queen Issue Detected',
      description: 'No laying pattern observed. Consider requeening within 2 weeks.',
      priority: Priority.HIGH,
    },
  ],
};

export const weather: Weather = {
  temperature: 72,
  humidity: 10,
  windSpeed: 5,
  condition: WeatherCondition.SUNNY,
  description: 'Good conditions for hive work. The bees will be calm and active today.',
};

export const getApiaryById = (id: string): Apiary | undefined => {
  return apiaries.find((apiary) => apiary.id === id);
};

export const getHiveById = (id: string): Hive | undefined => {
  return hives.find((hive) => hive.id === id);
};

export const getHivesForApiary = (apiaryId: string): Hive[] => {
  return hives.filter((hive) => hive.apiaryId === apiaryId);
};

export const getRecommendationsForHive = (hiveId: string): Recommendation[] => {
  return recommendations[hiveId] || [];
};

export const getActiveAlerts = (): Alert[] => {
  return alerts.filter((alert) => !alert.dismissed);
};
