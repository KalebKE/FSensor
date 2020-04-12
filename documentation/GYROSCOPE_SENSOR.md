
## Gyroscope Sensor

The FSensor Gyroscope Sensor uses Android's Sensor.TYPE_GYROSCOPE. It simply integrates the sensor measurements provided by the device to provide an orientation. The integration itself is done via Quaternions. The sensor can be initialized to any arbitrary orientation via *OrientationGyroscope.setBaseOrientation()*. To initialize the orientation relative to Earth Frame, use *RotationUtil.getOrientationVectorFromAccelerationMagnetic()*. 

The class GyroscopeSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{azimuth, pitch, roll, sensorFrequency}** in units of radians and Hz, respectively. Here is an example of GyroscopeSensor implemented in a LiveData object.

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
        fSensor = new GyroscopeSensor(this);
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
