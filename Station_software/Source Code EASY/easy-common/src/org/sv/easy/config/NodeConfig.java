/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Node configuration class for
 * reading mSEED files
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.config;

import java.util.Properties;

/**
 * @brief This contains the methods that are responsible for retrieving the node
 * configuration
 */
@SuppressWarnings("ClassWithoutLogger")
public class NodeConfig {

    private final Properties props;

    /**
     * @brief Class constructor
     * @param props the properties
     */
    NodeConfig(Properties props) {
        this.props = props;
    }

    /**
     * @brief Retrieves the Channel's name
     * @param channel the Channel
     * @return the given channel
     */
    public String getChannelName(int channel) {
        String property = "Channel_" + channel;
        return props.getProperty(property);
    }

    /**
     * @brief Retrieves the sampling frequency
     * @return SPS
     */
    public String getSampleFrequency() {
        return props.getProperty("Samplerate");
    }

    /**
     * @brief Retrieves the location type
     * @return Location type
     */
    public String getLocation() {
        return props.getProperty("Location");
    }

    /**
     * @brief Retrieves the network's code
     * @return The network's code
     */
    public String getNetworkCode() {
        return props.getProperty("NetworkCode");
    }

    /**
     * @brief Retrieves the station ID
     * @return The station ID
     */
    public String getStationId() {
        return props.getProperty("StationID");
    }

    /**
     * @brief Retrieves the location ID
     * @return The location ID
     */
    public String getLocationId() {
        return props.getProperty("LocationID");
    }

    /**
     * @brief Retrieves the node's latitude
     * @return The node's latitude
     */
    public String getLatitude() {
        return props.getProperty("Latitude");
    }

    /**
     * @brief Retrieves the node's longitude
     * @return The node's longitude
     */
    public String getLongitude() {
        return props.getProperty("Longitude");
    }

    /**
     * @brief Retrieves How many channels need to be sent to the Ringserver
     * @return How many channels need to be sent to the Ringserver
     */
    public String getMSEEDChan() {
        return props.getProperty("Seed");
    }

    /**
     * @brief Retrieves the node's Lpf order
     * @return The node's Lpf order
     */
    public int getLpfOrd() {
        return Integer.parseInt(props.getProperty("LpfOrd"));
    }

    /**
     * @brief Retrieves the node's Hpf order
     * @return The node's Hpf order
     */
    public int getHpfOrd() {
        return Integer.parseInt(props.getProperty("HpfOrd"));
    }

    /**
     * @brief Retrieves the node's Digital filter order
     * @return The node's Digital filter order
     */
    public int getDFOrdH() {
        return Integer.parseInt(props.getProperty("DFOrdH"));
    }

    /**
     * @brief Retrieves the node's Digital filter order
     * @return The node's Digital filter order
     */
    public int getDFOrdL() {
        return Integer.parseInt(props.getProperty("DFOrdL"));
    }

    /**
     * @brief Retrieves the node's Lpf cutoff frequency
     * @return The node's Lpf cutoff frequency
     */
    public float getLpfFc() {
        return Float.valueOf(props.getProperty("LpfFc"));
    }

    /**
     * @brief Retrieves the node's Hpf cutoff frequency fot the Transfer()
     * @return The node's Hpf cutoff frequency fot the Transfer method
     */
    public float getTHpfFc() {
        return Float.valueOf(props.getProperty("THpfFc"));
    }

    /**
     * @brief Retrieves the node's Lpf cutoff frequency fot the Transfer()
     * @return The node's Lpf cutoff frequency fot the Transfer method
     */
    public float getTLpfFc() {
        return Float.valueOf(props.getProperty("TLpfFc"));
    }

    /**
     * @brief Retrieves the node's Hpf cutoff frequency for the Digital filter.
     * @return The node's Hpf cutoff frequency for the Digital filter
     */
    public float getDFFcH() {
        return Float.valueOf(props.getProperty("DFFcH"));
    }

    /**
     * @brief Retrieves the node's Lpf cutoff frequency for the Digital filter.
     * @return The node's Lpf cutoff frequency for the Digital filter
     */
    public float getDFFcL() {
        return Float.valueOf(props.getProperty("DFFcL"));
    }

    /**
     * @brief Retrieves the accelerometer's  cutoff frequency for the Digital filter.
     * @return The node's Lpf cutoff frequency for the Digital filter
     */
    public float getACCFc() {
        return Float.valueOf(props.getProperty("ACCFC"));
    }
    
    /**
     * @brief Retrieves the XBEE ID.
     * @return The node's XBEE ID
     */
    public int getXBEEID() {
        return Integer.valueOf(props.getProperty("XBEEID"));
    }
    
    
    /**
     * @brief Retrieves the measurements variation
     * @return The measurements variation value
     */
    public float getME() {
        return Float.valueOf(props.getProperty("ME"));
    }
        
    /**
     * @brief Retrieves the estimation variation
     * @return The estimation variation value
     */
    public float getEE() {
        return Float.valueOf(props.getProperty("EE"));
    }
        
    /**
     * @brief Retrieves the process noise variation
     * @return The process noise
     */
    public float getQ() {
        return Float.valueOf(props.getProperty("PN"));
    }
    
    /**
     * @brief Retrieves Kalman filter state
     * @return The Kalman filter state
     */
    public String getKF() {
        return props.getProperty("KF");
    }

    /**
     * @brief Retrieves GPIO configuration for the digitiser
     * @return The GPIO enable setting
     */
    public String getGPIOConf() {
        return props.getProperty("GPIO");
    }

    /**
     * @brief Enables Reed Solomon encoding - decoding
     * @return The reed solomon setting
     */
    public String getRSConf() {
        return props.getProperty("RS");
    }

    /**
     * @brief Enables logging on a separate text file located in
     * var/log/digitiser_logs
     * @return The seed to file setting
     */
    public String getDigiLog() {
        return props.getProperty("DIGILOG");
    }

    @Override
    public String toString() {
        return "NodeConfig{" + "props=" + props + '}';
    }

}
