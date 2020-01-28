/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class for reading mSEED files
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.sv.easy.common.LoadSeedFile;

/**
 * @brief Loads the node configuration file
 */
public class NodeConfigLoader {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(LoadSeedFile.class);

    /**
     * @brief loads configuration from file
     * @param filename the file
     */
    public static NodeConfig loadFromFile(String filename) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(filename);
        }
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(filename)));
        } catch (FileNotFoundException ex) {
            LOGGER.error(filename, ex);
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            LOGGER.error(filename, ex);
            throw new RuntimeException(ex);
        }
        NodeConfig config = new NodeConfig(props);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(config.toString());
        }
        return config;
    }

    private NodeConfigLoader() {
    }
}
