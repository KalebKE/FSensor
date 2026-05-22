package com.tracqi.fsensor.sensor.acceleration;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.ekf.EkfRotation;

public class EkfLinearAccelerationFSensor extends LinearAccelerationFSensor {

    public EkfLinearAccelerationFSensor(SensorManager sensorManager) {
        super(sensorManager, new EkfRotation(sensorManager));
    }

    public EkfLinearAccelerationFSensor(SensorManager sensorManager, double processNoise, double accelNoise, double magNoise) {
        super(sensorManager, new EkfRotation(sensorManager, processNoise, accelNoise, magNoise));
    }
}
