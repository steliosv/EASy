/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that Integrates data
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

/**
 * @brief This contains the class that calculates a definite integral
 */
@SuppressWarnings("ClassWithoutLogger")
class Integral {

    private double prev;
    private double integral;

    /**
     * @brief Class constructor
     * @param initial_value The initial value
     * @param curr Current value
     * @param exponent The exponent
     *
     */
    Integral(double initial_value, double curr, double exponent) {
        integral = initial_value;
        prev = Math.pow(curr, exponent);
    }

    /**
     * @brief Integrates by calculating the trapezoid created between the
     * previous and the current sample
     * @details \f$ \int_{0}^{\tau _{0}}u^{n}(t)dt \approx \sum_{t=1}^{\tau
     * _{0}}\frac{1}{2}(u_{t-1}^{n}+u_{t}^{n})*t_{sampling} \f$
     * @param curr Current value
     * @param interval Time interval between the two measurements
     * @param exponent Exponent
     */
    public void addElement(double curr, float interval, double exponent) {
        double currsqred = Math.pow(curr, exponent);
        double current_value = ((currsqred + prev) * interval) / 2;
        integral += current_value;
        prev = currsqred;
    }

    /**
     * @brief returns the calculated integral
     * @return The calculated integral
     */
    public double getIntegral() {
        return integral;
    }
}
