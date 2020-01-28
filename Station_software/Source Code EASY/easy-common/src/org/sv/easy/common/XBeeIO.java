/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class and the methods
 * responsible for the XBEE communication
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.io.IOLine;
import com.digi.xbee.api.io.IOMode;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.HexUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.sv.easy.config.NodeConfig;

/**
 * @brief Contains methods for XBEE messaging and xbee I/O control
 */
@SuppressWarnings("ClassWithoutLogger")
public class XBeeIO {

    private final List<RemoteXBeeDevice> end_devices = new ArrayList<RemoteXBeeDevice>();
    private final ExecutorService executor;
    private static final Logger log = Logger.getLogger(XBeeIO.class);
    private final XBeeDevice xbee;

    /**
     * @brief Class constructor
     * @param xbee An XBee device to be controlled
     */
    public XBeeIO(XBeeDevice xbee) {
        this.xbee = xbee;
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * @brief Discovers nearby nodes
     * @exception XBeeException Generic XBee API exception.
     */
    public void nodeDiscovery() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    end_devices.clear();
                    XBeeNetwork NodeNet = xbee.getNetwork();
                    NodeNet.setDiscoveryTimeout(10000);
                    NodeNet.addDiscoveryListener(new XBeeDiscoveryListener());
                    NodeNet.startDiscoveryProcess();
                    System.out.println("\n>> Discovering remote XBee devices...");
                } catch (XBeeException xbe) {
                    System.out.println("Could not explore for nearby nodes: ");
                    log.error("Could not explore for nearby nodes: ", xbe);
                }
            }
        });
    }

    /**
     * @brief Discovers nearby nodes periodically
     * @exception XBeeException Generic XBee API exception.
     */
    public void nodeDiscoveryTask() {
        TimerTask discoveryTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    end_devices.clear();
                    XBeeNetwork NodeNet = xbee.getNetwork();
                    NodeNet.setDiscoveryTimeout(10000);
                    NodeNet.addDiscoveryListener(new XBeeDiscoveryListener());
                    NodeNet.startDiscoveryProcess();
                    System.out.println("\n>> Discovering remote XBee devices...");
                } catch (XBeeException xbe) {
                    System.out.println("Could not explore for nearby nodes: ");
                    log.error("Could not explore for nearby nodes: ", xbe);
                }
            }
        };
        Timer discoveryTimer = new Timer();
        //execute a discovery every one hour
        discoveryTimer.schedule(discoveryTask, 3600000, 10000);
    }

    /**
     * @brief Broadcasts a message
     * @param msg Message to be broadcasted\
     * @param status Enables XBee GPIO control
     * @param nodeConfig configuration file
     * @exception XBeeException Generic XBee API exception.
     */
    public void broadcastMsg(String msg, boolean status, NodeConfig nodeConfig) {
        byte[] brdcst_msg = msg.getBytes();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    XBeeNetwork NodeNet = xbee.getNetwork();
                    for (int i = 0; i < NodeNet.getNumberOfDevices(); i++) {
                        RemoteXBeeDevice remoteDevice = end_devices.get(i);
                        xbee.sendData(remoteDevice, brdcst_msg);
                        System.out.println("Mesage broadcasted successfully");
                    }
                } catch (XBeeException e) {
                    log.error("Error - Could not broadcast XBEE message: ", e);
                }
            }
        });
        if (status) {

            setpinHigh(true, nodeConfig);
        } else {
            setpinHigh(false, nodeConfig);
        }

    }

    /**
     * @brief Sets a pin high on xbee nodes
     * @exception XBeeException Generic XBee API exception.
     * @param cmd command
     * @param nodeConfig configuration file     */
    public void setpinHigh(boolean cmd, NodeConfig nodeConfig) {
        IOLine IOLINE_OUT;
        if (nodeConfig.getXBEEID() == 0) {
            IOLINE_OUT = IOLine.DIO0_AD0; //pin 20 @xbee
        } else if (nodeConfig.getXBEEID() == 1) {
            IOLINE_OUT = IOLine.DIO1_AD1; //pin 20 @xbee
        } else if (nodeConfig.getXBEEID() == 2) {
            IOLINE_OUT = IOLine.DIO2_AD2; //pin 20 @xbee
        } else {
            IOLINE_OUT = IOLine.DIO3_AD3; //pin 20 @xbee
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // Obtain the remote XBee device from the XBee network.
                    XBeeNetwork NodeNet = xbee.getNetwork();
                    for (int i = 0; i < NodeNet.getNumberOfDevices(); i++) {
                        RemoteXBeeDevice remoteDevice = end_devices.get(i);
                        if (remoteDevice == null) {
                            log.error("Couldn't find the remote XBEE device");
                        } else {
                            if (cmd) {
                                remoteDevice.setIOConfiguration(IOLINE_OUT, IOMode.DIGITAL_OUT_HIGH);
                                System.out.println("done");
                            } else {
                                remoteDevice.setIOConfiguration(IOLINE_OUT, IOMode.DIGITAL_OUT_LOW);
                                System.out.println("done");
                            }

                        }
                    }
                } catch (XBeeException e) {
                    log.error("XBEE failed to set pin value: ", e);

                }
            }
        });
    }

    /**
     * @brief Sets a pin low on xbee nodes
     * @exception XBeeException Generic XBee API exception.
     */
    @Deprecated
    public void setpinLow() {
        final IOLine IOLINE_OUT = IOLine.DIO0_AD0;
        RemoteXBeeDevice remoteDevice;
        try {
            // Obtain the remote XBee device from the XBee network.
            XBeeNetwork NodeNet = xbee.getNetwork();
            for (int i = 0; i < NodeNet.getNumberOfDevices(); i++) {
                remoteDevice = end_devices.get(i);
                if (remoteDevice == null) {
                    System.out.println("Couldn't find the remote XBEE device");
                } else {
                    remoteDevice.setIOConfiguration(IOLINE_OUT, IOMode.DIGITAL_OUT_LOW);
                    System.out.println("done");

                }
            }
        } catch (XBeeException xbe) {
            log.error("XBEE failed to set pin value: ", xbe);
        }
    }

    /**
     * @brief Non-blocking way of receiving messages from neighbouring nodes
     */
    public static class MyDataReceiveListener implements IDataReceiveListener {

        private ExecutorService executorR;

        @Override
        /**
         * @brief Called when data is received from a remote node of the
         * network.
         * @param xbeeMessage A received message from some node
         */
        public void dataReceived(XBeeMessage xbeeMessage) {
            executorR = Executors.newSingleThreadExecutor();
            executorR.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.format("From %s >> %s | %s%n", xbeeMessage.getDevice().get64BitAddress(),
                                HexUtils.prettyHexString(HexUtils.byteArrayToHexString(xbeeMessage.getData())),
                                new String(xbeeMessage.getData()));
                    } catch (Exception xbe) {
                        log.error("Could not read message: ", xbe);
                    }
                }
            });
        }
    }

    /**
     * @brief Node discovery listener
     */
    public class XBeeDiscoveryListener implements IDiscoveryListener {

        @Override
        /**
         * @brief Called when a remote node has been discovered network.
         * @param discoveredDevice the discovered node
         */
        public void deviceDiscovered(RemoteXBeeDevice discoveredDevice) {
            end_devices.add(discoveredDevice);
            System.out.format(">> Device discovered: %s%n", discoveredDevice.toString());
        }

        @Override
        /**
         * @brief Error function
         * @param error the error code
         */
        public void discoveryError(String error) {
            log.error(">> There was an error discovering devices: " + error);
        }

        @Override
        /**
         * @brief Displays stats when the discovery of the nodes is finished
         * @param error the error code
         */
        public void discoveryFinished(String error) {
            if (error == null) {
                System.out.println(">> Discovery process finished successfully.");
                System.out.println("There are " + end_devices.size() + " device(s) in the network.");

            } else {
                log.error(">> Discovery process finished due to the following error: " + error);
            }
        }
    }
}
