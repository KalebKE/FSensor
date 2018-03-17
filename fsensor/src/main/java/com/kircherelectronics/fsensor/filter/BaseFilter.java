package com.kircherelectronics.fsensor.filter;

/**
 * Created by kaleb on 7/6/17.
 */

public interface BaseFilter {
    float[] filter(float[] values);
    float[] getOutput();
    void setTimeConstant(float timeConstant);
    void reset();
}
