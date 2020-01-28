/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class and the methods
 * responsible email authentication
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import org.apache.log4j.Logger;

/**
 * @brief Authenticates an account on a mail server
 */
public class EasyAuthenticator extends Authenticator {

    private static final Logger LOGGER = Logger.getLogger(EasyAuthenticator.class);
    private final String sender;
    private final String passwd;

    /**
     * @brief Class constructor
     * @param sender sender's address
     * @param passwd passwird
     */
    public EasyAuthenticator(String sender, String passwd) {
        super();
        this.sender = sender;
        this.passwd = passwd;
    }

    /**
     * @brief A data holder that is used by Authenticator. It is simply a
     * repository for a user name and a password
     * @return
     */
    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public PasswordAuthentication getPasswordAuthentication() {
        String username, password;
        username = sender;
        password = passwd;
        System.out.println("authenticating. . ");
        return new PasswordAuthentication(username, password);
    }
}
