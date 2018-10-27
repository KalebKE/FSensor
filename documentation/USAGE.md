## Usage
Using FSensor involves two steps:

1. Setting up your filters
2. Feeding sensor data to the filters

## Averaging Filters

```java
private BaseFilter filter; 

private void init() {
  filter = new ... // LowPassFilter(), MeanFilter(), MedianFilter();
  filter.setTimeConstant(0.18);
}

@Override
public void onSensorChanged(SensorEvent event) {
    // Could be any of the Android sensors
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
      filteredAcceleration = filter.filter(acceleration);
    } 
}
```

## Averaging Filter Linear Acceleration

```
// Stil a BaseFilter under the hood
private AveragingFilter averagingFilter;
private LinearAcceleration linearAccelerationFilter;

private void init() {
  averagingFilter = new ... // LowPassFilter(), MeanFilter(), MedianFilter();
  // Make the filter "stiff" with a large time constant since gravity is a constant (mostly)
  averagingFilter.setTimeConstant(5);
  
  // Initialize the linear acceleration filter with the averaging filter
  linearAccelerationFilter = new LinearAccelerationAveraging(averagingFilter);
}

@Override
public void onSensorChanged(SensorEvent event) {
    // Could be any of the Android sensors
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
      
      // You need to drive *both* filters. The input and the outputs can be driven at different rates...
      // For instance, once your averaging filter has essentially converged on gravity, it doesn't
      // need anymore inputs...
      averagingFilter.filter(acceleration);
      linearAccelerationFilter.filter(acceleration);
    } 
}
```

## Orientation IMU Sensor Fusions

```
private OrientationFusion orientationFusion;

private void init() {
  orientationFusion = new ...  // OrientationComplimentaryFusion(), new OrientationKalmanFusion();
}

@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
      orientationFusion.setAcceleration(acceleration);
    } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
     // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
      orientationFusion.setMagneticField(this.magnetic);
    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, rotation, 0, event.values.length);
      // Filter the rotation 
      fusedOrientation = orientationFusion.filter(this.rotation);
    }
}

@Override
public void onResume() {
    super.onResume();

    // Required for OrientationKalmanFusion only
    orientationFusion.startFusion();
}

@Override
public void onPause() {
     // Required for OrientationKalmanFusion only
    orientationFusion.stopFusion();

    super.onPause();
}
```


## Orientation IMU Sensor Fusion Linear Acceleration

```
private LinearAcceleration linearAccelerationFilter;
private OrientationFusion orientationFusion;

private void init() {
  orientationFusion = new ...  // OrientationComplimentaryFusion(), new OrientationKalmanFusion();
  linearAccelerationFilter = new LinearAccelerationFusion(orientationFusion);
}

@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
      orientationFusion.setAcceleration(acceleration);
      
      // Apply the orientation to the raw acceleration to estimate linear acceleration
      linearAcceleration = linearAccelerationFilter.filter(acceleration)
    } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
     // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
      orientationFusion.setMagneticField(this.magnetic);
    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, rotation, 0, event.values.length);
      // Filter the rotation 
      orientationFusion.filter(this.rotation);
    }
}

@Override
public void onResume() {
    super.onResume();

    // Required for OrientationKalmanFusion only
    orientationFusion.startFusion();
}

@Override
public void onPause() {
     // Required for OrientationKalmanFusion only
    orientationFusion.stopFusion();

    super.onPause();
}

```

## Magnetic Tilt Compenstation

```
private OrientationFusion orientationFusion;

private void init() {
  orientationFusion = new ...  // OrientationComplimentaryFusion(), new OrientationKalmanFusion();
}

@Override
public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
      orientationFusion.setAcceleration(acceleration);
    } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
     // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
      orientationFusion.setMagneticField(this.magnetic);
      
      // Compensate for tilt. Note: This sensor orientation assumes portrait mode with the device laying flat and the compass       // pointing out of the top of the device. Your milage may vary.
      float[] output = TiltCompensationUtil.compensateTilt(new float[]{magnetic[0], -magnetic[1], magnetic[2]}, new float[]{fusedOrientation[1], fusedOrientation[2], 0});
      // Reorient to the device
      output[1] = -output[1];
      azimuth = AzimuthUtil.getAzimuth(output);
      
    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      // Android reuses events, so you probably want a copy
      System.arraycopy(event.values, 0, rotation, 0, event.values.length);
      // Filter the rotation 
      fusedOrientation = orientationFusion.filter(this.rotation);
    }
}

@Override
public void onResume() {
    super.onResume();

    // Required for OrientationKalmanFusion only
    orientationFusion.startFusion();
}

@Override
public void onPause() {
     // Required for OrientationKalmanFusion only
    orientationFusion.stopFusion();

    super.onPause();
}
```

## OrientationComplimentaryFusion Full Example
```
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LinearAcceleration linearAccelerationFilter;
    private OrientationFusion orientationFusion;

    private float[] acceleration;
    private float[] magnetic;
    private float[] linearAcceleration;
    private float[] rotation;

    private SensorManager sensorManager;
    private Sensor accelerationSensor;
    private Sensor magneticSensor;
    private Sensor gyroscopeSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    private void init() {
        acceleration = new float[3];
        magnetic = new float[3];
        rotation = new float[4];

        orientationFusion = new OrientationComplimentaryFusion();
        linearAccelerationFilter = new LinearAccelerationFusion(orientationFusion);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
            orientationFusion.setAcceleration(acceleration);

            // Apply the orientation to the raw acceleration to estimate linear acceleration
            linearAcceleration = linearAccelerationFilter.filter(acceleration);
        } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
            orientationFusion.setMagneticField(this.magnetic);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, rotation, 0, event.values.length);
            // Filter the rotation
            orientationFusion.filter(this.rotation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
```

## OrientationKalmanFusion Full Example

```
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LinearAcceleration linearAccelerationFilter;
    private OrientationFusion orientationFusion;

    private float[] acceleration;
    private float[] magnetic;
    private float[] linearAcceleration;
    private float[] rotation;

    private SensorManager sensorManager;
    private Sensor accelerationSensor;
    private Sensor magneticSensor;
    private Sensor gyroscopeSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        orientationFusion.startFusion();
    }

    @Override
    public void onPause() {
        orientationFusion.stopFusion();

        sensorManager.unregisterListener(this);
        super.onPause();
    }

    private void init() {
        acceleration = new float[3];
        magnetic = new float[3];
        rotation = new float[4];

        orientationFusion = new OrientationKalmanFusion();
        //orientationFusion = new OrientationComplimentaryFusion();
        linearAccelerationFilter = new LinearAccelerationFusion(orientationFusion);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
            orientationFusion.setAcceleration(acceleration);

            // Apply the orientation to the raw acceleration to estimate linear acceleration
            linearAcceleration = linearAccelerationFilter.filter(acceleration);
        } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
            orientationFusion.setMagneticField(this.magnetic);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, rotation, 0, event.values.length);
            // Filter the rotation
            orientationFusion.filter(this.rotation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
```


