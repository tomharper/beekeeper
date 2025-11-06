# Beekeeper App

A comprehensive beekeeping management application with AI-powered hive analysis, task scheduling, and inspection tracking.

## Project Structure

```
beekeeperApp/
├── beekeeperApp/          # Kotlin Multiplatform mobile & desktop app
│   ├── shared/            # Shared KMP code
│   ├── androidApp/        # Android application
│   ├── desktopApp/        # Desktop JVM application
│   └── iosApp/            # iOS application
│
└── beekeeper/             # Backend services
    ├── react-app/         # React web frontend
    └── fastapi-backend/   # FastAPI backend server
```

## Features

### Core Features
- **Hive Management** - Track multiple hives with detailed information
- **Task Scheduling** - Schedule and track beekeeping tasks (inspections, feeding, treatments, harvests)
- **Inspection Tracking** - Record detailed hive inspections with photos
- **AI-Powered Analysis** - Upload photos for AI analysis of bee health, pests, and hive conditions
- **Weather Integration** - Get weather information for optimal inspection timing
- **Historical Data** - Track hive conditions and performance over time
- **Cross-Platform** - Android, iOS, Desktop, and Web support

### AI Capabilities
- Hive health assessment
- Pest detection (varroa mites, etc.)
- Queen presence detection
- Brood pattern analysis
- Colony strength estimation

## Technology Stack

### Mobile & Desktop App (KMP)
- **Kotlin 2.1.0** - Primary language
- **Kotlin Multiplatform** - Cross-platform code sharing
- **Compose Multiplatform 1.8.1** - UI framework
- **Ktor 3.0.2** - HTTP client
- **SQLDelight 2.0.2** - Database
- **Koin 4.0.1** - Dependency injection
- **Kotlinx Serialization** - JSON serialization
- **Kotlinx DateTime** - Date/time handling

### Web Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router** - Navigation
- **Axios** - HTTP client

### Backend API
- **FastAPI** - Python web framework
- **Uvicorn** - ASGI server
- **Pydantic** - Data validation
- **Anthropic Claude** - AI vision analysis

## Getting Started

### Prerequisites
- JDK 11 or higher
- Node.js 18+ (for React app)
- Python 3.9+ (for FastAPI backend)
- Android Studio (for Android development)
- Xcode (for iOS development, macOS only)

### Quick Start

#### 1. Backend API

```bash
cd beekeeper/fastapi-backend

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Create .env file
cp .env.example .env
# Edit .env and add your ANTHROPIC_API_KEY

# Run the server
python main.py
# API will be available at http://localhost:8000
# API docs at http://localhost:8000/docs
```

#### 2. Web Frontend

```bash
cd beekeeper/react-app

# Install dependencies
npm install

# Create .env file
cp .env.example .env

# Run development server
npm run dev
# App will be available at http://localhost:3000
```

#### 3. Mobile/Desktop App

```bash
cd beekeeperApp

# Android
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run

# iOS (macOS only)
# Open iosApp/iosApp.xcodeproj in Xcode and run
```

## Development

### Backend API Development

The FastAPI backend provides REST endpoints for:
- Hive CRUD operations
- Inspection management
- Task scheduling
- Image upload and AI analysis
- Weather data

See `beekeeper/fastapi-backend/README.md` for detailed API documentation.

### Mobile App Development

The Kotlin Multiplatform app shares code across Android, iOS, and Desktop:

- **Domain models** - `shared/src/commonMain/kotlin/com/beekeeper/app/domain/model/`
- **UI screens** - `shared/src/commonMain/kotlin/com/beekeeper/app/presentation/screens/`
- **Data layer** - `shared/src/commonMain/kotlin/com/beekeeper/app/data/`

Platform-specific code is in `androidMain`, `iosMain`, and `desktopMain` respectively.

### Web Frontend Development

React app with TypeScript for web-based hive management.

See `beekeeper/react-app/README.md` for details.

## Architecture

### Data Flow

```
Mobile/Web UI → FastAPI Backend → AI Services (Claude Vision)
                      ↓
                 SQLite/Database
```

### Mobile App Architecture

```
Presentation Layer (Compose UI)
       ↓
ViewModel Layer (State Management)
       ↓
Domain Layer (Business Logic)
       ↓
Data Layer (API, Database)
```

## Roadmap

### Phase 1: Core Functionality (Current)
- [x] Project structure setup
- [x] Basic hive management
- [x] Task scheduling
- [x] Inspection tracking
- [ ] Photo capture and storage
- [ ] AI vision integration

### Phase 2: Advanced Features
- [ ] Weather API integration
- [ ] Push notifications
- [ ] Historical analytics and charts
- [ ] Data export (CSV, PDF)
- [ ] User authentication
- [ ] Multi-user support

### Phase 3: Community & Social
- [ ] Beekeeping tips and guides
- [ ] Community forums
- [ ] Share inspection data
- [ ] Best practices database

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

[Add your license here]

## Support

For issues, questions, or suggestions:
- Create an issue on GitHub
- Contact [your-email@example.com]

## Acknowledgments

- Built with Kotlin Multiplatform and Compose Multiplatform
- AI powered by Anthropic Claude
- Icons from [icon source]
