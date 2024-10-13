// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorMsalAuth",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorMsalAuth",
            targets: ["MsalPluginPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "MsalPluginPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/MsalPluginPlugin"),
        .testTarget(
            name: "MsalPluginPluginTests",
            dependencies: ["MsalPluginPlugin"],
            path: "ios/Tests/MsalPluginPluginTests")
    ]
)
