/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class for writing mSEED files
 * of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.logging;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimException;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.sv.easy.config.NodeConfig;

/**
 * @brief Basically it is a thread that opens an mSEED file and fills it with
 * data from an array of counts
 */
public class SeedFileLogger {

    private static final Logger LOGGER = Logger.getLogger(SeedFileLogger.class);
    private final DateFormat datestamp = new SimpleDateFormat("yyyy_MMM_dd_", Locale.US);
    private final SerialFileLogger sl = new SerialFileLogger();
    private final NodeConfig nodeConfig;
    private final int mseedSize;
    private final Calendar calendar;
    private final int samplingFrequency;
    private final int[] data;
    private final int[] dump;
    private int offset = 0;
    private int seq = 0;
    private Date lastSaveDate = new Date();
    private Btime btime;

    /**
     * @brief Class constructor
     * @param mseedSize the size of mseed file
     */
    public SeedFileLogger(int mseedSize) {
        this.btime = new Btime(new Date());
        this.nodeConfig = null;
        this.mseedSize = mseedSize;
        this.calendar = null;
        this.samplingFrequency = 0;
        data = new int[mseedSize];
        dump = new int[mseedSize];
    }

    /**
     * @brief Class constructor
     * @param nodeConfig node configuration options
     * @param mseedSize mseed size
     * @param calendar calendar instance
     * @param samplingFrequency sampling frequency
     */
    private SeedFileLogger(NodeConfig nodeConfig, int mseedSize, Calendar calendar, int samplingFrequency) {
        this.btime = new Btime(new Date());
        this.nodeConfig = nodeConfig;
        this.mseedSize = mseedSize;
        this.calendar = calendar;
        this.samplingFrequency = samplingFrequency;
        data = new int[mseedSize];
        dump = new int[mseedSize];
    }

    /**
     * @brief writes to the mseed file
     * @param channel channel
     * @param data data
     */
    private void write(String channel, int[] data) {
        writemseedfile(channel, data, nodeConfig, calendar, samplingFrequency);
    }

    /**
     * @brief flushes the buffer of the mseed file
     * @param channel channel
     * @param nodeConfig configuration options
     * @param calendar calendar
     * @param samplingFrequency the sampling frequency
     */
    public void flushmseedfile(final String channel,
            final NodeConfig nodeConfig, final Calendar calendar,
            final int samplingFrequency) {
        if (offset == 0) {
            return;
        }
        int length;
        int[] temp = new int[offset];
        length = offset;
        System.arraycopy(dump, 0, temp, 0, length);
        boolean allFited = false;
        int remSamples = offset;
        int fittedSamples = writemseedfile(channel, temp, nodeConfig, calendar, samplingFrequency);
        remSamples -= fittedSamples;
        if (remSamples != 0) {
            while (!allFited) {
                int[] leftover = new int[remSamples];
                System.arraycopy(this.data, fittedSamples, leftover, 0, leftover.length);
                if (leftover.length == 0) {
                    return;
                }
                fittedSamples = writemseedfile(channel, leftover, nodeConfig, calendar, samplingFrequency);
                remSamples -= fittedSamples;
                if (remSamples <= 0) {
                    // all samples fit into records
                    allFited = true;
                }
            }
        }
    }

    /**
     * @brief writes to the mseed file. If data wont fit into a record blockette
     * it splits them so they can fit
     * @param channel channel
     * @param data data
     * @param nodeConfig configuration options
     * @param calendar calendar
     * @param samplingFrequency the sampling frequency
     */
    public void writemseedfile(final String channel, final int data, final NodeConfig nodeConfig, final Calendar calendar,
            final int samplingFrequency) {
        this.data[offset] = data;
        System.arraycopy(this.data, 0, dump, 0, offset);
        offset++;
        if (offset == mseedSize) {
            int remSamples = mseedSize;
            boolean allFited = false;
            int fittedSamples = 0;//writemseedfile(channel, this.data, nodeConfig, calendar, samplingFrequency);
            //remSamples -= fittedSamples;
            if (remSamples != 0) {
                while (!allFited) {
                    int[] leftover = new int[remSamples];
                    System.arraycopy(this.data, fittedSamples, leftover, 0, leftover.length);
                    if (leftover.length == 0) {
                        return;
                    }
                    fittedSamples = writemseedfile(channel, leftover, nodeConfig, calendar, samplingFrequency);
                    remSamples -= fittedSamples;
                    if (remSamples <= 0) {
                        allFited = true;
                    }
                }
            }
            offset = 0;
        }
    }

    /**
     * @brief writes to the mseed file
     * @param Channel channel
     * @param data data
     * @param nodeConfig configuration options
     * @param calendar calendar
     * @param samp_rate the sampling rate
     * @return num of samples writen
     *
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "ConvertToTryWithResources"})
    public int writemseedfile(final String Channel, final int[] data,
            final NodeConfig nodeConfig, final Calendar calendar,
            final int samp_rate) {
        Date now = new Date();

        final String StationID = nodeConfig.getStationId();
        final String NetworkCode = nodeConfig.getNetworkCode();
        final String LocationID = nodeConfig.getLocationId();
        int out = 0;
        try {
            String outFilename = System.getProperty("user.dir") + "/mseed/" + NetworkCode + "." + StationID + "." + Channel + "." + LocationID + "." + Calendar.getInstance().get(Calendar.YEAR) + "." + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + ".mseed";

            byte seed512 = 9;
            try {
                seq++;
                DataHeader header = new DataHeader(seq, 'D', false);
                header.setStationIdentifier(StationID);
                header.setChannelIdentifier(Channel);
                header.setNetworkCode(NetworkCode);
                header.setLocationIdentifier(LocationID);
                header.setSampleRate(samp_rate);
                header.setStartBtime(btime);

                DataRecord record = new DataRecord(header);
                Blockette1000 blockette1000 = new Blockette1000();
                blockette1000.setEncodingFormat((byte) B1000Types.STEIM2);
                blockette1000.setWordOrder(Blockette1000.SEED_BIG_ENDIAN);
                blockette1000.setDataRecordLength(seed512);
                record.addBlockette(blockette1000);
                SteimFrameBlock steimData;
                steimData = Steim2.encode(data, 7);
                record.setData(steimData.getEncodedData());
                header.setNumSamples((short) steimData.getNumSamples());
                btime = record.getPredictedNextStartBtime();
                DataOutputStream seed_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFilename, true)));
                record.write(seed_out);
                out = steimData.getNumSamples();
                

                //execute on a new day
                if (sl.isNextDay(lastSaveDate, now)) {
                    seq = 0;
                    btime = new Btime(new Date());
                    Path movefrom = Paths.get(System.getProperty("user.dir") + "/mseed/" + NetworkCode + "." + StationID + "." + Channel + "." + LocationID + "." + Calendar.getInstance().get(Calendar.YEAR) + "." + (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1) + ".mseed");
                    Path moveto = Paths.get(System.getProperty("user.dir") + "/var/log/mseed-archive/" + NetworkCode + "." + StationID + "." + Channel + "." + LocationID + "." + Calendar.getInstance().get(Calendar.YEAR) + "." + (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1) + ".mseed");
                    try {
                        Files.move(movefrom, moveto, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
                lastSaveDate = now;
                seed_out.close();
            } catch (SeedFormatException sef) {
                LOGGER.error("Bad seed formating: ", sef);
            } catch (IOException ioe) {
                LOGGER.error("IO error while writing seed file: ", ioe);
            } catch (SteimException see) {
                LOGGER.error("STEIM compression: ", see);
            } catch (Exception exe) {
                LOGGER.error("Error while writing to Seed file: ", exe);
            }
        } catch (Throwable ttseed) {
            LOGGER.error("general throwable exception " + ttseed);
        } finally {
        }
        return out;
    }
}
