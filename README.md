# FSensor
Android Sensor Filter and Fusion

<img src="/documentation/images/complementary.gif" width="300">

## Introduction
FSensor (FusionSensor) is an Android library that provides linear acceleration and rotation sensors via LowPass, Complementary and Kalman based sensor fusions. The behavior of stock
Android sensor fusions can vary greatly between devices and manufacturers. FSensor provides a set of consistent and reliable sensor fusion implementations that can be used consistently,
across all devices. The FSensor API allows for custom fusion implementations optimized for specific use-cases. FSensor provides averaging filters for smoothing sensor data that
filters can be used to smooth sensor data. FSensor is designed to be easy to use and easy to integrate into your Android project.

* Provides estimations of device rotation with LowPass, Complimentary and Kalman based sensor fusions
* Provides estimations of linear acceleration with Low-Pass, Complimentary and Kalman based sensor fusions
* Provides device/sensor agnostic averaging filters in the of mean, median and low-pass varieties


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

## Orientation Sensor Fusions

FSensor offers two different estimations of rotation implementing both Complementary and Kalman based fusions. The fusions utilize the acceleration, magnetic, and gyroscope sensors
to provide an estimation the devices rotation.

The gyroscope is used to measure the devices rotation, but tends to drift due to round off errors and other factors. To account for drift, the gyroscope must be compensated 
with a second estimation of the devices rotation which typically come from the acceleration and magnetic sensors. The acceleration sensor provides an estimate of pitch and roll 
sensor provides an estimate of the azimuth.

FSensor uses Quaternion based calculations. Quaternions offer an angle-axis solution to rotations which do not suffer from many of the singularies, including gimbal lock, 
that you will find with Euclidean based rotation matrices.

### Complimentary Filter

<img src="/documentation/images/complementary.gif" width="300">

A complementary filter is a frequency domain filter. In its strictest sense, the definition of a complementary filter refers to the use of two or more transfer functions, which
are mathematical complements of one another. Thus, if the data from one sensor is operated on by G(s), then the data from the other sensor is operated on by I-G(s), and the sum of 
the transfer functions is I, the identity matrix. In practice, it looks nearly identical to a low-pass filter, but uses two different sets of sensor measurements to produce what can 
be thought of as a weighted estimation.

A complimentary filter is used to fuse the two rotation estimations (the gyroscope and acceleration/magnetic, respectively) together. It takes the form of 
gyro[0] = alpha * gyro[0] + (1 - alpha) * accel/magnetic[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the time constant is the length of signals the
filter should act on and dt is the sample period (1/frequency) of the sensor.

### Kalman Filter

<img src="/documentation/images/kalman.gif" width="300">

Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a series of measurements observed over time, containing noise (random variations) 
and other inaccuracies, and produces estimates of unknown variables that tend to be more precise than those based on a single measurement alone. More formally, the Kalman filter 
operates recursively on streams of noisy input data to produce a statistically optimal estimate of the underlying system state. Like complimentary filters, Kalman filters 
require inputs from the gyroscope, accelerometer, and magnetometer sensors.

### Low-Pass Filter

<img src="/documentation/images/low_pass.gif" width="300">

A low-pass filter can isolate the gravity component of the acceleration sensor. The gravity component can be used to determine the tilt of the device. The tilt can be used to then
determine the rotation of the device. The advantage to a low-pass filter is that it is computationally efficient, requires only the acceleration sensor, and is easy to implement.
A low-pass filter is not capable of determining the heading of the device and is sensitive to orientation changes. A low-pass filter can be effective in scenarios where the device is
held in a fixed orientation and the azimuth is not required.

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

## Linear Acceleration

Linear acceleration is defined as linearAcceleration = (acceleration - gravity). An acceleration sensor alone is not capable of determining the difference between gravity/tilt.
FSensor provides three different estimations of linear acceleration implementing Low-Pass, Complementary and Kalman based sensor fusions. 

