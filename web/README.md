# Beekeeper Web App

React + TypeScript web application for beekeeping management.

## Features

- **Apiary Sites Overview** - Manage multiple apiaries with status tracking
- **Site Dashboard** - View all hives in a grid layout with health indicators
- **Hive Details** - Detailed view of individual hive status and AI recommendations
- **AI Expert Advisor** - Weather-aware recommendations and upcoming alerts

## Getting Started

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

The app will be available at http://localhost:3000

### Build for Production

```bash
npm run build
```

## Tech Stack

- React 18
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Lucide Icons
- date-fns

## Project Structure

```
src/
├── data/          # Mock data repository
├── pages/         # Page components
├── types/         # TypeScript type definitions
├── utils/         # Utility functions
├── App.tsx        # Main app with routing
├── main.tsx       # Entry point
└── index.css      # Global styles
```
