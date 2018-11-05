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
