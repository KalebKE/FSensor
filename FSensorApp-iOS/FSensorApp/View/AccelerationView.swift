import SwiftUI

struct AccelerationView: View {
    @ObservedObject var viewModel: SensorViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                AccelerationGaugeView(
                    x: viewModel.acceleration[0],
                    y: viewModel.acceleration[1]
                )
                .frame(width: 200, height: 200)

                HStack(spacing: 32) {
                    ValueLabel(axis: "X", value: viewModel.acceleration[0], color: .red)
                    ValueLabel(axis: "Y", value: viewModel.acceleration[1], color: .green)
                    ValueLabel(axis: "Z", value: viewModel.acceleration[2], color: .blue)
                }

                TimeSeriesChartView(
                    data: viewModel.accelerationHistory,
                    yRange: 20,
                    unit: "m/s²"
                )
                .frame(maxHeight: .infinity)
                .padding(.horizontal)
            }
            .padding(.top)
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
        }
    }
}
