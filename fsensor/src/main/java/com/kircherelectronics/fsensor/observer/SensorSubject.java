package com.kircherelectronics.fsensor.observer;

import java.util.ArrayList;
import java.util.List;

public class SensorSubject {
    private final List<SensorObserver> observers = new ArrayList<>();

    public interface SensorObserver {
        void onSensorChanged(float[] values);
    }

    public void register(SensorObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregister(SensorObserver observer) {
        observers.remove(observer);
    }

    public void onNext(float[] values) {
        for(int i = 0; i < observers.size(); i++) {
            observers.get(i).onSensorChanged(values);
        }
    }
}
