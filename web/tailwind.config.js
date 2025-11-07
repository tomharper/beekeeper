/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Primary dark green colors
        'beekeeper-green-dark': '#1A3A2E',
        'beekeeper-green-medium': '#2D5444',
        'beekeeper-green-light': '#3D6B57',

        // Accent gold/yellow colors
        'beekeeper-gold': '#FDB71A',
        'beekeeper-gold-dark': '#E5A419',

        // Status colors
        'status-healthy': '#4CAF50',
        'status-warning': '#FFA726',
        'status-alert': '#EF5350',

        // Background colors
        'background-dark': '#0D1F1A',
        'surface-dark': '#1A2F27',
        'card-background': '#253D34',

        // Text colors
        'text-primary': '#FFFFFF',
        'text-secondary': '#B0BEC5',
        'text-tertiary': '#78909C',
      },
    },
  },
  plugins: [],
}
