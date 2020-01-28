/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class for converting counts
 * into Temperature(C) of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.spi.sampling;

import org.sv.easy.api.sampling.SampleConverter;

@SuppressWarnings("ClassWithoutLogger")
public class TemperatureConverter implements SampleConverter {

    /**
     * @brief Transforms counts to temperature
     * @param value Counts read from the digitising module
     * @return The temperature that corresponds to the input counts
     */
    @Override
    public double convert(int value) {
        double ADconversionfactor = 5/Math.pow(2,23);;  // A/D conversion factor = (AVdd/counts)=(5/(2^23))
        double vout = value * ADconversionfactor;
        double mVperC =0.01;
        double temp = vout / mVperC;  // 
        return temp;
    }
}
