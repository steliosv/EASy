/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that loads map data
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.api.map;

/**
 * @brief Class that assigns nodes to a map
 */
@SuppressWarnings("ClassWithoutLogger")
public class MapNode {

    private final float latitude;
    private final float longitude;
    private final String location;
    private final boolean triggered;

    /**
     * @brief Binds a node to the map
     * @param latitude latitude
     * @param longitude longitude
     * @param location location R/U
     * @param triggered triggered state
     */
    public MapNode(float latitude, float longitude, String location, boolean triggered) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.triggered = triggered;
    }

    /**
     * @brief Returns the Latitude
     * @return The latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * @brief Returns the Longitude
     * @return The longitude
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * @brief Returns the Location
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @brief Returns if it is triggered
     * @return The triggered state
     */
    public boolean isTriggered() {
        return triggered;
    }

}
