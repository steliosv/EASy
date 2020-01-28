/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Streamgobbler class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.log4j.Logger;

/**
 * @brief Basically it is a thread that does nothing but consume data from an
 * input stream until stopped
 */
public class StreamGobbler extends Thread {

    private InputStream is;
    private String type;
    //OutputStream os;
    Logger log = Logger.getLogger(StreamGobbler.class);

    /**
     * @brief Class constructor
     * @param is The input strream
     * @param type Text specifier
     */
    public StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }

    /**
     * @brief Class constructor
     * @param is The input stream
     * @param type Text Specifier
     * @param redirect The output stream
     *
     */
    public StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
    }

    /**
     * @brief consume data from a given input stream
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(type + line);
            }
        } catch (IOException ioe) {
            log.error("Could not init StreamGobbler: ", ioe);
        }
    }
}
