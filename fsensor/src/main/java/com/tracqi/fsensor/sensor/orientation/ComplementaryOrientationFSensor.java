package com.tracqi.fsensor.sensor.orientation;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.complementary.ComplimentaryRotation;

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

public class ComplementaryOrientationFSensor extends OrientationFSensor {
    private static final String TAG = KalmanOrientationFSensor.class.getSimpleName();

    public ComplementaryOrientationFSensor(SensorManager sensorManager) {
        super(sensorManager, new ComplimentaryRotation(sensorManager));
    }

    public ComplementaryOrientationFSensor(SensorManager sensorManager, float timeConstant) {
        super(sensorManager, new ComplimentaryRotation(sensorManager, timeConstant));
    }
}

