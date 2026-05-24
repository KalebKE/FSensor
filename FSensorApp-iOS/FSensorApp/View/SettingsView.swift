import SwiftUI

struct SettingsView: View {
    @ObservedObject var viewModel: SensorViewModel

    @State private var fusionType: FusionType
    @State private var filterType: FilterType
    @State private var rateHz: Int
    @State private var madgwickBeta: Float
    @State private var mahonyKp: Float
    @State private var mahonyKi: Float
    @State private var ekfProcessNoise: Float
    @State private var ekfAccelNoise: Float
    @State private var complementaryTc: Float
    @State private var kalmanProcessNoise: Float
    @State private var kalmanMeasurementNoise: Float
    @State private var filterTimeConstant: Float

    init(viewModel: SensorViewModel) {
        self.viewModel = viewModel
        let c = viewModel.config
        _fusionType = State(initialValue: c.fusionType)
        _filterType = State(initialValue: c.filterType)
        _rateHz = State(initialValue: c.rateHz)
        _madgwickBeta = State(initialValue: c.fusionParams.madgwickBeta)
        _mahonyKp = State(initialValue: c.fusionParams.mahonyKp)
        _mahonyKi = State(initialValue: c.fusionParams.mahonyKi)
        _ekfProcessNoise = State(initialValue: c.fusionParams.ekfProcessNoise)
        _ekfAccelNoise = State(initialValue: c.fusionParams.ekfAccelNoise)
        _complementaryTc = State(initialValue: c.fusionParams.complementaryTc)
        _kalmanProcessNoise = State(initialValue: c.fusionParams.kalmanProcessNoise)
        _kalmanMeasurementNoise = State(initialValue: c.fusionParams.kalmanMeasurementNoise)
        _filterTimeConstant = State(initialValue: c.filterTimeConstant)
    }

    var body: some View {
        NavigationStack {
            List {
                fusionSection
                filterSection
                rateSection
            }
            .scrollContentBackground(.hidden)
            .background(Color.cyberBackground)
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var fusionSection: some View {
        Section {
            Picker("Algorithm", selection: $fusionType) {
                Text("Madgwick").tag(FusionType.madgwick)
                Text("Mahony").tag(FusionType.mahony)
                Text("EKF").tag(FusionType.ekf)
                Text("Complementary").tag(FusionType.complementary)
                Text("Kalman").tag(FusionType.kalman)
            }
            .onChange(of: fusionType) { _ in apply() }

            fusionParams
        } header: {
            Text("Fusion Algorithm")
                .foregroundStyle(Color.neonCyan)
        }
        .listRowBackground(Color.cyberSurface)
    }

    @ViewBuilder
    private var fusionParams: some View {
        switch fusionType {
        case .madgwick:
            ParamSlider(label: "Beta", value: $madgwickBeta, range: 0.001...0.5, onChanged: apply)
        case .mahony:
            ParamSlider(label: "Kp", value: $mahonyKp, range: 0.1...10, onChanged: apply)
            ParamSlider(label: "Ki", value: $mahonyKi, range: 0...1, onChanged: apply)
        case .ekf:
            ParamSlider(label: "Process Noise", value: $ekfProcessNoise, range: 0.0001...0.1, onChanged: apply)
            ParamSlider(label: "Accel Noise", value: $ekfAccelNoise, range: 0.01...1, onChanged: apply)
        case .complementary:
            ParamSlider(label: "Time Constant", value: $complementaryTc, range: 0.01...1, onChanged: apply)
        case .kalman:
            ParamSlider(label: "Process Noise", value: $kalmanProcessNoise, range: 0.0001...0.1, onChanged: apply)
            ParamSlider(label: "Meas. Noise", value: $kalmanMeasurementNoise, range: 0.01...1, onChanged: apply)
        }
    }

    private var filterSection: some View {
        Section {
            Picker("Filter", selection: $filterType) {
                Text("None").tag(FilterType.none)
                Text("Low Pass").tag(FilterType.lowPass)
                Text("Mean").tag(FilterType.mean)
                Text("Median").tag(FilterType.median)
            }
            .onChange(of: filterType) { _ in apply() }

            if filterType != .none {
                ParamSlider(label: "Time Constant", value: $filterTimeConstant, range: 0.01...1, onChanged: apply)
            }
        } header: {
            Text("Smoothing Filter")
                .foregroundStyle(Color.neonCyan)
        }
        .listRowBackground(Color.cyberSurface)
    }

    private var rateSection: some View {
        Section {
            Picker("Rate (Hz)", selection: $rateHz) {
                Text("25 Hz").tag(25)
                Text("50 Hz").tag(50)
                Text("100 Hz").tag(100)
            }
            .pickerStyle(.segmented)
            .onChange(of: rateHz) { _ in apply() }
        } header: {
            Text("Update Rate")
                .foregroundStyle(Color.neonCyan)
        }
        .listRowBackground(Color.cyberSurface)
    }

    private func apply() {
        let config = SensorConfig(
            fusionType: fusionType,
            fusionParams: FusionParams(
                madgwickBeta: madgwickBeta,
                mahonyKp: mahonyKp,
                mahonyKi: mahonyKi,
                ekfProcessNoise: ekfProcessNoise,
                ekfAccelNoise: ekfAccelNoise,
                ekfMagNoise: 0.5,
                complementaryTc: complementaryTc,
                kalmanProcessNoise: kalmanProcessNoise,
                kalmanMeasurementNoise: kalmanMeasurementNoise
            ),
            filterType: filterType,
            filterTimeConstant: filterTimeConstant,
            rateHz: rateHz
        )
        viewModel.updateConfig(config)
    }
}

private struct ParamSlider: View {
    let label: String
    @Binding var value: Float
    let range: ClosedRange<Float>
    let onChanged: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("\(label): \(String(format: "%.4f", value))")
                .font(.caption)
                .foregroundStyle(Color.primaryText)
            Slider(value: $value, in: range, onEditingChanged: { editing in
                if !editing { onChanged() }
            })
            .tint(.neonCyan)
        }
    }
}
