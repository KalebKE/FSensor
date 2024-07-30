package com.kircherelectronics.fsensor.orientation;

public interface Orientation {
    void start(int sensorDelay);
    void stop();
    float[] getOrientation();
}
