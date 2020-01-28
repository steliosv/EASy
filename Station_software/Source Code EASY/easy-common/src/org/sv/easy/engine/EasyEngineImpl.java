/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that listens ton seed
 * events
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

import edu.sc.seis.seisFile.sac.SacPoleZero;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.fill;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.log4j.Logger;
import org.sv.easy.common.TransferFunction;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;
import org.sv.easy.engine.api.GeoEventListener;
import org.sv.easy.engine.api.SensorEventListener;
import org.sv.easy.engine.api.StaltaEvent;
import org.sv.easy.engine.api.StaltaEventListener;
import org.sv.easy.engine.logging.SeedFileLogger;
import org.sv.easy.engine.IndIntegral;
import org.sv.easy.fec.Reedsolomon;
import org.sv.easy.stalta.StaLta;
import uk.me.berndporr.iirj.Butterworth;
import uk.me.berndporr.iirj.DirectFormAbstract;
import org.sv.easy.engine.KalmanFilter;

/**
 * @brief Easy Engine implementation class
 */
@SuppressWarnings({"SleepWhileInLoop", "MismatchedReadAndWriteOfArray", "AssignmentToMethodParameter", "Convert2Lambda", "CollectionWithoutInitialCapacity", "ClassWithoutLogger", "Convert2Diamond"})
public class EasyEngineImpl implements EasyEngine {

    private static final Logger log = Logger.getLogger(EasyEngine.class);
    private static final int SERIAL_PACKET_SIZE = 8;
    private static final int SEED_PACKET_SIZE = 11;
    private ExecutorService executor;
    private final int mseedSize = 512;
    private final CopyOnWriteArrayList<SensorEventListener> listeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<StaltaEventListener> staltaListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<GeoEventListener> tauListeners = new CopyOnWriteArrayList<>();
    private final SeedFileLogger wfxa = new SeedFileLogger(mseedSize);
    private final SeedFileLogger wfya = new SeedFileLogger(mseedSize);
    private final SeedFileLogger wfza = new SeedFileLogger(mseedSize);
    private final SeedFileLogger wfxg = new SeedFileLogger(mseedSize);
    private final SeedFileLogger wfyg = new SeedFileLogger(mseedSize);
    private final SeedFileLogger wfzg = new SeedFileLogger(mseedSize);
    private final SeedFileLogger testmsw = new SeedFileLogger(mseedSize);
    private final SeedFileLogger testmsw1 = new SeedFileLogger(mseedSize);
    private final SeedFileLogger testmsw2 = new SeedFileLogger(mseedSize);
    private final SeedFileLogger testmsw3 = new SeedFileLogger(mseedSize);
    private final SeedFileLogger testmsw4 = new SeedFileLogger(mseedSize);
    private final SeedFileLogger testmsw5 = new SeedFileLogger(mseedSize);
    private final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final TaucPd taucPd;
    private final SacPoleZero pzaccx;
    private final SacPoleZero pzaccy;
    private final SacPoleZero pzaccz;
    private final SacPoleZero pzvx;
    private final SacPoleZero pzvy;
    private final SacPoleZero pzvz;
    final Butterworth nsbw = new Butterworth();
    final Butterworth ewbw = new Butterworth();
    final Butterworth zbw = new Butterworth();
    final Butterworth nsdfL = new Butterworth();
    final Butterworth ewdfL = new Butterworth();
    final Butterworth zdfL = new Butterworth();
    final Butterworth nsaccdfL = new Butterworth();
    final Butterworth ewaccdfL = new Butterworth();
    final Butterworth zaccdfL = new Butterworth();
    final Butterworth nsdfH = new Butterworth();
    final Butterworth ewdfH = new Butterworth();
    final Butterworth zdfH = new Butterworth();
    final Butterworth nsaccdfH = new Butterworth();
    final Butterworth ewaccdfH = new Butterworth();
    final Butterworth zaccdfH = new Butterworth();

    private NodeConfig nodeConfig;
    private final float[] currentSensorValues = new float[SEED_PACKET_SIZE];
    //for derivative
    private final float[] prevSensorValues = new float[SEED_PACKET_SIZE];
    private boolean geoacc = true;
    private final ArrayList<Double> staltaX = new ArrayList<Double>();
    private final ArrayList<Double> staltaY = new ArrayList<Double>();
    private final ArrayList<Double> staltaZ = new ArrayList<Double>();
    private final Object staltaLock = new Object();
    private StaLta staLtaImpl;
    private float nsta = 50;
    private int nlta = 100;
    private float staltaTrigger;
    private float staltaDeTrigger;
    private int samplingFrequency;
    private int count = 0;
    private float tc;
    private float pd_m;
    private boolean triggered = false;
    private double[] trigdisp;
    private double[] trigvel;
    private double[] trigacc;
    private double amp_ew = 0;
    private double amp_ns = 0;
    private final ArrayList<Long> timeRec = new ArrayList<Long>();
    private boolean ctrl = false;
    private boolean trigxon = false;
    private boolean trigyon = false;
    private boolean trigzon = false;
    private long Pwavets;
    private long Swavetsew;
    private long Swavetsns;
    private final List<RawData> rawData = new ArrayList<>();
    private final List<ProcessedData> processedData = new ArrayList<>();
    private final Lock lock = new ReentrantLock();
    private IndIntegral iintx = new IndIntegral(0.0);
    private IndIntegral iinty = new IndIntegral(0.0);
    private IndIntegral iintz = new IndIntegral(0.0);
    private IndIntegral integAcc2Velx = new IndIntegral(0.0);
    private IndIntegral integAcc2Vely = new IndIntegral(0.0);
    private IndIntegral integAcc2Velz = new IndIntegral(0.0);
    private IndIntegral integAcc2Disx = new IndIntegral(0.0);
    private IndIntegral integAcc2Disy = new IndIntegral(0.0);
    private IndIntegral integAcc2Disz = new IndIntegral(0.0);
    private String powerStatus;
    private boolean powok = false;
    private boolean powfail = false;
    private int cnt = 0;
    private float[] raw = new float[3];
    private float[] tempCurrentSensorValues = new float[6];
    private SecCounter sc = new SecCounter();
    private int cntSec = 0;
    private int defVal = 1;
    private final Object saveToFileLock = new Object();
    private  KalmanFilter kfaew;
    private  KalmanFilter kfans;
    private  KalmanFilter kfaz;
    private  KalmanFilter kfvew;
    private  KalmanFilter kfvns;
    private  KalmanFilter kfvz;

    /**
     * @brief constructor
     */
    public EasyEngineImpl() {
        try {
            this.taucPd = new TaucPd();
            //load sacpz file for accelerometer
            pzaccx = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/accEpzc.sacpz");
            pzaccy = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/accNpzc.sacpz");
            pzaccz = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/accZpzc.sacpz");
            //load sacpz file for geophone
            pzvx = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/pzclgt45E.sacpz");
            pzvy = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/pzclgt45N.sacpz");
            pzvz = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/pzclgt45Z.sacpz");
        } catch (IOException ex) {
            log.error("SACPZ file failed to load: ", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * @brief Retrieves the sta-lta implementation
     * @return the implementation
     */
    public StaLta getStaLtaImpl() {
        return staLtaImpl;
    }

    /**
     * @brief Sets the sta-lta implementation
     * @param staLtaImpl the implementation
     */
    @Override
    public void setStaLtaImpl(StaLta staLtaImpl) {
        this.staLtaImpl = staLtaImpl;
    }

    /**
     * @brief returns if it is triggered
     * @return
     */
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * @brief sets up a trigger value
     * @param triggered the triggered state
     */
    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    /**
     * @brief Retrieves the sampling frequency
     * @return the sampling frequency
     */
    @Override
    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    /**
     * @brief Retrieves the sampling period expressed in milliseconds
     * @return the sampling period in milliseconds
     */
    @Override
    public long getSamplingPeriodMillis() {
        return 1000 / samplingFrequency;
    }

    /**
     * @brief Retrieves the sampling period
     * @return the sampling period
     */
    public float getSamplingPeriod() {
        return 1.0f / samplingFrequency;
    }

    /**
     * @brief Retrieves Nyquist frequency
     * @return the Nyquist frequency
     */
    @Override
    public float getNyquistFrequency() {
        return 0.5f / getSamplingPeriod();
    }

    /**
     * @brief Retrieves a calendar obj.
     * @return the calendar
     */
    @SuppressWarnings("ReturnOfDateField")
    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * @brief Retrieves tc threshold
     * @return the tc
     */
    public float getTc() {
        return tc;
    }

    /**
     * @brief Sets up the tc threshold
     * @param tc the tc threshold
     */
    @Override
    public void setTc(float tc) {
        this.tc = tc;
    }

    /**
     * @brief Retrieves the pd threshold expressed in meters
     * @return the pd threshold
     */
    public float getPd_m() {
        return pd_m;
    }

    /**
     *
     * @brief Sets up the pd threshold
     * @param threshold the pd threshold
     */
    @Override
    public void setPd_m(float threshold) {
        this.pd_m = threshold;
    }

    /**
     * @brief Retrieves the node configuration
     * @return the node configuration
     */
    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    /**
     * @brief Sets up the node configuration
     * @param nodeConfig the node configuration
     */
    @Override
    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
        String sps = nodeConfig.getSampleFrequency();
        samplingFrequency = Integer.parseInt(sps);
        trigdisp = new double[getTau0()];
        trigvel = new double[getTau0()];
        trigacc = new double[getTau0()];
        init(staltaX, getSTALTAWindowSize());
        init(staltaY, getSTALTAWindowSize());
        init(staltaZ, getSTALTAWindowSize());
        kfaew = new KalmanFilter(nodeConfig.getME(), nodeConfig.getEE(), nodeConfig.getQ());
        kfans = new KalmanFilter(nodeConfig.getME(), nodeConfig.getEE(), nodeConfig.getQ());
        kfaz = new KalmanFilter(nodeConfig.getME(), nodeConfig.getEE(), nodeConfig.getQ());
        kfvew = new KalmanFilter(nodeConfig.getME(), nodeConfig.getEE(), nodeConfig.getQ());
        kfvns = new KalmanFilter(nodeConfig.getME(), nodeConfig.getEE(), nodeConfig.getQ());
        kfvz = new KalmanFilter(nodeConfig.getME(), nodeConfig.getEE(), nodeConfig.getQ());
    }

    /**
     * @brief Retrieves the STA length in samples
     * @return nsta samples
     */
    @Override
    public float getNsta() {
        return nsta;
    }

    /**
     * @brief Sets up the STA length in samples
     * @param value the nsta samples
     */
    @Override
    public void setNsta(float value) {
        this.nsta = value;
    }

    /**
     * @brief Retrieves the LTA length in samples
     * @return the nlta samples
     */
    @Override
    public int getNlta() {
        return nlta;
    }

    /**
     * @brief Sets up the LTA length in samples
     * @param value the nlta samples
     */
    @Override
    public void setNlta(int value) {
        this.nlta = value;
    }

    /**
     * @brief Retrieves the STA/LTA triggering threshold
     * @return the triggering threshold
     */
    @Override
    public float getStaltaTrigger() {
        return staltaTrigger;
    }

    /**
     * @brief Sets up the STA/LTA triggering threshold
     * @param value the triggering threshold
     */
    @Override
    public void setStaltaTrigger(float value) {
        this.staltaTrigger = value;
    }

    /**
     * @brief Retrieves the STA/LTA detriggering threshold
     * @return the detriggering threshold
     */
    @Override
    public float getStaltaDeTrigger() {
        return staltaDeTrigger;
    }

    /**
     * @brief Sets up the STA/LTA detriggering threshold
     * @param value the detriggering theshold
     */
    @Override
    public void setStaltaDeTrigger(float value) {
        this.staltaDeTrigger = value;
    }

    /**
     * @brief Retrieves the STA/LTA window size in samples
     * @return the STA/LTA window size
     */
    @Override
    public int getSTALTAWindowSize() {
        return samplingFrequency * 60;
    }

    /**
     * @brief Retrieves if the acceleration data derive from the accelerometer
     * or the geophone
     * @return if geophone is selected
     */
    public boolean isGeoacc() {
        return geoacc;
    }

    /**
     * @brief Sets if the acceleration data derive from the accelerometer or the
     * geophone
     * @param geoacc if geophone is selected
     */
    @Override
    public void setGeoacc(boolean geoacc) {
        this.geoacc = geoacc;
    }

    /**
     * @brief Retrieves the tau0 window size in samples
     * @return the Tau0 window size
     */
    @Override
    public int getTau0() {
        //sample length 3 sec * sample rate
        return 3 * samplingFrequency;
    }

    /**
     * @brief pushes data
     * @param sample as string
     */
    @Override
    public void push(String sample) {
        if (sample == null) {
            sample = "1 1 1 1 1 1 1 1 1";
        }
        sample = sample.trim();
        if (sample.isEmpty()) {
            sample = "1 1 1 1 1 1 1 1 1";
        }
        String[] tokens = sample.split(" ");
        if (tokens.length < (SERIAL_PACKET_SIZE + 1)) {
            log.warn("Received  message with invalid length: " + sample + " " + (tokens.length - (SERIAL_PACKET_SIZE + 1)) + " elements.");
            //return;
            tokens = new String[SERIAL_PACKET_SIZE + 1];
            Arrays.fill(tokens, "0");
        }

        if (nodeConfig.getGPIOConf().equals("ON")) {
            powerStatus = tokens[8];
            if (powerStatus.equals("0")) {
                if (!powok) {
                    log.info("Power OK");
                    powok = true;
                    powfail = false;
                }
            }
            if (powerStatus.equals("1")) {
                if (!powfail) {
                    log.warn("UnderVoltage");
                    powfail = true;
                    powok = false;

                }
            }
            if (powerStatus.equals("2")) {
                if (!powfail) {
                    log.warn("OverVoltage");
                    powfail = true;
                    powok = false;
                }
            }
            if (powerStatus.equals("-1")) {
                //just ignore it powerStatus is disabled
            }
        }

        int[] newSensorsValue = new int[SERIAL_PACKET_SIZE];
        for (int i = 0; i < SERIAL_PACKET_SIZE; i++) {
            try {
                newSensorsValue[i] = Integer.parseInt(tokens[i]);
            } catch (java.lang.NumberFormatException ex) {
                log.error("Unable to parseint " + tokens[i] + " " + ex);
                newSensorsValue[i] = defVal;
                //return;
            }
        }
        if (nodeConfig.getKF().equals("ON")) {
            newSensorsValue[0] = (int) kfaew.updateEstimation((float) ewaccdfH.filter(ewaccdfL.filter(newSensorsValue[0])));
            newSensorsValue[1] = (int) kfans.updateEstimation((float) nsaccdfH.filter(nsaccdfL.filter(newSensorsValue[1])));
            newSensorsValue[2] = (int) kfaz.updateEstimation((float) zaccdfH.filter(zaccdfL.filter(newSensorsValue[2])));
            newSensorsValue[3] = (int) kfvew.updateEstimation((float) ewdfH.filter(ewdfL.filter(newSensorsValue[3])));
            newSensorsValue[4] = (int) kfvns.updateEstimation((float) nsdfH.filter(nsdfL.filter(newSensorsValue[4])));
            newSensorsValue[5] = (int) kfvz.updateEstimation((float) zdfH.filter(zdfL.filter(newSensorsValue[5])));
        } else {
            newSensorsValue[0] = (int) ewaccdfH.filter(ewaccdfL.filter(newSensorsValue[0]));
            newSensorsValue[1] = (int) nsaccdfH.filter(nsaccdfL.filter(newSensorsValue[1]));
            newSensorsValue[2] = (int) zaccdfH.filter(zaccdfL.filter(newSensorsValue[2]));
            newSensorsValue[3] = (int) ewdfH.filter(ewdfL.filter(newSensorsValue[3]));
            newSensorsValue[4] = (int) nsdfH.filter(nsdfL.filter(newSensorsValue[4]));
            newSensorsValue[5] = (int) zdfH.filter(zdfL.filter(newSensorsValue[5]));
        }
        raw[0] = newSensorsValue[3];
        raw[1] = newSensorsValue[4];
        raw[2] = newSensorsValue[5];
        runstalta(raw);
        if (log.isTraceEnabled()) {
            log.trace("data from serial");
        }
        saveToFile(newSensorsValue);
        if (log.isTraceEnabled()) {
            log.trace("enter");
        }

        rawData.add(new RawData(newSensorsValue));
        if (rawData.size() == mseedSize) {
            int[] countsAccX = new int[mseedSize];
            int[] countsAccY = new int[mseedSize];
            int[] countsAccZ = new int[mseedSize];
            int[] countsVelX = new int[mseedSize];
            int[] countsVelY = new int[mseedSize];
            int[] countsVelZ = new int[mseedSize];
            int[] countsVolt = new int[mseedSize];
            int[] countsTemp = new int[mseedSize];
            for (int i = 0; i < mseedSize; i++) {
                countsAccX[i] = rawData.get(i).data[0];
                countsAccY[i] = rawData.get(i).data[1];
                countsAccZ[i] = rawData.get(i).data[2];
                countsVelX[i] = rawData.get(i).data[3];
                countsVelY[i] = rawData.get(i).data[4];
                countsVelZ[i] = rawData.get(i).data[5];
                countsVolt[i] = rawData.get(i).data[6];
                countsTemp[i] = rawData.get(i).data[7];
            }
            rawData.clear();

            float fn = getNyquistFrequency();
            float[] countdata = new float[3];
            float[][] geodata = new float[3][mseedSize];
//f1= f2/2 and f4 >= 2*f3. -> f2<f<f3 unity
            float fl = nodeConfig.getTLpfFc();
            float fh = nodeConfig.getTHpfFc();
            if (2 * fl > getNyquistFrequency()) {
                log.warn("Warning! " + 2 * fl + " must be less or equal than "
                        + getNyquistFrequency());
            }
            geodata[0] = TransferFunction.transferFunction(pzvx, countsVelX,
                    samplingFrequency, fh / 2, fh, fl, 2 * fl);
            geodata[1] = TransferFunction.transferFunction(pzvy, countsVelY,
                    samplingFrequency, fh / 2, fh, fl, 2 * fl);
            geodata[2] = TransferFunction.transferFunction(pzvz, countsVelZ,
                    samplingFrequency, fh / 2, fh, fl, 2 * fl);
            countdata[0] = countsVelX[countsVelX.length - 1];
            countdata[1] = countsVelY[countsVelY.length - 1];
            countdata[2] = countsVelZ[countsVelZ.length - 1];
            float[][] acceleration = null;
            if (geoacc) {
                acceleration = new float[3][mseedSize];
                acceleration[0] = TransferFunction.transferFunction(pzaccx, countsAccX,
                        samplingFrequency, 0.0375f, 0.075f, nodeConfig.getACCFc(), nodeConfig.getACCFc() * 2);
                acceleration[1] = TransferFunction.transferFunction(pzaccy, countsAccY,
                        samplingFrequency, 0.0375f, 0.075f, nodeConfig.getACCFc(), nodeConfig.getACCFc() * 2);
                acceleration[2] = TransferFunction.transferFunction(pzaccz, countsAccZ,
                        samplingFrequency, 0.0375f, 0.075f, nodeConfig.getACCFc(), nodeConfig.getACCFc() * 2);
            }
            for (int i = 0; i < mseedSize; i++) {
                float[] accel = null;
                if (acceleration != null) {
                    accel = new float[3];
                    accel[0] = acceleration[0][i];
                    accel[1] = acceleration[1][i];
                    accel[2] = acceleration[2][i];
                }

                float[] geo = new float[3];
                geo[0] = geodata[0][i];
                geo[1] = geodata[1][i];
                geo[2] = geodata[2][i];

                processedData.add(new ProcessedData(accel, geo, countsVolt[i], countsTemp[i], countdata));
            }
        }
        if (processedData.isEmpty()) {
            return;
        }
        ProcessedData data = processedData.remove(0);
        push(data.acceleration, data.geophoneData, data.volt, data.temp);
        if (tokens.length > (SERIAL_PACKET_SIZE + 1)) {
            log.warn("Received  message with invalid length: " + sample + " " + (tokens.length - (SERIAL_PACKET_SIZE + 1)) + " elements.");
            //return;
            tokens = new String[SERIAL_PACKET_SIZE + 1];
            Arrays.fill(tokens, "0");
            push("0 0 0 0 0 0 0 0 0");
        }
    }

    /**
     * @brief pushes data
     * @param acceleration acceleration data
     * @param geodata geophone data
     * @param volt volt data
     * @param temp temperature data
     */
    private void push(float[] acceleration, float[] geodata, float volt, float temp) {
        System.arraycopy(currentSensorValues, 0, prevSensorValues, 0, currentSensorValues.length);
        currentSensorValues[6] = volt;
        currentSensorValues[7] = temp;
//        //Alternative way of calculation: instead of using a transfer function of velocity
//        //load an transfer function of displacement. then use derivatives to calculate velocity and acceleration
//        // displacement data are ready
//        currentSensorValues[8] = (float) ewbw.filter(ewdf.filter(geodata[0]));
//        currentSensorValues[9] = (float) nsbw.filter(nsdf.filter(geodata[1]));
//        currentSensorValues[10] = (float) zbw.filter(zdf.filter(geodata[2]));
//
//        // first derivative of displacement data -> velocity data ready
//        currentSensorValues[3] = Derivative.calculate(getSamplingPeriod(),
//                prevSensorValues[8], currentSensorValues[8]);
//        currentSensorValues[4] = Derivative.calculate(getSamplingPeriod(),
//                prevSensorValues[9], currentSensorValues[9]);
//        currentSensorValues[5] = Derivative.calculate(getSamplingPeriod(),
//                prevSensorValues[10], currentSensorValues[10]);

        currentSensorValues[3] = geodata[0];
        currentSensorValues[4] = geodata[1];
        currentSensorValues[5] = geodata[2];

        currentSensorValues[8] = (float) ewbw.filter(iintx.calculate(geodata[0], getSamplingPeriod()));
        currentSensorValues[9] = (float) nsbw.filter(iinty.calculate(geodata[1], getSamplingPeriod()));
        currentSensorValues[10] = (float) zbw.filter(iintz.calculate(geodata[2], getSamplingPeriod()));
        if (geoacc && acceleration != null) {
            currentSensorValues[0] = acceleration[0];
            currentSensorValues[1] = acceleration[1];
            currentSensorValues[2] = acceleration[2];
            tempCurrentSensorValues[0] = (float) integAcc2Velx.calculate(acceleration[0], getSamplingPeriod());
            tempCurrentSensorValues[1] = (float) integAcc2Vely.calculate(acceleration[1], getSamplingPeriod());
            tempCurrentSensorValues[2] = (float) integAcc2Velz.calculate(acceleration[2], getSamplingPeriod());
            tempCurrentSensorValues[3] = (float) integAcc2Disx.calculate(tempCurrentSensorValues[0], getSamplingPeriod());
            tempCurrentSensorValues[4] = (float) integAcc2Disy.calculate(tempCurrentSensorValues[1], getSamplingPeriod());
            tempCurrentSensorValues[5] = (float) integAcc2Disz.calculate(tempCurrentSensorValues[2], getSamplingPeriod());
            // check if the geophone sensor has clipped; if yes switch to accelerometer
            if (currentSensorValues[3] >= Math.abs(0.0002745)) {
                currentSensorValues[3] = tempCurrentSensorValues[0];
                currentSensorValues[8] = tempCurrentSensorValues[3];
            }
            if (currentSensorValues[4] >= Math.abs(0.0002745)) {
                currentSensorValues[4] = tempCurrentSensorValues[1];
                currentSensorValues[9] = tempCurrentSensorValues[4];
            }
            if (currentSensorValues[5] >= Math.abs(0.0002745)) {
                currentSensorValues[5] = tempCurrentSensorValues[2];
                currentSensorValues[10] = tempCurrentSensorValues[5];
            }

        } else {
            // first derivative of velocity data -> acceleration data ready
            currentSensorValues[0] = Derivative.calculate(getSamplingPeriod(),
                    prevSensorValues[3], currentSensorValues[3]);
            currentSensorValues[1] = Derivative.calculate(getSamplingPeriod(),
                    prevSensorValues[4], currentSensorValues[4]);
            currentSensorValues[2] = Derivative.calculate(getSamplingPeriod(),
                    prevSensorValues[5], currentSensorValues[5]);
        }
        if (log.isTraceEnabled()) {
            log.trace("sending sensor events");
        }
        float[] copy = Arrays.copyOf(currentSensorValues, currentSensorValues.length);

        try {
            sendEvents(copy);
        } catch (Throwable t) {
            log.error("in sendEvents", t);
            throw new RuntimeException(t);
        }
        if (log.isTraceEnabled()) {
            log.trace("sending sensor events done");
            log.trace("entering stalta");
        }
        try {
            runTaucPd(copy);
        } catch (Throwable t) {
            log.error("in runStalta", t);
            throw new RuntimeException(t);
        }
        if (log.isTraceEnabled()) {
            log.trace("exiting stalta");
        }
    }

    /**
     * @brief pushes data
     * @param geodata geophone data
     * @param counts geophone counts for sta-lta use
     */
    @Override
    public void push(float geodata[], float counts[]) {
        push(null, geodata, 0, 0);
        runstalta(counts);
    }

    /**
     * @brief Runs the selected STA/LTA algorithm
     * @param sensorValues the sensor values
     */
    private void runstalta(float[] sensorValues) {
        boolean locked = lock.tryLock();
        if (locked) {
            int STALTAWindowSize = getSTALTAWindowSize();
            if (staltaX.size() != STALTAWindowSize) {
                throw new RuntimeException("DataIn_X size  not " + STALTAWindowSize);
            }
            if (staltaY.size() != STALTAWindowSize) {
                throw new RuntimeException("DataIn_Y size not " + STALTAWindowSize);
            }
            if (staltaZ.size() != STALTAWindowSize) {
                throw new RuntimeException("DataIn_Z size not " + STALTAWindowSize);
            }
            staltaX.remove(0);
            staltaX.add((double) sensorValues[0]);
            staltaY.remove(0);
            staltaY.add((double) sensorValues[1]);
            staltaZ.remove(0);
            staltaZ.add((double) sensorValues[2]);

            double[] sampleX = new double[STALTAWindowSize];
            double[] sampleY = new double[STALTAWindowSize];
            double[] sampleZ = new double[STALTAWindowSize];
            for (int i = 0; i < STALTAWindowSize; i++) {
                sampleX[i] = staltaX.get(i);
                sampleY[i] = staltaY.get(i);
                sampleZ[i] = staltaZ.get(i);
            }
            int _nsta = (int) (nsta * samplingFrequency);
            int _nlta = nlta * samplingFrequency;
            double[] charfctX = staLtaImpl.calculate(sampleX, _nsta, _nlta);
            double[] charfctY = staLtaImpl.calculate(sampleY, _nsta, _nlta);
            double[] charfctZ = staLtaImpl.calculate(sampleZ, _nsta, _nlta);
            double stX = charfctX[charfctX.length - 1];
            double stY = charfctY[charfctY.length - 1];
            double stZ = charfctZ[charfctZ.length - 1];
            sendStaltaEvents(stX, stY, stZ);
            computePointX(stX);
            computePointY(stY);
            computePointZ(stZ);
        } else {
            log.warn("locking failed, waiting");
            lock.lock();
            log.info("locking done");
        }
        lock.unlock();
    }

    /**
     * @brief Runs the Tauc Pd algorithm
     * @param sensorValues the sensor values
     */
    private void runTaucPd(float[] sensorValues) {
        boolean locked = lock.tryLock();
        if (locked) {
            if (triggered) {
                if (count == getTau0() - 1) {
                    trigdisp[count] = sensorValues[10]; //displacement in m
                    trigvel[count] = sensorValues[5];   //velocity in m/s
                    trigacc[count] = sensorValues[2];   //acceleration in m/s²
                    log.info("running tauC-Pd");
                    GeoEvent event = taucPd.taucPd(getSamplingPeriod(), getTau0(), trigacc, trigvel, trigdisp, tc, pd_m, amp_ew, amp_ns); //Run TauC-Pd algorithm
                    new Thread() {
                        @Override
                        public void run() {
                            sendTauEvents(event);
                        }
                    }.start();
                    count = 0;
                    triggered = false;
                } else if (count == 0) {
                    amp_ew = sensorValues[8];
                    amp_ns = sensorValues[9];
                    trigdisp[count] = sensorValues[10]; //displacement in m
                    trigvel[count] = sensorValues[5];   //velocity in m/s
                    trigacc[count] = sensorValues[2];   //acceleration in m/s²
                    count++;
                } else {
                    trigdisp[count] = sensorValues[10]; //displacement in m
                    trigvel[count] = sensorValues[5];   //velocity in m/s
                    trigacc[count] = sensorValues[2];   //acceleration in m/s²
                    count++;
                }
            }
        } else {
            log.warn("locking failed, waiting");
            lock.lock();
            log.info("locking done");
        }
        lock.unlock();
    }

    /**
     * @brief adds a new Sensor event listener
     * @param l the listener
     */
    @Override
    public void addListener(SensorEventListener l) {
        listeners.add(l);
    }

    /**
     * @brief removes a Sensor event listener
     * @param l the listener
     */
    @Override
    public void removeListener(SensorEventListener l) {
        listeners.remove(l);
    }

    /**
     * @brief Sends the events
     * @param sensorValues sensor values
     */
    private void sendEvents(float[] sensorValues) {
        SensorEventImpl e = new SensorEventImpl(this, System.currentTimeMillis());
        e.voltage = sensorValues[6];
        e.temperature = sensorValues[7];
        e.accelerationX = sensorValues[0];
        e.accelerationY = sensorValues[1];
        e.accelerationZ = sensorValues[2];
        e.velocityX = sensorValues[3];
        e.velocityY = sensorValues[4];
        e.velocityZ = sensorValues[5];
        e.displacementX = sensorValues[8];
        e.displacementY = sensorValues[9];
        e.displacementZ = sensorValues[10];
        Iterator<SensorEventListener> it = listeners.iterator();
        while (it.hasNext()) {
            SensorEventListener l = it.next();
            l.sensorEvent(e);
        }
    }

    /**
     * @brief adds a new stalta event listener
     * @param l the listener
     */
    @Override
    public void addStaltaListener(StaltaEventListener l) {
        staltaListeners.add(l);
    }

    /**
     * @brief removes a stalta event listener
     * @param l the listener
     */
    @Override
    public void removeStaltaListener(StaltaEventListener l) {
        staltaListeners.remove(l);
    }

    /**
     * @brief Sends the STA/LTA events
     * @param stX axis values
     * @param stY axis values
     * @param stZ axis values
     */
    private void sendStaltaEvents(double stX, double stY, double stZ) {
        StaltaEvent e = new StaltaEventImpl(stX, stY, stZ);
        Iterator<StaltaEventListener> it = staltaListeners.iterator();
        while (it.hasNext()) {
            StaltaEventListener l = it.next();
            l.staltaEvent(e);
        }
    }

    /**
     * @brief Sends the STA/LTA events
     * @param e the event
     */
    private void sendStaltaEvents(StaltaEvent e) {
        Iterator<StaltaEventListener> it = staltaListeners.iterator();
        while (it.hasNext()) {
            StaltaEventListener l = it.next();
            l.staltaEvent(e);
        }
    }

    /**
     * @brief creates a mseed test file
     */
    @Override
    public void createTestFile() {
        int hour = 60 * 60 * samplingFrequency;
        Random r = new Random();
        for (int i = 0; i < hour; i++) {
            testmsw.writemseedfile("TES", i, nodeConfig, getCalendar(), samplingFrequency);
            testmsw1.writemseedfile("TE1", r.nextInt((1000 - 1) + 1) + 1, nodeConfig, getCalendar(), samplingFrequency);
            testmsw2.writemseedfile("TE2", r.nextInt((1000 - 1) + 1) + 1, nodeConfig, getCalendar(), samplingFrequency);
            testmsw3.writemseedfile("TE3", r.nextInt((1000 - 1) + 1) + 1, nodeConfig, getCalendar(), samplingFrequency);
            testmsw4.writemseedfile("TE4", r.nextInt((1000 - 1) + 1) + 1, nodeConfig, getCalendar(), samplingFrequency);
            testmsw5.writemseedfile("TE5", r.nextInt((1000 - 1) + 1) + 1, nodeConfig, getCalendar(), samplingFrequency);
            //testmsw.writemseedfile("TES", i, nodeConfig, getCalendar(), samplingFrequency);
        }
    }

    /**
     * @brief Saves data to the specified mseed files
     * @param SensorsValue data
     */
    private void saveToFile(int[] SensorsValue) {
        //synchronized (saveToFileLock) {

        switch (nodeConfig.getMSEEDChan()) {
            case "up":
                wfza.writemseedfile(nodeConfig.getChannelName(2), SensorsValue[2], nodeConfig, calendar, samplingFrequency);
                wfzg.writemseedfile(nodeConfig.getChannelName(5), SensorsValue[5], nodeConfig, calendar, samplingFrequency);
                break;

            case "ew":
                wfxa.writemseedfile(nodeConfig.getChannelName(0), SensorsValue[0], nodeConfig, calendar, samplingFrequency);
                wfxg.writemseedfile(nodeConfig.getChannelName(3), SensorsValue[3], nodeConfig, calendar, samplingFrequency);
                break;

            case "ns":
                wfya.writemseedfile(nodeConfig.getChannelName(1), SensorsValue[1], nodeConfig, calendar, samplingFrequency);
                wfyg.writemseedfile(nodeConfig.getChannelName(4), SensorsValue[4], nodeConfig, calendar, samplingFrequency);
                break;

            case "upgeo":
                wfzg.writemseedfile(nodeConfig.getChannelName(5), SensorsValue[5], nodeConfig, calendar, samplingFrequency);
                break;

            case "all":
                wfxa.writemseedfile(nodeConfig.getChannelName(0), SensorsValue[0], nodeConfig, calendar, samplingFrequency);
                wfya.writemseedfile(nodeConfig.getChannelName(1), SensorsValue[1], nodeConfig, calendar, samplingFrequency);
                wfza.writemseedfile(nodeConfig.getChannelName(2), SensorsValue[2], nodeConfig, calendar, samplingFrequency);
                wfxg.writemseedfile(nodeConfig.getChannelName(3), SensorsValue[3], nodeConfig, calendar, samplingFrequency);
                wfyg.writemseedfile(nodeConfig.getChannelName(4), SensorsValue[4], nodeConfig, calendar, samplingFrequency);
                wfzg.writemseedfile(nodeConfig.getChannelName(5), SensorsValue[5], nodeConfig, calendar, samplingFrequency);
                break;

            default:
                wfxa.writemseedfile(nodeConfig.getChannelName(0), SensorsValue[0], nodeConfig, calendar, samplingFrequency);
                wfya.writemseedfile(nodeConfig.getChannelName(1), SensorsValue[1], nodeConfig, calendar, samplingFrequency);
                wfza.writemseedfile(nodeConfig.getChannelName(2), SensorsValue[2], nodeConfig, calendar, samplingFrequency);
                wfxg.writemseedfile(nodeConfig.getChannelName(3), SensorsValue[3], nodeConfig, calendar, samplingFrequency);
                wfyg.writemseedfile(nodeConfig.getChannelName(4), SensorsValue[4], nodeConfig, calendar, samplingFrequency);
                wfzg.writemseedfile(nodeConfig.getChannelName(5), SensorsValue[5], nodeConfig, calendar, samplingFrequency);
                log.info("invalid mseed channels option. check your " + System.getProperty("user.dir") + "/etc/easy/nodesettings.conf");
        }
        // }
    }

    /**
     * @brief Flushes mseed files during termination
     */
    public void flush() {
        switch (nodeConfig.getMSEEDChan()) {
            case "up":
                wfza.flushmseedfile(nodeConfig.getChannelName(2), nodeConfig, calendar, samplingFrequency);
                wfzg.flushmseedfile(nodeConfig.getChannelName(5), nodeConfig, calendar, samplingFrequency);
                break;

            case "ew":
                wfxa.flushmseedfile(nodeConfig.getChannelName(0), nodeConfig, calendar, samplingFrequency);
                wfxg.flushmseedfile(nodeConfig.getChannelName(3), nodeConfig, calendar, samplingFrequency);
                break;

            case "ns":
                wfya.flushmseedfile(nodeConfig.getChannelName(1), nodeConfig, calendar, samplingFrequency);
                wfyg.flushmseedfile(nodeConfig.getChannelName(4), nodeConfig, calendar, samplingFrequency);
                break;

            case "upgeo":
                wfzg.flushmseedfile(nodeConfig.getChannelName(5), nodeConfig, calendar, samplingFrequency);
                break;

            case "all":
                wfxa.flushmseedfile(nodeConfig.getChannelName(0), nodeConfig, calendar, samplingFrequency);
                wfya.flushmseedfile(nodeConfig.getChannelName(1), nodeConfig, calendar, samplingFrequency);
                wfza.flushmseedfile(nodeConfig.getChannelName(2), nodeConfig, calendar, samplingFrequency);
                wfxg.flushmseedfile(nodeConfig.getChannelName(3), nodeConfig, calendar, samplingFrequency);
                wfyg.flushmseedfile(nodeConfig.getChannelName(4), nodeConfig, calendar, samplingFrequency);
                wfzg.flushmseedfile(nodeConfig.getChannelName(5), nodeConfig, calendar, samplingFrequency);
                break;

            default:
                wfxa.flushmseedfile(nodeConfig.getChannelName(0), nodeConfig, calendar, samplingFrequency);
                wfya.flushmseedfile(nodeConfig.getChannelName(1), nodeConfig, calendar, samplingFrequency);
                wfza.flushmseedfile(nodeConfig.getChannelName(2), nodeConfig, calendar, samplingFrequency);
                wfxg.flushmseedfile(nodeConfig.getChannelName(3), nodeConfig, calendar, samplingFrequency);
                wfyg.flushmseedfile(nodeConfig.getChannelName(4), nodeConfig, calendar, samplingFrequency);
                wfzg.flushmseedfile(nodeConfig.getChannelName(5), nodeConfig, calendar, samplingFrequency);
                log.info("invalid mseed channels option. check your " + System.getProperty("user.dir") + "/etc/easy/nodesettings.conf");
        }
    }

    /**
     * @brief adds a new Geo event listener
     * @param l the listener
     */
    @Override
    public void addGeoEventListener(GeoEventListener l) {
        tauListeners.add(l);
    }

    /**
     * @brief removes a geo event listener
     * @param l the listener
     */
    @Override
    public void removeGeoEventListener(GeoEventListener l) {
        tauListeners.remove(l);
    }

    /**
     * @brief Sends an Tauc event
     * @param e the event
     */
    private void sendTauEvents(GeoEvent e) {
        Iterator<GeoEventListener> it = tauListeners.iterator();
        while (it.hasNext()) {
            GeoEventListener l = it.next();
            l.taucPdEvent(e);
        }
    }

    /**
     * @brief HPF and LPF initialisation
     */
    @Override
    public void loadFilters() {
        taucPd.getVelocityFilter().highPass(nodeConfig.
                getHpfOrd(), getSamplingFrequency(), 0.075, DirectFormAbstract.DIRECT_FORM_II);
        taucPd.getDisplacementFilter().highPass(
                nodeConfig.getHpfOrd(), getSamplingFrequency(), 0.075,
                DirectFormAbstract.DIRECT_FORM_II);
        nsbw.lowPass(nodeConfig.getLpfOrd(),
                getSamplingFrequency(), nodeConfig.getLpfFc(), DirectFormAbstract.DIRECT_FORM_II);
        ewbw.lowPass(nodeConfig.getLpfOrd(),
                getSamplingFrequency(), nodeConfig.getLpfFc(), DirectFormAbstract.DIRECT_FORM_II);
        zbw.lowPass(nodeConfig.getLpfOrd(),
                getSamplingFrequency(), nodeConfig.getLpfFc(), DirectFormAbstract.DIRECT_FORM_II);
        nsdfL.lowPass(nodeConfig.getDFOrdL(),
                getSamplingFrequency(), nodeConfig.getDFFcL(), DirectFormAbstract.DIRECT_FORM_II);
        ewdfL.lowPass(nodeConfig.getDFOrdL(),
                getSamplingFrequency(), nodeConfig.getDFFcL(), DirectFormAbstract.DIRECT_FORM_II);
        zdfL.lowPass(nodeConfig.getDFOrdL(),
                getSamplingFrequency(), nodeConfig.getDFFcL(), DirectFormAbstract.DIRECT_FORM_II);
        nsaccdfL.lowPass(nodeConfig.getDFOrdL(),
                getSamplingFrequency(), nodeConfig.getACCFc(), DirectFormAbstract.DIRECT_FORM_II);
        ewaccdfL.lowPass(nodeConfig.getDFOrdL(),
                getSamplingFrequency(), nodeConfig.getACCFc(), DirectFormAbstract.DIRECT_FORM_II);
        zaccdfL.lowPass(nodeConfig.getDFOrdL(),
                getSamplingFrequency(), nodeConfig.getACCFc(), DirectFormAbstract.DIRECT_FORM_II);
        nsdfH.highPass(nodeConfig.getDFOrdH(),
                getSamplingFrequency(), nodeConfig.getDFFcH(), DirectFormAbstract.DIRECT_FORM_II);
        ewdfH.highPass(nodeConfig.getDFOrdH(),
                getSamplingFrequency(), nodeConfig.getDFFcH(), DirectFormAbstract.DIRECT_FORM_II);
        zdfH.highPass(nodeConfig.getDFOrdH(),
                getSamplingFrequency(), nodeConfig.getDFFcH(), DirectFormAbstract.DIRECT_FORM_II);
        nsaccdfH.highPass(nodeConfig.getDFOrdH(),
                getSamplingFrequency(), nodeConfig.getDFFcH(), DirectFormAbstract.DIRECT_FORM_II);
        ewaccdfH.highPass(nodeConfig.getDFOrdH(),
                getSamplingFrequency(), nodeConfig.getDFFcH(), DirectFormAbstract.DIRECT_FORM_II);
        zaccdfH.highPass(nodeConfig.getDFOrdH(),
                getSamplingFrequency(), nodeConfig.getDFFcH(), DirectFormAbstract.DIRECT_FORM_II);
    }

    /**
     * @brief plot a pixel on a graph
     * @param staltaValueX data to be plotted
     */
    private void computePointX(double staltaValueX) {
        double temp = staltaValueX;
        double trig = getStaltaTrigger();
        double detrig = getStaltaDeTrigger();
        if ((temp != 0) && (!Double.isNaN(temp))) {
            // value above maximum threshold
            if (temp >= trig) {
                if (!trigxon) {  //new event declare it
                    sendStaltaEvents(new StaltaEventImpl(StaltaEvent.AXIS_X, true, System.currentTimeMillis(), 0));
                    Swavetsew = System.currentTimeMillis();
                    if ((!ctrl) && (!timeRec.isEmpty())) {
                        long diffpstsew;

                        diffpstsew = Swavetsew - timeRec.get(0);
                        ctrl = true;
                        sendStaltaEvents(new StaltaEventImpl(StaltaEvent.AXIS_X, System.currentTimeMillis(),
                                diffpstsew, true));

                    }
                    trigxon = true;
                }
            }
            // value below minimum threshold
            if (temp <= detrig) {
                if (trigxon) {  // event has ended declare it
                    long countx = System.currentTimeMillis();
                    sendStaltaEvents(new StaltaEventImpl(
                            StaltaEvent.AXIS_X, false, countx, countx - Swavetsew));

                    trigxon = false;
                    if (ctrl) {
                        ctrl = false;
                        sendStaltaEvents(new StaltaEventImpl(StaltaEvent.AXIS_X, System.currentTimeMillis(),
                                0, false));
                        timeRec.clear(); // event cleared remove Z axis timestamps
                    }
                }
            }
        }
    }

    /**
     * @brief plot a pixel on a graph
     * @param staltaValueY data to be plotted
     */
    private void computePointY(double staltaValueY) {
        double temp = staltaValueY;
        double trig = getStaltaTrigger();
        double detrig = getStaltaDeTrigger();
        if ((temp != 0) && (!Double.isNaN(temp))) {
            // value above maximum threshold
            if (temp >= trig) {
                if (!trigyon) {  //new event declare it
                    sendStaltaEvents(new StaltaEventImpl(StaltaEvent.AXIS_Y, true, System.currentTimeMillis(), 0));
                    Swavetsns = System.currentTimeMillis();
                    if ((!ctrl) && (!timeRec.isEmpty())) {
                        long diffpstsns = Swavetsns - timeRec.get(0);
                        ctrl = true;
                        sendStaltaEvents(new StaltaEventImpl(StaltaEvent.AXIS_Y, System.currentTimeMillis(),
                                diffpstsns, true));

                    }
                    trigyon = true;
                }
            }
            // value below minimum threshold
            if (temp <= detrig) {
                if (trigyon) {  // event has ended declare it
                    long county = System.currentTimeMillis();
                    sendStaltaEvents(
                            new StaltaEventImpl(StaltaEvent.AXIS_Y, false, county, county - Swavetsns));
                    trigyon = false;
                    if (ctrl) {
                        ctrl = false;
                        sendStaltaEvents(new StaltaEventImpl(StaltaEvent.AXIS_Y, System.currentTimeMillis(),
                                0, false));
                        timeRec.clear(); // event cleared remove Z axis timestamps
                    }
                }
            }
        }
    }

    /**
     * @brief plot a pixel on a graph
     * @param staltaValueZ data to be plotted
     */
    private void computePointZ(double staltaValueZ) {
        double temp = staltaValueZ;
        double trig = getStaltaTrigger();
        double detrig = getStaltaDeTrigger();
        // value above maximum threshold
        if ((temp != 0) && (!Double.isNaN(temp))) {
            if (temp >= trig) {
                if (!trigzon) {  //new event declare it
                    log.info("Z axis trigger");
                    sendStaltaEvents(
                            new StaltaEventImpl(StaltaEvent.AXIS_Z, true,
                                    System.currentTimeMillis(), 0));

                    Pwavets = System.currentTimeMillis();
                    timeRec.add(Pwavets);
                    setTriggered(true);
                    trigzon = true;
                }
            }
            // value below minimum threshold
            if (temp <= detrig) {
                if (trigzon) {  // event has ended declare it
                    long countz = System.currentTimeMillis();
                    long trigOnz = countz - Pwavets;
                    sendStaltaEvents(
                            new StaltaEventImpl(StaltaEvent.AXIS_Z, false, countz, trigOnz));
                    //setTriggered(false);
                    trigzon = false;
                }
            }
        }
    }

    /**
     * @brief Initialises an arraylist
     * @param list Data for initialisation
     * @param count Data for initialisation
     */
    private void init(ArrayList<Double> list, int count) {
        double k = 1000; // if it is set to 0 sta/lta will trigger when started
        list.clear();
        for (int i = 0; i < count; i++) {
            list.add(k);
        }

    }

    /**
     * @brief Retrieves the node's Hpf cutoff frequency fot the Transfer()
     * @return
     */
    @Override
    public float getTHpfFc() {
        return nodeConfig.getTHpfFc();
    }

    /**
     * @brief Retrieves the node's Lpf cutoff frequency fot the Transfer()
     * @return
     */
    @Override
    public float getTLpfFc() {
        return nodeConfig.getTLpfFc();
    }
}
