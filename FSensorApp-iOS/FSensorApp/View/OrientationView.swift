import SwiftUI

struct OrientationView: View {
    @ObservedObject var viewModel: SensorViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                RotationGaugeView(
                    heading: viewModel.orientation[0],
                    pitch: viewModel.orientation[1],
                    roll: viewModel.orientation[2]
                )
                .frame(width: 200, height: 200)

                HStack(spacing: 32) {
                    let deg = viewModel.orientation.map { $0 * 180.0 / .pi }
                    ValueLabel(axis: "H", value: deg[0], color: .red)
                    ValueLabel(axis: "P", value: deg[1], color: .green)
                    ValueLabel(axis: "R", value: deg[2], color: .blue)
                }

                TimeSeriesChartView(
                    data: viewModel.orientationHistory,
                    yRange: 180,
                    unit: "°"
                )
                .frame(maxHeight: .infinity)
                .padding(.horizontal)
            }
            .padding(.top)
            .navigationTitle("Orientation")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

private struct ValueLabel: View {
    let axis: String
    let value: Float
    let color: Color

    var body: some View {
        VStack(spacing: 2) {
            Text(axis)
                .font(.caption2)
                .foregroundStyle(color)
            Text(String(format: "%.1f°", value))
                .font(.body)
                .monospacedDigit()
        }
    }
}
