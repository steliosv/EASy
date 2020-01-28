/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that implements the
 * Delayed STA/LTA algorithm
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.stalta;

import java.util.Arrays;

@SuppressWarnings("ClassWithoutLogger")
class ClassicStaLta implements StaLta {

    /**
     * @brief Classic STA/LTA algorithm
     * @param a[] data input
     * @param nsta sta length
     * @param nlta lta length
     * @return Characteristic function of the algorithm
     * @details calculates the Characteristic function from the equation \f$
     * \frac{STA}{LTA}=\frac{\frac{x_i^2-x_{i-Nsta}^2}{Nsta}+STA_{i-1}}{\frac{x_{i-Nsta}^2-x_{i-Nsta-Nlta-1}^2}{Nlta}+LTA_{i-1}}
     * \f$
     * @image html classic_stalta.png
     * @image latex classic_stalta.eps "Classic STA/LTA block diagram"
     * 
     */
    @Override
    public double[] calculate(double a[], int nsta, int nlta) {
        int datalen = a.length;
        int i;
        double sta = 0.0;
        double lta;
        double ratio = nlta / (double) nsta;
        double[] charfct = new double[datalen];
        Arrays.fill(charfct, 0.0);

        for (i = 0; i < nsta; i++) {
            sta += Math.pow(a[i], 2.0);
        }
        lta = sta;
        for (i = (int) nsta; i < nlta; ++i) {
            double buf = Math.pow(a[i], 2.0);
            lta += buf;
            sta += buf - Math.pow(a[i - (int) nsta], 2.0);
        }
        charfct[nlta - 1] = sta / lta * ratio;
        for (i = nlta; i < datalen; ++i) {
            double buf = Math.pow(a[i], 2.0);
            sta += buf - Math.pow(a[i - (int) nsta], 2.0);
            lta += buf - Math.pow(a[i - nlta], 2.0);
            charfct[i] = sta / lta * ratio;
        }
        return charfct;
    }
}
