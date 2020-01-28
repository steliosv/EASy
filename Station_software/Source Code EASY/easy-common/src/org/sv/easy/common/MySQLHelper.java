/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the methods for the necessary sql
 * queries
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.sv.easy.api.map.MapNode;
import org.sv.easy.api.map.MapNodeProvider;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;

/**
 * @brief Class that contains methods that control the sql database
 */
@SuppressWarnings({"CollectionWithoutInitialCapacity"})
public class MySQLHelper implements MapNodeProvider, EasyPlotListener {

    private final ExecutorService executor;
    public static final Logger log = Logger.getLogger(MySQLHelper.class);
    private String hostname = "localhost";
    private int port = 3306;
    private String username = "easyadmin";
    private String password = "toor";
    private String db = "easyDB";
    private boolean triggered = false;

    /**
     * @brief Class constructor
     */
    public MySQLHelper() {
        executor = Executors.newSingleThreadExecutor();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();// .getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @brief Retrieves the hostname
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @brief Sets the hostname
     * @param hostname the hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @brief Retrieves the port
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @brief Sets the port
     * @param port the port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @brief Retrieves the username
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @brief Sets the username
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @brief Retrieves the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @brief Sets the password
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @brief Retrieves the db
     * @return the db
     */
    public String getDb() {
        return db;
    }

    /**
     * @brief Sets the db
     * @param db the database
     */
    public void setDb(String db) {
        this.db = db;
    }

    /**
     * @brief Retrievees the triggered state
     * @return the triggered state
     */
    @Override
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * @brief Sets the triggered state
     * @param triggered the triggered value
     */
    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    /**
     * @brief Creates a connection between the application and the database
     */
    private Connection openConnection() {
        Connection conn = null;
        final String url = "jdbc:mysql://" + hostname + ":" + port + "/" + db;
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    /**
     * @brief Updates database with triggered/detriggered event
     * @param nodeConfig The mysql query to be executed
     */
    public void sqlDetrigger(NodeConfig nodeConfig) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Connection conn = openConnection();
                if (conn == null) {
                    return;
                }
                String quer = "UPDATE Nodes SET Triggered = 0 WHERE StationID =" + "'" + nodeConfig.getStationId() + "'";
                Statement stmt;
                try {
                    stmt = conn.createStatement();
                    int rupd = stmt.executeUpdate(quer);
                    if (rupd > 0) {
                        log.info("Detrigger update finished successfully!");
                    }
                    stmt.close();
                } catch (SQLException se) {
                    log.error("Error while sql querring: ", se);
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    /**
     * @brief Updates database with triggered/detriggered event
     * @param mw Magnitude of the event
     * @param timestamp Timestamp of the event
     * @param StationID the station's ID
     */
    public void sqlTrigger(double mw, String timestamp, String StationID) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Connection conn = openConnection();
                if (conn == null) {
                    return;
                }

                String quer = "UPDATE `Nodes` SET `Triggered` = 1, `Magnitude` = ?, `Timestamp` = ? WHERE `StationID` = ?";

                try {
                    PreparedStatement statement = conn.prepareStatement(quer);
                    statement.setFloat(1, (float) mw);
                    statement.setString(2, timestamp);
                    statement.setString(3, StationID);

                    int rupd = statement.executeUpdate();
                    if (rupd > 0) {
                        log.info("Trigger update finished successfully! " + rupd);
                    }
                    statement.close();

                } catch (SQLException se) {
                    log.error("Error while sql querring: ", se);
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    /**
     * @brief Registers the event to the database
     * @param StationID The node's StationID
     * @param maxrecaz Peak Ground Acceleration
     * @param maxrecvz Peak Ground Velocity
     * @param maxrecdz Peak Ground Displacement
     * @param tauc Calculated Tc
     * @param azi Estimated Azimuth
     * @param mw Estimated Moment Magnitude
     * @param timestamp Timestamp of the event
     */
    public void sqlRegister(String StationID, double maxrecaz, double maxrecvz, double maxrecdz, double tauc, double azi, double mw, String timestamp) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Connection conn = openConnection();
                if (conn == null) {
                    return;
                }

                try {
                    String quer = "INSERT INTO `Triggering` (`StationID`, `maxrecaz`, `maxrecvz`, `maxrecdz`, `tauc`, `azi`, `mw`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(quer);
                    statement.setString(1, StationID);
                    statement.setDouble(2, maxrecaz);
                    statement.setDouble(3, maxrecvz);
                    statement.setDouble(4, maxrecdz);
                    statement.setDouble(5, tauc);
                    statement.setDouble(6, azi);
                    statement.setDouble(7, mw);
                    statement.setString(8, timestamp);
                    int rupd = statement.executeUpdate();
                    if (rupd > 0) {
                        log.info("Event registered successfully! " + rupd);
                    }
                    statement.close();
                } catch (SQLException se) {
                    log.error("Error while sql querring: ", se);
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    /**
     * @brief Registers a node to the database
     * @param nodeConfig Channel configuration
     */
    @Override
    public void coordinates(NodeConfig nodeConfig) {
        Connection conn = openConnection();
        if (conn == null) {
            return;
        }
        float Magnitude = 0.0f;
        String Timestamp = "-";
        boolean Triggered = false;

        try {

            String quer = "INSERT INTO `Nodes` (`Channel_0`, `Channel_1`, `Channel_2`, `Channel_3`, `Channel_4`, `Channel_5`, `Channel_6`, `Channel_7`, `Samplerate`, `Location`, `NetworkCode`, `StationID`, `LocationID`, `Latitude`, `Longitude`, `Magnitude`, `Timestamp`, `Triggered`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE       `Channel_0`=?, `Channel_1`=?, `Channel_2`=?, `Channel_3`=?, `Channel_4`=?, `Channel_5`=?, `Channel_6`=?, `Channel_7`=?, `Samplerate`=?, `Location`=?, `NetworkCode`=?, `StationID`=?, `LocationID`=?, `Latitude`=?, `Longitude`=?, `Magnitude`=?, `Timestamp`=?, `Triggered`=?";
            PreparedStatement statement = conn.prepareStatement(quer);

            statement.setString(1, nodeConfig.getChannelName(0));
            statement.setString(2, nodeConfig.getChannelName(1));
            statement.setString(3, nodeConfig.getChannelName(2));
            statement.setString(4, nodeConfig.getChannelName(3));
            statement.setString(5, nodeConfig.getChannelName(4));
            statement.setString(6, nodeConfig.getChannelName(5));
            statement.setString(7, nodeConfig.getChannelName(6));
            statement.setString(8, nodeConfig.getChannelName(7));
            statement.setString(9, nodeConfig.getSampleFrequency());
            statement.setString(10, nodeConfig.getLocation());
            statement.setString(11, nodeConfig.getNetworkCode());
            statement.setString(12, nodeConfig.getStationId());
            statement.setString(13, nodeConfig.getLocationId());
            statement.setString(14, nodeConfig.getLatitude());
            statement.setString(15, nodeConfig.getLongitude());
            statement.setFloat(16, Magnitude);
            statement.setString(17, Timestamp);
            statement.setBoolean(18, Triggered);
            statement.setString(19, nodeConfig.getChannelName(0));
            statement.setString(20, nodeConfig.getChannelName(1));
            statement.setString(21, nodeConfig.getChannelName(2));
            statement.setString(22, nodeConfig.getChannelName(3));
            statement.setString(23, nodeConfig.getChannelName(4));
            statement.setString(24, nodeConfig.getChannelName(5));
            statement.setString(25, nodeConfig.getChannelName(6));
            statement.setString(26, nodeConfig.getChannelName(7));
            statement.setString(27, nodeConfig.getSampleFrequency());
            statement.setString(28, nodeConfig.getLocation());
            statement.setString(29, nodeConfig.getNetworkCode());
            statement.setString(30, nodeConfig.getStationId());
            statement.setString(31, nodeConfig.getLocationId());
            statement.setString(32, nodeConfig.getLatitude());
            statement.setString(33, nodeConfig.getLongitude());
            statement.setFloat(34, Magnitude);
            statement.setString(35, Timestamp);
            statement.setBoolean(36, Triggered);
            int rupd = statement.executeUpdate();
            if (rupd > 0) {
                log.info("Node registered successfully! " + rupd);
            }
            statement.close();
            conn.close();
        } catch (SQLException se) {
            log.error("Error while sql querring: ", se);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @return a list of nodes
     * @brief Select node locations from db
     */
    @Override
    public List<MapNode> getMapNodes() {
        List<MapNode> nodes = new ArrayList<>();
        Connection conn = openConnection();
        if (conn == null) {
            return nodes;
        }
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            String sql = "SELECT Latitude, Longitude, Location, Triggered FROM Nodes";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                float lat = rs.getFloat("Latitude");
                float lon = rs.getFloat("Longitude");
                String loc = rs.getString("Location");
                boolean tr = rs.getBoolean("Triggered");
                nodes.add(new MapNode(lat, lon, loc, tr));
            }
            stmt.close();

        } catch (SQLException se) {
            log.error("Error while sql querring: ", se);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return nodes;
    }

    /**
     * @brief Saves the event to the db
     * @param seedListener the seed listener
     * @param nodeConfig configuration options
     * @param e the event
     */
    public void saveEvent(EasyEngine seedListener, NodeConfig nodeConfig, GeoEvent e) {
        if (e.getType() == GeoEvent.TYPE_0) {
            return;
        }
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        //String timestamp = stamp.format(seedListener.getCalendar().getTime());
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String timestamp=stamp.format(calendar.getTime());
        sqlTrigger(e.getMw(), timestamp, nodeConfig.getStationId());
        sqlRegister(nodeConfig.getStationId(), e.getPa(), e.getPv(), e.getPd(), e.getTauc(), e.getAzi(), e.getMw(), timestamp);
        triggered = true;

    }
}
