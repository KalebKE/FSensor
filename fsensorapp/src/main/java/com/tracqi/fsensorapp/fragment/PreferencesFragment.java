package com.tracqi.fsensorapp.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.tracqi.fsensorapp.R;
import com.tracqi.fsensorapp.preference.Preferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;


/*
 * AccelerationExplorer
 * Copyright 2018 Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Preferences for the smoothing and linear acceleration filters.
 *
 * @author Kaleb
 */
public class PreferencesFragment extends PreferenceFragmentCompat implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    private static final String tag = PreferencesFragment.class
            .getSimpleName();

    private SwitchPreferenceCompat fSensorLpfLinearAccel;
    private SwitchPreferenceCompat fSensorComplimentaryLinearAccel;
    private SwitchPreferenceCompat fSensorKalmanLinearAccel;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        addPreferencesFromResource(R.xml.preference_filter);

        fSensorComplimentaryLinearAccel =  findPreference(Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY);
        fSensorKalmanLinearAccel =  findPreference(Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY);
        fSensorLpfLinearAccel = findPreference(Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorKalmanLinearAccel.setChecked(false);
                    fSensorComplimentaryLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
            case Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorKalmanLinearAccel.setChecked(false);
                    fSensorLpfLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
            case Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY:
                if (sharedPreferences.getBoolean(key, false)) {
                    fSensorComplimentaryLinearAccel.setChecked(false);
                    fSensorLpfLinearAccel.setChecked(false);

                    Editor edit = sharedPreferences.edit();
                    edit.putBoolean(Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.putBoolean(Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false);
                    edit.apply();
                }
                break;
        }
    }
}
