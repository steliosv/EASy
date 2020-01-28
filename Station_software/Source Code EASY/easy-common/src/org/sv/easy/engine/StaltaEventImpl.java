/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the STA/LTA event class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

import org.sv.easy.engine.api.StaltaEvent;

@SuppressWarnings("ClassWithoutLogger")
class StaltaEventImpl implements StaltaEvent {

    public final double X, Y, Z;
    public final boolean trigger;
    public final int type;
    public final long ts;
    public final long duration;
    public final int axis;

    /**
     * @brief Class constructor
     * @param X EW axis
     * @param Y NS axis
     * @param Z Z axis
     */
    StaltaEventImpl(double X, double Y, double Z) {
        this.type = EVENT_SAMPLE;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.ts = 0;
        this.duration = 0;
        this.axis = 0;
        this.trigger = false;
    }

    /**
     * @brief Class constructor
     * @param axis the axis
     * @param ts t samp
     * @param duration the duration 
     * @param trigger trigger state
     */
    StaltaEventImpl(int axis, long ts, long duration, boolean trigger) {
        this.type = EVENT_END_AXIS;
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
        this.ts = ts;
        this.duration = duration;
        this.axis = axis;
        this.trigger = trigger;
    }

    /**
     * @brief Class constructor
     * @param axis the axis
     * @param ts t samp
     * @param duration the duration 
     * @param trigger trigger state
     */
    StaltaEventImpl(int axis, boolean trigger, long ts, long duration) {
        this.type = EVENT_TRIGGER;
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
        this.ts = ts;
        this.duration = duration;
        this.axis = axis;
        this.trigger = trigger;
    }

    /**
     * @brief Retrieves x data
     */
    @Override
    public double getX() {
        return X;
    }

    /**
     * @brief Retrieves y data
     */
    @Override
    public double getY() {
        return Y;
    }

    /**
     * @brief Retrieves z data
     */
    @Override
    public double getZ() {
        return Z;
    }

    /**
     * @brief Retrieves the trigger state
     * @return the trigger state
     */
    @Override
    public boolean isTrigger() {
        return trigger;
    }

    /**
     * @brief Retrieves the type
     * @return type
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * @brief Retrieves ts
     * @return ts
     */
    @Override
    public long getTs() {
        return ts;
    }

    /**
     * @brief Retrieves the evetn duration
     * @return duration
     */
    @Override
    public long getDuration() {
        return duration;
    }

    /**
     * @brief Retrieves the axis
     * @return the axis
     */
    @Override
    public int getAxis() {
        return axis;
    }

    @Override
    public String toString() {
        return "StaltaEventImpl{" + "X=" + X + ", Y=" + Y + ", Z=" + Z + ", trigger=" + trigger + ", type=" + type + ", ts=" + ts + ", duration=" + duration + ", axis=" + axis + '}';
    }

}
