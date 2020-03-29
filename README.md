# FSensor
Android Sensor Filter and Fusion

![Alt text](http://kircherelectronics.com/wp-content/uploads/2017/12/FSensor.png "FSensor")

## Introduction
FSensor (FusionSensor) is an Android library that (hopefully) removes some/most of the complexity of using Androids orientation sensors (Acceleration, Magnetic and Gyroscope). FSensor expands greatly on the "out-of-the-box" sensor implementations provided by Android allowing you to customize sensor filters and fusions for your specific needs, or just add default filters on what Android already provides. 

* Provides device/sensor agnostic averaging filters in the mean, median and low-pass varieties
* Provides IMU sensor fusion backed estimations of device orientation in the complimentary and Kalman varieties
* Provides estimations of linear acceleration (linear acceleration = acceleration - gravity) in the averaging filter and sensor fusion varieties

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

```
dependencies {
    implementation 'com.github.KalebKE:FSensor:v1.2.3'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.2'
}
```

[![](https://jitpack.io/v/KalebKE/FSensor.svg)](https://jitpack.io/#KalebKE/FSensor)

## Usage

Simple examples of using FSensor can be found here:

* [Rotation Sensor Usage](/documentation/ROTATION_SENSORS.md)
* [Acceleration Sensor Usage](/documentation/ACCELERATION_SENSORS.md)
* [Averaging Filters Usage](/documentation/AVERAGING_FILTERS.md)

Full app usage examples of FSensor can be found here:

* [AccelerationExplorer](https://github.com/KalebKE/AccelerationExplorer)
* [GyroscopeExplorer](https://github.com/KalebKE/GyroscopeExplorer)

## Averaging Filters

FSensor implements three of the most common smoothing filters, low-pass, mean and median filters. The averaging filters can be found in the *.filter.averaging* package. All the filters are user configurable based on the time constant in units of seconds. The larger the time constant is, the smoother the signal will be. However, latency also increases with the time constant. Because the filter coefficient is in the time domain, differences in sensor output frequencies have little effect on the performance of the filter. These filters should perform about the same across all devices regardless of the sensor frequency. FSensor is clever about providing an implementation where the time constant is agnostic to the output frequencies of the devices sensors which vary greatly by model and manufacturer.

### Low-Pass Filter

FSensor implements an IIR single-pole low-pass filter. The coefficient (alpha) can be adjusted based on the sample period of the sensor to produce the desired time constant that the filter will act on. It takes a simple form of output[0] = alpha * output[0] + (1 - alpha) * input[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the time constant is the length of signals the filter should act on and dt is the sample period (1/frequency) of the sensor. Computationally efficient versus a mean or median filter (constant time vs linear time). For more information on low-pass filters, see the [Acceleration Explorer Wiki](https://github.com/KalebKE/AccelerationExplorer/wiki).

### Mean Filter

FSensor implements a mean filter designed to smooth the data points based on a time constant in units of seconds. The mean filter will average the samples that occur over a period defined by the time constant... the number of samples that are averaged is known as the filter window. The approach allows the filter window to be defined over a period of time, instead of a fixed number of samples.

### Median Filter

FSensor uses a median filter designed to smooth the data points based on a time constant in units of seconds. The median filter will take the median of the samples that occur over a period defined by the time constant... the number of samples that are considered is known as the filter window. The approach allows the filter window to be defined over a period of time, instead of a fixed number of samples.

## Orientation Sensor Fusions

FSensor offers two different estimations of rotation using IMU sensor fusions. These filters can be found in the *.filter.fusion* package. One fusion is based on a quaternion backed complimentary filter and the second fusion is based on a quaternion backed Kalman filter. Both fusions use the acceleration sensor, magnetic sensor and gyroscope sensor to provide an estimation the devices orientation relative to world space coordinates.

The gyroscope is used to measure the devices orientation. However, the gyroscope tends to drift due to round off errors and other factors. Most gyroscopes work by measuring very small vibrations in the earth's rotation, which means they really do not like external vibrations. Because of drift and external vibrations, the gyroscope has to be compensated with a second estimation of the devices orientation, which comes from the acceleration sensor and magnetic sensor. The acceleration sensor provides the pitch and roll estimations while the magnetic sensor provides the azimuth.

### Quaternions Complimentary Filter

Quaternions offer an angle-axis solution to rotations which do not suffer from many of the singularies, including gimbal lock, that you will find with rotation matrices. Quaternions can also be scaled and applied to a complimentary filter. The quaternion complimentary filter is probably the most elegant, robust and accurate of the filters, although it can also be the most difficult to implement.

The complementary filter is a frequency domain filter. In its strictest sense, the definition of a complementary filter refers to the use of two or more transfer functions, which are mathematical complements of one another. Thus, if the data from one sensor is operated on by G(s), then the data from the other sensor is operated on by I-G(s), and the sum of the transfer functions is I, the identity matrix. In practice, it looks nearly identical to a low-pass filter, but uses two different sets of sensor measurements to produce what can be thought of as a weighted estimation.

 A complimentary filter is used to fuse the two orientation estimations (the gyroscope and acceleration/magnetic, respectively) together. It takes the form of gyro[0] = alpha * gyro[0] + (1 - alpha) * accel/magnetic[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the time constant is the length of signals the filter should act on and dt is the sample period (1/frequency) of the sensor.
 
 ### Quaternion Kalman Filter
 
Quaternions offer an angle-axis solution to rotations which do not suffer from many of the singularities, including gimbal lock, that you will find with rotation matrices. Quaternions can also be scaled and applied to a complimentary filter. The quaternion complimentary filter is probably the most elegant, robust and accurate of the filters, although it can also be the most difficult to implement.

Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a series of measurements observed over time, containing noise (random variations) and other inaccuracies, and produces estimates of unknown variables that tend to be more precise than those based on a single measurement alone. More formally, the Kalman filter operates recursively on streams of noisy input data to produce a statistically optimal estimate of the underlying system state. Much like complimentary filters, Kalman filters require two sets of estimations, which we have from the gyroscope and acceleration/magnetic senor.

## Linear Acceleration

Acceleration Explorer offers a number of different linear acceleration filters. These filters can be found in the *.linearacceleration* package. Linear acceleration is defined as linearAcceleration = (acceleration - gravity). An acceleration sensor is not capable of determining the difference between gravity/tilt and true linear acceleration. There is one standalone approach, a low-pass filter, and many sensor fusion based approaches. Acceleration Explorer offers implementations of all the common linear acceleration filters as well as the Android API implementation.

### Android Linear Acceleration

Android offers its own implementation of linear acceleration with Sensor.TYPE_LINEAR_ACCELERATION, which is supported by Acceleration Explorer. Most of the time the device must have a gyroscope for this sensor type to be supported. However, some devices implement Sensor.TYPE_LINEAR_ACCELERATION without a gyroscope, presumably with a low-pass filter. Regardless of the underlying impelementation, I have found that Sensor.TYPE_LINEAR_ACCELERATION works well for short periods of linear acceleration, but not for long periods (more than a few seconds).

### Low-Pass Linear Acceleration

The most simple linear acceleration filter is based on a low-pass filter. It has the advantage that no other sensors are required to estimate linear acceleration and it is computationally efficient. A low-pass filter is implemented in such a way that only very long term (low-frequency) signals (i.e, gravity) are allow to pass through. Anything short term (high-frequency) is filtered out. The gravity estimation is then subtracted from the current acceleration sensor measurement, providing an estimation of linear acceleration. The low-pass filter is an IIR single-pole implementation. The coefficient, a (alpha), can be adjusted based on the sample period of the sensor to produce the desired time constant that the filter will act on. It is essentially the same as the Wikipedia LPF. It takes a simple form of gravity[0] = alpha * gravity[0] + (1 - alpha) * acceleration[0]. Alpha is defined as alpha = timeConstant / (timeConstant + dt) where the time constant is the length of signals the filter should act on and dt is the sample period (1/frequency) of the sensor. Linear acceleration can then be calculated as linearAcceleration = (acceleration - gravity). This implementation can work very well assuming the acceleration sensor is mounted in a relativly fixed position and the periods of linear acceleration is relatively short. For more information on low-pass filters, see [here](http://www.kircherelectronics.com/blog/index.php/11-android/sensors/8-low-pass-filter-the-basics) and [here](http://www.kircherelectronics.com/blog/index.php/11-android/sensors/9-low-pass-filter-optimizing-alpha).

### IMU Sensor Fusion Linear Acceleration

Calculating the gravity components of a normalized orientation is trivial, so FSensor can use the IMU orientation fusions to provide an estimation of linear acceleration that is far more customizable than what Android provides alone.

## Sensor Offset Calibration

FSensor contains an algorithm capable of compenstating for hard and soft iron distortions in the magnetic field. This algorithm can be found in the *.util* package. This same algorithm can also be used to correct for static offsets found in acceleration sensors. The algorithm fits points from an ellipsoid to the polynomial expression Ax^2 + By^2 + Cz^2 + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz = 1. The polynomial expression is then solved and the center and radii of the ellipse are determined. 

Under certain conditions this algorithm can calibrate a magnetic sensor in an environment that has hard and soft iron distortions (usually from metal or electronics). These environments are commonly found in vehicles, boats and aircraft.

### Hard Iron Distortions

Hard iron distortions manifest as an offset of center from the point (0,0,0) Once the center of the ellipsoid is known, it can be transposed to the position (0,0,0) correcting for any hard iron distortions or static offsets found on the sensor. 

### Soft Iron Distortions

Soft iron distortions manifest as radii that do not have a normalized unit length of 1 (effectively creating a ellipsoid instead of a sphere). Since the radii of the ellipsoid are known, they can be rescaled to a unit length of 1 providing a sphere with a radii of (1,1,1) centered on the point (0,0,0).

The algorithm can be visualized. The red cloud of dots represent the initial measurements as an ellipsoid with an offset. The green cloud of dots represent the red cloud of dots after being corrected by the algorithm with a center of (0,0,0) and a radii of (1,1,1).

![Alt text](http://kircherelectronics.com/wp-content/uploads/2018/03/fsensor_ellipsoid_fit.png "FSensor")

## Magnetic Tilt Compensation

The trigonometry used to calculate the heading from the magnetic sensors vector depends the vector being measured on a flat cartesian plane. Any tilt in the device will skew the measurements. FSensor provides helper functions to compenstate the tilt from either the acceleration sensor directly (for static cases) or from one of the Orientation Fusions provided by FSensor (in dynamic cases).

Published under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
