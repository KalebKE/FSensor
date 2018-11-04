
## Low Pass Linear Acceleration Sensor

Linear acceleration is defined as *acceleration - gravity*, or as the acceleration of an object compensated for the tilt of the object.

The FSensor Low Pass Linear Acceleration Sensor uses Android's Sensor.TYPE_ACCELERATION. Low Pass Linear Acceleration Sensor uses a low-pass filter to estimate gravity (the tilt of the device) and then subtracts that estimation from the raw acceleration measurements. If the devices orientation is relatively static, this can be a good approach.

The class LowPassLinearAccelerationSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{x, y, z, sensorFrequency}** in units of meters/sec and Hz, respectively. Here is an example of AccelerationSensor implemented in a LiveData object.

```java
public class LowPassLinearAccelerationSensorLiveData extends LiveData<float[]> {
    private LowPassLinearAccelerationSensor sensor;
    private CompositeDisposable compositeDisposable;
    private Context context;
    private AveragingFilter averagingFilter;

    public LowPassLinearAccelerationSensorLiveData(Context context) {
        this.context = context;
        this.sensor = new LowPassLinearAccelerationSensor(context);
    }

    @Override
    protected void onActive() {
        this.sensor.setFSensorLpfLinearAccelerationTimeConstant(PrefUtils.getPrefFSensorLpfLinearAccelerationTimeConstant(context));
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

LowPassLinearAccelerationSensor.java is backed by LinearAccelerationAveraging.java and LowPassFilter.java. LowPassFilter and LinearAccelerationAveraging know how to estimate gravity and subtract the gravity estimation from the raw acceleration measurements to estimate linear acceleration. Any averaging filter could be used to estimate gravity, but the low-pass filter is among the most efficient.

```java
public class LowPassLinearAccelerationSensor implements FSensor {
    private static final String TAG = LowPassLinearAccelerationSensor.class.getSimpleName();

    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private float[] rawAcceleration = new float[3];
    private float[] acceleration = new float[3];
    private float[] output = new float[4];

    private LinearAcceleration linearAccelerationFilterLpf;

    private LowPassFilter lpfGravity;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    public LowPassLinearAccelerationSensor(Context context) {
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

    public void setFSensorLpfLinearAccelerationTimeConstant(float timeConstant) {
        lpfGravity.setTimeConstant(timeConstant);
    }

    public void reset() {
        onStop();
        acceleration = new float[3];
        output = new float[4];
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

    private float[] invert(float[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = -values[i];
        }

        return values;
    }

    private void initializeFSensorFusions() {
        lpfGravity = new LowPassFilter();
        linearAccelerationFilterLpf = new LinearAccelerationAveraging(lpfGravity);
    }

    private void processRawAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.rawAcceleration, 0, this.rawAcceleration.length);
    }

    private void processAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void registerSensors(int sensorDelay) {

        lpfGravity.reset();

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
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
                lpfGravity.filter(rawAcceleration);
                processAcceleration(linearAccelerationFilterLpf.filter(rawAcceleration));
                setOutput(acceleration);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}

```
