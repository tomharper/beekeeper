#!/bin/bash

# Quick iOS Build Script
# Builds just the iOS Simulator framework for faster iteration

set -e

echo "🚀 Quick building iOS Simulator framework..."

cd "$(dirname "$0")/.."

# Build only for simulator (much faster)
./gradlew linkDebugFrameworkIosSimulatorArm64

echo "✅ iOS Simulator framework ready!"
echo "📍 Location: shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework"
