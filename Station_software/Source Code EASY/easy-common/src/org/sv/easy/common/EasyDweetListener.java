/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that is related with the
 * Dweet.io platform
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import com.google.gson.JsonObject;
import io.dweet.DweetIO;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;

/**
 * @brief Contains methods for IOT communication via dweet.io service
 */
@SuppressWarnings({"Convert2Lambda", "BroadCatchBlock", "TooBroadCatch"})
public class EasyDweetListener implements SeismicEventListener, EasyPlotListener {

    private static final Logger LOG = Logger.getLogger(EasyDweetListener.class.getName());
    private final DecimalFormat df = new DecimalFormat("0.000E00");
    private final DecimalFormat f = new DecimalFormat("###.##");
    private final ExecutorService executor;

    public EasyDweetListener() {
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * @brief Updates nodes coordinates to IOT service
     * @details Connects to the specified IOT stream and dweets its JSON object
     * data
     * @param nodeConfig Thing name to connect to
     */
    @Override
    public void coordinates(NodeConfig nodeConfig) {
        final String thingName = "java-client-iot-coordinates-" + nodeConfig.getStationId();
        final JsonObject json = new JsonObject();
        json.addProperty("lat", nodeConfig.getLatitude());
        json.addProperty("lon", nodeConfig.getLongitude());
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DweetIO.publish(thingName, json);
                } catch (IOException ex) {
                    LOG.error(thingName, ex);
                } catch (Throwable ex) {
                    LOG.error(thingName, ex);
                }
            }
        });
    }

    /**
     * @brief Notifies IOT service for an earthquake event
     * @details Connects to the specified IOT stream and dweets its JSON object
     * data
     * @param seedListener listener
     * @param nodeConfig configuration data
     * @param trigger Triggered property value
     * @param mag Magnitude value
     * @param threat Threat level
     * @param azimuth Direction value
     * @param event What caused the event
     */
    @Override
    public void alert(EasyEngine seedListener, NodeConfig nodeConfig, String trigger, double mag,
            double threat, double azimuth, GeoEvent event) {
        final String thingName = "java-client-iot-" + nodeConfig.getStationId();
        final String thingManager = "java-client-easy-iot-manager";
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        //String timestamp = stamp.format(seedListener.getCalendar().getTime());
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String timestamp = stamp.format(calendar.getTime());
        LOG.info("Executing DWEET transmission: " + timestamp);

        final JsonObject json = new JsonObject();
        json.addProperty("trigger", trigger);
        json.addProperty("nodename", nodeConfig.getStationId());
        json.addProperty("mag", String.valueOf(f.format(mag)));
        json.addProperty("threat", String.valueOf(threat));
        json.addProperty("direction", String.valueOf(f.format(azimuth)));
        json.addProperty("lat", nodeConfig.getLatitude());
        json.addProperty("lon", nodeConfig.getLongitude());
        json.addProperty("time", timestamp);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean retdw1, retdw2;
                    retdw1 = DweetIO.publish(thingName, json);
                    retdw2 = DweetIO.publish(thingManager, json);
                    if (retdw1 != true) {
                        LOG.info("Failed to dweet local: " + timestamp);
                    }
                    if (retdw2 != true) {
                        LOG.info("Failed to dweet manager:" + timestamp);
                    }
                } catch (IOException ex) {
                    LOG.error(thingName, ex);
                    LOG.error(thingManager, ex);
                } catch (Throwable ex) {
                    LOG.error(thingName, ex);
                    LOG.error(thingManager, ex);
                }
            }
        });
    }

    /**
     * @brief Notifies IOT service the PGA from the past seismic event
     * @details Connects to the specified IOT stream and dweets its JSON object
     * data
     * @param nodeConfig configuration data
     * @param pga pga value
     */
    public void sendPGA(NodeConfig nodeConfig, double pga) {
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String timestamp = stamp.format(calendar.getTime());
        final String thingManager = "java-client-easy-iot-manager-pga";
        final JsonObject json = new JsonObject();
        json.addProperty("pga", String.valueOf(pga));
        json.addProperty("nodename", nodeConfig.getStationId());
        json.addProperty("lat", nodeConfig.getLatitude());
        json.addProperty("lon", nodeConfig.getLongitude());
        json.addProperty("time", timestamp);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DweetIO.publish(thingManager, json);
                } catch (IOException ex) {
                    LOG.error(thingManager, ex);
                } catch (Throwable ex) {
                    LOG.error(thingManager, ex);
                }
            }
        });

    }

    /**
     * @brief Notifies IOT service for an earthquake event
     * @details Connects to the specified IOT stream and dweets its JSON object
     * data
     * @param nodeConfig configuration data
     * @param trigger Triggered property value
     * @param mag Magnitude value
     * @param msg Message for last recorded event
     * @param threat Threat level
     * @param azimuth Direction value
     * @param event What caused the event
     *
     */
    public void alert(NodeConfig nodeConfig, String trigger, double mag, String msg,
            double threat, double azimuth, GeoEvent event) {
        final String thingName = "java-client-iot-" + nodeConfig.getStationId();
        final JsonObject json = new JsonObject();
        json.addProperty("trigger", trigger);
        json.addProperty("mag", String.valueOf(f.format(mag)) + msg);
        json.addProperty("threat", String.valueOf(threat));
        json.addProperty("direction", String.valueOf(f.format(azimuth)));
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DweetIO.publish(thingName, json);
                } catch (IOException ex) {
                    LOG.error(thingName, ex);
                } catch (Throwable ex) {
                    LOG.error(thingName, ex);
                }
            }
        });

    }
}
