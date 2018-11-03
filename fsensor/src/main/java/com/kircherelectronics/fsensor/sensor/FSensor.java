package com.kircherelectronics.fsensor.sensor;

import io.reactivex.subjects.PublishSubject;

public interface FSensor {
    PublishSubject<float[]> getPublishSubject();
}
