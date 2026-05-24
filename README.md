# FSensor
Android Sensor Filter and Fusion

<img src="/documentation/images/orientation_tab.png" width="300"> <img src="/documentation/images/acceleration_tab.png" width="300"> <img src="/documentation/images/gps_tab.png" width="300">

## Introduction
FSensor (FusionSensor) is an Android library that provides linear acceleration and rotation sensors via sensor fusions including Madgwick, Mahony, EKF, Complementary, Kalman and Low-Pass
implementations. The behavior of stock Android sensor fusions can vary greatly between devices and manufacturers. FSensor provides a set of consistent and reliable sensor fusion
implementations that can be used consistently across all devices. The FSensor API allows for custom fusion implementations optimized for specific use-cases. FSensor also provides
averaging filters for smoothing sensor data, a GPS Kalman filter for position estimation, and coordinate conversion utilities for working with GPS data in local tangent plane coordinates.

* Provides estimations of device rotation with Madgwick, Mahony, EKF, Complementary and Kalman based sensor fusions
* Provides estimations of linear acceleration with Madgwick, Mahony, EKF, Complementary and Kalman based sensor fusions
* Provides device/sensor agnostic averaging filters in the of mean, median and low-pass varieties
* Provides a GPS Kalman filter for smoothing GPS position, velocity and acceleration estimates
* Provides WGS84 coordinate conversions between geodetic (lat/lon/alt), ECEF and local ENU frames


## Get FSensor

In the project level build.gradle:

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

In the module level build.gradle:

```kotlin
implementation("com.github.KalebKE:FSensor:3.x.y")
```

[![](https://jitpack.io/v/KalebKE/FSensor.svg)](https://jitpack.io/#KalebKE/FSensor)

## Usage


The FSensor API is very similar to the Android Sensor API. 

```kotlin
val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
var fSensor: FSensor? = ComplementaryLinearAccelerationFSensor(sensorManager, timeConstant)

val filter: FSensor? = LowPassFilter()

fSensor!!.registerListener({ event -> filter!!.filter(event.values) }, sensorDelay)
```

See the sample app 'fsensorapp' for more examples.

## Orientation

FSensor conforms with the Android API such that the y-axis points north, the x-axis points east, and the z-axis points up. The device is assumed to be in the portrait orientation 
laying flat on a level surface. 

* The orientation vector returned by FSensor will be orientation[]{azimuth, pitch, roll}. 

* The linear acceleration returned by FSensor will be acceleration[]{x, y, z}.

<img src="/documentation/images/axis_device.png" width="300">

## Linear Acceleration

Linear acceleration is defined as linearAcceleration = (acceleration - gravity). An acceleration sensor alone is not capable of determining the difference between gravity/tilt.
FSensor provides estimations of linear acceleration implementing Madgwick, Mahony, EKF, Complementary and Kalman based sensor fusions.

## Orientation Sensor Fusions

FSensor offers several estimations of rotation implementing Madgwick, Mahony, EKF, Complementary and Kalman based fusions. The fusions utilize the acceleration, magnetic, and gyroscope sensors
to provide an estimation the devices rotation.

The gyroscope is used to measure the devices rotation, but tends to drift due to round off errors and other factors. To account for drift, the gyroscope must be compensated 
with a second estimation of the devices rotation which typically come from the acceleration and magnetic sensors. The acceleration sensor provides an estimate of pitch and roll 
sensor provides an estimate of the azimuth.

FSensor uses Quaternion based calculations. Quaternions offer an angle-axis solution to rotations which do not suffer from many of the singularies, including gimbal lock, 
that you will find with Euclidean based rotation matrices.

### Madgwick Filter

The Madgwick filter is an orientation estimation algorithm that fuses data from the gyroscope, accelerometer and magnetometer. It uses a gradient descent optimization to compute
the orientation quaternion, minimizing an objective function that represents the error between the expected and measured direction of gravity and earth's magnetic field.

The algorithm supports both 6-DOF (gyroscope + accelerometer) and 9-DOF (gyroscope + accelerometer + magnetometer) modes. In 6-DOF mode, the filter estimates pitch and roll
from gravity alone. In 9-DOF mode, the magnetometer provides an absolute heading reference by projecting the magnetic field vector into the earth frame to determine the
earth's magnetic flux components.

The filter gain is controlled by a single parameter, beta, which represents the estimated mean zero gyroscope measurement error expressed as the magnitude of a quaternion
derivative. Larger values of beta increase the influence of the accelerometer and magnetometer corrections, improving convergence speed at the cost of increased sensitivity to
vibrations. Smaller values of beta favor the gyroscope, providing smoother output but slower convergence.

### Mahony Filter

The Mahony filter is an orientation estimation algorithm that uses a proportional-integral (PI) controller to fuse gyroscope measurements with accelerometer and magnetometer
corrections. Unlike gradient-based approaches, the Mahony filter computes the rotation error directly as a cross product between the measured and expected direction of gravity
(and optionally the magnetic field), then applies this error as a correction to the gyroscope measurements before quaternion integration.

The filter is controlled by two parameters: Kp (proportional gain) which determines how aggressively the filter corrects for gyroscope drift, and Ki (integral gain) which
accumulates a bias estimate over time to compensate for systematic gyroscope bias. In practice, the Mahony filter is computationally lighter than gradient-based approaches while
providing comparable accuracy for many applications.

### EKF (Extended Kalman Filter)

The Extended Kalman Filter provides an orientation estimation by modeling the quaternion state with a process model driven by gyroscope measurements and a measurement model driven
by accelerometer and magnetometer observations. The EKF linearizes the nonlinear quaternion dynamics around the current state estimate at each time step, computing Jacobians
of the state transition and measurement functions to propagate the state covariance and compute the optimal Kalman gain.

The EKF is controlled by three noise parameters: process noise (modeling gyroscope uncertainty), accelerometer measurement noise, and magnetometer measurement noise. The process
noise determines how much the filter trusts the gyroscope versus the correction sensors. Increasing the process noise causes the filter to rely more heavily on accelerometer and
magnetometer corrections, while decreasing it favors the gyroscope. The EKF provides statistically optimal estimates when the noise parameters are well-tuned, but is
computationally more expensive than the Complementary, Madgwick and Mahony approaches.

### Complementary Filter

A complementary filter is a frequency domain filter. In its strictest sense, the definition of a complementary filter refers to the use of two or more transfer functions, which
are mathematical complements of one another. Thus, if the data from one sensor is operated on by G(s), then the data from the other sensor is operated on by I-G(s), and the sum of 
the transfer functions is I, the identity matrix. In practice, it looks nearly identical to a low-pass filter, but uses two different sets of sensor measurements to produce what can 
be thought of as a weighted estimation.

A complimentary filter is used to fuse the two rotation estimations (the gyroscope and acceleration/magnetic, respectively) together. It takes the form of 
gyro[0] = alpha * gyro[0] + (1 - alpha) * accel/magnetic[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the time constant is the length of signals the
filter should act on and dt is the sample period (1/frequency) of the sensor.

### Kalman Filter

Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a series of measurements observed over time, containing noise (random variations) 
and other inaccuracies, and produces estimates of unknown variables that tend to be more precise than those based on a single measurement alone. More formally, the Kalman filter 
operates recursively on streams of noisy input data to produce a statistically optimal estimate of the underlying system state. Like complimentary filters, Kalman filters 
require inputs from the gyroscope, accelerometer, and magnetometer sensors.

### Low-Pass Filter

A low-pass filter can isolate the gravity component of the acceleration sensor. The gravity component can be used to determine the tilt of the device. The tilt can be used to then
determine the rotation of the device. The advantage to a low-pass filter is that it is computationally efficient, requires only the acceleration sensor, and is easy to implement.
A low-pass filter is not capable of determining the heading of the device and is sensitive to orientation changes. A low-pass filter can be effective in scenarios where the device is
held in a fixed orientation and the azimuth is not required.

## GPS Kalman Filter

FSensor provides an 8-state linear Kalman filter for smoothing GPS position estimates. The filter maintains a state vector of
[east, north, velocity_east, velocity_north, accel_east, accel_north, speed, longitudinal_accel] and fuses GPS position and speed measurements using a standard predict/correct cycle.

The caller provides a heading angle (in radians) that decomposes scalar speed and longitudinal acceleration into east/north components via the state transition matrix.
GPS positions must be provided in East-North-Up (ENU) meters relative to a chosen origin. Use `CoordinateConversion` to convert GPS lat/lon to ENU coordinates.

The filter uses static Q/R noise matrices with configurable measurement noise variances for position, velocity and acceleration. When no GPS fix is available, the measurement
noise for position is inflated to effectively ignore the position measurement while still accepting speed and acceleration updates. The covariance update uses the numerically
stable Joseph form: P = (I-KH)P(I-KH)^T + KRK^T.

```kotlin
val filter = GpsKalmanFilter()

// On each GPS fix, convert lat/lon to ENU and feed the filter
val enu = CoordinateConversion.llaToEnu(lat, lon, alt, originLat, originLon, originAlt)
filter.predict(headingRadians, dt)
filter.correct(enu[0], enu[1], speedMps, longitudinalAccel, hasGpsFix = true)

val state = filter.getState() // GpsState(east, north, velocityEast, velocityNorth, speed, longitudinalAcceleration)
```

## Coordinate Conversion

FSensor provides WGS84 coordinate conversion utilities for transforming between geodetic coordinates (latitude, longitude, altitude), Earth-Centered Earth-Fixed (ECEF) coordinates,
and local East-North-Up (ENU) tangent plane coordinates. These conversions are essential for working with GPS data in a local Cartesian frame suitable for filtering and visualization.

The ENU frame is defined relative to a chosen origin point (typically the first GPS fix). East points along increasing longitude, North points along increasing latitude, and Up
points away from the earth's surface. Distances are in meters.

```kotlin
// Convert a GPS position to local ENU coordinates relative to an origin
val enu = CoordinateConversion.llaToEnu(lat, lon, alt, originLat, originLon, originAlt)
val eastMeters = enu[0]
val northMeters = enu[1]
val upMeters = enu[2]

// Convert back to geodetic coordinates
val lla = CoordinateConversion.enuToLla(east, north, up, originLat, originLon, originAlt)
```

## Filters

FSensor implements three of the most common smoothing filters: low-pass, mean and median filters. All the filters are user configurable based on the time constant in units of seconds. 
The larger the time constant is, the smoother the signal will be. However, latency also increases with the time constant. Because the filter coefficient is in the time domain,
differences in sensor output frequencies have little effect on the performance of the filter. These filters should perform about the same across all devices regardless of the sensor frequency.
FSensor provides an implementation where the time constant is agnostic to the output frequencies of the devices sensors which vary greatly by model and manufacturer.

### Low-Pass Filter

FSensor implements an IIR single-pole low-pass filter.

A low-pass filter eliminates all frequencies above a cut-off frequency, while all other frequencies pass through unaltered. This makes them ideal for eliminating noise that occurs above a certain frequency.
A first order filter reduces the signal amplitude by half every time the frequency doubles. A second order filter attenuates higher frequencies more steeply resulting in a signal that is one
fourth the original level every time the frequency doubles.

The coefficient (alpha) can be adjusted based on the sample period of the sensor to produce the desired time constant that 
the filter will act on. It takes a simple form of output[0] = alpha * output[0] + (1 - alpha) * input[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the 
time constant is the length of signals the filter should act on and dt is the sample period (1/frequency) of the sensor. Computationally efficient versus a mean or median filter 
(constant time vs linear time).

Sensors have an expected noise  density. The noise density, denoted in units of ug/sqrt(Hz), is defined as the noise per unit of square root bandwidth and can be used to determine the expected noise output
from a sensor. To determine the expected noise output from noise density, we take the equivalent noise bandwidth, B, of the output filter. The equivalent noise bandwidth of a filter
is the -3dB bandwidth multiplied by a coefficient corresponding to the order of the filter. The coefficients are as follows:

B = 1.57 * f-3dB for a 1st order filter
B = 1.11 * f-3dB for a 2nd order
B = 1.05 * f-3dB for a 3rd order filter
B = 1.025 * f-3dB for a 4th order filter

### Mean Filter

FSensor implements a mean filter designed to smooth the data points based on a time constant in units of seconds. The mean filter will average the samples that occur over a 
period defined by the time constant... the number of samples that are averaged is known as the filter window. The approach allows the filter window to be defined over a period of 
time, instead of a fixed number of samples.

### Median Filter

FSensor uses a median filter designed to smooth the data points based on a time constant in units of seconds. The median filter will take the median of the samples that 
occur over a period defined by the time constant... the number of samples that are considered is known as the filter window. The approach allows the filter window to be defined 
over a period of time, instead of a fixed number of samples.

 

