/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the AlarmListener class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.alarm;

/**
 * @brief Class that signals an Alarm
 */
@SuppressWarnings("ClassWithoutLogger")
public class AlarmEvent {

    public static final int ALARM_DETRIGGERED = 1;
    public static final int ALARM_TRIGGERED = 2;
    public static final int ALARM_VOLT = 1;
    public static final int ALARM_TEMP = 2;
    private final Object source;
    private final long timestamp;
    private final int type;
    private final int status;

    /**
     * @brief constructor
     */
    AlarmEvent(Object source, long timestamp, int type, int status) {
        this.source = source;
        this.timestamp = timestamp;
        this.type = type;
        this.status = status;
    }

    /**
     * @brief Returns the source of the event
     * @return the source of the event
     */
    public Object getSource() {
        return source;
    }

    /**
     * @brief Returns the timestamp of the event
     * @return the timestamp of the event
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @brief Returns the type of the event
     * @return type of the event
     */
    public int getType() {
        return type;
    }

    /**
     * @brief Returns the status of the event
     * @return status of the event
     */
    public int getStatus() {
        return status;
    }
}
