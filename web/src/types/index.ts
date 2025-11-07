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

export enum TaskType {
  INSPECTION = 'INSPECTION',
  FEEDING = 'FEEDING',
  WATER_CHECK = 'WATER_CHECK',
  SPRING_INSPECTION = 'SPRING_INSPECTION',
  SUMMER_INSPECTION = 'SUMMER_INSPECTION',
  FALL_PREPARATION = 'FALL_PREPARATION',
  WINTER_CHECK = 'WINTER_CHECK',
  PEST_TREATMENT = 'PEST_TREATMENT',
  DISEASE_TREATMENT = 'DISEASE_TREATMENT',
  MEDICATION = 'MEDICATION',
  HARVEST_HONEY = 'HARVEST_HONEY',
  EXTRACT_HONEY = 'EXTRACT_HONEY',
  BOTTLE_HONEY = 'BOTTLE_HONEY',
  HARVEST_WAX = 'HARVEST_WAX',
  HARVEST_PROPOLIS = 'HARVEST_PROPOLIS',
  SPLIT_HIVE = 'SPLIT_HIVE',
  COMBINE_HIVES = 'COMBINE_HIVES',
  REQUEEN = 'REQUEEN',
  SWARM_PREVENTION = 'SWARM_PREVENTION',
  SWARM_COLLECTION = 'SWARM_COLLECTION',
  ADD_BOXES = 'ADD_BOXES',
  REMOVE_BOXES = 'REMOVE_BOXES',
  CLEAN_EQUIPMENT = 'CLEAN_EQUIPMENT',
  REPAIR_EQUIPMENT = 'REPAIR_EQUIPMENT',
  BUILD_FRAMES = 'BUILD_FRAMES',
  EDUCATION = 'EDUCATION',
  RECORD_KEEPING = 'RECORD_KEEPING',
  ORDER_SUPPLIES = 'ORDER_SUPPLIES',
  GENERAL = 'GENERAL',
  OTHER = 'OTHER',
}

export enum TaskStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  OVERDUE = 'OVERDUE',
}

export enum TaskPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT',
}

export enum RecurrenceFrequency {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  BIWEEKLY = 'BIWEEKLY',
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  YEARLY = 'YEARLY',
}

export interface Task {
  id: string;
  title: string;
  description: string;
  taskType: TaskType;
  dueDate: Date;
  reminderDate?: Date;
  hiveId?: string;
  apiaryId?: string;
  userId: string;
  status: TaskStatus;
  priority: TaskPriority;
  completedDate?: Date;
  estimatedDurationMinutes?: number;
  weatherDependent: boolean;
  minimumTemperature?: number;
  notes: string;
  recurrenceFrequency?: RecurrenceFrequency;
  recurrenceInterval?: number;
  recurrenceEndDate?: Date;
  recurrenceCount?: number;
  createdAt: Date;
  updatedAt: Date;
}

// Inspection Types
export enum QueenCellStatus {
  NONE = 'NONE',
  QUEEN_CUPS = 'QUEEN_CUPS',
  CHARGED_CELLS = 'CHARGED_CELLS',
  CAPPED_CELLS = 'CAPPED_CELLS',
  SUPERSEDURE_CELLS = 'SUPERSEDURE_CELLS',
  SWARM_CELLS = 'SWARM_CELLS',
}

export enum BroodPattern {
  EXCELLENT = 'EXCELLENT',
  GOOD = 'GOOD',
  SPOTTY = 'SPOTTY',
  POOR = 'POOR',
  NONE = 'NONE',
}

export enum ColonyTemperament {
  VERY_CALM = 'VERY_CALM',
  CALM = 'CALM',
  MODERATE = 'MODERATE',
  DEFENSIVE = 'DEFENSIVE',
  AGGRESSIVE = 'AGGRESSIVE',
  VERY_AGGRESSIVE = 'VERY_AGGRESSIVE',
}

export enum ColonyPopulation {
  VERY_WEAK = 'VERY_WEAK',
  WEAK = 'WEAK',
  MEDIUM = 'MEDIUM',
  STRONG = 'STRONG',
  VERY_STRONG = 'VERY_STRONG',
}

export enum InspectionHealthStatus {
  EXCELLENT = 'EXCELLENT',
  HEALTHY = 'HEALTHY',
  CONCERNING = 'CONCERNING',
  NEEDS_ATTENTION = 'NEEDS_ATTENTION',
  CRITICAL = 'CRITICAL',
}

export enum ResourceLevel {
  NONE = 'NONE',
  VERY_LOW = 'VERY_LOW',
  LOW = 'LOW',
  ADEQUATE = 'ADEQUATE',
  GOOD = 'GOOD',
  EXCELLENT = 'EXCELLENT',
}

export interface Inspection {
  id: string;
  hiveId: string;
  userId: string;
  inspectionDate: Date;
  durationMinutes?: number;

  // Weather
  weatherTemp?: number;
  weatherConditions?: string;

  // Queen observations
  queenSeen: boolean;
  queenMarked: boolean;
  queenCells: QueenCellStatus;

  // Brood observations
  broodPattern: BroodPattern;
  eggsSeen: boolean;
  larvaeSeen: boolean;
  cappedBroodSeen: boolean;
  estimatedBroodFrames?: number;

  // Colony observations
  temperament: ColonyTemperament;
  population: ColonyPopulation;
  estimatedFramesCovered?: number;

  // Health observations
  healthStatus: InspectionHealthStatus;
  varroaMitesDetected: boolean;
  pestsNotes: string;
  diseaseSigns: string;

  // Resources
  honeyStores: ResourceLevel;
  pollenStores: ResourceLevel;
  cappedHoney: boolean;

  // Actions taken
  actionsTaken: string;
  feedingDone: boolean;
  feedingNotes: string;
  treatmentApplied: boolean;
  treatmentNotes: string;

  // Media and notes
  photos: string;
  notes: string;
  nextInspectionDate?: Date;

  createdAt: Date;
  updatedAt: Date;
}
