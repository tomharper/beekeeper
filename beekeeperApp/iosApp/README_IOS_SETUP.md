# Cinefiller iOS App Setup

This directory contains the iOS application for Cinefiller, built using Kotlin Multiplatform Mobile (KMM) with SwiftUI.

## Project Structure

```
iosApp/
├── CineFillerApp/              # iOS app source code
│   ├── CineFillerAppApp.swift  # Main app entry point
│   ├── ContentView.swift       # Main content view
│   ├── Info.plist             # App configuration
│   └── Assets.xcassets/       # App icons and assets
├── CineFillerApp.xcodeproj/   # Xcode project files
├── Package.swift              # Swift Package Manager configuration
├── build-ios-framework.sh     # Full iOS framework build script
└── quick-build-ios.sh         # Quick simulator build script

## Prerequisites

- macOS with Xcode 14.0 or later
- Kotlin Multiplatform plugin for Xcode (optional but recommended)
- Java 17 or later (for Gradle)

## Building the iOS App

### Option 1: Quick Build (Simulator Only - Fastest)

For rapid development iterations:

```bash
cd iosApp
./quick-build-ios.sh
```

This builds only the iOS Simulator framework, which is much faster.

### Option 2: Full Build (All Architectures)

For production or device testing:

```bash
cd iosApp
./build-ios-framework.sh
```

This builds frameworks for:
- iOS Simulator (x64 and ARM64)
- iOS Device (ARM64)
- Creates XCFramework for distribution

### Option 3: Direct Gradle Commands

From the project root (`fillerApp/`):

```bash
# Build for iOS Simulator (ARM64 - M1/M2/M3 Macs)
./gradlew linkDebugFrameworkIosSimulatorArm64

# Build for iOS Device
./gradlew linkDebugFrameworkIosArm64

# Build all iOS frameworks
./gradlew buildAllIOSFrameworks

# Create XCFramework
./gradlew createXCFramework
```

## Opening in Xcode

1. Build the shared framework first (see above)
2. Open `CineFillerApp.xcodeproj` in Xcode
3. Select your target device/simulator
4. Build and run (⌘R)

## Swift Package Manager Integration

The iOS app uses SPM to integrate the shared KMP framework. The `Package.swift` file references the compiled framework at:

```
../shared/build/xcode-frameworks/Debug/shared.framework
```

If you encounter framework not found errors:
1. Make sure you've built the framework first
2. Check that the framework path in `Package.swift` is correct
3. Clean and rebuild the Xcode project

## Troubleshooting

### "shared.framework not found"

Build the shared framework:
```bash
./quick-build-ios.sh
```

### "Module 'shared' not found"

1. Clean Xcode build folder (Shift + ⌘ + K)
2. Rebuild the shared framework
3. Close and reopen Xcode

### Framework Build Errors

Make sure iOS targets are enabled in `shared/build.gradle`:
```gradle
iosX64()
iosArm64()
iosSimulatorArm64()
```

## Development Workflow

1. Make changes to shared code in `shared/src/commonMain/`
2. Rebuild the iOS framework: `./quick-build-ios.sh`
3. The framework is automatically updated in Xcode
4. Build and run in Xcode

## App Configuration

- **Bundle ID**: `com.cinefiller.fillerapp` (configure in Xcode project settings)
- **Minimum iOS Version**: iOS 14.0
- **Target Devices**: iPhone, iPad

## Next Steps

1. Customize `CineFillerAppApp.swift` to add app initialization logic
2. Build out `ContentView.swift` or add new SwiftUI views
3. Integrate with shared KMP repositories and ViewModels
4. Add platform-specific iOS features as needed

## Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [KMP for iOS Guide](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
