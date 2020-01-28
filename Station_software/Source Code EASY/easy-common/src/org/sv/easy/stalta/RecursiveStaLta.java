/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that implements the
 * Recursive STA/LTA algorithm
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.stalta;

import java.util.Arrays;

@SuppressWarnings("ClassWithoutLogger")
class RecursiveStaLta implements StaLta {

    /**
     * @brief Recursive STA/LTA algorithm
     * @param a[] data input
     * @param nsta sta length
     * @param nlta lta length
     * @return Characteristic function of the algorithm
     * @details calculates the Characteristic function from the equations \f$
     * STA_{i}=Cx_{i}-(1-C)STA_{i-1} \f$ and \f$
     * C=1-e^{S/T}\approx\frac{1}{Nsta} \f$
     * @image html recursive_stalta.png
     * @image latex recursive_stalta.eps "Recursive STA/LTA block diagram"
     * 
     */
    @Override
    public double[] calculate(double a[], int nsta, int nlta) {
        int i;
        int datalen = a.length;
        double csta = 1.0 / nsta;
        double clta = 1.0 / nlta;
        double sta = 0.0;
        double lta = 0.0;
        double[] charfct = new double[datalen];
        Arrays.fill(charfct, 0);
        for (i = 0; i < datalen; i++) {
            sta = csta * Math.pow(a[i], 2.0) + (1 - csta) * sta;
            lta = clta * Math.pow(a[i], 2.0) + (1 - clta) * lta;
            charfct[i] = sta / lta;
        }
        if (nlta < datalen) {
            for (i = 0; i < nlta; i++) {
                charfct[i] = 0.0;
            }
        }
        return charfct;
    }
}
