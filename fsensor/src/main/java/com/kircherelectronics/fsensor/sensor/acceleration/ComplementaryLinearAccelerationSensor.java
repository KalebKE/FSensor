package com.kircherelectronics.fsensor.sensor.acceleration;

import android.hardware.SensorManager;
import com.kircherelectronics.fsensor.orientation.fusion.complementary.ComplimentaryOrientation;


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

public class ComplementaryLinearAccelerationSensor extends LinearAccelerationSensor {
    private static final String TAG = KalmanLinearAccelerationSensor.class.getSimpleName();

    public ComplementaryLinearAccelerationSensor(SensorManager sensorManager) {
        super(sensorManager, new ComplimentaryOrientation());
    }
}
