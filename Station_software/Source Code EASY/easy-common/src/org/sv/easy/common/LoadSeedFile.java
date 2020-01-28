/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that loads the seed file
 * into the application
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import edu.sc.seis.seisFile.sac.SacPoleZero;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.EasyEngine;

/**
 * @brief This contains the class that loads the seed file into the application
 * and acts like an ActionListener
 */
@SuppressWarnings({"CollectionWithoutInitialCapacity"})
public class LoadSeedFile extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(LoadSeedFile.class);

    private final SacPoleZero pzrecz;
    private final SacPoleZero pzrecx;
    private final SacPoleZero pzrecy;
    private final String xchanelfile;
    private final String ychanelfile;
    private final String zchanelfile;
    private final String xpzc;
    private final String ypzc;
    private final String zpzc;
    private float samp_rate;
    private float fn;
    private float[][] seedtestx;
    private float[][] seedtesty;
    private float[][] seedtestz;
    private int pos = 0;
    private final Timer timer = new Timer();
    private EasyEngine seedListener;
    private NodeConfig nodeConfig;

//    public LoadSeedFile(String fnx, String fny, String fnz) {
//        xchanelfile = fnx;
//        ychanelfile = fny;
//        zchanelfile = fnz;
//        try {
//            //load sacpz file for recordings
//            pzrecx = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/recBHE.sacpz");
//            pzrecy = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/recBHN.sacpz");
//            pzrecz = new SacPoleZero(System.getProperty("user.dir") + "/etc/easy/recBHZ.sacpz");
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//
//    }
    /**
     * @brief Class constructor
     * @param fnx MSEED file for X axis
     * @param fny MSEED file for Y axis
     * @param fnz MSEED file for Z axis
     * @param sacpzx Pole zero file for X axis
     * @param sacpzy Pole zero file for Y axis
     * @param sacpzz Pole zero file for Z axis
     */
    public LoadSeedFile(String fnx, String fny, String fnz, String sacpzx, String sacpzy, String sacpzz) {
        xchanelfile = fnx;
        ychanelfile = fny;
        zchanelfile = fnz;
        xpzc = sacpzx;
        ypzc = sacpzy;
        zpzc = sacpzz;
        try {
            //load sacpz file for recordings
            pzrecx = new SacPoleZero(xpzc);
            pzrecy = new SacPoleZero(ypzc);
            pzrecz = new SacPoleZero(zpzc);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * @brief Reads files from the three axes and loads its contents on arrays
     * @param seedListener the seed listener
     */
    public void loadData(final EasyEngine seedListener) {
        this.seedListener = seedListener;
        samp_rate = seedListener.getSamplingFrequency();
        fn = seedListener.getNyquistFrequency();
        seedtestx = readmseedfile(xchanelfile, pzrecx);
        seedtesty = readmseedfile(ychanelfile, pzrecy);
        seedtestz = readmseedfile(zchanelfile, pzrecz);
        timer.schedule(this, seedListener.getSamplingPeriodMillis(),
                seedListener.getSamplingPeriodMillis());
    }

    /**
     * @brief Reads data from mSEED file
     * @return A two dimensional array containing physical values in m/s and
     * samples expressed in counts that were read from the miniSEED file
     * @exception SeedFormatException when bad formatting occurs
     * @exception EOFException when end of file or end of stream has been
     * reached unexpectedly during input
     * @exception SteimException when problems encountered with Steim
     * compression.
     */
    private float[][] readmseedfile(String filename, SacPoleZero pz) {
        List<DataRecord> drList = new ArrayList<DataRecord>();
        try {
            DataInput dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
//            PrintWriter seed_out = new PrintWriter(System.out, true);
            int size = 0;
            while (true) {
                try {

                    SeedRecord sr = SeedRecord.read(dis, size);
                    size = sr.getRecordSize(); //dynamicaly set data record size
                    if (sr instanceof DataRecord) {
                        DataRecord dr = (DataRecord) sr;
                        drList.add(dr);
                    } else {
                        LOGGER.info(sr + " Not a DataRecord, skipping...");
                    }
                } catch (EOFException eofe) {
//                    seed_out.close();

                    break;
                } catch (IOException ioe) {
                    LOGGER.error("Seed file not found: ", ioe);
                } catch (SeedFormatException sef) {
                    LOGGER.error("Bad seed formating occured: ", sef);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while reading seed file: ", e);
        }
        int numPts = 0;
        for (DataRecord dr : drList) {
            numPts += dr.getHeader().getNumSamples();
        }
        float[][] data = new float[numPts][2];
        float[] value = new float[numPts];
        int[] counts = new int[numPts];

        int numSoFar = 0;
        try {
            for (DataRecord dr : drList) {
                DecompressedData decompData = dr.decompress();
                int[] temp = decompData.getAsInt();
                float fl = seedListener.getTLpfFc();
                float fh = seedListener.getTHpfFc();
                if (2 * fl > seedListener.getNyquistFrequency()) {
                    LOGGER.warn("Warning! " + 2 * fl + " must be less or equal than " + seedListener.getNyquistFrequency());
                }
                TransferFunction tf = new TransferFunction(pz, samp_rate, 0.075f, fh, fl, fl * 2);
                float[] out = tf.transferFunction(temp);
                //stores physical values
                System.arraycopy(out, 0, value, numSoFar, out.length);
                //stores counts
                System.arraycopy(temp, 0, counts, numSoFar, temp.length);
                numSoFar += temp.length;
            }

        } catch (UnsupportedCompressionType ucte) {
            LOGGER.error("Unsupported compression type for seed file: ", ucte);
        } catch (CodecException ce) {
            LOGGER.error("Wrong Codec: ", ce);
        } catch (SeedFormatException sef) {
            LOGGER.error("Bad seed formating: ", sef);
        } catch (Exception e) {
            LOGGER.error("Error uppon reading seed file: ", e);
        }
        for (int i = 0; i < numPts; ++i) {

            data[i][0] = value[i];
            data[i][1] = counts[i];
        }
        return data;
    }

    @Override
    public void run() {
        try {
            if (pos == seedtestz.length) {
                seedListener.push(new float[3], new float[3]);
                return;
            }
            float[] data = new float[3];
            data[0] = seedtestx[pos][0];
            data[1] = seedtesty[pos][0];
            data[2] = seedtestz[pos][0];
            float[] counts = new float[3];
            counts[0] = seedtestx[pos][1];
            counts[1] = seedtesty[pos][1];
            counts[2] = seedtestz[pos][1];

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(pos + "/" + seedtestz.length);
            }
            seedListener.push(data, counts);
            pos++;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
        }
    }
}
