/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the interface calculating each
 * STA/LTA algorithm
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.stalta;

public interface StaLta {

    public double[] calculate(double[] a, int nsta, int nlta);
}
