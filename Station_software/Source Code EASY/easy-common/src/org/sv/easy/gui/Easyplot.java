package org.sv.easy.gui;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.OperatingMode;
import com.sun.jna.Platform;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import controlP5.Knob;
import controlP5.Println;
import controlP5.Textarea;
import controlP5.Textfield;
import controlP5.Textlabel;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.gicentre.utils.multisketch.PopupWindow;
import org.sv.easy.alarm.AlarmEvent;
import org.sv.easy.alarm.AlarmListener;
import org.sv.easy.alarm.AlarmService;
import org.sv.easy.common.EasyDweetListener;
import org.sv.easy.common.EasyPlotListener;
import org.sv.easy.common.GPIO;
import org.sv.easy.common.LoadSeedFile;
import org.sv.easy.common.MySQLHelper;
import org.sv.easy.common.SeismicEventListener;
import org.sv.easy.common.StreamGobbler;
import org.sv.easy.common.Utils;
import org.sv.easy.common.XBeeIO;
import org.sv.easy.common.XBeeIO.MyDataReceiveListener;
import org.sv.easy.common.mail.MailAlert;
import org.sv.easy.common.mail.Mailer;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.config.NodeConfigLoader;
import org.sv.easy.engine.EasyEngineImpl;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;
import org.sv.easy.engine.api.GeoEventListener;
import org.sv.easy.engine.logging.SerialFileLogger;
import org.sv.easy.fec.Reedsolomon;
import org.sv.easy.stalta.StaLta;
import org.sv.easy.stalta.StaLtaFactory;
import processing.core.PApplet;
import processing.core.PImage;
import processing.serial.Serial;

@SuppressWarnings({"SleepWhileInLoop", "UseOfSystemOutOrSystemErr", "serial", "PackageVisibleField", "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch",
    "ConvertToTryWithResources", "ClassWithoutLogger", "CollectionWithoutInitialCapacity", "PublicInnerClass", "Convert2Lambda"})
public class Easyplot extends PApplet {

    /**
     * @file //<>//
     * @brief E.A.SY. Application. This contains the functions of the main
     * application of Earthquake alert system
     * @author Stelios Voutsinas (stevo)
     * @bug No known bugs.
     */
    private static final Logger log = Logger.getLogger(Easyplot.class);
//color aliases
    final int white = color(255, 255, 255);
    final int black = color(0, 0, 0);
    final int red = color(128, 0, 0);
    final int green = color(0, 200, 0);
    final int yellow = color(255, 255, 0);
    final int veraman = color(51, 255, 204);
    final int orange = color(255, 142, 03);
    private boolean xbeeActv = false;

    private ControlP5 cp5; //GUI
    private Serial COMPort; //Serial data bus (USB line to Launchpad)
    private Textarea myTextarea;
    private Println console;
    private PopupWindow mapWindow, aboutWindow;
    /*    private final FFTspect accSpectro = new FFTspect(this);
    private final FFTspect dispSpectro = new FFTspect(this);
    private final FFTspect velSpectro = new FFTspect(this);*/
    private AccCanvas accCanvas;
    private VelCanvas velCanvas;
    private DispCanvas dispCanvas;
    private TempCanvas tempCanvas;
    private VoltCanvas voltCanvas;
    private STALTACanvas staltaCanvas;
    private DropdownList d1, d2, d3, d4, d5, d6, d7, d8;
    private Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b13, b14, b15, b16, b17, b18, b19, b20;
    private Textlabel l1, l2, l3, l4, l5, l6, l7, l8, l9, l16, l19, l20, l30, l31, l32, l33, ln, lazi;
    Textlabel l10;
    Textlabel l11, l12, l13, l14, l15, l17, l18;
    Textlabel l21, l22, l23;
    Textlabel l24, l25, l26;
    Textlabel l27, l28, l29;
    private Knob KnobTRIG, KnobSTA, KnobLTA, KnobDET, KnobTEMP, KnobVOLT, Knobtauc, Knobpd;
    private Textfield ip, recipaddr, sendaddr, sendpasswd;
    private XBeeDevice xbee;
    private PImage img;
    private PImage bg;
    private final SerialFileLogger serialLogger = new SerialFileLogger();
    private GPIO gpio;
    private ExitOn exitproc;
    private XBeeIO xbeeIO;
    private boolean uvwarn = false;
    private boolean ovwarn = false;
    private boolean wdt = false;
    private int gpioActive;
    private int XBeeIndex;
    double g_azi;
    double g_mw = 0;
    double threat;
    private static String inputBuffer = "";
    private final ArrayList<EasyPlotListener> easyPlotListener = new ArrayList<>();
    final ArrayList<SeismicEventListener> seismicEventListener = new ArrayList<>();
    MySQLHelper sql = new MySQLHelper();
    private Mailer mailer;
    private MailAlert mailAlert;
    private AlarmService voltageAlarm = new AlarmService();
    NodeConfig nodeConfig;
    private final EasyDweetListener dweet = new EasyDweetListener();
    private int ECC_LENGTH = 10;
    private int msglen = 200;
    private int msppcktlen = 80;
    char decoded_frame[] = new char[msglen];
    private char[] encoded_frame = new char[msglen + ECC_LENGTH];
    private final GeoEventListener tauCPdListener = new GeoEventListener() {

        @Override
        public void taucPdEvent(GeoEvent event) {
            taucPdEvent_received(event);
        }
    };
    EasyEngine seedListener = new EasyEngineImpl();

    /**
     * @brief Initialization of the application's forms and settings of the
     * application's settings
     *
     */
    @Override
    public void setup() {
        //first we need logging
        initLogging();
        log.info("E.A.Sy started logging successfully");
        //load configuration
        loadConfig();
        int sps = Integer.parseInt(nodeConfig.getSampleFrequency());

        //show splash
        showSplash();

        //init GUI
        frame.setTitle("EASYplot");
        size(1250, 700);
        try {
            byte[] imageData = IOUtils.toByteArray(getClass().getResource("resources/easylogosm.png"));
            Image awtImage = Toolkit.getDefaultToolkit().createImage(imageData);
            img = loadImage(System.getProperty("user.dir") + "/data/Media/easylogosm.png");
            imageData = IOUtils.toByteArray(getClass().getResource("resources/bg.png"));
            awtImage = Toolkit.getDefaultToolkit().createImage(imageData);
            bg = loadImageMT(awtImage);
        } catch (IOException e) {
        }
        //add custom app logo
        ImageIcon titlebaricon = new ImageIcon(getClass().getResource("resources/easylogosm.png"));
        frame.setIconImage(titlebaricon.getImage());
        frame.setResizable(false);
        cp5 = new ControlP5(this);
        cp5.enableShortcuts();
        voltCanvas = new VoltCanvas(Integer.parseInt(nodeConfig.getSampleFrequency()));
        tempCanvas = new TempCanvas(Integer.parseInt(nodeConfig.getSampleFrequency()));
        accCanvas = new AccCanvas(Integer.parseInt(nodeConfig.getSampleFrequency()), this);
        velCanvas = new VelCanvas(Integer.parseInt(nodeConfig.getSampleFrequency()), this);
        dispCanvas = new DispCanvas(Integer.parseInt(nodeConfig.getSampleFrequency()), this);
        staltaCanvas = new STALTACanvas(Integer.parseInt(nodeConfig.getSampleFrequency()), this, nodeConfig);

        gpio = new GPIO();
        exitproc = new ExitOn();

        AlarmListener alarmListener = new AlarmListener() {
            @Override
            public void alert(AlarmEvent e) {
                if (e.getStatus() == AlarmEvent.ALARM_TRIGGERED) {
                    if (e.getType() == AlarmEvent.ALARM_VOLT) {
                        log.warn("Warning!! low voltage!!");
                    }
                    if (e.getType() == AlarmEvent.ALARM_TEMP) {
                        log.warn("Warning!! high temperature!!");
                    }
                } else {
                    if (e.getType() == AlarmEvent.ALARM_VOLT) {
                        log.warn("Warning!! normal voltage!!");
                    }
                    if (e.getType() == AlarmEvent.ALARM_TEMP) {
                        log.warn("Warning!! normal temperature!!");
                    }
                }
            }
        };
        voltageAlarm.addListener(alarmListener);
        seedListener.addListener(voltCanvas);
        seedListener.addListener(accCanvas);
        seedListener.addListener(dispCanvas);
        seedListener.addListener(velCanvas);
        seedListener.addListener(tempCanvas);
        seedListener.addListener(staltaCanvas);
        seedListener.addStaltaListener(staltaCanvas);
        seedListener.addListener(voltageAlarm);
        seedListener.addGeoEventListener(tauCPdListener);
        setupGUI();
        KnobTRIG.setValue(4);
        KnobSTA.setValue(1.0f);
        KnobLTA.setValue(60.0f);
        KnobDET.setValue(2f);
        Knobtauc.setValue(1);
        Knobpd.setValue(1f);
        KnobTEMP.setValue(100);
        KnobVOLT.setValue(0);
        loadConf();
        easyPlotListener.add(dweet);
        easyPlotListener.add(sql);
        seismicEventListener.add(dweet);
        mailer = new Mailer();
        mailer.setSender(sendaddr.getText());
        mailer.setRecipient(recipaddr.getText());
        mailer.setPassword(sendpasswd.getText());
        mailAlert = new MailAlert(mailer);
        //load defaults

        recipaddr.setText("email2@example.com");
        sendaddr.setText("email@example.com");
        sendpasswd.setText("123test");

        //notify that the application is up and running
        for (EasyPlotListener listener : easyPlotListener) {
            listener.coordinates(nodeConfig);
        }
    }

    /**
     * @brief Initialisation of the application's forms and settings of the
     * application's forms
     */
    public void setupGUI() {

        cp5.getTab("default")
                .activateEvent(true)
                .setLabel("DataLogger Configuration")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.addTab("Acceleration")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Acceleration")
                .activateEvent(true)
                .addCanvas(accCanvas);

        cp5.addTab("Velocity")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Velocity")
                .activateEvent(true)
                .addCanvas(velCanvas);

        cp5.addTab("Displacement")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Displacement")
                .activateEvent(true)
                .addCanvas(dispCanvas);

        cp5.addTab("Temperature")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Temperature")
                .activateEvent(true)
                .addCanvas(tempCanvas);

        cp5.addTab("Voltage")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Voltage")
                .activateEvent(true)
                .addCanvas(voltCanvas);

        cp5.addTab("Triggering")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Triggering")
                .activateEvent(true)
                .addCanvas(staltaCanvas);

        cp5.addTab("Load SEED file")
                .setColorBackground(color(60))
                .setColorLabel(color(255))
                .setColorActive(color(255, 128));

        cp5.getTab("Load SEED file")
                .activateEvent(true);

        myTextarea = cp5.addTextarea("Console")
                .setPosition(300, 30)
                .setSize(900, 588)
                .setFont(createFont("Courrier", 12))
                .setLineHeight(14)
                .scroll(1)
                .showScrollbar()
                .setColor(color(0, 255, 0))
                .setColorBackground(color(0, 0, 0))
                .setColorForeground(color(0, 0, 0));

        console = cp5.addConsole(myTextarea);
        console.setMax(42);

        d1 = cp5.addDropdownList("Available COMs")
                .setPosition(11, 196)
                .setSize(100, 160);

        d2 = cp5.addDropdownList("TR_Algorithm")
                .setPosition(11, 196)
                .setSize(160, 160)
                .actAsPulldownMenu(true)
                .moveTo("Triggering")
                .setId(7);
        d2.setBackgroundColor(255);

        d3 = cp5.addDropdownList("XBEE")
                .setPosition(140, 196)
                .setSize(100, 160)
                .actAsPulldownMenu(true);

        d5 = cp5.addDropdownList("FFTaccAxis")
                .setPosition(140, 196)
                .setSize(100, 160)
                .actAsPulldownMenu(true)
                .moveTo("Acceleration");

        d6 = cp5.addDropdownList("FFTvelAxis")
                .setPosition(140, 196)
                .setSize(100, 160)
                .actAsPulldownMenu(true)
                .moveTo("Velocity");

        d7 = cp5.addDropdownList("FFTdispAxis")
                .setPosition(140, 196)
                .setSize(100, 160)
                .actAsPulldownMenu(true)
                .moveTo("Displacement");

        d8 = cp5.addDropdownList("MonitorGPIO")
                .setPosition(11, 602)
                .setSize(105, 160)
                .actAsPulldownMenu(true);

        b1 = cp5.addButton("Connect_COM")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 390)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(1)
                .setOn();

        b2 = cp5.addButton("Exit_App")
                .setColorBackground(color(0xff999967))
                .setPosition(135, 585)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(2)
                .setOn();

        b3 = cp5.addButton("Connect_XBEE")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 415)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(3)
                .setOn();

        b4 = cp5.addButton("Show_fft_acc")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 180)
                .setSize(100, 15)
                .moveTo("Acceleration")
                .setSwitch(false)
                .setId(4)
                .setOn();

        b5 = cp5.addButton("Save Settings")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 440)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(5)
                .setOn();

        b6 = cp5.addButton("Stop Ringserver")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 365)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(6)
                .setOn();

        b7 = cp5.addButton("Show map")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 465)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(7)
                .setOn();

        b8 = cp5.addButton("About")
                .setColorBackground(color(0xff999967))
                .setPosition(135, 490)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(8)
                .setOn();

        b9 = cp5.addButton("Show_fft_vel")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 180)
                .setSize(100, 15)
                .moveTo("Velocity")
                .setSwitch(false)
                .setId(9)
                .setOn();

        b10 = cp5.addButton("Show_fft_disp")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 180)
                .setSize(100, 15)
                .moveTo("Displacement")
                .setSwitch(false)
                .setId(10)
                .setOn();

        b11 = cp5.addButton("Alert Withdrawal")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 440)
                .setSize(105, 20)
                .moveTo("Triggering")
                .setSwitch(false)
                .setId(11)
                .setOn();

        b13 = cp5.addButton("Clear old files")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 490)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(13)
                .setOn();

        b14 = cp5.addButton("Select mSEED files")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 196)
                .setSize(105, 20)
                .moveTo("Load SEED file")
                .setSwitch(false)
                .setId(14)
                .setOn();

        b18 = cp5.addButton("Clear Terminal")
                .setColorBackground(color(0xff999967))
                .setPosition(170, 465)
                .setSize(70, 20)
                .setSwitch(false)
                .setId(18)
                .setOn();

        b19 = cp5.addButton("Instuctions")
                .setColorBackground(color(0xff999967))
                .setPosition(190, 440)
                .setSize(50, 20)
                .setSwitch(false)
                .setId(19)
                .setOn();

        b20 = cp5.addButton("Select Accel. Input")
                .setColorBackground(color(0xff999967))
                .setPosition(11, 340)
                .setSize(105, 20)
                .setSwitch(false)
                .setId(20)
                .setOn();

        l1 = cp5.addTextlabel("COM-Status")
                .setText("Disconnected")
                .setPosition(116, 390)
                .setFont(createFont("Calibri", 12))
                .setColor(orange);

        l2 = cp5.addTextlabel("Xbee-Status")
                .setText("Disconnected")
                .setPosition(116, 415)
                .setFont(createFont("Calibri", 12))
                .setColor(orange);

        l3 = cp5.addTextlabel("Nodesettings")
                .setText("not loaded")
                .setPosition(116, 440)
                .setFont(createFont("Calibri", 12))
                .setColor(orange);

        l7 = cp5.addTextlabel("SEED")
                .setText("not loaded")
                .setPosition(116, 196)
                .setFont(createFont("Calibri", 12))
                .moveTo("Load SEED file")
                .setColor(orange);

        l8 = cp5.addTextlabel("MAP")
                .setText("Hidden")
                .setPosition(116, 465)
                .setFont(createFont("Calibri", 12))
                .setColor(orange);

        l9 = cp5.addTextlabel("Seedlink-Status")
                .setText("Stopped")
                .setPosition(116, 365)
                .setFont(createFont("Calibri", 12))
                .setColor(orange);

        l10 = cp5.addTextlabel("trigx")
                .setText("Not-Triggered")
                .setPosition(330, 185)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l11 = cp5.addTextlabel("trigy")
                .setText("Not-Triggered")
                .setPosition(330, 355)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l12 = cp5.addTextlabel("trigz")
                .setText("Not-Triggered")
                .setPosition(330, 525)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l13 = cp5.addTextlabel("tonNS")
                .setText("Event Duration on NS: - s")
                .setPosition(11, 520)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l14 = cp5.addTextlabel("tonEW")
                .setText("Event Duration on EW: - s")
                .setPosition(11, 535)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l15 = cp5.addTextlabel("tonZ")
                .setText("Event Duration on Z: - s")
                .setPosition(11, 550)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l16 = cp5.addTextlabel("MW")
                .setText("Magnitude Estimation: - Mw")
                .setPosition(11, 625)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l17 = cp5.addTextlabel("PSZEW")
                .setText("P-S time diff(Z-EW): - s")
                .setPosition(11, 580)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l18 = cp5.addTextlabel("PSZNS")
                .setText("P-S time diff(Z-NS): - s")
                .setPosition(11, 565)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l19 = cp5.addTextlabel("Tc")
                .setText("Tc: - s")
                .setPosition(11, 595)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        lazi = cp5.addTextlabel("Azi")
                .setText("Azi: - \u00b0N")
                .setPosition(107, 595)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l20 = cp5.addTextlabel("Pd")
                .setText("Pd: - m")
                .setPosition(11, 610)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l21 = cp5.addTextlabel("Max Acc-EW")
                .setText("Max Acc-EW: 0m/s\u00b2")
                .setPosition(11, 300)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Acceleration");

        l22 = cp5.addTextlabel("Max Acc-NS")
                .setText("Max Acc-NS: 0m/s\u00b2")
                .setPosition(11, 325)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Acceleration");

        l23 = cp5.addTextlabel("Max Acc-Z")
                .setText("Max Acc-Z: 0m/s\u00b2")
                .setPosition(11, 350)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Acceleration");

        l24 = cp5.addTextlabel("Max Vel-EW")
                .setText("Max Vel-EW: 0m/s")
                .setPosition(11, 300)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Velocity");

        l25 = cp5.addTextlabel("Max Vel-NS")
                .setText("Max Vel-NS: 0m/s")
                .setPosition(11, 325)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Velocity");

        l26 = cp5.addTextlabel("Max Vel-Z")
                .setText("Max Vel-Z: 0m/s")
                .setPosition(11, 350)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Velocity");

        l27 = cp5.addTextlabel("Max Disp-EW")
                .setText("Max Disp-EW: 0m")
                .setPosition(11, 300)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Displacement");

        l28 = cp5.addTextlabel("Max Disp-NS")
                .setText("Max Disp-NS: 0m")
                .setPosition(11, 325)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Displacement");

        l29 = cp5.addTextlabel("Max Disp-Z")
                .setText("Max Disp-Z: 0m")
                .setPosition(11, 350)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Displacement");

        l30 = cp5.addTextlabel("STA")
                .setText("STA Window size: - ms")
                .setPosition(11, 490)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");
        l31 = cp5.addTextlabel("LTA")
                .setText("LTA Window size: - ms")
                .setPosition(11, 505)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        l32 = cp5.addTextlabel("framewin")
                .setText(" Window frame: - ms")
                .setPosition(11, 460)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");
        l33 = cp5.addTextlabel("staltawin")
                .setText("STA+LTA size: - ms")
                .setPosition(11, 475)
                .setFont(createFont("Calibri", 12))
                .setColor(yellow)
                .moveTo("Triggering");

        ln = cp5.addTextlabel("Select-Status")
                .setText("Not Configured")
                .setPosition(116, 340)
                .setFont(createFont("Calibri", 12))
                .setColor(orange);

        KnobTRIG = cp5.addKnob("STA/LTA_thres")
                .setColorBackground(color(0xff999967))
                .setRange(0, 20)
                .setValue(4)
                .setPosition(185, 180 + 5)
                .setRadius(17)
                .showTickMarks()
                .snapToTickMarks(true)
                .setNumberOfTickMarks(20)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Triggering");

        KnobSTA = cp5.addKnob("STA value (s)")
                .setColorBackground(color(0xff999967))
                .setRange(1, 10)
                .setValue(5.0f)
                .setPosition(185, 230 + 5)
                .setRadius(17)
                .showTickMarks()
                .snapToTickMarks(true)
                .setNumberOfTickMarks(18)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Triggering");

        KnobLTA = cp5.addKnob("LTA value (s)")
                .setColorBackground(color(0xff999967))
                .setRange(5, 100)
                .setValue(10.0f)
                .setPosition(185, 290 + 5)
                .setRadius(17)
                .showTickMarks()
                .snapToTickMarks(true)
                .setNumberOfTickMarks(19)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Triggering");

        KnobDET = cp5.addKnob("Detrigger value")
                .setColorBackground(color(0xff999967))
                .setRange(0, 10)
                .setValue(2.5f)
                .setPosition(185, 350 + 5)
                .setRadius(17)
                .showTickMarks()
                .snapToTickMarks(true)
                .setNumberOfTickMarks(20)
                .setDragDirection(Knob.VERTICAL)
                .setColorForeground(color(orange))
                .moveTo("Triggering");

        Knobtauc = cp5.addKnob("Tc value")
                .setColorBackground(color(0xff999967))
                .setRange(0, 2)
                .setValue(1)
                .setPosition(185, 410 + 5)
                .setRadius(17)
                .showTickMarks()
                .setNumberOfTickMarks(10)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Triggering");

        Knobpd = cp5.addKnob("Pd value(mm)")
                .setColorBackground(color(0xff999967))
                .setRange(0.1f, 2)
                .setValue(0.1f)
                .setPosition(185, 470 + 5)
                .setRadius(17)
                .showTickMarks()
                .setNumberOfTickMarks(20)
                .snapToTickMarks(true)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Triggering");

        KnobTEMP = cp5.addKnob("Temp_thres")
                .setColorBackground(color(0xff999967))
                .setRange(-40, 140)
                .setValue(10)
                .setPosition(185, 180 + 5)
                .setRadius(20)
                .showTickMarks()
                .setNumberOfTickMarks(10)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Temperature");

        KnobVOLT = cp5.addKnob("LOW Volt_thres")
                .setColorBackground(color(0xff999967))
                .setRange(-0, 25)
                .setValue(12)
                .setPosition(185, 180 + 5)
                .setRadius(20)
                .showTickMarks()
                .setNumberOfTickMarks(10)
                .setColorForeground(color(orange))
                .setDragDirection(Knob.VERTICAL)
                .moveTo("Voltage");

        ip = cp5.addTextfield("Conf Server's IP")
                .setColorActive(color(0xff999967))
                .setPosition(12, 515)
                .setSize(105, 20)
                .setText("8.8.8.8");

        recipaddr = cp5.addTextfield("Recipient's Address")
                .setColorActive(color(0xff999967))
                .setPosition(135, 515)
                .setSize(104, 20)
                .setText("email@example.com");

        sendaddr = cp5.addTextfield("Sender's Address")
                .setColorActive(color(0xff999967))
                .setPosition(12, 550)
                .setSize(105, 20)
                .setText("email@example.com");

        sendpasswd = cp5.addTextfield("Sender's Password")
                .setColorActive(color(0xff999967))
                .setPosition(135, 550)
                .setPasswordMode(true)
                .setSize(104, 20)
                .setText("123test");

        int idx = 0;
        for (String s : org.sv.easy.stalta.StaLtaFactory.getNames()) {
            d2.addItem(s, idx);
            idx++;
        }

        d5.addItem("EW_acc", 0);
        d5.addItem("NS_acc", 1);
        d5.addItem("Z_acc", 2);

        d6.addItem("EW_vel", 0);
        d6.addItem("NS_vel", 1);
        d6.addItem("Z_vel", 2);

        d7.addItem("EW_disp", 0);
        d7.addItem("NS_disp", 1);
        d7.addItem("Z_disp", 2);

        d8.addItem("Off", 0);
        d8.addItem("On", 1);

        customize(d1);
        customize(d2);
        customize(d3);
        customize(d5);
        customize(d6);
        customize(d7);
        customize(d8);

        generateCOMDropdownList();
        generateXBEEDropdownList();
        System.out.println("Welcome to e.a.sy. Earthquake Alert System");
        if (Platform.isMac() || Platform.isLinux()) {
            System.out.println("  ___  ____ _____        _ ");
            System.out.println(" / _  \\/ __ `/ ___///      // ");
            System.out.println("/  __ / /_/ (__  ) // ___//  ");
            System.out.println("\\___/\\__,_/____/ \\__, //  ");
            System.out.println("                     /____//  ");
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
        System.out.println("System's Cores : " + Runtime.getRuntime().availableProcessors());
        System.out.println("\nNotice!!! you have do load nodesettings.conf and download nodes list before starting recording!");
        System.out.println("Notice!!! Before start reading from either the digitizer unit or the recorded mseed files user has to press the Save Settings button!");
        startProc();
        l9.setText("Started");
        l9.setColor(green);

        if (Platform.isWindows()) {
            System.out.println("Warning!!! GPIO monitoring is not available on Windows");
        }
        if (Platform.isLinux()) {
            if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
            } else {
                System.out.println("Warning!!! GPIO monitoring is not supported for x86_64 architectures");
            }
        }
        if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
        } else {
            mapWindow = new PopupWindow(this, new DisplayMap(sql));
            mapWindow.setSize(1120, 500);
            mapWindow.setTitle("EASYplot Map");
            mapWindow.setResizable(true);

            aboutWindow = new PopupWindow(this, new About());
            aboutWindow.setSize(600, 600);
            aboutWindow.setTitle("EASYplot About");
            aboutWindow.setResizable(true);
        }

        frameRate(100);

    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     */
    @Override
    public void draw() {
        background(bg);
        stroke(0);
        fill(0, 128);
        rect(10, 30, 230, 615);
        image(img, 50, 30);
        stroke(0);
        line(11, 179, 239, 179);
        staltaOptions_unused();
        if (Platform.isLinux()) {
            if ((System.getProperty("os.arch").toLowerCase().contains("arm")) && (gpioActive == 1)) {
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
     * @brief Customizes the appearence of the selected dropdown list
     *
     * @param ddl The Dropdown list to be modified
     *
     */
    public void customize(DropdownList ddl) {
        ddl.setItemHeight(20);
        ddl.setBarHeight(15);
        ddl.getCaptionLabel().getStyle().marginTop = 3;
        ddl.getCaptionLabel().getStyle().marginLeft = 5;
        ddl.getCaptionLabel().getStyle().marginRight = 5;
        ddl.getValueLabel().getStyle().marginTop = 3;
        ddl.setColorBackground(color(0xff999967));
        ddl.setColorActive(color(255, 128));
    }

    /**
     * @brief Generates a dropdown list for the digitiser module
     */
    public void generateCOMDropdownList() {
        d1.clear();
        int numCOM = Serial.list().length;
        for (int i = 0; i < numCOM; i++) {
            d1.addItem(Serial.list()[i], i);
        }
    }

    /**
     * @brief Generates a dropdown list for the XBEE module
     */
    public void generateXBEEDropdownList() {
        d3.clear();
        int numCOM = Serial.list().length;
        for (int i = 0; i < numCOM; i++) {
            d3.addItem(Serial.list()[i], i);
        }
    }

    /**
     * @brief Serial Event listener
     *
     * @details Parses data from serial port, removes delimeters and appends
     * them on arraylists and mSEED files
     * @param port The selected serial port
     * @exception RuntimeException for sizes mismatch
     *
     */
    public void serialEvent(Serial port) {

        if (log.isTraceEnabled()) {
            log.trace("event at " + System.currentTimeMillis() + ", available: " + port.available());
        }
        try {
            int available = port.available();
            for (int i = 0; i < available; i++) {
                int receivedVal = port.read();//read all incoming characters one by one
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
                            Reedsolomon rs = new Reedsolomon(msppcktlen, ECC_LENGTH);
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
        } catch (Throwable tr) {
            log.error("Data parsing error: ", tr);
            throw new RuntimeException(tr);
        }
    }

    /**
     * @brief Control Event listener
     *
     * @param theEvent The controlEvent that is sent to the PApplet
     * @exception RuntimeException if the selected port is busy or it does not
     * represent the XBEE or the Digitizer module
     */
    public void controlEvent(ControlEvent theEvent) {

        int typedisp = 2;
        int typevel = 1;
        int typeacc = 0;

        if (d2 != null && theEvent.isFrom(d2.getName())) {
            int selectedTrigger = PApplet.parseInt(theEvent.getGroup().getValue());
            loadSTALTAValues(selectedTrigger);
            return;
        }
        if (KnobSTA != null && theEvent.isFrom(KnobSTA.getName())) {
            float nsta = KnobSTA.getValue();
            seedListener.setNsta(nsta);
            l30.setText("STA Window size: " + nsta + "s");
            return;
        }
        if (KnobLTA != null && theEvent.isFrom(KnobLTA.getName())) {
            int nlta = (int) KnobLTA.getValue();
            seedListener.setNlta(nlta);
            l31.setText("LTA Window size: " + nlta + "s");
            seedListener.setNodeConfig(nodeConfig);
            checkStaLtaOverflow();
            return;
        }
        if (KnobDET != null && theEvent.isFrom(KnobDET.getName())) {
            float d = KnobDET.getValue();
            seedListener.setStaltaDeTrigger(d);
            return;
        }
        if (KnobTRIG != null && theEvent.isFrom(KnobTRIG.getName())) {
            float l = KnobTRIG.getValue();
            seedListener.setStaltaTrigger(l);
            return;
        }
        if (Knobtauc != null && theEvent.isFrom(Knobtauc.getName())) {
            staltaOptions_unused();
            seedListener.setTc(Knobtauc.getValue());
            return;
        }
        if (Knobpd != null && theEvent.isFrom(Knobpd.getName())) {
            staltaOptions_unused();
            seedListener.setPd_m(Knobpd.getValue() / 1000.0f); // mm -> m
            return;
        }
        if (KnobTEMP != null && theEvent.isFrom(KnobTEMP.getName())) {
            double temperatureThreshold = KnobTEMP.getValue();
            voltageAlarm.setTemperatureThreshold(temperatureThreshold);
            return;
        }
        if (KnobVOLT != null && theEvent.isFrom(KnobVOLT.getName())) {
            double voltThreshold = KnobVOLT.getValue();
            voltageAlarm.setVoltThreshold(voltThreshold);
            return;
        }
        if (theEvent.isGroup()) {
            if ("MonitorGPIO".equals(theEvent.getGroup().getName())) {
                gpioActive = PApplet.parseInt(theEvent.getGroup().getValue());
            }
        } else if (theEvent.isController()) {

            switch (theEvent.getController().getId()) {

                case (1):
                    try {
                    if (b1.getBooleanValue() == true) {
                        try {
                            accCanvas.start();
                            dispCanvas.start();
                            staltaCanvas.start();
                            velCanvas.start();

                            int index = PApplet.parseInt(d1.getValue());
                            COMPort = new Serial(this, Serial.list()[index], 115200);
                            b1.setCaptionLabel("Disconnect_COM");
                            l1.setText("Connected");
                            l1.setColor(green);
                            System.out.println("Serial monitor is active");
                            COMPort.clear(); //Discart data from previous connections
                        } catch (Exception e1) {
                            //Error, failed to connect
                            log.error("Error while opening Digitiser port: " + e1);
                            log.error("Please check your connection and ensure you 've selected the correct Com port");
                            l1.setText("Connection Failed");
                            l1.setColor(orange);
                        }
                    } else {
                        COMPort.clear();
                        COMPort.stop();
                        System.out.println("Connection Terminated. ");
                        b1.setCaptionLabel("Connect_COM");
                        l1.setText("Disconnected");
                        l1.setColor(orange);
                        serialLogger.close();
                    }
                } catch (Throwable e2) {
                    log.error("Generic Digitizer error: ", e2);
                }
                break;

                case (2):
                    if (b2.getBooleanValue() == true) {
                        exitproc.onSuccess();
                    }
                    break;

                case (3):
                    try {
                    if (b3.getBooleanValue() == true) {
                        try {
                            XBeeIndex = PApplet.parseInt(d3.getValue());
                            xbee = new XBeeDevice(Serial.list()[XBeeIndex], 19200);
                            xbeeIO = new XBeeIO(xbee);

                            try {
                                xbee.open();
                                OperatingMode operatingMode = xbee.getOperatingMode();
                                log.info("operating mode: " + operatingMode);
                                log.info("Xbee protocol: " + xbee.getXBeeProtocol());
                                b3.setCaptionLabel("Disconnect_XBEE");
                                l2.setText("Connected");
                                l2.setColor(green);
                                System.out.println("Xbee monitor is active");
                                xbeeActv = true;
                                xbee.addDataListener(new XBeeIO.MyDataReceiveListener());
                                System.out.println("Waiting for incoming messages...");
                                xbeeIO.nodeDiscovery();
                                xbeeIO.nodeDiscoveryTask();// a scheduler now
                                //scans every one hour for new end devices.
                            } catch (XBeeException xe) {
                                log.error("Error while opening XBee port: " + xe);
                                log.error("Please check your connection and ensure you 've selected the correct Com port");
                                l2.setText("Connection Failed");
                                l2.setColor(orange);
                            }
                        } catch (Exception e) {
                            log.error("Connection failed: " + e);
                            l2.setText("Connection Failed - No COM Port");
                            l2.setColor(orange);
                        }
                    } else {
                        xbee.close();
                        xbeeActv = false;
                        System.out.println("Connection Terminated. ");
                        b3.setCaptionLabel("Connect_COM");
                        l2.setText("Disconnected");
                        l2.setColor(orange);
                    }
                } catch (Throwable e) {
                    log.error("XBEE init error: ", e);
                }
                break;

                case (4):
                    SwingSpectrogram spectroacc = new SwingSpectrogram(typeacc, PApplet.parseInt(d5.getValue()), Integer.parseInt(nodeConfig.getSampleFrequency()));
                    if (b4.getBooleanValue() == true) {
                        System.out.println("Showing FFT window");
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                spectroacc.setVisible(true);
                            }
                        });
                        this.seedListener.addListener(spectroacc);
                    } else {
                        System.out.println("Hiding FFT window");
                        spectroacc.setVisible(false);
                    }
                    break;

                case (5):
                    if (b5.getBooleanValue() == true) {
                        updateFields();
                        l3.setText("User Def.");
                        l3.setColor(green);
                    } else {
                        for (int i = 0; i < 42; i++) {
                            System.out.println();
                        }
                        l3.setVisible(false);
                    }
                    break;

                case (6):
                    if (b6.getBooleanValue() == true) {
                        terminateProc();
                        l9.setText("Stopped");
                        l9.setColor(orange);
                    } else {
                        startProc();

                        l9.setText("Started");
                        l9.setColor(green);
                    }
                    break;

                case (7):
                    if (b7.getBooleanValue() == true) {
                        System.out.println("Showing Map window");
                        if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
                            System.out.println("OpenGl not supported here");
                        } else {
                            mapWindow.setVisible(true);
                            l8.setText("Active");
                            l8.setColor(green);
                        }

                    } else {
                        System.out.println("Hiding Map window");
                        if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
                            System.out.println("OpenGl not supported here");
                        } else {
                            mapWindow.setVisible(false);
                            l8.setText("Hiden");
                            l8.setColor(orange);
                        }
                    }
                    break;

                case (8):
                    if (b8.getBooleanValue() == true) {
                        System.out.println("About E.A.Sy application");
                        System.out.println("NATIONAL AND KAPODISTRIAN UNIVERSITY OF ATHENS\nSCHOOL OF SCIENCES\nFaculty of Geology and Geoenvironment");
                        System.out.println("Developper: Stylianos Voutsinas, steliosvo@teipir.gr");
                        System.out.println("Athens, 2015-2018");
                        if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
                            System.out.println("OpenGl not supported here");
                        } else {
                            aboutWindow.setVisible(true);
                        }
                    } else {
                        System.out.println("Hiding About window");
                        if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
                            System.out.println("OpenGl not supported here");
                        } else {
                            aboutWindow.setVisible(false);
                        }
                    }
                    break;

                case (9):
                    SwingSpectrogram spectrovel = new SwingSpectrogram(typevel, PApplet.parseInt(d6.getValue()), Integer.parseInt(nodeConfig.getSampleFrequency()));
                    if (b9.getBooleanValue() == true) {
                        System.out.println("Showing FFT window");
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                spectrovel.setVisible(true);
                            }
                        });
                        this.seedListener.addListener(spectrovel);
                    } else {
                        System.out.println("Hiding FFT window");
                        spectrovel.setVisible(false);
                    }
                    break;

                case (10):
                    SwingSpectrogram spectrodisp = new SwingSpectrogram(typedisp, PApplet.parseInt(d7.getValue()), Integer.parseInt(nodeConfig.getSampleFrequency()));
                    if (b10.getBooleanValue() == true) {
                        System.out.println("Showing FFT window");
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                spectrodisp.setVisible(true);
                            }
                        });
                        this.seedListener.addListener(spectrodisp);
                    } else {
                        System.out.println("Hiding FFT window");
                        spectrodisp.setVisible(false);
                    }
                    break;

                case (11):
                    if (b11.getBooleanValue() == true) {
                        xbeeIO.broadcastMsg("It is now safe to turn on power again", true, nodeConfig);
                        for (SeismicEventListener l : seismicEventListener) {
                            l.alert(seedListener, nodeConfig, "0", g_mw, threat, g_azi, null);
                        }
                    } else {
                    }
                    break;

                case (13):
                    if (b13.getBooleanValue() == true) {
                        deleteFilesOlder();
                    } else {
                    }
                    break;
                case (14):
                    loadSeedFiles();
                    break;
                case (18):
                    if (b18.getBooleanValue() == true) {
                        myTextarea.setText("");
                        System.out.println("Cleared...");
                    } else {
                        myTextarea.setText("");
                        System.out.println("Cleared...");
                    }
                    break;
                case (19):
                    if (b19.getBooleanValue() == true) {
                        instructions();
                    } else {
                        myTextarea.setText("");
                        System.out.println("Cleared...");
                    }
                    break;
                case (20):
                    if (b20.getBooleanValue() == true) {
                        b20.setCaptionLabel("Geophone");
                        ln.setText("Configured");
                        ln.setColor(green);
                        seedListener.setGeoacc(false);
                    } else {
                        b20.setCaptionLabel("Accelerometer");
                        ln.setText("Configured");
                        ln.setColor(green);
                        seedListener.setGeoacc(true);
                    }
                    break;
            }
        }
    }

    void instructions() {
        myTextarea.setText("");
        System.out.println("++-----------------------------------------------------------------------------------------Help menu-----------------------------------------------------------------------------------------++");
        System.out.println("++-----------------------------------------------------------------------------------General instructions:----------------------------------------------------------------------------------++");
        System.out.println(" |If it is the first time that the Application starts please make sure that the drivers are installed properly.");
        if (Platform.isMac() || Platform.isLinux()) {
            System.out.println(" |For linux/MacOS versions please run setup.sh, located on the utils folder.");
            System.out.println(" |For a high-quality NTP time server, please make sure that you have connected a GPS receiver with PPS output on a Serial port.");
            System.out.println(" |Make sure that you have NTPD and GPSD services installed, and they both are up and running.");
        }
        if (Platform.isWindows()) {
            System.out.println(" |For the Windows version please install drivers located on the Utils folder.");
        }
        System.out.println(" |Please make sure that the digitizer unit and the XBEE module are connected before starting the real-time monitoring.");
        System.out.println(" |User has to select the prefered options and make sure that nodesettings.conf file is already stored inside data/settings folder.");
        System.out.println(" |nodesettings.conf can be retrieved via the online node configuration tool which is available at: http://" + sql.getHostname() + "/form.html");
        System.out.println(" |User has to save inside data/settings folder the pole zero files for the instruments that has installed on the digitizing unit.");
        System.out.println(" |User has to select whether the system will operate in real-time, or with recorded data.");
        System.out.println(" |Any unsupported action that the user will try to perform, will cause the program to halt and exit to prevent any damage.");
        System.out.println(" |Press instructions button again to clear the console window...");
        System.out.println("++------------------------------------------------------------------------------------------Tabs menu-----------------------------------------------------------------------------------------++");
        System.out.println(" |*.sacpz files require specific format. If you have downloaded data from IRIS service make sure to run sacpzformatter");
        System.out.println(" | sacpz formatter syntax: java -cp easy-common.jar org.sv.easy.sacpzformatter.MainProper file_in file_out");
        System.out.println(" | Easyplot command line interface (support for real-time data): \n sudo ./service start");
        System.out.println("++-----------------------------------------------------------------------------------------Shortcuts-------------------------------------------------------------------------------------------++");
        System.out.println(" |Press up arrow for: Discovering new EASY nodes that are available.(TestMODE)");
        System.out.println(" |Press down arrow for: DWEET message. (TestMODE)");
        System.out.println(" |Press left arrow for: alert transmission via email. (TestMODE)");
        System.out.println(" |Press right arrow for: creating a dummy mseed package. (TestMODE)");
        System.out.println(" |Press +/- to Zoom in/out on the map window or to inc/decrease gain on the FFT window. ");
        System.out.println("++--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------++");
    }

    /**
     * @brief Searches for data files that are older than 30 days, and deletes
     * them. Also clear old log files
     */
    public void deleteFilesOlder() {
        long current = System.currentTimeMillis();
        long month = 1000L * 60 * 60 * 24 * 30;
        File root = new File(System.getProperty("user.dir") + "/var/log/digitiser_logs");
        System.out.println(root.getAbsolutePath());
        Collection<File> files = FileUtils.listFiles(root, new String[]{
            "txt"
        }, true);
        for (File fl : files) {
            long mtime = fl.lastModified();
            long dtime = current - mtime;
            if (dtime > month) {
                System.out.println("Deleting file: " + fl);
                fl.delete();
            }
        }

        File logroot = new File(System.getProperty("user.dir") + "/var/log/system_logs");
        Collection<File> logfiles = FileUtils.listFiles(logroot, new String[]{
            "log"
        }, true);
        for (File lfl : logfiles) {
            System.out.println("Deleting file: " + lfl);
            lfl.delete();
        }
    }

    /**
     * @brief Help/test function
     */
    @Override
    public void keyPressed() {
        if (key == CODED) {
            switch (keyCode) {
                case UP:
                    System.out.println("Texting Xbee module: ");
                    System.out.println("Setting pins HIGH: ");
                    xbeeIO.setpinHigh(true, nodeConfig);
                    System.out.println("Broadcast msg: ");
                    xbeeIO.broadcastMsg("Notice! this is a 40 byte test message!", false, nodeConfig);
                     {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Easyplot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    System.out.println("Setting pins LOW: ");
                    xbeeIO.setpinHigh(false, nodeConfig);
                    System.out.println("done!");
                    break;
                case DOWN:
                    System.out.println("Testing DWEET transmission: ");
                    dweet.alert(seedListener, nodeConfig, "0", 2, 0, 0, null);
                     {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Easyplot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    dweet.alert(seedListener, nodeConfig, "1", 4, 25, 90, null);
                     {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Easyplot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    dweet.alert(seedListener, nodeConfig, "1", 5, 50, 180, null);
                     {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Easyplot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    dweet.alert(seedListener, nodeConfig, "1", 5.5, 75, 270, null);
                     {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Easyplot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    dweet.alert(seedListener, nodeConfig, "1", 6, 100, 360, null);
                     {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(Easyplot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    dweet.alert(seedListener, nodeConfig, "0", 0, 0, 0, null);
                    System.out.println("done!");
                    break;
                case LEFT:
                    System.out.println("Testing mail service:");
                    DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    String timestamp = stamp.format(calendar.getTime());
                    mailer.sendMail("test" + timestamp);
                    System.out.println("done!");
                    break;
                case RIGHT:
                    System.out.println("Executing mseed test (1h data):");
                    seedListener.createTestFile();
                    System.out.println("done!");
                    break;
                case java.awt.event.KeyEvent.VK_F1:
                    instructions();
                    break;
                case java.awt.event.KeyEvent.VK_F2:
                    myTextarea.setText("");
                    System.out.println("Cleared...");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @brief Terminates Seedlink Service
     */
    public void terminateProc() {
        if (Platform.isMac() || Platform.isLinux()) {

            try {
                System.out.println("\nStopping ringserver...");
                ProcessBuilder pb1;
                if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
                    pb1 = new ProcessBuilder("pkill", "ringserver_armv");
                } else {
                    pb1 = new ProcessBuilder("pkill", "ringserver");
                }
                Process p1 = pb1.start();
                // any error message?
                StreamGobbler errorGobbler1 = new StreamGobbler(p1.getErrorStream(), "terminateProc-error: ");
                // any output?
                StreamGobbler outputGobbler1 = new StreamGobbler(p1.getInputStream(), "terminateProc: ");
                // kick them off
                errorGobbler1.start();
                outputGobbler1.start();
            } catch (Exception e7) {
                log.error("Error on terminating ringserver: ", e7);
            }
            System.out.println("done");
        }
        if (Platform.isWindows()) {
            try {
                System.out.println("\nStopping ringserver...");
                ProcessBuilder pb1 = new ProcessBuilder("Taskkill", "/IM", "ringserver.exe", "/F");
                Process p1 = pb1.start();
                // any error message?
                StreamGobbler errorGobbler1 = new StreamGobbler(p1.getErrorStream(), "terminateProc-error: ");
                // any output?
                StreamGobbler outputGobbler1 = new StreamGobbler(p1.getInputStream(), "terminateProc: ");
                // kick them off
                errorGobbler1.start();
                outputGobbler1.start();
            } catch (Exception e8) {
                log.error("Error on terminating ringserver: ", e8);
            }
            System.out.println("done");
        }
    }

    /**
     * @brief Starts Seedlink Service
     * @details Creates a new process for the IRIS Ringserver and exports
     * Ringserver's messages on the console via StreamGobbler Class. Note that
     * Ringserver for Windows platform has to be compiled via Cygwin
     */
    public void startProc() {
        if (Platform.isMac() || Platform.isLinux()) {

            try {
                System.out.println("\nStarting ringserver...");
                File executable1;
                if (System.getProperty("os.arch").toLowerCase().contains("arm")) {
                    executable1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer/ringserver_armv");
                } else {
                    executable1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer/ringserver");
                }
                File config1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer/ring.conf");
                File root1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer");
                ProcessBuilder pb2 = new ProcessBuilder(executable1.getAbsolutePath(), "-v", config1.getAbsolutePath());
                pb2.directory(root1);
                //pb.inheritIO();
                Process p2 = pb2.start();

                // any error message?
                StreamGobbler errorGobbler2 = new StreamGobbler(p2.getErrorStream(), "Ringserver-error: ");

                // any output?
                StreamGobbler outputGobbler2 = new StreamGobbler(p2.getInputStream(), "Ringserver: ");

                // kick them off
                errorGobbler2.start();
                outputGobbler2.start();
            } catch (Exception e9) {
                log.error("Error on starting ringserver: ", e9);
            }
        }
        if (Platform.isWindows()) {
            try {
                System.out.println("\nStarting ringserver...");
                File executable1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer/ringserver.exe");
                File config1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer/ring_win.conf");
                File root1 = new File(System.getProperty("user.dir") + "/bin/SeedlinkServer");
                ProcessBuilder pb2 = new ProcessBuilder(executable1.getAbsolutePath(), config1.getAbsolutePath());
                pb2.directory(root1);
                //pb.inheritIO();
                Process p2 = pb2.start();

                // any error message?
                StreamGobbler errorGobbler2 = new StreamGobbler(p2.getErrorStream(), "Ringserver-error: ");

                // any output?
                StreamGobbler outputGobbler2 = new StreamGobbler(p2.getInputStream(), "Ringserver: ");

                // kick them off
                errorGobbler2.start();
                outputGobbler2.start();
                l9.setText("Started");
                l9.setColor(green);
            } catch (Exception e) {
                log.error("Error on starting ringserver: ", e);
            }
        }
    }

    private void loadSeedFiles() {
        Runnable R = new Runnable() {
            @Override
            public void run() {
                String xChannelFile;
                String yChannelFile;
                String zChannelFile;
                String xpzc;
                String ypzc;
                String zpzc;
                if (b1.getBooleanValue() == true) {
                    JOptionPane.showMessageDialog(frame,
                            "Loading data from miniSeed file cannot be done while serial monitor is active.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                FileSelectDialog dialog = new FileSelectDialog(null, true);
                dialog.setVisible(true);
                if (dialog.isOk()) {
                    xChannelFile = dialog.getEw().getAbsolutePath();
                    yChannelFile = dialog.getNs().getAbsolutePath();
                    zChannelFile = dialog.getZ().getAbsolutePath();
                    xpzc = dialog.getEwsacpz().getAbsolutePath();
                    ypzc = dialog.getNssacpz().getAbsolutePath();
                    zpzc = dialog.getZsacpz().getAbsolutePath();

                    LoadSeedFile lsf = new LoadSeedFile(xChannelFile, yChannelFile,
                            zChannelFile, xpzc, ypzc, zpzc);
                    lsf.loadData(seedListener);

                    log.info("mseed files loaded: " + dialog.getZ());
                    accCanvas.start();
                    dispCanvas.start();
                    staltaCanvas.start();
                    velCanvas.start();
                    l7.setText("Loaded");
                    l7.setColor(green);
                }
            }

        };
        //R.run();
        new Thread(R).start();
    }

    /**
     * @file //<>//
     * @brief E.A.SY. Application.
     *
     * This contains the methods for seismic activity and alert
     *
     * @author Stelios Voutsinas (stevo)
     * @bug No known bugs.
     */
    /**
     * @brief Fetch values that are needed for triggering initialisation
     */
    void staltaOptions_unused() {
        l32.setText("Window frame size: " + seedListener.getSTALTAWindowSize() + "samples");
    }

    /**
     * @brief Checks if the user has entered an invalid time window
     */
    private void checkStaLtaOverflow() {
        int nlta = seedListener.getNlta();
        float nsta = seedListener.getNsta();
        if (nlta > seedListener.getSTALTAWindowSize()) {
            l33.setColor(red)
                    .setText("Warning! Overflow!!!!");
            log.warn("Out of STALTAWindowSize " + seedListener.getSTALTAWindowSize());
        } else {
            l33.setText("STALTA window size: " + "60s").setColor(yellow);
        }
    }

    /**
     * @brief Receives an incoming GeoEvent and issues the appropriate alerts
     * @param event The received event
     */
    private void taucPdEvent_received(GeoEvent event) {
        g_azi = event.getAzi();
        g_mw = event.getMw();
        DecimalFormat f = new DecimalFormat("###.##");
        DecimalFormat df = new DecimalFormat("0.000E00");

        lazi.setText("Azi: " + f.format(event.getAzi()) + "\u00b0N");
        l19.setText("Tc: " + f.format(event.getTauc()) + " s");
        l20.setText("Pd: " + df.format(event.getPd()) + " m");
        l16.setText("Magnitude Estimation: " + f.format(event.getMw()) + "Mw");
        notifyLabels();
        mailAlert.sendMail(seedListener, nodeConfig, event);
        sql.saveEvent(seedListener, nodeConfig, event);
        if (xbeeActv) {
            if (event.getType() == GeoEvent.TYPE_1) {
                l10.setText("Distant large magnitude incoming event!");
                threat = 60;
                xbeeIO.broadcastMsg("Distant large magnitude incoming event!", true, nodeConfig);
            }

            if (event.getType() == GeoEvent.TYPE_2) {
                l10.setText("Local small magnittude incoming event!");
                threat = 30;
                xbeeIO.broadcastMsg("Local small magnittude incoming event !", false, nodeConfig);
            }

            if (event.getType() == GeoEvent.TYPE_3) {
                l10.setText("!!! W A R N I N G !!!");
                l10.setColor(black);
                threat = 90;
                xbeeIO.broadcastMsg("!!! WARNING  Damaging incoming event!!!", true, nodeConfig);
            }
            if (event.getType() == GeoEvent.TYPE_4) {

                l10.setText("Not damaging event!");
                threat = 10;
                xbeeIO.broadcastMsg("Notice!!! Not damaging incoming event!!", false, nodeConfig);
            }
        }
        for (SeismicEventListener l : seismicEventListener) {
            l.alert(seedListener, nodeConfig, "1", event.getMw(), threat, event.getAzi(), event);
        }
    }

    /**
     * @brief Initialises the application's logger
     */
    private void initLogging() {
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
    }

    /**
     * @brief Starts the application's splash screen
     */
    private void showSplash() {
        SplashScreen splash = new SplashScreen(1500);
        splash.showSplash();
    }

    /**
     * @brief Loads the configuration files
     */
    private void loadConfig() {
        try {
            nodeConfig = NodeConfigLoader.loadFromFile(System.getProperty("user.dir") + "/etc/easy/nodesettings.conf");
        } catch (Throwable e) {
            log.error("ERROR!!! could not load nodesettings.conf: ", e);
            exitproc.onFailure();
        }
    }

    /**
     * @brief Updates values of various knobs and fields that are necessary for
     * the application
     */
    private void updateFields() {
        sql.setHostname(ip.getText());
        if (mailer != null) {
            mailer.setSender(sendaddr.getText());
            mailer.setRecipient(recipaddr.getText());
            mailer.setPassword(sendpasswd.getText());
        }
        float d = KnobDET.getValue();
        seedListener.setStaltaDeTrigger(d);
        float l = KnobTRIG.getValue();
        seedListener.setStaltaTrigger(l);
        seedListener.setTc(Knobtauc.getValue());
        seedListener.setPd_m(Knobpd.getValue() / 1000.0f); // mm -> m
        double temperatureThreshold = KnobTEMP.getValue();
        voltageAlarm.setTemperatureThreshold(temperatureThreshold);
        double voltThreshold = KnobVOLT.getValue();
        voltageAlarm.setVoltThreshold(voltThreshold);
    }

    /**
     * @brief Prints necessary information from the configuration file
     */
    private void loadConf() {
        seedListener.setNodeConfig(nodeConfig);
        System.out.println("Nyquist Freq: " + seedListener.getNyquistFrequency() + " Hz");
        System.out.println("Tau0 : " + seedListener.getTau0() + " samples");
        System.out.println("Data will be filtered with a " + Utils.getOrdinal(nodeConfig.getLpfOrd()) + " order LPF :");
        System.out.println("Fc: " + nodeConfig.getLpfFc() + "Hz");
        System.out.println("Tauc-Pd Data will be filtered with a " + Utils.getOrdinal(nodeConfig.getHpfOrd()) + " order HPF :");
        System.out.println("Fc: 0.075Hz ");
        seedListener.loadFilters();

        //Set default values to two important dropdown lists
        d2.setIndex(0);
        d8.setIndex(0);
        //app settings
        System.out.println("Field values uploaded successfully!");
        updateFields();
    }

    /**
     * @brief Changes color on the PGx Labels
     */
    private void notifyLabels() {
        l21.setColor(orange);
        l22.setColor(orange);
        l23.setColor(orange);
        l24.setColor(orange);
        l25.setColor(orange);
        l26.setColor(orange);
        l27.setColor(orange);
        l28.setColor(orange);
        l29.setColor(orange);
    }

    /**
     * @brief Resets color and values on the PGx Labels
     */
    void resetLabelsX() {
        l21.setColor(yellow);
        l24.setColor(yellow);
        l27.setColor(yellow);
        accCanvas.resetX();
        velCanvas.resetX();
        dispCanvas.resetX();
    }

    /**
     * @brief Resets color and values on the PGx Labels
     */
    void resetLabelsY() {
        l22.setColor(yellow);
        l25.setColor(yellow);
        l28.setColor(yellow);
        accCanvas.resetY();
        velCanvas.resetY();
        dispCanvas.resetY();
    }

    /**
     * @brief Resets color and values on the PGx Labels
     */
    void resetLabelsZ() {
        l23.setColor(yellow);
        l26.setColor(yellow);
        l29.setColor(yellow);
        accCanvas.resetZ();
        velCanvas.resetZ();
        dispCanvas.resetZ();
    }

    /**
     * @brief Returns max. acceleration on x (EW) axis
     */
    double getPGAx() {
        return accCanvas.getPGAx();
    }

    /**
     * @brief Returns max acceleration on y (NS) axis
     */
    double getPGAy() {
        return accCanvas.getPGAy();
    }

    /**
     * @brief Loads the defaults STA and LTA values for the selected algorithm
     * @param selectedTrigger the selected trigger
     *
     */
    public void loadSTALTAValues(int selectedTrigger) {
        StaLta staltaImpl = StaLtaFactory.get(d2.getItem(selectedTrigger).getName());
        seedListener.setStaLtaImpl(staltaImpl);
        switch (selectedTrigger) {
            case 0: {
                KnobSTA.setValue(1);
                KnobLTA.setValue(60);
                KnobTRIG.setValue(4.0f);
                KnobDET.setValue(2.0f);
                break;
            }
            case 1: {
                KnobSTA.setValue(1);
                KnobLTA.setValue(60);
                KnobTRIG.setValue(4.0f);
                KnobDET.setValue(2.0f);
                break;
            }
            case 2: {
                KnobSTA.setValue(1);
                KnobLTA.setValue(60);
                KnobTRIG.setValue(4.0f);
                KnobDET.setValue(2.0f);
                break;
            }
        }
    }

    /**
     * @brief Class that synchronises the start/stop of the plots
     */
    @SuppressWarnings("ClassMayBeInterface")
    private class xLoadSeedFile {

    }

    /**
     * @brief This contains methods related on sudden or deliberate termination
     * of EASY app
     */
    public class ExitOn {

        /**
         * @brief 10 sec reverse count down before termination on Failure
         */
        public void onFailure() {
            Thread failthr;
            final char[] animationChars = new char[]{
                '|', '/', '-', '\\'
            };
            myTextarea.setText("");
            failthr = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 10; i > 0; i--) {
                        System.out.print("A severe error has been occured, E.A.SY will now shutdown in: " + i + " Sec." + animationChars[i % 4]);
                        myTextarea.setText("");
                        serialLogger.close();
                        seedListener.flush();
                        if (b1.getBooleanValue() == true) {
                            COMPort.clear();
                            COMPort.stop();
                        }
                        if (b3.getBooleanValue() == true) {
                            xbee.close();
                        }
                        if (i == 1) {
                            log.warn("E.A.Sy. terminated by some error.");
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            log.error("Thead on failure error: ", ie);
                        } finally {
                        }
                    }
                    exit();
                }
            }
            );
            failthr.start();
        }

        /**
         * @brief 10 sec reverse count down before termination on Success
         */
        public void onSuccess() {
            Thread succthr;
            final char[] animationChars = new char[]{
                '|', '/', '-', '\\'
            };
            myTextarea.setText("");
            succthr = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 10; i > 0; i--) {
                        System.out.print("E.A.SY will now Terminate in: " + i + " Sec." + animationChars[i % 4]);
                        myTextarea.setText("");
                        if (i == 8) {
                            serialLogger.close();
                            seedListener.flush();
                        }
                        if (i == 5) {
                            terminateProc();
                            l9.setText("Stopped");
                        }
                        if (i == 3) {
                            if (b1.getBooleanValue() == true) {
                                COMPort.clear();
                                COMPort.stop();
                            }
                            if (b3.getBooleanValue() == true) {
                                xbee.close();
                            }
                            l1.setText("Disconnected");
                            l1.setColor(orange);
                            l2.setText("Disconnected");
                            l2.setColor(orange);
                        }
                        if (i < 2) {
                            log.info("E.A.Sy. terminated successfully");
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ef) {
                            log.error("Thread on success error: ", ef);
                        } finally {
                        }
                    }
                    exit();
                }
            }
            );
            succthr.start();
        }
    }
}
