## Usage
Using FSensor involves two steps:

1. Setting up your filters
2. Feeding sensor data to the filters

## Averaging Filters

```
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
  orientationFusion = new ...  //OrientationComplimentaryFusion(), new OrientationKalmanFusion();
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
```
