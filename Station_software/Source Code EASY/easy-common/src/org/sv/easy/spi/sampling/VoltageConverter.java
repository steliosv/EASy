/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class for converting counts
 * into voltage of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.spi.sampling;

import org.sv.easy.api.sampling.SampleConverter;

@SuppressWarnings("ClassWithoutLogger")
public class VoltageConverter implements SampleConverter {

    /**
     * @brief Transform counts to voltage
     * @param value Counts read from the digitising module
     * @return The voltage that corresponds to the input counts
     */
    @Override
    public double convert(int value) {
        double supplyVoltage;
        double ADconversionfactor = 5/Math.pow(2,23);   // A/D conversion factor = (AVdd/counts)=(5/(2^23))
        supplyVoltage = value * ADconversionfactor;      //  Use the ratio calculated for the voltage divider
        return supplyVoltage;
    }
}
