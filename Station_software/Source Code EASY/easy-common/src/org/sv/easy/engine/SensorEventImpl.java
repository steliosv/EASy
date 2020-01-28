/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the SensorEvent Class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.SensorEvent;

@SuppressWarnings({"ClassWithoutLogger", "PackageVisibleField"})
class SensorEventImpl implements SensorEvent {

    private final EasyEngine source;
    private final long timestamp;
    double voltage;
    double temperature;
    double accelerationX;
    double accelerationY;
    double accelerationZ;
    double displacementX;
    double displacementY;
    double displacementZ;
    double velocityX;
    double velocityY;
    double velocityZ;

    /**
     * @brief constructor
     */
    SensorEventImpl(EasyEngine source, long timestamp) {
        this.source = source;
        this.timestamp = timestamp;
    }

    /**
     * @brief Retrieves the source of the event
     * @return source
     */
    @Override
    public EasyEngine getSource() {
        return source;
    }

    /**
     * @brief Retrieves the event's timestamp
     * @return timestamp
     */
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @brief Retrieves the event's voltage value
     * @return voltage
     */
    @Override
    public double getVoltage() {
        return voltage;
    }

    /**
     * @brief Retrieves the event's temperature value
     * @return temperature
     */
    @Override
    public double getTemperature() {
        return temperature;
    }

    /**
     * @brief Retrieves the event's acceleration value on x axis
     * @return accelerationX
     */
    @Override
    public double getAccelerationX() {
        return accelerationX;
    }

    /**
     * @brief Retrieves the event's acceleration value on y axis
     * @return accelerationY
     */
    @Override
    public double getAccelerationY() {
        return accelerationY;
    }

    /**
     * @brief Retrieves the event's acceleration value on z axis
     * @return accelerationZ
     */
    @Override
    public double getAccelerationZ() {
        return accelerationZ;
    }

    /**
     * @brief Retrieves the event's displacement value on x axis
     * @return displacementX
     */
    @Override
    public double getDisplacementX() {
        return displacementX;
    }

    /**
     * @brief Retrieves the event's displacement value on y axis
     * @return displacementY
     */
    @Override
    public double getDisplacementY() {
        return displacementY;
    }

    /**
     * @brief Retrieves the event's displacement value on z axis
     * @return displacementZ
     */
    @Override
    public double getDisplacementZ() {
        return displacementZ;
    }

    /**
     * @brief Retrieves the event's velocity value on x axis
     * @return velocityX
     */
    @Override
    public double getVelocityX() {
        return velocityX;
    }

    /**
     * @brief Retrieves the event's velocity value on y axis
     * @return velocityY
     */
    @Override
    public double getVelocityY() {
        return velocityY;
    }

    /**
     * @brief Retrieves the event's velocity value on z axis
     * @return velocityZ
     */
    @Override
    public double getVelocityZ() {
        return velocityZ;
    }

    @Override
    public String toString() {
        return "SensorEventImpl{" + "source=" + source + ", timestamp=" + timestamp + ", voltage=" + voltage + ", temperature=" + temperature + ", accelerationX=" + accelerationX + ", accelerationY=" + accelerationY + ", accelerationZ=" + accelerationZ + ", displacementX=" + displacementX + ", displacementY=" + displacementY + ", displacementZ=" + displacementZ + ", velocityX=" + velocityX + ", velocityY=" + velocityY + ", velocityZ=" + velocityZ + '}';
    }

}
