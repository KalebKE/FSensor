
## Acceleration Sensor

The FSensor Acceleration Sensor uses Android's Sensor.TYPE_ACCELERATION. 

The class AccelerationSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{x, y, z, sensorFrequency}** in units of meters/sec and Hz, respectively. Here is an example of AccelerationSensor implemented in a LiveData object.

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
        fSensor = new AccelerationSensor(this);
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
