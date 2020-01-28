/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that Calculates the first
 * derivative of the input data
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

/**
 * @brief This contains the class that calculates the first derivative of the
 * input data
 */
@SuppressWarnings("ClassWithoutLogger")
class Derivative {

    /**
     * @brief Derivative function, Calculates the first derivative from input
     * data using the equation \f$x'_{n}=\frac{x_{n}-x_{n-1}}{\Delta t}\f$
     * @param interval Time interval between the two measurements
     * @param xmin Low value
     * @param xmax High value
     * @return The calculated integral
     */
    public static float calculate(float interval, float xmin, float xmax) {
        float deltav = xmax - xmin;
        return deltav / interval;
    }

    /**
     * @brief Class constructor
     */
    private Derivative() {
    }
}
