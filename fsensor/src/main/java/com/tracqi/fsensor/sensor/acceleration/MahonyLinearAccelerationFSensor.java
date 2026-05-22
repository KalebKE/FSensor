package com.tracqi.fsensor.sensor.acceleration;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.mahony.MahonyRotation;

public class MahonyLinearAccelerationFSensor extends LinearAccelerationFSensor {

    public MahonyLinearAccelerationFSensor(SensorManager sensorManager) {
        super(sensorManager, new MahonyRotation(sensorManager));
    }

    public MahonyLinearAccelerationFSensor(SensorManager sensorManager, float kp, float ki) {
        super(sensorManager, new MahonyRotation(sensorManager, kp, ki));
    }
}
