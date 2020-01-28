/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the command line interface of the
 * E.A.Sy. Application
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.cli;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.OperatingMode;
import com.sun.jna.Platform;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sv.easy.common.EasyDweetListener;
import org.sv.easy.common.EasyPlotListener;
import org.sv.easy.common.GPIO;
import org.sv.easy.common.MySQLHelper;
import org.sv.easy.common.SeismicEventListener;
import org.sv.easy.common.XBeeIO;
import org.sv.easy.common.mail.MailAlert;
import org.sv.easy.common.mail.Mailer;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.config.NodeConfigLoader;
import org.sv.easy.engine.EasyEngineImpl;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;
import org.sv.easy.engine.api.GeoEventListener;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.engine.api.SensorEventListener;
import org.sv.easy.engine.api.StaltaEvent;
import org.sv.easy.engine.api.StaltaEventListener;
import org.sv.easy.engine.logging.SerialFileLogger;
import org.sv.easy.fec.Reedsolomon;
import org.sv.easy.gui.Easyplot;
import org.sv.easy.stalta.StaLta;
import org.sv.easy.stalta.StaLtaFactory;

public class EasyMain {

    private static double g_azi;
    private static double g_mw = 0;
    private static double threat;
    private static double staltaValueX;
    private static double staltaValueY;
    private static double staltaValueZ;
    private static double dispValueX;
    private static double dispValueY;
    private static double dispValueZ;
    private static double accValueX;
    private static double accValueY;
    private static double accValueZ;
    private static double pgax = 0;
    private static double pgay = 0;
    private static double pga = 0;
    private static String st;
    private static InputStream input;
    private static XBeeDevice xbee;
    private static XBeeIO xbeeIO;
    private static final Logger log = Logger.getLogger(EasyMain.class);
    private static SerialPort serialPort;
    private static CommPortIdentifier portId;
    private static SerialPortEventListener sl;
    private static SerialPortEvent oEvent;
    private static NodeConfig nodeConfig;
    private static EasyEngine seedListener;
    private static final SerialFileLogger serialLogger = new SerialFileLogger();
    private static final ArrayList<EasyPlotListener> easyPlotListener = new ArrayList<>();
    private final static ArrayList<SeismicEventListener> seismicEventListener = new ArrayList<>();
    private static MySQLHelper sql = new MySQLHelper();
    private static Mailer mailer;
    private static MailAlert mailAlert;
    private static Easyplot proc = new Easyplot();
    private static final EasyDweetListener dweet = new EasyDweetListener();
    private volatile static boolean uvwarn = false;
    private volatile static boolean ovwarn = false;
    private volatile static boolean wdt = false;
    private volatile static boolean gpioEnable = false;
    private static GPIO gpio;
    private static boolean xbeeActv = false;
    private static String inputBuffer = "";

    private static final GeoEventListener tauCPdListener = new GeoEventListener() {

        @Override
        public void taucPdEvent(GeoEvent event) {
            taucPdEvent_received(event);
        }
    };

    private static final SensorEventListener sensorListener = new SensorEventListener() {

        @Override
        public void sensorEvent(SensorEvent e) {
            sensorData(e);
        }
    };

    private static final StaltaEventListener staltaListener = new StaltaEventListener() {

        @Override
        public void staltaEvent(StaltaEvent event) {
            staltaData(event);
        }
    };

    /**
     * @brief Receives an incoming stalta event and issues the appropriate
     * alerts
     * @param event The received event
     */
    public static void staltaData(StaltaEvent event) {
        if (event.getType() == StaltaEvent.EVENT_SAMPLE) {
            staltaValueX = event.getX();
            staltaValueY = event.getY();
            staltaValueZ = event.getZ();
            return;
        }
        if (event.getType() == StaltaEvent.EVENT_TRIGGER) {
            if (event.getAxis() == StaltaEvent.AXIS_X) {
                if (event.isTrigger()) {

                }
            }
            if (event.getAxis() == StaltaEvent.AXIS_Y) {
                if (event.isTrigger()) {

                }
            }
            if (Math.abs(pgax) > Math.abs(pgay)) {
                pga = pgax;
            } else {
                pga = pgay;
            }
            dweet.sendPGA(nodeConfig, pga);
            pga = 0;
            pgax = 0;
            pgay = 0;
            if (event.getAxis() == StaltaEvent.AXIS_Z) {
                if (event.isTrigger()) {
                } else {
                    sql.sqlDetrigger(nodeConfig);
                    sql.setTriggered(false);
                }
            }
        }
    }

    /**
     * @brief Receives an incoming sensor data and issues the appropriate alerts
     * @param SensorEvent The received event
     */
    private static void sensorData(SensorEvent e) {
        dispValueX = e.getDisplacementX();
        dispValueY = e.getDisplacementY();
        dispValueZ = e.getDisplacementZ();
        accValueX = e.getAccelerationX();
        accValueY = e.getAccelerationY();
        accValueZ = e.getAccelerationZ();
        if (Math.abs(accValueX) > pgax) {
            pgax = accValueX;
        }
        if (Math.abs(accValueY) > pgay) {
            pgay = accValueY;
        }

    }

    /**
     * @brief Receives an incoming GeoEvent and issues the appropriate alerts
     * @param event The received event
     */
    private static void taucPdEvent_received(GeoEvent event) {
        g_azi = event.getAzi();
        g_mw = event.getMw();
        DecimalFormat f = new DecimalFormat("###.##");
        DecimalFormat df = new DecimalFormat("0.000E00");
        mailAlert.sendMail(seedListener, nodeConfig, event);
        sql.saveEvent(seedListener, nodeConfig, event);
        if (xbeeActv) {
            if (event.getType() == GeoEvent.TYPE_1) {
                threat = 60;
                xbeeIO.broadcastMsg("Distant large magnitude incoming event!", true, nodeConfig);
            }

            if (event.getType() == GeoEvent.TYPE_2) {
                threat = 30;
                xbeeIO.broadcastMsg("Local small magnittude incoming event !", false, nodeConfig);
            }

            if (event.getType() == GeoEvent.TYPE_3) {
                threat = 90;
                xbeeIO.broadcastMsg("!!! WARNING  Damaging incoming event!!!", true, nodeConfig);
            }

            if (event.getType() == GeoEvent.TYPE_4) {

                threat = 10;
                xbeeIO.broadcastMsg("Notice!!! Not damaging incoming event!!", false, nodeConfig);
            }
        }
        dweet.alert(seedListener, nodeConfig, "1", event.getMw(), threat, event.getAzi(), event);
    }

    /**
     * @brief Serial event for the digitiser module
     */
    private static class SerialListener implements SerialPortEventListener {

        public synchronized void serialEvent(SerialPortEvent oEvent) {
            int ECC_LENGTH = 10;
            int msglen = 200;
            int msppcktlen = 80;
            char decoded_frame[] = new char[msglen];
            char[] encoded_frame = new char[msglen + ECC_LENGTH + 1];
            Reedsolomon rs = new Reedsolomon(msppcktlen, ECC_LENGTH);
            if (oEvent.getEventType()
                    != SerialPortEvent.DATA_AVAILABLE) {
                log.error("serial NO DATA event: " + oEvent.getEventType());
            }

            if (oEvent.getEventType()
                    == SerialPortEvent.DATA_AVAILABLE) {
                //log.warn("serial event data available");
                if (log.isTraceEnabled()) {
                    log.trace("serial event: DATA AVAILABLE");
                }
                try {
                    int available = input.available();
                    for (int i = 0; i < available; i++) {
                        int receivedVal = input.read();//read all incoming characters one by one
                        if (receivedVal != 10) {//if the character is not a new line "\n"
                            inputBuffer += (char) receivedVal;//store the new character into a buffer
                        } else {
                            if (inputBuffer == null) {
                                if (log.isTraceEnabled()) {
                                    log.trace("\tnull");
                                }
                                return;
                            } else if (log.isTraceEnabled()) {
                                log.trace("\t" + inputBuffer);
                            }
                            try {
                                if (nodeConfig.getDigiLog().equals("ON")) {
                                    serialLogger.appendTextToFile(inputBuffer, nodeConfig);
                                }
                                if (nodeConfig.getRSConf().equals("ON")) {
                                    Arrays.fill(encoded_frame, (char) 0);//clear the buffer
                                    Arrays.fill(decoded_frame, (char) 0);//clear the buffer
                                    System.arraycopy(inputBuffer.toCharArray(), 0, encoded_frame, 0, inputBuffer.toCharArray().length);
                                    rs.Decode(encoded_frame, decoded_frame, null, (char) 0);
                                    seedListener.push(new String(decoded_frame));
                                    inputBuffer = "";//clear the buffer
                                } else {
                                    seedListener.push(inputBuffer);
                                    inputBuffer = "";//clear the buffer
                                }
                            } catch (Exception e) {
                                log.warn("received excepion: " + e);
                            }
                        }
                    }

                    //log.warn("buffer_string " + buffer_string);
                } catch (Throwable tr) {
                    log.error("Data parsing error: ", tr);
                    throw new RuntimeException(tr);
                }
            }
        }
    }

    /**
     * @brief executes commangds given to the command line interface of the
     * E.A.Sy. Application
     * @param command the command to be executed
     * @param args the command line arguments
     */
    private static void cmd(String command, String[] args) {
        if ((command.equals("EOWARN"))) {
            System.out.println("\nIt is now safe to turn on power again");
            if (xbeeActv) {
                xbeeIO.broadcastMsg("It is now safe to turn on power again", true, nodeConfig);
            }
            dweet.alert(seedListener, nodeConfig, "0", g_mw, threat, g_azi, null);
        } else if ((command.equals("TESTMAIL"))) {
            System.out.println("Executing mail service test:");
            mailer.sendMail("test");
        } else if ((command.equals("TESTXBEE"))) {
            System.out.println("XBee test");
            System.out.println("Setting pins HIGH: ");
            xbeeIO.setpinHigh(true, nodeConfig);
            System.out.println("Setting pins LOW: ");
            xbeeIO.setpinHigh(false, nodeConfig);
            System.out.println("Broadcast msg: ");
            xbeeIO.broadcastMsg("Notice! this is a 40 byte test message!", false, nodeConfig);
        } else if ((command.equals("TESTIOT"))) {
            System.out.println("IOT test");
            dweet.alert(seedListener, nodeConfig, "0", 2, 0, 0, null);
            {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Easyplot.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }

            dweet.alert(seedListener, nodeConfig, "1", 4, 25, 90, null);
            {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Easyplot.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            dweet.alert(seedListener, nodeConfig, "1", 5, 50, 180, null);
            {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Easyplot.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            dweet.alert(seedListener, nodeConfig, "1", 5.5, 75, 270, null);
            {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Easyplot.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            dweet.alert(seedListener, nodeConfig, "1", 6, 100, 360, null);
            {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(Easyplot.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            dweet.alert(seedListener, nodeConfig, "0", 0, 0, 0, null);
        } else if ((command.equals("STOPR"))) {
            System.out.println("Stopping Ringserver");
            proc.terminateProc();
        } else if ((command.equals("STARTR"))) {
            System.out.println("Starting Ringserver");
            proc.startProc();
        } else if ((command.equals("DELF"))) {
            System.out.println("Deleting files and logs older than 30 days");
            proc.deleteFilesOlder();
        } else if ((command.equals("ABOUT"))) {
            System.out.println("About E.A.Sy application");
            System.out.println("NATIONAL AND KAPODISTRIAN UNIVERSITY OF ATHENS\nSCHOOL OF SCIENCES\nFaculty of Geology and Geoenvironment");
            System.out.println("Developper: Stylianos Voutsinas, steliosvo@teipir.gr");
            System.out.println("Athens, 2015-2017");
        } else if ((command.equals("GPIOEN"))) {
            System.out.println("Enabling GPIO");
            gpioEnable = true;
        } else if ((command.equals("GPIODIS"))) {
            System.out.println("Disabling GPIO");
            gpioEnable = false;
        } else if ((command.equals("EXIT"))) {
            System.out.println();
            proc.terminateProc();
            if (!args[3].equalsIgnoreCase("null")) {
                System.out.println("Closing XBee module");
                xbee.close();
            }
            if (!args[2].equalsIgnoreCase("null")) {
                serialLogger.close();
                System.out.println("Closing Digitiser module");
                serialPort.removeEventListener();
                serialPort.close();
            }
            seedListener.flush();
            File file = new File(System.getProperty("user.dir") + "/EASYd.pid");
            if (file.exists()) {
                file.delete();
            }
            System.out.println("Terminating application.");
            System.exit(0);
        } else if (command.equals("HELP")) {
            System.out.println("EOWARN removes the issued warning from the end devices");
            System.out.println("TESTMAIL sends a test email");
            System.out.println("TESTXBEE sends a test xbee packet");
            System.out.println("TESTIOT tests the IOT service");
            System.out.println("STOPR terminates ringserver");
            System.out.println("STARTR starts ringserver");
            System.out.println("GPIOEN enables GPIOs");
            System.out.println("GPIODIS disables GPIOs");
            System.out.println("DELF Deletes files and logs older than 30days");
            System.out.println("EXIT terminates properly the application");
        } else {
            System.out.println("Invalid option type HELP for more options");
        }
    }

    /**
     * @brief gpio monitor
     */
    private static void gpio() {
        if (Platform.isLinux()) {
            if ((System.getProperty("os.arch").toLowerCase().contains("arm")) && (gpioEnable)) {
                //check for powerok
                if (gpio.digitalRead("75").equals("1")) {
                    uvwarn = false;
                    ovwarn = false;
                }      //check for undervoltage
                if (gpio.digitalRead("91").equals("1") && !uvwarn) {
                    uvwarn = true;
                    //powok=false;
                    System.out.println("Warning!!! undervoltage!!!!");
                }      //check for overvoltage
                if (gpio.digitalRead("191").equals("1") && !ovwarn) {
                    ovwarn = true;
                    //powok=false;
                    System.out.println("Warning!!! overvoltage!!!!");
                }      //reset the watch dog
                if (wdt) {
                    gpio.digitalWrite("24", "1");
                } else {
                    gpio.digitalWrite("24", "0");
                }
                wdt = !wdt;
            }
        }
    }

    /**
     * @brief command line interface of the E.A.Sy. Application
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO code application logic here
        if (args.length != 10) {
            System.out.println("user incorrectly gave: " + args.length + " arguments instead of 10:");
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }
            System.out.println("Proper Usage is: java -cp easy-common.jar org.sv.easy.cli.EasyMain Mode STALTA  EWaxis NSaxis Zaxis SQL_IP_ADDR trig_thres detrig_thres nsta nlta");
            System.out.println("Proper Usage is: java -cp easy-common.jar org.sv.easy.cli.EasyMain Mode STALTA Digitiser_serial XBee_serial Acceleration_src SQL_IP_ADDR trig_thres detrig_thres nsta nlta");
            System.out.println("Supported modes: rec for data records, rt for real-time mode");
            System.out.println("Supported STA/LTA options: 0-2((0)Classic (1)Recursive (2)ZDetect)");
            System.out.println("Acceleration source modes: true -> accelerometer, false -> geophone");
            System.exit(0);
        }
        System.out.println("Welcome to e.a.sy. Earthquake Alert System");
        if (Platform.isMac() || Platform.isLinux()) {
            System.out.println("  ___   ____  _____        _ ");
            System.out.println(" / _  \\/ __ `/ ___/ //    // ");
            System.out.println("/  __ / /_/ (__  ) // ___//  ");
            System.out.println("\\___/ \\__,_ /____/ \\__, //  ");
            System.out.println("                  /____//  ");
        }
        if (Platform.isWindows()) {
            System.out.println("  ___  ____ _____       _ ");
            System.out.println(" / _  \\/ __ ` / ___/ //      // ");
            System.out.println("/  __ / /_/  (__  ) // ___//  ");
            System.out.println("\\___/\\__,_/____/ \\__, //  ");
            System.out.println("                        /____//  ");
        }

        System.out.println("OS Architecture : " + System.getProperty("os.arch"));
        System.out.println("OS Name : " + System.getProperty("os.name"));
        System.out.println("OS Version : " + System.getProperty("os.version"));
        System.out.println("System's Cores : " + Runtime.getRuntime().availableProcessors() + "\n\n");
        Properties props = new Properties();
        try {
            FileInputStream configStream = new FileInputStream(System.getProperty("user.dir") + "/etc/easy/log4j.properties");
            props.load(configStream);
            configStream.close();
        } catch (IOException e) {
            System.out.println("Error could not load log4j.properties file ");
        }
        props.setProperty("log4j.appender.main-appender.File", System.getProperty("user.dir") + "/var/log/system_logs/easyplot_msgs.log");
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(props);

        nodeConfig = NodeConfigLoader.loadFromFile(System.getProperty("user.dir") + "/etc/easy/nodesettings.conf");
        seedListener = new EasyEngineImpl();
        seedListener.setNodeConfig(nodeConfig);
        seedListener.loadFilters();
        StaLta staltaImpl = StaLtaFactory.get(StaLtaFactory.getNames()[Integer.valueOf(args[1])]);
        seedListener.setStaLtaImpl(staltaImpl);
        seedListener.setNsta(Float.parseFloat(args[8]));
        seedListener.setNlta(Integer.parseInt(args[9]));
        seedListener.setStaltaTrigger(Float.parseFloat(args[6]));
        seedListener.setStaltaDeTrigger(Float.parseFloat(args[7]));
        seedListener.setTc(1);
        seedListener.setPd_m(1f / 1000.0f); // mm -> m
        //seedListener.addGeoEventListener(new SwingSensorEventListener());
        seedListener.addListener(sensorListener);
        seedListener.addStaltaListener(staltaListener);
        seedListener.addGeoEventListener(tauCPdListener);
        sql.setHostname(args[5]);
        easyPlotListener.add(dweet);
        easyPlotListener.add(sql);
        seismicEventListener.add(dweet);
        mailer = new Mailer();
        mailer.setSender("email@example.com");
        mailer.setRecipient("email2@example.com");
        mailer.setPassword("123test");
        mailAlert = new MailAlert(mailer);
        int sps = Integer.parseInt(nodeConfig.getSampleFrequency());
//        if ((sps != 100) && (sps != 50) && (sps != 10)) {
//            log.info("Notice! for use with the ADS1256 ,you should use only values equal to: 100, 50 and 10sps");
//        }
        for (EasyPlotListener listener : easyPlotListener) {
            listener.coordinates(nodeConfig);
        }

        if (args[0].equals("rt")) {
            proc.startProc();
            Thread tcpsocThread;
            tcpsocThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String cmd = "";
                    ServerSocket easySocket = null;
                    try {
                        easySocket = new ServerSocket(22212);

                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(EasyMain.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    while (true) {
                        try {
                            Socket connectionSocket = easySocket.accept();
                            BufferedReader cmdFromClient
                                    = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                            cmd = cmdFromClient.readLine();
                            System.out.println("Received: " + cmd);
                            cmd(cmd, args);

                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(EasyMain.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });

            tcpsocThread.start();
            if (!args[3].equalsIgnoreCase("null")) {
                xbee = new XBeeDevice(args[3], 19200);
                xbeeIO = new XBeeIO(xbee);
                try {
                    xbee.open();
                    OperatingMode operatingMode = xbee.getOperatingMode();
                    log.info("operating mode: " + operatingMode);
                    log.info("Xbee protocol: " + xbee.getXBeeProtocol());
                    System.out.println("Xbee monitor is active");
                    xbeeActv = true;
                    xbee.addDataListener(new XBeeIO.MyDataReceiveListener());
                    System.out.println("Waiting for incoming messages...");
                    xbeeIO.nodeDiscovery();
                    xbeeIO.nodeDiscoveryTask();// a scheduler now
                    //scans every one hour for new end devices.
                    System.out.println("XBee module is set.");
                } catch (XBeeException xe) {
                    log.error("Error while opening XBee port: " + xe);
                }
            } else {
                System.out.println("Xbee communications: disabled.");
            }
            if (!args[2].equalsIgnoreCase("null")) {
                try {
                    portId = CommPortIdentifier.getPortIdentifier(args[2]);
                    serialPort = (SerialPort) portId.open("EASY-CLI", 2000);
                    serialPort.setSerialPortParams(115200,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    serialPort.setFlowControlMode(
                            SerialPort.FLOWCONTROL_NONE);
                    input = serialPort.getInputStream();
                    serialPort.addEventListener(new SerialListener());
                    serialPort.notifyOnDataAvailable(true);
                    serialPort.notifyOnOverrunError(true);
                    serialPort.notifyOnParityError(true);
                    serialPort.notifyOnFramingError(true);
                    if (args[4].equalsIgnoreCase("false")) {
                        seedListener.setGeoacc(false); // acceleration data from geophone set true for accelerometer
                    } else {
                        seedListener.setGeoacc(true);
                    }
                    System.out.println("Digitiser module is set.");
                } catch (Exception e) {
                    log.error("Error while opening digitiser port: " + e);
                    e.printStackTrace();
                }
            } else {
                System.out.println("Digitiser communications: disabled.");
            }
        } else {
            System.out.println("Invalid Mode. Type rec for data recordings and rt for realtime mode");
            System.exit(0);
        }

    }

}
