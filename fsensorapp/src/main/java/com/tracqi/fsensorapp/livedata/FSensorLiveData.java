package com.tracqi.fsensorapp.livedata;

import android.content.Context;

import com.tracqi.fsensor.filter.LowPassFilter;
import com.tracqi.fsensor.filter.MeanFilter;
import com.tracqi.fsensor.filter.MedianFilter;
import com.tracqi.fsensor.filter.SensorFilter;
import com.tracqi.fsensor.sensor.FSensor;
import com.tracqi.fsensor.sensor.FSensorEvent;
import com.tracqi.fsensor.sensor.FSensorEventListener;
import com.tracqi.fsensorapp.preference.Preferences;

import androidx.lifecycle.LiveData;



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

public class FSensorLiveData extends LiveData<float[]> {
    private final FSensor fSensor;
    private final SensorFilter filter;
    private final int sensorFrequency;

    private final FSensorEventListener sensorEventListener = new FSensorEventListener() {
        @Override
        public void onSensorChanged(FSensorEvent fSensorEvent) {
            if (filter != null) {
                setValue(filter.filter(fSensorEvent.values()));
            } else {
                setValue(fSensorEvent.values());
            }
        }
    };

    public FSensorLiveData(Context context, FSensor fSensor) {
        this.fSensor = fSensor;
        sensorFrequency = Preferences.getSensorFrequencyPrefs(context);

        if (Preferences.getPrefLpfSmoothingEnabled(context)) {
            filter = new LowPassFilter();
            filter.setTimeConstant(Preferences.getPrefLpfSmoothingTimeConstant(context));
        } else if (Preferences.getPrefMeanFilterSmoothingEnabled(context)) {
            filter = new MeanFilter();
            filter.setTimeConstant(Preferences.getPrefMeanFilterSmoothingTimeConstant(context));
        } else if (Preferences.getPrefMedianFilterSmoothingEnabled(context)) {
            filter = new MedianFilter();
            filter.setTimeConstant(Preferences.getPrefMedianFilterSmoothingTimeConstant(context));
        } else {
            filter = null;
        }
    }

    @Override
    protected void onActive() {
        fSensor.registerListener(sensorEventListener, sensorFrequency);
    }

    @Override
    protected void onInactive() {
        fSensor.unregisterListener(sensorEventListener);
    }
}
