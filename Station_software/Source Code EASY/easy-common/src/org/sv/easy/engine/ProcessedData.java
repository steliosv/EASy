/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that handles the data as
 * actual values
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

@SuppressWarnings("ClassWithoutLogger")
class ProcessedData {

    final float[] acceleration;
    final float[] geophoneData;
    final float volt;
    final float temp;
    final float[] countdata;

    /**
     * @brief Class constructor
     * @param acceleration acceleration data
     * @param geophoneData geophone data
     * @param volt voltage data
     * @param temp temperature data
     * @param countdata raw counts from triaxial geophone (STA/LTA use)
     */
    ProcessedData(float[] acceleration, float[] geophoneData, float volt, float temp, float[] countdata) {
        this.acceleration = acceleration;
        this.geophoneData = geophoneData;
        this.volt = volt;
        this.temp = temp;
        this.countdata = countdata;
    }

}
