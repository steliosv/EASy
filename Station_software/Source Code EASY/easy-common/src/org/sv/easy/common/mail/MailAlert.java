/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class and the methods
 * responsible for email alerts
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common.mail;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;

public class MailAlert {

    private static final Logger LOGGER = Logger.getLogger(MailAlert.class);
    private final Mailer mailer;

    /**
     * @brief Class constructor
     * @param mailer the mailer instance to send the data
     */
    public MailAlert(Mailer mailer) {
        this.mailer = mailer;
    }

    /**
     * @brief sends an email
     * @param seedListener the listener
     * @param nodeConfig configuration
     * @param e the event
     */
    public void sendMail(EasyEngine seedListener, NodeConfig nodeConfig, GeoEvent e) {
        if (e.getType() == GeoEvent.TYPE_0) {
            return;
        }
        @SuppressWarnings("StringBufferWithoutInitialCapacity")
        StringBuilder b = new StringBuilder();
        if (e.getType() == GeoEvent.TYPE_1) {
            b.append("!!! Warning !!! Distant large magnitude incoming event, "
                    + "not damaging in the station area, but it can be damaging in other areas.");
        }
        if (e.getType() == GeoEvent.TYPE_2) {
            b.append("!!! Notice !!! Local small magnittude incoming event, "
                    + "damaging  only  in the limited area around the station.");
        }
        if (e.getType() == GeoEvent.TYPE_3) {
            b.append("!!! DANGER !!! Local large magnitude incoming event, "
                    + "most likely damaging in the station area as well as a larger area.");
        }
        if (e.getType() == GeoEvent.TYPE_4) {
            b.append("!!! Notice !!! Not damaging event.");
        }
        b.append(" Recorded at Latitude: ").append(nodeConfig.getLatitude()).append(" Longitude: ").append(nodeConfig.getLongitude());
        DecimalFormat f = new DecimalFormat("###.##");
        DecimalFormat df = new DecimalFormat("0.000E00");
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        //String timestamp = stamp.format(seedListener.getCalendar().getTime());
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String timestamp = stamp.format(calendar.getTime());
        //DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        //String timestamp = stamp.format(seedListener.getCalendar().getTime());
        double mw = e.getMw();
        double azi = e.getAzi();
        double pd = e.getPd();
        double tc = e.getTauc();
        String vals = "\n\n First calculated data:" + "\n Estimated Moment Magnitude:"
                + f.format(mw) + "\n " + " \n Azimuth: " + f.format(azi) + "degrees N"
                + " \n Pd: " + df.format(pd) + "m"
                + " \n Tc: " + f.format(tc) + "sec.\nat: ";
        b.append(vals).append(timestamp);
        mailer.sendMail(b.toString());
    }
}
