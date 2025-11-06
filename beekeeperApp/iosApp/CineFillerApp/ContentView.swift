// File: iosApp/CineFillerApp/ContentView.swift

import SwiftUI
import shared

struct ContentView: View {
    @EnvironmentObject var themeState: ThemeState

    var body: some View {
        // Use the Compose Multiplatform UI from the shared module
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

// Wrapper to properly control interface style
class ThemeAwareViewController: UIViewController {
    private let composeViewController: UIViewController
    var currentStyle: UIUserInterfaceStyle = .unspecified {
        didSet {
            print("ThemeAwareViewController: currentStyle didSet to \(currentStyle.rawValue)")
            // Set on self
            overrideUserInterfaceStyle = currentStyle

            // Set on child
            composeViewController.overrideUserInterfaceStyle = currentStyle

            // Set on all windows
            UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap { $0.windows }
                .forEach { window in
                    window.overrideUserInterfaceStyle = currentStyle
                    print("ThemeAwareViewController: Set window \(window) to style \(currentStyle.rawValue)")
                }

            // Update status bar appearance
            setNeedsStatusBarAppearanceUpdate()
        }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        return currentStyle == .dark ? .lightContent : .darkContent
    }

    init(composeViewController: UIViewController) {
        self.composeViewController = composeViewController
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        // Set the interface style immediately
        self.overrideUserInterfaceStyle = currentStyle

        // Add Compose view controller as child
        addChild(composeViewController)
        view.addSubview(composeViewController.view)
        composeViewController.view.frame = view.bounds
        composeViewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        composeViewController.didMove(toParent: self)

        print("ThemeAwareViewController: viewDidLoad with style \(currentStyle.rawValue)")
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        // Ensure window style is set when view appears
        view.window?.overrideUserInterfaceStyle = currentStyle
        print("ThemeAwareViewController: viewDidAppear, set window style to \(currentStyle.rawValue)")
    }
}

struct ComposeView: UIViewControllerRepresentable {
    @Environment(\.colorScheme) var colorScheme
    @EnvironmentObject var themeState: ThemeState

    func makeUIViewController(context: Context) -> UIViewController {
        // Set initial theme based on iOS system appearance
        let isDarkMode = colorScheme == .dark
        IOSHelperKt.setIOSTheme(isDarkMode: isDarkMode)
        themeState.update()

        // Create the Compose view controller
        let composeVC = MainViewControllerKt.MainViewController()

        // Wrap it to control interface style
        let wrapper = ThemeAwareViewController(composeViewController: composeVC)
        let initialStyle: UIUserInterfaceStyle = themeState.isDark ? .dark : .light
        wrapper.currentStyle = initialStyle

        print("ComposeView: Created wrapper with initial style \(initialStyle.rawValue)")

        context.coordinator.wrapperViewController = wrapper
        return wrapper
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Update the wrapper's style when theme changes
        if let wrapper = uiViewController as? ThemeAwareViewController {
            let newStyle: UIUserInterfaceStyle = themeState.isDark ? .dark : .light
            print("ComposeView: updateUIViewController, setting style to \(newStyle.rawValue)")
            wrapper.currentStyle = newStyle
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        weak var wrapperViewController: ThemeAwareViewController?
    }
}

#Preview {
    ContentView()
}
