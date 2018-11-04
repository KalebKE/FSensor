
## Gyroscope Sensor

The FSensor Gyroscope Sensor uses Android's Sensor.TYPE_GYROSCOPE. It simply integrates the sensor measurements provided by the device to provide an orientation. The integration itself is done via Quaternions. The sensor can be initialized to any arbitrary orientation via *OrientationGyroscope.setBaseOrientation()*. To initialize the orientation relative to Earth Frame, use *RotationUtil.getOrientationVectorFromAccelerationMagnetic()*. 

The class GyroscopeSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{pitch, roll, azimuth, sensorFrequency}** in units of radians and Hz, respectively. Here is an example of GyroscopeSensor implemented in a LiveData object.

```java
public class GyroscopeSensorLiveData extends LiveData<float[]> {
    private GyroscopeSensor sensor;
    private CompositeDisposable compositeDisposable;

    public GyroscopeSensorLiveData(Context context) {
        this.sensor = new GyroscopeSensor(context);
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
        this.sensor.onStart();
    }

    @Override
    protected void onInactive() {
        this.compositeDisposable.dispose();
        this.sensor.onStop();
    }
}
```

## Orientation Gyroscope

GyroscopeSensor.java is backed by OrientationGyroscope.java. OrientationGyroscope is the class that knows how to integrate the measurements from the devices Gyroscope sensor. It is the implementors responsibility to drive OrientationGyroscope with sensor data.

```java
public class GyroscopeSensor implements FSensor {

    private static final String TAG = GyroscopeSensor.class.getSimpleName();

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

    private OrientationGyroscope orientationGyroscope;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    public GyroscopeSensor(Context context) {
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
        orientationGyroscope = new OrientationGyroscope();
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

        orientationGyroscope.reset();

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

        private int sensorEventThreshold = 500;
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
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                processRotation(event.values);

                if (!orientationGyroscope.isBaseOrientationSet()) {
                    if (hasAcceleration && hasMagnetic) {
                        orientationGyroscope.setBaseOrientation(RotationUtil.getOrientationVectorFromAccelerationMagnetic(acceleration, magnetic));
                    }
                } else {
                    setOutput(orientationGyroscope.calculateOrientation(rotation, event.timestamp));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}

```
