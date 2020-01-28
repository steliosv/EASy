/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the methods that are responsible
 * for the file I/o of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.log4j.Logger;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.SecCounter;

public class SerialFileLogger {

    private SecCounter sc = new SecCounter();
    private int cntSec = 0;
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static final String VAR_NAME = "var";
    private static final String LOG_NAME = "log/digitiser_logs";
    private static final Logger LOGGER = Logger.getLogger(SerialFileLogger.class);
    private final File logDirectory;
    private final DateFormat timestampFormat;
    private PrintWriter out = null;
    private Date lastSaveDate = new Date();

    /**
     * @brief Class constructor
     */
    public SerialFileLogger() {

        timestampFormat = org.sv.easy.common.DateUtils.getDateFormat();
        File var = new File(VAR_NAME);
        var.mkdir();
        logDirectory = new File(var, LOG_NAME);
        logDirectory.mkdir();

    }

    /**
     * @brief Appends data from serial port to a file
     * @param text File that data will be appended into it
     * @param nodeConfig
     *
     */
    public synchronized void appendTextToFile(String text, final NodeConfig nodeConfig) {

        Date now = new Date();
        String StationID = nodeConfig.getStationId();
        final String NetworkCode = nodeConfig.getNetworkCode();
        final String LocationID = nodeConfig.getLocationId();
        if (isNextDay(lastSaveDate, now) && out != null) {
            out.close();
            out = null;
        }
        lastSaveDate = now;
        if (out == null) {
            String name = NetworkCode + "." + StationID + "." + LocationID + "." + Calendar.getInstance().get(Calendar.YEAR) + "." + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + ".rawdata";
            File file = new File(logDirectory, name);
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            } catch (IOException ex) {
                LOGGER.error("Failed to open file: ", ex);
                return;
            }
        }
            out.print(text.trim());
            out.print(" ");
            out.println(timestampFormat.format(now));
            out.flush();
    }

    /**
     * @brief Checks if a day has passed
     * @param earlier Last day
     * @param later New day
     * @return returns true/false depending on if a day has passed
     */
    public boolean isNextDay(Date earlier, Date later) {
        boolean isNextDay = false;
        Calendar cEarlier = Calendar.getInstance();
        Calendar cLater = Calendar.getInstance();
        cEarlier.setTime(earlier);
        cLater.setTime(later);
        if (cLater.after(cEarlier)) {
            boolean dayIsAfter = cLater.get(Calendar.DAY_OF_YEAR) > cEarlier.get(Calendar.DAY_OF_YEAR);
            boolean yearIsAfter = cLater.get(Calendar.YEAR) > cEarlier.get(Calendar.YEAR);
            isNextDay = dayIsAfter || yearIsAfter;
        }
        return isNextDay;
    }

    /**
     * @brief Closes the file before the application terminates
     */
    public void close() {
        if (out != null) {
            out.flush(); // Writes the remaining data to the file
            out.close(); // Finishes the file
        }
    }

}
