
## Low Pass Linear Acceleration Sensor

Linear acceleration is defined as *acceleration - gravity*, or as the acceleration of an object compensated for the tilt of the object.

The FSensor Low Pass Linear Acceleration Sensor uses Android's Sensor.TYPE_ACCELERATION. Low Pass Linear Acceleration Sensor uses a low-pass filter to estimate gravity (the tilt of the device) and then subtracts that estimation from the raw acceleration measurements. If the devices orientation is relatively static, this can be a good approach.

The class LowPassLinearAccelerationSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{x, y, z, sensorFrequency}** in units of meters/sec and Hz, respectively. Here is an example of AccelerationSensor implemented in a LiveData object.

```java
public class FSensorExample extends AppCompatActivity {

    private FSensor fSensor;

    private SensorSubject.SensorObserver sensorObserver = new SensorSubject.SensorObserver() {
        @Override
        public void onSensorChanged(float[] values) {
            // Do interesting things here
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        fSensor = new LowPassLinearAccelerationSensor(this);
        fSensor.register(sensorObserver);
        fSensor.start();
    }

    @Override
    public void onPause() {
        fSensor.unregister(sensorObserver);
        fSensor.stop();

        super.onPause();
    }
}
```
