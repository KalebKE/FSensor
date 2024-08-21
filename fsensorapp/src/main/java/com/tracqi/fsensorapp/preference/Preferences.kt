package com.tracqi.fsensorapp.preference

import android.content.Context
import android.content.SharedPreferences
import android.hardware.SensorManager
import androidx.preference.PreferenceManager


object Preferences {
    const val SENSOR_FREQUENCY_KEY: String = "sensor_frequency_preference"

    // Preference keys for smoothing filters
    const val MEAN_FILTER_SMOOTHING_ENABLED_KEY: String = "mean_filter_smoothing_enabled_preference"
    const val MEDIAN_FILTER_SMOOTHING_ENABLED_KEY: String = "median_filter_smoothing_enabled_preference"
    const val LPF_SMOOTHING_ENABLED_KEY: String = "lpf_smoothing_enabled_preference"

    const val MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY: String = "mean_filter_smoothing_time_constant_preference"
    const val MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY: String = "median_filter_smoothing_time_constant_preference"
    const val LPF_SMOOTHING_TIME_CONSTANT_KEY: String = "lpf_smoothing_time_constant_preference"

    // Preference keys for linear acceleration filters
    const val FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY: String = "lpf_linear_accel_enabled_preference"
    const val FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY: String = "complimentary_fusion_enabled_preference"
    const val FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY: String = "kalman_fusion_enabled_preference"

    const val FSENSOR_LPF_LINEAR_ACCEL_TIME_CONSTANT_KEY: String = "lpf_linear_accel_time_constant_preference"
    const val FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_TIME_CONSTANT_KEY: String = "complimentary_fusion_time_constant_preference"

    fun registerPreferenceChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterPreferenceChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun getPrefFSensorLpfLinearAccelerationEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, true)
    }

    fun getPrefFSensorLpfLinearAccelerationTimeConstant(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(FSENSOR_LPF_LINEAR_ACCEL_TIME_CONSTANT_KEY, 0.5f.toString())!!.toFloat()
    }

    fun getPrefFSensorComplimentaryLinearAccelerationEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false)
    }

    fun getPrefFSensorComplimentaryLinearAccelerationTimeConstant(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_TIME_CONSTANT_KEY, 0.5f.toString())!!.toFloat()
    }

    fun getPrefFSensorKalmanLinearAccelerationEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false)
    }

    fun getPrefLpfSmoothingEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(LPF_SMOOTHING_ENABLED_KEY, false)
    }

    fun getPrefLpfSmoothingTimeConstant(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(LPF_SMOOTHING_TIME_CONSTANT_KEY, 0.5f.toString())!!.toFloat()
    }

    fun getPrefMeanFilterSmoothingEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(MEAN_FILTER_SMOOTHING_ENABLED_KEY, false)
    }

    fun getPrefMeanFilterSmoothingTimeConstant(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY, 0.5f.toString())!!.toFloat()
    }

    fun getPrefMedianFilterSmoothingEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(MEDIAN_FILTER_SMOOTHING_ENABLED_KEY, false)
    }

    fun getPrefMedianFilterSmoothingTimeConstant(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY, 0.5f.toString())!!.toFloat()
    }

    fun getSensorFrequencyPrefs(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(SENSOR_FREQUENCY_KEY, SensorManager.SENSOR_DELAY_FASTEST.toString())!!.toInt()
    }
}
