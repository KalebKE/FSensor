
## Complementary Linear Acceleration Sensor

Linear acceleration is defined as *acceleration - gravity*, or as the acceleration of an object compensated for the tilt of the object.

The FSensor Complementary Linear Acceleration Sensor uses Android's Sensor.TYPE_ACCELERATION. A sensor fusion is used to calculate an estimation of the orientatoin of the device, which is then used to estimate the gravity components of the orientation, which is then subtracted from the raw acceleration measurements to estimate linear acceleration.

Android's Sensor.TYPE_GYROSCOPE documentation states "In practice, the gyroscope noise and offset will introduce some errors which need to be compensated for. This is usually done using the information from other sensors, but is beyond the scope of this document." ComplementaryGyroscopeSensor is an implementation of a sensor fusion backed by a complementary filter. Without compensation a gyroscope will begin to drift over time due to the integration of small errors. The combination of an acceleration and magenetic sensor can provide an estimation of the devices orientation that can be used to compenstate for the gyroscopes drift.

The fusions and integrations are done via Quaternions. The sensor can be initialized to any arbitrary orientation via OrientationFusedComplementary.setBaseOrientation(). To initialize the orientation relative to Earth Frame, use RotationUtil.getOrientationVectorFromAccelerationMagnetic().

The class ComplementaryGyroscopeSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{pitch, roll, azimuth, sensorFrequency}** in units of radians and Hz, respectively.

The class LowPassLinearAccelerationSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{x, y, z, sensorFrequency}** in units of meters/sec and Hz, respectively. Here is an example of AccelerationSensor implemented in a LiveData object.

```java
public class ComplimentaryLinearAccelerationSensorLiveData extends LiveData<float[]> {
    private ComplimentaryLinearAccelerationSensor sensor;
    private CompositeDisposable compositeDisposable;
    private Context context;
    private AveragingFilter averagingFilter;

    public ComplimentaryLinearAccelerationSensorLiveData(Context context) {
        this.context = context;
        this.sensor = new ComplimentaryLinearAccelerationSensor(context);
    }

    @Override
    protected void onActive() {
        this.sensor.setFSensorComplimentaryLinearAccelerationTimeConstant(PrefUtils.getPrefFSensorComplimentaryLinearAccelerationTimeConstant(context));
        this.sensor.setSensorFrequency(PrefUtils.getSensorFrequencyPrefs(context));

        if(PrefUtils.getPrefLpfSmoothingEnabled(context)) {
            averagingFilter = new LowPassFilter();
            ((LowPassFilter) averagingFilter).setTimeConstant(PrefUtils.getPrefLpfSmoothingTimeConstant(context));
        } else if(PrefUtils.getPrefMeanFilterSmoothingEnabled(context)) {
            averagingFilter = new MeanFilter();
            ((MeanFilter) averagingFilter).setTimeConstant(PrefUtils.getPrefMeanFilterSmoothingTimeConstant(context));
        } else if(PrefUtils.getPrefMedianFilterSmoothingEnabled(context)) {
            averagingFilter = new MedianFilter();
            ((MedianFilter) averagingFilter).setTimeConstant(PrefUtils.getPrefMedianFilterSmoothingTimeConstant(context));
        } else {
            averagingFilter = null;
        }

        this.compositeDisposable = new CompositeDisposable();
        this.sensor.getPublishSubject().subscribe(new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(float[] values) {
                if(averagingFilter != null) {
                    setValue(averagingFilter.filter(values));
                } else {
                    setValue(values);
                }
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

## Low Pass Linear Acceleration Sensor

ComplementaryGyroscopeSensor.java is backed by OrientationFusedComplementary.java. OrientationFusedComplementary is the class that knows how to integrate the measurements from the devices Gyroscope sensor, produce an orientation from the acceleration and magnetric sensors and finally fuse the two orientation estimations together. It is the implementors responsibility to drive OrientationFusedComplementary with sensor data.

LinearAcceleration.java then backed by OrientationFusedComplementary.java to estimate linear acceleration. OrientationFusedComplementary provides an estimation of the orientation of the device. LinearAcceleration then estimates the gravity components of the orientation and then subtracts it from the raw acceleration measurements to estimate linear acceleration. 

```java
public class ComplementaryLinearAccelerationSensor implements FSensor {
    private static final String TAG = ComplementaryLinearAccelerationSensor.class.getSimpleName();

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

    private LinearAcceleration linearAccelerationFilterComplimentary;
    private OrientationFusedComplementary orientationFusionComplimentary;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    public ComplementaryLinearAccelerationSensor(Context context) {
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

    public void setFSensorComplimentaryLinearAccelerationTimeConstant(float timeConstant) {
        orientationFusionComplimentary.setTimeConstant(timeConstant);
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
        orientationFusionComplimentary = new OrientationFusedComplementary();
        linearAccelerationFilterComplimentary = new LinearAccelerationFusion(orientationFusionComplimentary);
    }

    private void processRawAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.rawAcceleration, 0, this.rawAcceleration.length);
    }

    private void processAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, this.acceleration.length);
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
                if (!orientationFusionComplimentary.isBaseOrientationSet()) {
                    if (hasRotation && hasMagnetic) {
                        orientationFusionComplimentary.setBaseOrientation(RotationUtil.getOrientationVectorFromAccelerationMagnetic(rawAcceleration, magnetic));
                    }
                } else {
                    orientationFusionComplimentary.calculateFusedOrientation(rotation, event.timestamp, rawAcceleration, magnetic);
                    processAcceleration(linearAccelerationFilterComplimentary.filter(rawAcceleration));

                    setOutput(acceleration);
                }
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                processMagnetic(event.values);
                hasMagnetic = true;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                processRotation(event.values);
                hasRotation = true;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}

```
