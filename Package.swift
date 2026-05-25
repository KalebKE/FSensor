// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "FSensor",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "FSensor",
            targets: ["FSensor"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "FSensor",
            url: "https://github.com/KalebKE/FSensor/releases/download/v0.0.0/FSensor.xcframework.zip",
            checksum: "0000000000000000000000000000000000000000000000000000000000000000"
        )
    ]
)
