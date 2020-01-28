/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that implements the Date
 * related actions
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @brief This contains methods related on the initialisation of the time format
 */
@SuppressWarnings("ClassWithoutLogger")
public class DateUtils {

    /**
     * @brief Returns the current timestamp in UTC format
     * @return
     */
    public static DateFormat getDateFormat() {
        DateFormat stamp = new SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        stamp.setTimeZone(TimeZone.getTimeZone("UTC"));
        return stamp;
    }

    private DateUtils() {
    }
}
