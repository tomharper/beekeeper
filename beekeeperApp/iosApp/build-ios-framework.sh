#!/bin/bash

# Build iOS Framework Script for Cinefiller
# This script builds the shared KMP module as an iOS framework

set -e  # Exit on error

echo "🔨 Building iOS Frameworks for Cinefiller..."
echo ""

# Navigate to project root
cd "$(dirname "$0")/.."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew cleanIOSFrameworks 2>/dev/null || true

# Build all iOS frameworks
echo ""
echo "📦 Building iOS frameworks..."
./gradlew buildAllIOSFrameworks

# Create XCFramework for distribution
echo ""
echo "📦 Creating XCFramework..."
./gradlew createXCFramework

echo ""
echo "✅ iOS frameworks built successfully!"
echo ""
echo "Framework locations:"
./gradlew iosFrameworkPaths -q
