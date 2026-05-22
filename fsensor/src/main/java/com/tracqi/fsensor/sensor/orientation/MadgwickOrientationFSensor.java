package com.tracqi.fsensor.sensor.orientation;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.madgwick.MadgwickRotation;

public class MadgwickOrientationFSensor extends OrientationFSensor {

    public MadgwickOrientationFSensor(SensorManager sensorManager) {
        super(sensorManager, new MadgwickRotation(sensorManager));
    }

    public MadgwickOrientationFSensor(SensorManager sensorManager, float beta) {
        super(sensorManager, new MadgwickRotation(sensorManager, beta));
    }
}
