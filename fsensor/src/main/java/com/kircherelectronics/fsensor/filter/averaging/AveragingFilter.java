package com.kircherelectronics.fsensor.filter.averaging;

import com.kircherelectronics.fsensor.filter.BaseFilter;

/**
 * Created by kaleb on 7/6/17.
 */

public abstract class AveragingFilter implements BaseFilter {
    public static float DEFAULT_TIME_CONSTANT = 0.18f;

    protected float timeConstant;
    protected long startTime;
    protected long timestamp;
    protected int count;

    public AveragingFilter() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public AveragingFilter(float timeConstant) {
        this.timeConstant = timeConstant;
        reset();
    }

    public void reset() {
        startTime = 0;
        timestamp = 0;
        count = 0;
    }
}
