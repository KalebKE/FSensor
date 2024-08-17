package com.tracqi.fsensorapp.preference;

/*
 * Copyright 2024, Tracqi Technology, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

public class Preferences
{
	public final static String SENSOR_FREQUENCY_KEY = "sensor_frequency_preference";

	// Preference keys for smoothing filters
	public static final String MEAN_FILTER_SMOOTHING_ENABLED_KEY = "mean_filter_smoothing_enabled_preference";
	public static final String MEDIAN_FILTER_SMOOTHING_ENABLED_KEY = "median_filter_smoothing_enabled_preference";
	public static final String LPF_SMOOTHING_ENABLED_KEY = "lpf_smoothing_enabled_preference";

	public static final String MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "mean_filter_smoothing_time_constant_preference";
	public static final String MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY = "median_filter_smoothing_time_constant_preference";
	public static final String LPF_SMOOTHING_TIME_CONSTANT_KEY = "lpf_smoothing_time_constant_preference";

	// Preference keys for linear acceleration filters
	public static final String FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY = "lpf_linear_accel_enabled_preference";
	public static final String FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY = "complimentary_fusion_enabled_preference";
	public static final String FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY = "kalman_fusion_enabled_preference";

	public static final String FSENSOR_LPF_LINEAR_ACCEL_TIME_CONSTANT_KEY = "lpf_linear_accel_time_constant_preference";
	public static final String FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_TIME_CONSTANT_KEY = "complimentary_fusion_time_constant_preference";

	public static void registerPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	public static void unregisterPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public static boolean getPrefFSensorLpfLinearAccelerationEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, true);
	}

    public static float getPrefFSensorLpfLinearAccelerationTimeConstant(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Float.parseFloat(prefs.getString(FSENSOR_LPF_LINEAR_ACCEL_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
    }

    public static boolean getPrefFSensorComplimentaryLinearAccelerationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false);
    }

    public static float getPrefFSensorComplimentaryLinearAccelerationTimeConstant(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Float.parseFloat(prefs.getString(FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
    }

    public static boolean getPrefFSensorKalmanLinearAccelerationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false);
    }

    public static boolean getPrefLpfSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(LPF_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefLpfSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(LPF_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static boolean getPrefMeanFilterSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(MEAN_FILTER_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefMeanFilterSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(MEAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static boolean getPrefMedianFilterSmoothingEnabled(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(MEDIAN_FILTER_SMOOTHING_ENABLED_KEY, false);
	}

    public static float getPrefMedianFilterSmoothingTimeConstant(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Float.parseFloat(prefs.getString(MEDIAN_FILTER_SMOOTHING_TIME_CONSTANT_KEY, String.valueOf(0.5f)));
	}

    public static int getSensorFrequencyPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(SENSOR_FREQUENCY_KEY, String.valueOf(SensorManager.SENSOR_DELAY_FASTEST)));
	}
}
