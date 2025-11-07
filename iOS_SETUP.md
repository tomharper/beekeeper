# iOS Setup Guide for Beekeeper App

## Current Status

The Beekeeper app uses **Compose Multiplatform** which shares 95% of code between Android and iOS. The iOS-specific files are ready, but you need to build and run from a Mac.

## What's Already Done ✅

1. **iOS Framework Configuration** - `composeApp/build.gradle.kts` has iOS targets configured:
   - iosX64 (Intel simulator)
   - iosArm64 (Physical devices)
   - iosSimulatorArm64 (M1/M2/M3 simulator)

2. **Platform-Specific Code**:
   - `composeApp/src/iosMain/kotlin/com/beekeeper/app/data/database/DatabaseDriverFactory.kt` - SQLite for iOS
   - `composeApp/src/iosMain/kotlin/com/beekeeper/app/data/api/ApiConfig.ios.kt` - API configuration
   - `composeApp/src/iosMain/kotlin/com/beekeeper/app/MainViewController.kt` - iOS entry point

3. **iOS App Wrapper**:
   - `iosApp/iOSApp.swift` - SwiftUI wrapper
   - `iosApp/Info.plist` - App permissions (camera, location, photos)

## Building for iOS (macOS Required)

### Step 1: Build the Kotlin Framework

From the project root:

```bash
# For M1/M2/M3 Mac (Apple Silicon)
./gradlew linkDebugFrameworkIosSimulatorArm64

# For Intel Mac
./gradlew linkDebugFrameworkIosX64

# For physical iPhone/iPad
./gradlew linkDebugFrameworkIosArm64
```

The framework will be built to:
```
composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework
```

### Step 2: Create Xcode Project

1. Open Xcode
2. Create new App project:
   - Product Name: **Beekeeper**
   - Bundle Identifier: **com.beekeeper.app**
   - Interface: **SwiftUI**
   - Language: **Swift**

3. Replace the default App file with `iosApp/iOSApp.swift`

4. Add the Framework:
   - Drag `ComposeApp.framework` into Xcode project
   - Select "Copy items if needed"
   - Add to "Frameworks, Libraries, and Embedded Content"
   - Set to "Embed & Sign"

5. Add `Info.plist` permissions from `iosApp/Info.plist`

### Step 3: Run

- Select your simulator or device
- Press ⌘R to build and run

## iOS-Specific Features Still Needed

### 1. Camera Implementation ⚠️

Currently only Android has camera support (CameraX). For iOS, you need to add:

```swift
// In a new file: CameraView.swift
import SwiftUI
import AVFoundation

struct CameraView: UIViewControllerRepresentable {
    @Binding var capturedImage: UIImage?
    @Environment(\.presentationMode) var presentationMode

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .camera
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraView

        init(_ parent: CameraView) {
            self.parent = parent
        }

        func imagePickerController(_ picker: UIImagePickerController,
                                 didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.capturedImage = image
            }
            parent.presentationMode.wrappedValue.dismiss()
        }
    }
}
```

Then integrate it into the Compose screens using `UIViewController` interop.

### 2. Testing on Physical Device

To test on a real iPhone/iPad:

1. Connect device via USB
2. Build for iosArm64:
   ```bash
   ./gradlew linkDebugFrameworkIosArm64
   ```
3. In Xcode, select your device and run

### 3. Backend Connection

Make sure your Mac can reach the backend:
- If backend is on same Mac: `localhost:2020` works
- If backend is on another machine: Update `ApiConfig.ios.kt` with the IP address

## Development Workflow

1. Make changes to Kotlin code in `composeApp/src/commonMain/`
2. Rebuild framework:
   ```bash
   ./gradlew linkDebugFrameworkIosSimulatorArm64
   ```
3. In Xcode: Product → Clean Build Folder (Shift + ⌘ + K)
4. Build and run in Xcode (⌘R)

## Troubleshooting

### "Framework not found"
- Make sure you built the framework first
- Check the framework path in Xcode project settings

### "Module 'ComposeApp' not found"
- Clean Xcode build: Shift + ⌘ + K
- Rebuild the Kotlin framework
- Restart Xcode

### API Connection Issues
- Check `ApiConfig.ios.kt` has correct backend URL
- Ensure backend is running and accessible from iOS simulator/device
- Check `Info.plist` has `NSAppTransportSecurity` if using HTTP (not HTTPS)

## Next Steps

1. ✅ Build the Kotlin framework
2. ✅ Create Xcode project
3. ✅ Test app launches
4. ⚠️ Add iOS camera implementation
5. ⚠️ Test all features (tasks, inspections, AI chat)
6. ⚠️ Handle iOS-specific UI adjustments if needed

## Resources

- [Compose Multiplatform iOS Guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
- [KMP iOS Integration](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [SQLDelight iOS Setup](https://cashapp.github.io/sqldelight/multiplatform_sqlite/)
