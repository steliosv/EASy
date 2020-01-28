/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that compensates the G
 * constant from the accelerometer data
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

/**
 * @brief Gravity compensation methods
 */
@SuppressWarnings("ClassWithoutLogger")
class GCompensate {

    public final static int STANDARD_GRAVITY_DIVIDEND = 980665;
    public final static int STANDARD_GRAVITY_DIVISOR = 100000;
    public final static double GCONST = STANDARD_GRAVITY_DIVIDEND / (double) STANDARD_GRAVITY_DIVISOR; //9.80665 m/s\u00b2 as defined on ISO 80000-3:2006

    /**
     * @brief Calculates pitch angle from the equation \f$Pitch^{\circ}=\left
     * (arctan\left(\frac{a_{_{x}}}{\sqrt{{a_{y}}^{2}+{a_{z}}^{2}}}\right)
     * \right )*\frac{180}{\pi }\f$ , provided by the sensor's manufacturer
     * @param x Raw acceleration on x axis
     * @param y Raw acceleration on y axis
     * @param z Raw acceleration on z axis
     * @return Pitch in degrees
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private static double getPitch(double x, double y, double z) {
        return Math.toDegrees(Math.atan2(x, Math.sqrt(Math.pow(y, 2.0) + Math.pow(z, 2.0))));
    }

    /**
     * @brief Calculates roll angle from the equation \f$Roll^{\circ}=\left
     * (arctan\left(\frac{a_{_{y}}}{\sqrt{{a_{x}}^{2}+{a_{z}}^{2}}}\right)
     * \right )*\frac{180}{\pi }\f$ , provided by the sensor's manufacturer
     * @param x Raw acceleration on x axis
     * @param y Raw acceleration on y axis
     * @param z Raw acceleration on z axis
     * @return Roll in degrees
     */
    private static double getRoll(double x, double y, double z) {
        return Math.toDegrees(Math.atan2(y, Math.sqrt(Math.pow(x, 2.0) + Math.pow(z, 2.0))));
    }

    /**
     * @brief Removes gravity constant from z axis. Notice! use only for real
     * time data
     *
     * @param x Raw acceleration on x axis
     * @param y Raw acceleration on y axis
     * @param z Raw acceleration on z axis
     * @return Gravity constant free acceleration in z axis
     */
    public static double removeGzConst(double x, double y, double z) {
        double pitch = getPitch(x, y, z);
        double roll = getRoll(x, y, z);
        double gz = GCONST * Math.sqrt(0.5f) * Math.sqrt(Math.cos(2 * pitch) + Math.cos(2 * roll));
        return z - gz;
    }

    /**
     * @brief Removes gravity constant from x axis. Notice! use only for real
     * time data
     * @param x Raw acceleration on x axis
     * @param y Raw acceleration on y axis
     * @param z Raw acceleration on z axis
     * @return Gravity constant free acceleration in x axis
     */
    public static double removeGxConst(double x, double y, double z) {
        double pitch = getPitch(x, y, z);
        double gx = GCONST * Math.sin(pitch);
        return x - gx;
    }

    /**
     * @brief Removes gravity constant from y axis. Notice! use only for real
     * time data
     * @param x Raw acceleration on x axis
     * @param y Raw acceleration on y axis
     * @param z Raw acceleration on z axis
     * @return Gravity constant free acceleration in y axis
     */
    public static double removeGyConst(double x, double y, double z) {
        double roll = getRoll(x, y, z);
        double gy = GCONST * Math.sin(roll);
        return y - gy;
    }

    /**
     * @brief Class constructor
     */
    private GCompensate() {
    }
}
