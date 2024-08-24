package com.tracqi.fsensor.sensor.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.Rotation;
import com.tracqi.fsensor.sensor.BaseFSensor;
import com.tracqi.fsensor.sensor.FSensorEvent;
import com.tracqi.fsensor.sensor.FSensorEventListener;
import com.tracqi.fsensor.sensor.acceleration.ComplementaryLinearAccelerationFSensor;

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

public class OrientationFSensor extends BaseFSensor {
    private static final String TAG = ComplementaryLinearAccelerationFSensor.class.getSimpleName();
    private final SensorEventListener sensorEventListener = new SensorListener();

    public OrientationFSensor(SensorManager sensorManager, Rotation rotation) {
        super(sensorManager, rotation);
    }

    @Override
    protected SensorEventListener getSensorEventListener() {
        return sensorEventListener;
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                System.arraycopy(rotation.getOrientation(), 0, output, 0, event.values.length);

                for (FSensorEventListener sensorEventListener : fSensorEventListeners) {
                    sensorEventListener.onSensorChanged(new FSensorEvent(event.sensor, event.accuracy, event.timestamp, output));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}

