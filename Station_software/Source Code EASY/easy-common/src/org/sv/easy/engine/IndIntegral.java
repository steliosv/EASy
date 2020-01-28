/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that calculates an indefinite integral
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

/**
 *
 * @author stevo
 */
public class IndIntegral {

    private double prev;
    private double integral;

    /**
     * @brief Class constructor
     * @param initial_value The initial value
     *
     */
    IndIntegral(double initial_value) {
        integral = initial_value;
    }
    
    /**
     * @brief Integrates by calculating the trapezoid created between the
     * previous and the current sample
     * @param curr Current value
     * @param interval Time interval between the two measurements
     * @return The calculated integral
     */
    public double calculate(double curr, float interval) {
        double current_value = ((curr + prev) * interval) / 2;
        integral += current_value;
        prev = curr;
        return integral;
    }
}
