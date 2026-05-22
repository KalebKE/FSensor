package com.tracqi.fsensor.sensor.orientation;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.ekf.EkfRotation;

public class EkfOrientationFSensor extends OrientationFSensor {

    public EkfOrientationFSensor(SensorManager sensorManager) {
        super(sensorManager, new EkfRotation(sensorManager));
    }

    public EkfOrientationFSensor(SensorManager sensorManager, double processNoise, double accelNoise, double magNoise) {
        super(sensorManager, new EkfRotation(sensorManager, processNoise, accelNoise, magNoise));
    }
}
