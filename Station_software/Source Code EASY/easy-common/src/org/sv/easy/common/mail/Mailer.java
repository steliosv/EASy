/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class and the methods
 * responsible for mail notifications
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

public class Mailer {

    private static final Logger LOGGER = Logger.getLogger(Mailer.class);

    private final Properties props = new Properties();
    private final ExecutorService executor;
    private String sender;
    private String recipient;
    private String password;

    /**
     * @brief Class constructor
     */
    public Mailer() {
        // SMTP Session
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");//25
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * @brief Retrieves the sender's address
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @brief Sets the sender's address
     * @param sender the address
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * @brief Retrieves the recipient
     * @return the recipient address
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * @brief Sets the recipient's address
     * @param recipient the address
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * @brief Retrieves the password
     * @return the password
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
     * @brief Sends an email
     * @param content Data input text
     *
     */
    public void sendMail(String content) {
        sendMail("Earthquake Alert!", content);
    }

    /**
     * @brief Sends an email
     * @param subject the email's subject
     * @param content the email's content
     */
    @SuppressWarnings("Convert2Lambda")
    public void sendMail(final String subject, final String content) {
        // Create a session
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Session session = Session.getDefaultInstance(props, new EasyAuthenticator(sender, password));
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(sender, "E.A.Sy"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
                    message.setSubject(subject);
                    message.setText(content);
                    Transport.send(message);
                    System.out.println("sent!");
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (AddressException ex) {
                    LOGGER.error("Wrong formated adress: ", ex);
                } catch (MessagingException ex) {
                    LOGGER.error("Smtp settings are wrong: ", ex);
                } catch (Throwable ex) {
                    LOGGER.error("mail transmission failed: ", ex);
                }
            }
        });
    }
}
