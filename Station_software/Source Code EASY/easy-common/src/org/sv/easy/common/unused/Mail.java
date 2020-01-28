/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class and the methods for
 * sending alert mail
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common.unused;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.sv.easy.engine.api.GeoEvent;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.common.mail.EasyAuthenticator;

/**
 * @brief This contains the Class that sends an e-mail
 */
@Deprecated
class Mail {

    static Logger log = Logger.getLogger(Mail.class);

    /**
     * @brief Sends an email
     * @param text Data input text
     * @param data configuration data
     * @exception AddressException when a wrongly formatted address is
     * encountered
     * @exception MessagingException when SMTP settings are wrong
     */
    public static void xsendMail(String text, MailData data) {
        xsendMail(text, data.sender, data.receiver, data.passwd);
    }

    public static void sendMail(EasyEngine seedListener, MailData data, NodeConfig nodeConfig, GeoEvent e) {
        if (e.getType() == GeoEvent.TYPE_0) {
            return;
        }
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
        String timestamp = stamp.format(seedListener.getCalendar().getTime());

        String vals = "\n\n First calculated data:" + "\n Estimated Moment Magnitude:"
                + f.format(e.getMw()) + "\n " + " \n Azimuth: " + f.format(e.getAzi()) + "°N"
                + "\n Max Acc: " + df.format(e.getPa()).toLowerCase()
                + " \n Max Vel: " + df.format(e.getPv()).toLowerCase() + " \n Max Disp: "
                + df.format(e.getPd()).toLowerCase() + " \n Tc: " + f.format(e.getTauc());
        b.append(vals).append(timestamp);
        xsendMail(b.toString(), data.sender, data.receiver, data.passwd);
    }

    private static void xsendMail(String text, String sender, String receiver, String passwd) {
        // SMTP Session
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");//25
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        // Create a session
        Session session = Session.getDefaultInstance(props, new EasyAuthenticator(sender, passwd));
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender, "E.A.Sy"));
            message.setRecipients(RecipientType.TO, InternetAddress.parse(receiver, false));
            message.setSubject("Earthquake Alert!");
            message.setText(text);
            Transport.send(message);
            System.out.println("sent!");
        } catch (AddressException ae) {
            log.error("Wrong formated adress: ", ae);
        } catch (MessagingException me) {
            log.error("Smtp settings are wrong: ", me);
        } catch (Exception ef1) {
            log.error("mail transmission failed: ", ef1);
        }
    }
}
