// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorMsalAuth",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorMsalAuth",
            targets: ["MsalPluginPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main"),
        .package(url: "https://github.com/AzureAD/microsoft-authentication-library-for-objc.git", from: "1.1.0")
    ],
    targets: [
        .target(
            name: "MsalPluginPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "MSAL", package: "microsoft-authentication-library-for-objc") 
            ],
            path: "ios/Sources/MsalPluginPlugin"),
        .testTarget(
            name: "MsalPluginPluginTests",
            dependencies: ["MsalPluginPlugin"],
            path: "ios/Tests/MsalPluginPluginTests")
    ]
)
