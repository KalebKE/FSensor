package com.tracqi.fsensorapp.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.tracqi.fsensorapp.R
import com.tracqi.fsensorapp.preference.Preferences

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
class PreferencesFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private var fSensorLpfLinearAccel: SwitchPreferenceCompat? = null
    private var fSensorComplimentaryLinearAccel: SwitchPreferenceCompat? = null
    private var fSensorKalmanLinearAccel: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_filter)

        fSensorComplimentaryLinearAccel = findPreference(Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY)
        fSensorKalmanLinearAccel = findPreference(Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY)
        fSensorLpfLinearAccel = findPreference(Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY)
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String?) {
        when (key) {
            Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY -> if (sharedPreferences.getBoolean(key, false)) {
                fSensorKalmanLinearAccel!!.isChecked = false
                fSensorComplimentaryLinearAccel!!.isChecked = false

                val edit = sharedPreferences.edit()
                edit.putBoolean(Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false)
                edit.putBoolean(Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false)
                edit.apply()
            }

            Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY -> if (sharedPreferences.getBoolean(key, false)) {
                fSensorKalmanLinearAccel!!.isChecked = false
                fSensorLpfLinearAccel!!.isChecked = false

                val edit = sharedPreferences.edit()
                edit.putBoolean(Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY, false)
                edit.putBoolean(Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false)
                edit.apply()
            }

            Preferences.FSENSOR_KALMAN_LINEAR_ACCEL_ENABLED_KEY -> if (sharedPreferences.getBoolean(key, false)) {
                fSensorComplimentaryLinearAccel!!.isChecked = false
                fSensorLpfLinearAccel!!.isChecked = false

                val edit = sharedPreferences.edit()
                edit.putBoolean(Preferences.FSENSOR_COMPLIMENTARY_LINEAR_ACCEL_ENABLED_KEY, false)
                edit.putBoolean(Preferences.FSENSOR_LPF_LINEAR_ACCEL_ENABLED_KEY, false)
                edit.apply()
            }
        }
    }

    companion object {
        private val tag: String = PreferencesFragment::class.java
                .simpleName
    }
}
