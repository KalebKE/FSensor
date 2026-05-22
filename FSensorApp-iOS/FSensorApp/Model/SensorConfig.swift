import Foundation

enum FusionType: String, CaseIterable, Identifiable {
    case madgwick = "Madgwick"
    case mahony = "Mahony"
    case ekf = "EKF"
    case complementary = "Complementary"
    case kalman = "Kalman"

    var id: String { rawValue }
}

enum FilterType: String, CaseIterable, Identifiable {
    case none = "None"
    case lowPass = "Low Pass"
    case mean = "Mean"
    case median = "Median"

    var id: String { rawValue }
}

struct FusionParams: Equatable {
    var madgwickBeta: Float = 0.033
    var mahonyKp: Float = 1.0
    var mahonyKi: Float = 0.0
    var ekfProcessNoise: Float = 0.001
    var ekfAccelNoise: Float = 0.1
    var ekfMagNoise: Float = 0.5
    var complementaryTc: Float = 0.18
    var kalmanProcessNoise: Float = 0.001
    var kalmanMeasurementNoise: Float = 0.1
}

struct SensorConfig: Equatable {
    var fusionType: FusionType = .madgwick
    var fusionParams: FusionParams = FusionParams()
    var filterType: FilterType = .none
    var filterTimeConstant: Float = 0.18
    var rateHz: Int = 50
}
