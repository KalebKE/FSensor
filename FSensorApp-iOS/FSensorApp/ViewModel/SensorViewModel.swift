import Foundation
import FSensor
import Combine

@MainActor
class SensorViewModel: ObservableObject {
    @Published var orientation: [Float] = [0, 0, 0]
    @Published var acceleration: [Float] = [0, 0, 0]
    @Published var orientationHistory = ChartData(capacity: 250)
    @Published var accelerationHistory = ChartData(capacity: 250)
    @Published var config = SensorConfig()

    private var orientationProvider: IosSensorProvider?
    private var accelerationProvider: IosSensorProvider?
    private var orientationFilter: SensorFilter?
    private var accelerationFilter: SensorFilter?
    private var startTime: TimeInterval = 0
    private var isRunning = false

    func start() {
        guard !isRunning else { return }
        isRunning = true
        startTime = ProcessInfo.processInfo.systemUptime
        orientationHistory.clear()
        accelerationHistory.clear()
        rebuildAndStart()
    }

    func stop() {
        guard isRunning else { return }
        isRunning = false
        orientationProvider?.stop()
        accelerationProvider?.stop()
        orientationProvider = nil
        accelerationProvider = nil
    }

    func updateConfig(_ newConfig: SensorConfig) {
        let wasRunning = isRunning
        if wasRunning { stop() }
        config = newConfig
        if wasRunning { start() }
    }

    private func rebuildAndStart() {
        let fusion1 = createFusion()
        let fusion2 = createFusion()
        orientationFilter = createFilter()
        accelerationFilter = createFilter()

        let orientationProv = IosSensorProvider(fusion: fusion1)
        let accelerationProv = IosSensorProvider(fusion: fusion2)
        self.orientationProvider = orientationProv
        self.accelerationProvider = accelerationProv

        let rate = Int32(config.rateHz)

        orientationProv.startOrientation(rateHz: rate) { [weak self] values in
            guard let self = self else { return }
            let raw = self.toSwiftArray(values)
            let filtered = self.applyFilter(raw, filter: self.orientationFilter)
            Task { @MainActor in
                self.orientation = filtered
                let t = Float(ProcessInfo.processInfo.systemUptime - self.startTime)
                let deg = filtered.map { $0 * 180.0 / .pi }
                self.orientationHistory.append(x: deg[0], y: deg[1], z: deg[2], time: t)
                self.objectWillChange.send()
            }
        }

        accelerationProv.startLinearAcceleration(rateHz: rate) { [weak self] values in
            guard let self = self else { return }
            let raw = self.toSwiftArray(values)
            let filtered = self.applyFilter(raw, filter: self.accelerationFilter)
            Task { @MainActor in
                self.acceleration = filtered
                let t = Float(ProcessInfo.processInfo.systemUptime - self.startTime)
                self.accelerationHistory.append(x: filtered[0], y: filtered[1], z: filtered[2], time: t)
                self.objectWillChange.send()
            }
        }
    }

    private func createFusion() -> FusionAlgorithm {
        let p = config.fusionParams
        switch config.fusionType {
        case .madgwick:
            return MadgwickFusion(beta: p.madgwickBeta)
        case .mahony:
            return MahonyFusion(kp: p.mahonyKp, ki: p.mahonyKi)
        case .ekf:
            return EkfFusion(processNoiseVariance: Double(p.ekfProcessNoise),
                           accelNoiseVariance: Double(p.ekfAccelNoise),
                           magNoiseVariance: Double(p.ekfMagNoise))
        case .complementary:
            return ComplementaryFusion(timeConstant: p.complementaryTc)
        case .kalman:
            return KalmanFusion(processNoise: Double(p.kalmanProcessNoise),
                              measurementNoise: Double(p.kalmanMeasurementNoise))
        }
    }

    private func createFilter() -> SensorFilter? {
        let tc = config.filterTimeConstant
        switch config.filterType {
        case .none: return nil
        case .lowPass: return LowPassFilter(timeConstant: tc)
        case .mean: return MeanFilter(timeConstant: tc)
        case .median: return MedianFilter(timeConstant: tc)
        }
    }

    private func applyFilter(_ values: [Float], filter: SensorFilter?) -> [Float] {
        guard let filter = filter else { return values }
        let kArray = KotlinFloatArray(size: 3)
        for i in 0..<3 { kArray.set(index: Int32(i), value: values[i]) }
        let result = filter.filter(data: kArray)
        return toSwiftArray(result)
    }

    private func toSwiftArray(_ kArray: KotlinFloatArray) -> [Float] {
        return (0..<3).map { kArray.get(index: Int32($0)) }
    }
}
