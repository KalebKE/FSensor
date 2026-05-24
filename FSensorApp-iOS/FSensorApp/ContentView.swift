import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = SensorViewModel()

    var body: some View {
        TabView {
            OrientationView(viewModel: viewModel)
                .tabItem {
                    Label("Orientation", systemImage: "gyroscope")
                }

            AccelerationView(viewModel: viewModel)
                .tabItem {
                    Label("Acceleration", systemImage: "move.3d")
                }

            SettingsView(viewModel: viewModel)
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                }
        }
        .tint(.neonCyan)
        .onAppear { viewModel.start() }
        .onDisappear { viewModel.stop() }
    }
}
