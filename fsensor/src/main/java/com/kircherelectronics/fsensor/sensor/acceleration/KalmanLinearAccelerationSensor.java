package com.kircherelectronics.fsensor.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.OrientationFusedKalman;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationFusion;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

import io.reactivex.subjects.PublishSubject;

public class KalmanLinearAccelerationSensor implements FSensor {
    private static final String TAG = KalmanLinearAccelerationSensor.class.getSimpleName();

    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private boolean hasRotation = false;
    private boolean hasMagnetic = false;

    private float[] magnetic = new float[3];
    private float[] rawAcceleration = new float[3];
    private float[] rotation = new float[3];
    private float[] acceleration = new float[3];
    private float[] output = new float[4];

    private LinearAcceleration linearAccelerationFilterKalman;
    private OrientationFusedKalman orientationFusionKalman;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    public KalmanLinearAccelerationSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = new SimpleSensorListener();
        this.publishSubject = PublishSubject.create();
        initializeFSensorFusions();
    }

    @Override
    public PublishSubject<float[]> getPublishSubject() {
        return publishSubject;
    }

    public void onStart() {
        startTime = 0;
        count = 0;
        registerSensors(sensorFrequency);
        orientationFusionKalman.startFusion();
    }

    public void onStop() {
        orientationFusionKalman.stopFusion();
        unregisterSensors();
    }

    public void setSensorFrequency(int sensorFrequency) {
        this.sensorFrequency = sensorFrequency;
    }

    public void reset() {
        onStop();
        magnetic = new float[3];
        acceleration = new float[3];
        rotation = new float[3];
        output = new float[4];
        hasRotation = false;
        hasMagnetic = false;
        onStart();
    }

    private float calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.

        return (count++ / ((timestamp - startTime) / 1000000000.0f));
    }

    private void initializeFSensorFusions() {
        orientationFusionKalman = new OrientationFusedKalman();
        linearAccelerationFilterKalman = new LinearAccelerationFusion(orientationFusionKalman);
    }

    private void processRawAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.rawAcceleration, 0, rawAcceleration.length);
    }

    private void processAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, acceleration.length);
    }

    private void processMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, magnetic.length);
    }

    private void processRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, rotation.length);
    }

    private void registerSensors(int sensorDelay) {

        orientationFusionKalman.reset();

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                sensorDelay);

    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    private void setOutput(float[] value) {
        System.arraycopy(value, 0, output, 0, value.length);
        output[3] = calculateSensorFrequency();
        publishSubject.onNext(output);
    }

    private class SimpleSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                processRawAcceleration(event.values);
                if (!orientationFusionKalman.isBaseOrientationSet()) {
                    if (hasRotation && hasMagnetic) {
                        orientationFusionKalman.setBaseOrientation(RotationUtil.getOrientationVectorFromAccelerationMagnetic(rawAcceleration, magnetic));
                    }
                } else {
                    orientationFusionKalman.calculateFusedOrientation(rotation, event.timestamp, rawAcceleration, magnetic);
                    processAcceleration(linearAccelerationFilterKalman.filter(rawAcceleration));

                    setOutput(acceleration);
                }
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                processMagnetic(event.values);
                hasMagnetic = true;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                processRotation(event.values);
                hasRotation = true;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
