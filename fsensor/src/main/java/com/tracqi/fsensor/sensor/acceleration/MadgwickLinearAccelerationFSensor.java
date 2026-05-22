package com.tracqi.fsensor.sensor.acceleration;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.madgwick.MadgwickRotation;

public class MadgwickLinearAccelerationFSensor extends LinearAccelerationFSensor {

    public MadgwickLinearAccelerationFSensor(SensorManager sensorManager) {
        super(sensorManager, new MadgwickRotation(sensorManager));
    }

    public MadgwickLinearAccelerationFSensor(SensorManager sensorManager, float beta) {
        super(sensorManager, new MadgwickRotation(sensorManager, beta));
    }
}
