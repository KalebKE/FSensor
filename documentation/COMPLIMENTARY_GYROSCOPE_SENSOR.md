## Complementary Gyroscope Sensor

Android's Sensor.TYPE_GYROSCOPE documentation states "In practice, the gyroscope noise and offset will introduce some errors which need to be compensated for. This is usually done using the information from other sensors, but is beyond the scope of this document." ComplementaryGyroscopeSensor is an implementation of a sensor fusion backed by a complementary filter. Without compensation a gyroscope will begin to drift over time due to the integration of small errors. The combination of an acceleration and magenetic sensor can provide an estimation of the devices orientation that can be used to compenstate for the gyroscopes drift.

The fusions and integrations are done via Quaternions. The sensor can be initialized to any arbitrary orientation via OrientationFusedComplementary.setBaseOrientation(). To initialize the orientation relative to Earth Frame, use RotationUtil.getOrientationVectorFromAccelerationMagnetic().

The class ComplementaryGyroscopeSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as float[]{pitch, roll, azimuth, sensorFrequency} in units of radians and Hz, respectively. Here is an example of ComplementaryGyroscopeSensor implemented in a LiveData object.

```java
public class ComplimentaryGyroscopeSensorLiveData extends LiveData<float[]> {
    private ComplimentaryGyroscopeSensor sensor;
    private CompositeDisposable compositeDisposable;

    public ComplimentaryGyroscopeSensorLiveData(Context context) {
        this.sensor = new ComplimentaryGyroscopeSensor (context);
    }

    @Override
    protected void onActive() {
        this.compositeDisposable = new CompositeDisposable();
        this.sensor.getPublishSubject().subscribe(new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(float[] values) {
                setValue(values);
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onComplete() {}
        });
        sensor.onStart();
    }

    @Override
    protected void onInactive() {
        this.compositeDisposable.dispose();
        this.sensor.onStop();
    }
}
```

## Orientation Fused Complementary

ComplementaryGyroscopeSensor.java is backed by OrientationFusedComplementary.java. OrientationFusedComplementary is the class that knows how to integrate the measurements from the devices Gyroscope sensor, produce an orientation from the acceleration and magnetric sensors and finally fuse the two orientation estimations together. It is the implementors responsibility to drive OrientationFusedComplementary with sensor data.

```java
public class ComplementaryGyroscopeSensor implements FSensor {
    private static final String TAG = ComplementaryGyroscopeSensor.class.getSimpleName();

    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private boolean hasAcceleration = false;
    private boolean hasMagnetic = false;

    private float[] magnetic = new float[3];
    private float[] acceleration = new float[3];
    private float[] rotation = new float[3];
    private float[] output = new float[4];

    private OrientationFusedComplementary orientationFusionComplimentary;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    public ComplementaryGyroscopeSensor(Context context) {
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
    }

    public void onStop() {
        unregisterSensors();
    }

    public void setSensorFrequency(int sensorFrequency) {
        this.sensorFrequency = sensorFrequency;
    }

    public void setFSensorComplimentaryTimeConstant(float timeConstant) {
        orientationFusionComplimentary.setTimeConstant(timeConstant);
    }

    public void reset() {
        onStop();
        magnetic = new float[3];
        acceleration = new float[3];
        rotation = new float[3];
        output = new float[4];
        hasAcceleration = false;
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
        orientationFusionComplimentary = new OrientationFusedComplementary();
    }

    private void processAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void processMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, this.magnetic.length);
    }

    private void processRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
    }

    private void registerSensors(int sensorDelay) {

        orientationFusionComplimentary.reset();

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
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
                sensorDelay);

    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    private void setValue(float[] value) {
        System.arraycopy(value, 0, output, 0, value.length);
        output[3] = calculateSensorFrequency();
        publishSubject.onNext(output);
    }

    private class SimpleSensorListener implements SensorEventListener {

        private int sensorEventThreshold = 100;
        private int numAccelerationEvents = 0;
        private int numMagneticEvents = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                processAcceleration(event.values);
                if(numAccelerationEvents++ > sensorEventThreshold) {
                    hasAcceleration = true;
                }
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                processMagnetic(event.values);
                if(numMagneticEvents ++ > sensorEventThreshold) {
                    hasMagnetic = true;
                }
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                processRotation(event.values);
                if (!orientationFusionComplimentary.isBaseOrientationSet()) {
                    if (hasAcceleration && hasMagnetic) {
                        orientationFusionComplimentary.setBaseOrientation(RotationUtil.getOrientationVectorFromAccelerationMagnetic(acceleration, magnetic));
                    }
                } else {
                    setValue(orientationFusionComplimentary.calculateFusedOrientation(rotation, event.timestamp, acceleration, magnetic));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
```
