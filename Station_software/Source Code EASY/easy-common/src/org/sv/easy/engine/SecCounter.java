/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class checks weather a second
 * has passed. It is being used to ommit extra samples that ocur due to the digitiser's crystal
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

/**
 * @brief Easy second Counter class
 */
public class SecCounter {

    private static long second = 1000000000;
    private long lastSecond;

    /**
     * @brief Retrieves time difference
     * @return Change in time (nanosecond resolution
     */
    private long getSeconds() {
        return (System.nanoTime() - lastSecond);
    }

    /**
     * @brief Updates the time
     */
    private void updateSeconds() {
        this.lastSecond = System.nanoTime();
    }

    /**
     * @brief constructor
     */
    public SecCounter() {
        this.lastSecond = System.nanoTime();
    }

    /**
     * @brief Updates time and checks if a second has passed
     * @return check True: if changed, false if not
     */
    public boolean hasSecondPassed() {
        boolean check;
        if (getSeconds() >= second) {
            updateSeconds();
            check = true;
        } else {
            check = false;
        }
        return check;
    }
}
