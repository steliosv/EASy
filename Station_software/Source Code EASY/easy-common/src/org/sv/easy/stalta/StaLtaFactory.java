/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the STA/LTA factory class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.stalta;

/**
 * @brief Contains methods for triggering detection
 */
@SuppressWarnings("ClassWithoutLogger")
public class StaLtaFactory {

    public static String[] getNames() {
        String[] names = new String[]{
            "Classic StaLta",
            "Recursive StaLta",
            "ZDetect"
        };

        return names;
    }

    public static StaLta get(String name) {
        if ("Classic StaLta".equals(name)) {
            return new ClassicStaLta();
        }
        if ("Recursive StaLta".equals(name)) {
            return new RecursiveStaLta();
        }
        if ("ZDetect".equals(name)) {
            return new ZDetect();
        }

        throw new RuntimeException(name + ": not found");
    }

    private StaLtaFactory() {
    }
}
