/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the SensorEvent interface
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.api;

public interface SensorEvent {

    double getAccelerationX();

    double getAccelerationY();

    double getAccelerationZ();

    double getDisplacementX();

    double getDisplacementY();

    double getDisplacementZ();

    EasyEngine getSource();

    double getTemperature();

    long getTimestamp();

    double getVelocityX();

    double getVelocityY();

    double getVelocityZ();

    double getVoltage();

}
