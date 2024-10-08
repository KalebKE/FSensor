package com.tracqi.fsensor.sensor.orientation;

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

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.kalman.KalmanRotation;

import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;

public class KalmanOrientationFSensor extends OrientationFSensor {
    private static final String TAG = KalmanOrientationFSensor.class.getSimpleName();

    public KalmanOrientationFSensor(SensorManager sensorManager) {
        super(sensorManager, new KalmanRotation(sensorManager));
    }

    public KalmanOrientationFSensor(SensorManager sensorManager, ProcessModel processModel, MeasurementModel measurementModel) {
        super(sensorManager, new KalmanRotation(sensorManager, processModel, measurementModel));
    }
}
