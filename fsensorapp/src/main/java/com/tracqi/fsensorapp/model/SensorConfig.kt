package com.tracqi.fsensorapp.model

import android.hardware.SensorManager
import com.tracqi.fsensor.filter.LowPassFilter
import com.tracqi.fsensor.filter.MeanFilter
import com.tracqi.fsensor.filter.MedianFilter
import com.tracqi.fsensor.filter.SensorFilter
import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.fusion.complementary.ComplementaryFusion
import com.tracqi.fsensor.fusion.ekf.EkfFusion
import com.tracqi.fsensor.fusion.kalman.KalmanFusion
import com.tracqi.fsensor.fusion.madgwick.MadgwickFusion
import com.tracqi.fsensor.fusion.mahony.MahonyFusion

enum class FusionType(val label: String) {
    MADGWICK("Madgwick"),
    MAHONY("Mahony"),
    EKF("EKF"),
    COMPLEMENTARY("Complementary"),
    KALMAN("Kalman");

    fun createInstance(params: FusionParams): FusionAlgorithm = when (this) {
        MADGWICK -> MadgwickFusion(params.madgwickBeta)
        MAHONY -> MahonyFusion(params.mahonyKp, params.mahonyKi)
        EKF -> EkfFusion(params.ekfProcessNoise.toDouble(), params.ekfAccelNoise.toDouble(), params.ekfMagNoise.toDouble())
        COMPLEMENTARY -> ComplementaryFusion(params.complementaryTc)
        KALMAN -> KalmanFusion(params.kalmanProcessNoise.toDouble(), params.kalmanMeasurementNoise.toDouble())
    }
}

enum class FilterType(val label: String) {
    NONE("None"),
    LOW_PASS("Low Pass"),
    MEAN("Mean"),
    MEDIAN("Median");

    fun createInstance(timeConstant: Float): SensorFilter? = when (this) {
        NONE -> null
        LOW_PASS -> LowPassFilter(timeConstant)
        MEAN -> MeanFilter(timeConstant)
        MEDIAN -> MedianFilter(timeConstant)
    }
}

enum class SensorRate(val label: String, val delay: Int) {
    NORMAL("Normal", SensorManager.SENSOR_DELAY_NORMAL),
    GAME("Game", SensorManager.SENSOR_DELAY_GAME),
    FASTEST("Fastest", SensorManager.SENSOR_DELAY_FASTEST)
}

data class FusionParams(
    val madgwickBeta: Float = 0.033f,
    val mahonyKp: Float = 1.0f,
    val mahonyKi: Float = 0.0f,
    val ekfProcessNoise: Float = 0.001f,
    val ekfAccelNoise: Float = 0.1f,
    val ekfMagNoise: Float = 0.5f,
    val complementaryTc: Float = 0.18f,
    val kalmanProcessNoise: Float = 0.001f,
    val kalmanMeasurementNoise: Float = 0.1f
)

data class SensorConfig(
    val fusionType: FusionType = FusionType.MADGWICK,
    val fusionParams: FusionParams = FusionParams(),
    val filterType: FilterType = FilterType.NONE,
    val filterTimeConstant: Float = 0.18f,
    val sensorRate: SensorRate = SensorRate.GAME
)
