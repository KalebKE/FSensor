package com.kircherelectronics.fsensor.util.magnetic;

/*
 * Copyright 2018, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A helper class for
 * Created by kaleb on 3/18/18.
 */
public class AzimuthUtil {

    /**
     * Get the azimuth from a magnetic sensor.
     * @param magnetic The magnetic measurements.
     * @return The azimuth in units of degrees with range: 0 < range <= 360.
     */
    public static float getAzimuth(float[] magnetic) {
        float azimuth = (int) Math.toDegrees(Math.atan2(magnetic[0], magnetic[1]));

        // Adjust the range: 0 < range <= 360 (from: -180 < range <=
        // 180)
        return (azimuth + 360) % 360;
    }
}
