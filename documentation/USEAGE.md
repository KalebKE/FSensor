## Usage
Using FSensor involves two steps:

1. Setting up your filters
2. Feeding sensor data to the filters

## Averaging Filters

```
private BaseFilter filter; 

private void initLpf() {
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

