## Kalman Gyroscope Sensor

Android's Sensor.TYPE_GYROSCOPE documentation states "In practice, the gyroscope noise and offset will introduce some errors which need to be compensated for. This is usually done using the information from other sensors, but is beyond the scope of this document." KalmanGyroscopeSensor is an implementation of a sensor fusion backed by a Kalman filter. Without compensation a gyroscope will begin to drift over time due to the integration of small errors. The combination of an acceleration and magenetic sensor can provide an estimation of the devices orientation that can be used to compenstate for the gyroscopes drift.

The fusions and integrations are done via Quaternions. The sensor can be initialized to any arbitrary orientation via OrientationFusedKalman.setBaseOrientation(). To initialize the orientation relative to Earth Frame, use RotationUtil.getOrientationVectorFromAccelerationMagnetic().

The class KalmanGyroscopeSensor.java provides a ready to use implementation. Measurements can be observered via RxJava PublishSubjects. Measurements are provided as **float[]{azimuth, pitch, roll, sensorFrequency}** in units of radians and Hz, respectively. Here is an example of ComplementaryGyroscopeSensor implemented in a LiveData object.

```java
public class KalmanGyroscopeSensorLiveData extends LiveData<float[]> {
    private KalmanGyroscopeSensor sensor;
    private CompositeDisposable compositeDisposable;

    public KalmanGyroscopeSensorLiveData(Context context) {
        this.sensor = new KalmanGyroscopeSensor (context);
    }

    @Override
    protected void onActive() {
        this.compositeDisposable = new CompositeDisposable();
        this.sensor.getPublishSubject().subscribe(new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(float[] values) {
                setValue(values);
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
