// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Cinefiller",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "Cinefiller",
            targets: ["Cinefiller"]
        ),
    ],
    dependencies: [
        // Add any external SPM dependencies here if needed
        // .package(url: "https://github.com/realm/SwiftLint", from: "0.50.0"),
    ],
    targets: [
        .target(
            name: "Cinefiller",
            dependencies: [
                "shared"
                // Add SPM dependencies here if needed
            ]
        ),
        .binaryTarget(
            name: "shared",
            path: "../shared/build/xcode-frameworks/Debug/shared.framework"
        ),
        .testTarget(
            name: "CinefillerTests",
            dependencies: ["Cinefiller"]
        )
    ]
)
