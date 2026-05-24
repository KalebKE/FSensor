import SwiftUI

struct AccelerationView: View {
    @ObservedObject var viewModel: SensorViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                AccelerationGaugeView(
                    x: viewModel.acceleration[0],
                    y: viewModel.acceleration[1]
                )
                .padding(.horizontal, 32)

                HStack(spacing: 32) {
                    ValueLabel(axis: "X", value: viewModel.acceleration[0], color: .neonCyan)
                    ValueLabel(axis: "Y", value: viewModel.acceleration[1], color: .neonMagenta)
                    ValueLabel(axis: "Z", value: viewModel.acceleration[2], color: .neonGreen)
                }

                TimeSeriesChartView(
                    data: viewModel.accelerationHistory,
                    yRange: 20,
                    unit: "m/s²"
                )
                .frame(maxHeight: .infinity)
                .padding(.horizontal)

                Button(action: { viewModel.reset() }) {
                    Text("Reset")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .tint(.neonCyan)
                .padding(.horizontal, 32)
                .padding(.bottom, 8)
            }
            .padding(.top)
            .background(Color.cyberBackground)
            .navigationTitle("Acceleration")
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
            Text(String(format: "%.2f", value))
                .font(.body)
                .monospacedDigit()
                .foregroundStyle(Color.primaryText)
        }
    }
}
