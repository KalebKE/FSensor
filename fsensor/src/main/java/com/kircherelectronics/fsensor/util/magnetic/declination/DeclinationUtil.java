package com.kircherelectronics.fsensor.util.magnetic.declination;

import android.hardware.GeomagneticField;
import android.location.Location;

/**
 * Compensate for magnetic declination based on a location.
 *
 * Created by kaleb on 3/18/18.
 */
public class DeclinationUtil {

    private GeomagneticField geoMagField;

    /**
     * Default constructor.
     * @param location The location to use for the compensation.
     */
    public DeclinationUtil(Location location) {
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
