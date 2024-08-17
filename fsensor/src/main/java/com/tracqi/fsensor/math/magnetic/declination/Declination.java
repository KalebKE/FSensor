package com.tracqi.fsensor.math.magnetic.declination;

import android.hardware.GeomagneticField;
import android.location.Location;

/*
 * Copyright 2024, Tracqi Technology, LLC
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
 * Compensate for magnetic declination based on a location.
 *
 * Created by kaleb on 3/18/18.
 */
public class Declination {

    private GeomagneticField geoMagField;

    /**
     * Default constructor.
     * @param location The location to use for the compensation.
     */
    public Declination(Location location) {
        setGeomagneticField(location);
    }

    /**
     * Compensate the azimuth with the magnetic declination.
     * @param azimuth The azimuth in units of degrees with the range: 0 < range <= 360
     * @return the azimuth compensated for magnetic declination.
     */
    public float compenstateDeclination(float azimuth) {
        return (azimuth + geoMagField.getDeclination()) % 360;
    }

    /**
     * Set the GeomagneticField based on a Location. Note that this should only be called when there has been a significant change in location.
     * @param location The location to use for the compensation.
     */
    public void setGeomagneticField(Location location) {
        geoMagField = new GeomagneticField((float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(), System.currentTimeMillis());
    }
}
