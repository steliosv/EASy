/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that implements the
 * Z-Detect algorithm
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.stalta;

import java.util.Arrays;

@SuppressWarnings("ClassWithoutLogger")
class ZDetect implements StaLta {

    /**
     * @brief Z-detect algorithm
     * @param a[] data input
     * @param nsta sta length
     * @param ignore just ignore this
     * @return Characteristic function of the algorithm
     * @details calculates the Characteristic function from the equation \f$
     * Z\left(x_{i}\right)=\frac{x_{i}-\mu}{\sigma} \f$
     * @image html zdetect.png
     * @image latex zdetect.eps "Z-detect block diagram" 
     */
    @Override
    public double[] calculate(double[] a, int nsta, int ignore) {
        int m = a.length;
        double[] sta = new double[m];
        // Standard Sta
        double[] pad_sta = new double[(int) nsta];
        // window size to smooth over
        for (int i = 0; i < nsta; i++) {
            double[] tmp = Arrays.copyOfRange(a, i, m - (int) nsta + i);
            for (int j = 0; j < tmp.length; j++) {
                tmp[j] *= tmp[j];
            }
            double[] npc = Arrays.copyOf(pad_sta, pad_sta.length + tmp.length);
            for (int j = pad_sta.length; j < npc.length; j++) {
                npc[j] = tmp[j - pad_sta.length];
            }

            for (int j = 0; j < sta.length; j++) {
                sta[j] += npc[j];
            }
        }

        double a_mean = 0;
        for (double d : sta) {
            a_mean += d;
        }
        a_mean /= sta.length;
        double a_std = 0;
        for (int i = 0; i < sta.length; i++) {
            a_std += (sta[i] - a_mean) * (sta[i] - a_mean);
        }
        a_std /= sta.length;
        a_std = Math.sqrt(a_std);

        for (int i = 0; i < sta.length; i++) {
            sta[i] -= a_mean;
            sta[i] /= a_std;
        }
        return sta;
    }
}
