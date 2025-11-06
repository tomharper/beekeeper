// File: iosApp/CineFillerApp/CineFillerAppApp.swift

import SwiftUI
import shared

@main
struct CinefillerApp: App {
    @StateObject private var themeState = ThemeState()

    // Initialize the shared KMP module
    init() {
        // Initialize SQLDelight database and repositories
        IOSHelperKt.initializeIOS()
    }

    var body: some SwiftUI.Scene {
        WindowGroup {
            ContentView()
                .environmentObject(themeState)
                .preferredColorScheme(themeState.isDark ? .dark : .light)
        }
    }
}

// Observable state for theme changes - called explicitly when theme changes
class ThemeState: ObservableObject {
    @Published var isDark: Bool = false

    init() {
        // Register callback so Kotlin can notify us of theme changes
        IOSHelperKt.setThemeChangeCallback { [weak self] in
            print("ThemeState: Callback received from Kotlin")
            DispatchQueue.main.async {
                self?.update()
            }
        }
    }

    func update() {
        let newValue = IOSHelperKt.isCurrentThemeDark()
        print("ThemeState: update() called, isDark changing from \(isDark) to \(newValue)")
        print("ThemeState: Will set preferredColorScheme to \(newValue ? "dark" : "light")")
        isDark = newValue
    }
}
