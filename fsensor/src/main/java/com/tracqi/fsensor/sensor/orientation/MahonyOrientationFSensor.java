package com.tracqi.fsensor.sensor.orientation;

import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.fusion.mahony.MahonyRotation;

public class MahonyOrientationFSensor extends OrientationFSensor {

    public MahonyOrientationFSensor(SensorManager sensorManager) {
        super(sensorManager, new MahonyRotation(sensorManager));
    }

    public MahonyOrientationFSensor(SensorManager sensorManager, float kp, float ki) {
        super(sensorManager, new MahonyRotation(sensorManager, kp, ki));
    }
}
