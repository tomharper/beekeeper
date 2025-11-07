export enum ApiaryStatus {
  HEALTHY = 'HEALTHY',
  WARNING = 'WARNING',
  ALERT = 'ALERT',
}

export interface Apiary {
  id: string;
  name: string;
  location: string;
  latitude?: number;
  longitude?: number;
  hiveCount: number;
  status: ApiaryStatus;
}

export enum HiveStatus {
  STRONG = 'STRONG',
  ALERT = 'ALERT',
  NEEDS_INSPECTION = 'NEEDS_INSPECTION',
  WEAK = 'WEAK',
}

export enum ColonyStrength {
  STRONG = 'STRONG',
  MODERATE = 'MODERATE',
  WEAK = 'WEAK',
}

export enum QueenStatus {
  LAYING = 'LAYING',
  NOT_LAYING = 'NOT_LAYING',
  MISSING = 'MISSING',
  UNKNOWN = 'UNKNOWN',
}

export enum Temperament {
  CALM = 'CALM',
  MODERATE = 'MODERATE',
  DEFENSIVE = 'DEFENSIVE',
}

export enum HoneyStores {
  FULL = 'FULL',
  ADEQUATE = 'ADEQUATE',
  LOW = 'LOW',
  EMPTY = 'EMPTY',
}

export interface Hive {
  id: string;
  name: string;
  apiaryId: string;
  status: HiveStatus;
  lastInspected: Date;
  imageUrl?: string;
  colonyStrength: ColonyStrength;
  queenStatus: QueenStatus;
  temperament: Temperament;
  honeyStores: HoneyStores;
}

export enum AlertType {
  SWARM_WARNING = 'SWARM_WARNING',
  VARROA_MITE = 'VARROA_MITE',
  INSPECTION_DUE = 'INSPECTION_DUE',
  TREATMENT_DUE = 'TREATMENT_DUE',
  WEATHER_WARNING = 'WEATHER_WARNING',
  HONEY_FLOW = 'HONEY_FLOW',
  GENERAL = 'GENERAL',
}

export enum AlertSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL',
}

export interface Alert {
  id: string;
  type: AlertType;
  title: string;
  message: string;
  severity: AlertSeverity;
  timestamp: Date;
  hiveIds?: string[];
  dismissed?: boolean;
}

export enum RecommendationType {
  POSITIVE = 'POSITIVE',
  WARNING = 'WARNING',
  ACTION_REQUIRED = 'ACTION_REQUIRED',
  INFO = 'INFO',
}

export enum Priority {
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW',
}

export interface Recommendation {
  id: string;
  hiveId: string;
  type: RecommendationType;
  title: string;
  description: string;
  priority: Priority;
}

export enum WeatherCondition {
  SUNNY = 'SUNNY',
  PARTLY_CLOUDY = 'PARTLY_CLOUDY',
  CLOUDY = 'CLOUDY',
  RAINY = 'RAINY',
  STORMY = 'STORMY',
}

export interface Weather {
  temperature: number;
  humidity: number;
  windSpeed: number;
  condition: WeatherCondition;
  description: string;
}

export interface User {
  id: string;
  email: string;
  fullName: string;
}

export interface AuthToken {
  accessToken: string;
  tokenType: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}
