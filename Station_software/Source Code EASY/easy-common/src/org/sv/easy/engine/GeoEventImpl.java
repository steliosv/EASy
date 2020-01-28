/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the GeoEvent Class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

import org.sv.easy.engine.api.GeoEvent;

/**
 * @brief This contains the GeoEvent Class
 */
@SuppressWarnings("ClassWithoutLogger")
class GeoEventImpl implements GeoEvent {

    private int type;
    private double pd;
    private double pa;
    private double pv;
    private double tauc;
    private double azi;
    private double mw;

    /**
     * @brief Constructor
     * @param type type
     * @param pd displacement value
     * @param pa acceleration value
     * @param pv velocity value
     * @param tauc tauc value
     * @param azi azimuth value
     * @param mw magnitude value
     */
    GeoEventImpl(int type, double pd, double pa, double pv, double tauc, double azi, double mw) {
        this.type = type;
        this.pd = pd;
        this.pa = pa;
        this.pv = pv;
        this.tauc = tauc;
        this.azi = azi;
        this.mw = mw;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public double getPd() {
        if (Double.isNaN(pd)) {
            pd = 0.0;
        }
        return pd;
    }

    @Override
    public double getPa() {
        if (Double.isNaN(pa)) {
            pa = 0.0;
        }
        return pa;
    }

    @Override
    public double getPv() {
        if (Double.isNaN(pv)) {
            pv = 0.0;
        }
        return pv;
    }

    @Override
    public double getTauc() {
        if (Double.isNaN(tauc)) {
            tauc = 0.0;
        }
        return tauc;
    }

    @Override
    public double getAzi() {
        if (Double.isNaN(azi)) {
            azi = 0.0;
        }
        return azi;
    }

    @Override
    public double getMw() {
        if (Double.isNaN(mw)) {
            mw = 0.0;
        }
        return mw;
    }

    @Override
    public String toString() {
        return "GeoEventImpl{" + "type=" + type + ", pd=" + pd + ", pa=" + pa + ", pv=" + pv + ", tauc=" + tauc + ", azi=" + azi + ", mw=" + mw + '}';
    }

}
