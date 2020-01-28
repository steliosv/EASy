/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Class for monitoring the
 * System's voltage and temperature of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.alarm;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import org.sv.easy.api.sampling.SampleConverter;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.spi.sampling.TemperatureConverter;
import org.sv.easy.spi.sampling.VoltageConverter;
import org.sv.easy.engine.api.SensorEventListener;

/**
 * @brief Contains methods for monitoring the Systems temperature and power
 * supply
 */
public class AlarmService implements SensorEventListener {

    private static final Logger LOGGER = Logger.getLogger(AlarmService.class);

    private final SampleConverter voltConverter = new VoltageConverter();
    private final SampleConverter tempConverter = new TemperatureConverter();
    private final CopyOnWriteArrayList<AlarmListener> listeners = new CopyOnWriteArrayList<>();

    private double temperatureThreshold;
    private double voltThreshold;
    private boolean tempTriggered = false;
    private boolean voltTriggered = false;

    /**
     * @brief Retrieves the temperature threshold
     * @return the temperature threshold
     */
    public double getTemperatureThreshold() {
        return temperatureThreshold;
    }

    /**
     * @brief Sets up the temperature threshold
     * @param temperatureThreshold
     */
    public void setTemperatureThreshold(double temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }

    /**
     * @brief Retrieves the voltage threshold
     * @return the voltage threshold
     */
    public double getVoltThreshold() {
        return voltThreshold;
    }

    /**
     * @brief Sets up the voltage threshold
     * @param voltThreshold voltage threshold
     */
    public void setVoltThreshold(double voltThreshold) {
        this.voltThreshold = voltThreshold;
    }

    /**
     * @brief Checks for extreme values
     * @param e the event
     */
    @Override
    public void sensorEvent(SensorEvent e) {
        double voltValue = voltConverter.convert((int) e.getVoltage());
        double tempValue = tempConverter.convert((int) e.getTemperature());

        checkVolt(voltValue);
        checkTemp(tempValue);

    }

    /**
     * @brief Adds a listener
     * @param l the listener
     */
    public void addListener(AlarmListener l) {
        listeners.add(l);
    }

    /**
     * @brief Removes a listener
     * @param l the listener
     */
    public void removeListener(AlarmListener l) {
        listeners.remove(l);
    }

    /**
     * @brief Sends the events
     * @param type type of the event
     * @param status status of the event
     */
    private void sendEvents(int type, int status) {
        AlarmEvent e = new AlarmEvent(this, System.currentTimeMillis(), type, status);
        Iterator<AlarmListener> it = listeners.iterator();
        while (it.hasNext()) {
            AlarmListener l = it.next();
            l.alert(e);
        }
    }

    /**
     * @brief Checks for over temperature and issues the appropriate event
     * @param tempValue temperature value
     */
    private void checkTemp(double tempValue) {
        if (tempTriggered) {
            if (tempValue < temperatureThreshold) {
                tempTriggered = false;
                sendEvents(AlarmEvent.ALARM_TEMP, AlarmEvent.ALARM_DETRIGGERED);
            }
        } else if (tempValue >= temperatureThreshold) {
            tempTriggered = true;
            sendEvents(AlarmEvent.ALARM_TEMP, AlarmEvent.ALARM_TRIGGERED);
        }
    }

    /**
     * @brief Checks for over/under voltage and issues the appropriate event
     * @param voltValue Voltage value
     */
    private void checkVolt(double voltValue) {
        if (voltTriggered) {
            if (voltValue >= voltThreshold) {
                voltTriggered = false;
                sendEvents(AlarmEvent.ALARM_VOLT, AlarmEvent.ALARM_DETRIGGERED);
            }
        } else if (voltValue < voltThreshold) {
            voltTriggered = true;
            sendEvents(AlarmEvent.ALARM_VOLT, AlarmEvent.ALARM_TRIGGERED);

        }
    }
}
