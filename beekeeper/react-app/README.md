# Beekeeper React App

Web frontend for the Beekeeper app built with React, TypeScript, and Vite.

## Features

- **Hive Management** - Track and manage multiple hives
- **Task Scheduling** - Create and manage beekeeping tasks with due dates
- **Inspections** - Record detailed hive inspections
- **AI Photo Analysis** - Upload photos for AI-powered health analysis
- **Responsive Design** - Works on desktop and mobile devices

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create `.env` file:
```env
VITE_API_URL=http://localhost:8000
```

3. Run development server:
```bash
npm run dev
```

The app will be available at http://localhost:3000

## Building for Production

```bash
npm run build
npm run preview
```

## Technology Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **date-fns** - Date utilities

## Project Structure

```
src/
├── components/     # Reusable React components
├── pages/          # Page components
│   ├── HomePage.tsx
│   ├── HivesPage.tsx
│   ├── TasksPage.tsx
│   └── InspectionsPage.tsx
├── services/       # API service layer
│   └── api.ts
├── types/          # TypeScript type definitions
│   └── index.ts
├── styles/         # CSS stylesheets
│   ├── index.css
│   └── App.css
├── App.tsx         # Main app component
└── main.tsx        # App entry point
```

## TODO

- [ ] Add user authentication
- [ ] Implement weather widget
- [ ] Add charts for historical data
- [ ] Implement push notifications
- [ ] Add offline support (PWA)
- [ ] Implement data export (CSV/PDF)
