/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Util class of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

/**
 * @brief helping class
 */
@SuppressWarnings("ClassWithoutLogger")
public class Utils {

    /**
     * @brief Converts a number to its ordinal
     * @param order The number to be replaced with the ordinal
     * @return the numbers's ordinal
     */
    public static String getOrdinal(int order) {
        int mdl100 = order % 100;
        int mdl10 = order % 10;
        if ((mdl10 == 1) && (mdl100 != 11)) {
            return order + "st";
        } else if ((mdl10 == 2) && (mdl100 != 12)) {
            return order + "nd";
        } else if ((mdl10 == 3) && (mdl100 != 13)) {
            return order + "rd";
        } else {
            return order + "th";
        }
    }

    private Utils() {
    }
}
