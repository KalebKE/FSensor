package com.tracqi.fsensorapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorManager;

import com.tracqi.fsensor.sensor.acceleration.ComplementaryLinearAccelerationFSensor;
import com.tracqi.fsensor.sensor.acceleration.KalmanLinearAccelerationFSensor;
import com.tracqi.fsensor.sensor.acceleration.LowPassLinearAccelerationFSensor;
import com.tracqi.fsensor.sensor.orientation.ComplementaryOrientationFSensor;
import com.tracqi.fsensor.sensor.orientation.KalmanOrientationFSensor;
import com.tracqi.fsensor.sensor.orientation.LowPassOrientationFSensor;
import com.tracqi.fsensorapp.livedata.FSensorLiveData;
import com.tracqi.fsensorapp.preference.Preferences;

import androidx.lifecycle.AndroidViewModel;



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
 * Created by kaleb on 7/7/17.
 */
public class FSensorViewModel extends AndroidViewModel {

    private final FSensorLiveData lowPassLinearAccelerationSensorLiveData;
    private final FSensorLiveData complimentaryLinearAccelerationSensorLiveData;
    private final FSensorLiveData kalmanLinearAccelerationSensorLiveData;
    private final FSensorLiveData lowPassOrientationSensorLiveData;
    private final FSensorLiveData complimentaryOrientationSensorLiveData;
    private final FSensorLiveData kalmanOrientationSensorLiveData;

    public FSensorViewModel(Application application) {
        super(application);

        float timeConstant = Preferences.getPrefFSensorComplimentaryLinearAccelerationTimeConstant(application);
        SensorManager sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);


        this.lowPassLinearAccelerationSensorLiveData = new FSensorLiveData(application, new LowPassLinearAccelerationFSensor(sensorManager));
        this.complimentaryLinearAccelerationSensorLiveData = new FSensorLiveData(application, new ComplementaryLinearAccelerationFSensor(sensorManager, timeConstant));
        this.kalmanLinearAccelerationSensorLiveData = new FSensorLiveData(application, new KalmanLinearAccelerationFSensor(sensorManager));

        this.lowPassOrientationSensorLiveData = new FSensorLiveData(application, new LowPassOrientationFSensor(sensorManager));
        this.complimentaryOrientationSensorLiveData = new FSensorLiveData(application, new ComplementaryOrientationFSensor(sensorManager));
        this.kalmanOrientationSensorLiveData = new FSensorLiveData(application, new KalmanOrientationFSensor(sensorManager));
    }

    public FSensorLiveData getLowPassLinearAccelerationSensorLiveData() {
        return lowPassLinearAccelerationSensorLiveData;
    }

    public FSensorLiveData getComplimentaryLinearAccelerationSensorLiveData() {
        return complimentaryLinearAccelerationSensorLiveData;
    }

    public FSensorLiveData getKalmanLinearAccelerationSensorLiveData() {
        return kalmanLinearAccelerationSensorLiveData;
    }

    public FSensorLiveData getLowPassOrientationSensorLiveData() {
        return lowPassOrientationSensorLiveData;
    }

    public FSensorLiveData getComplimentaryOrientationSensorLiveData() {
        return complimentaryOrientationSensorLiveData;
    }

    public FSensorLiveData getKalmanOrientationSensorLiveData() {
        return kalmanOrientationSensorLiveData;
    }
}
