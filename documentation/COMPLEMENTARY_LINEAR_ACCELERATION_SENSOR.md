
## Complementary Linear Acceleration Sensor

Linear acceleration is defined as *acceleration - gravity*, or as the acceleration of an object compensated for the tilt of the object.

The FSensor Complementary Linear Acceleration Sensor uses Android's Sensor.TYPE_ACCELERATION. A sensor fusion is used to calculate an estimation of the orientatoin of the device, which is then used to estimate the gravity components of the orientation, which is then subtracted from the raw acceleration measurements to estimate linear acceleration.

Android's Sensor.TYPE_GYROSCOPE documentation states "In practice, the gyroscope noise and offset will introduce some errors which need to be compensated for. This is usually done using the information from other sensors, but is beyond the scope of this document." ComplementaryGyroscopeSensor is an implementation of a sensor fusion backed by a complementary filter. Without compensation a gyroscope will begin to drift over time due to the integration of small errors. The combination of an acceleration and magenetic sensor can provide an estimation of the devices orientation that can be used to compenstate for the gyroscopes drift.

The fusions and integrations are done via Quaternions. The sensor can be initialized to any arbitrary orientation via OrientationFusedComplementary.setBaseOrientation(). To initialize the orientation relative to Earth Frame, use RotationUtil.getOrientationVectorFromAccelerationMagnetic().

The class ComplementaryGyroscopeSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{azimuth, pitch, roll, sensorFrequency}** in units of radians and Hz, respectively.

The class LowPassLinearAccelerationSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{x, y, z, sensorFrequency}** in units of meters/sec and Hz, respectively. Here is an example of AccelerationSensor implemented in a LiveData object.

```java
ublic class FSensorExample extends AppCompatActivity {

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
