/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the SensorEvent interface
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.api;

public interface StaltaEvent {

    int AXIS_X = 1;
    int AXIS_Y = 2;
    int AXIS_Z = 3;
    int EVENT_END_AXIS = 3;
    int EVENT_SAMPLE = 1;
    int EVENT_TRIGGER = 2;

    int getAxis();

    long getDuration();

    long getTs();

    int getType();

    double getX();

    double getY();

    double getZ();

    boolean isTrigger();

}
