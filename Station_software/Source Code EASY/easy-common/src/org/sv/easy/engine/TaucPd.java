/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Class for Tauc - Pd algorithm
 * of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine;

import java.util.Arrays;
import org.sv.easy.engine.api.GeoEvent;
import uk.me.berndporr.iirj.Butterworth;

/**
 * @brief Class that implements the Tauc Pd algorithm
 */
@SuppressWarnings("ClassWithoutLogger")
class TaucPd {

    private final Butterworth velocityFilter = new Butterworth();
    private final Butterworth displacementFilter = new Butterworth();

    /**
     * @brief Retrieves the filter for the velocity data
     */
    Butterworth getVelocityFilter() {
        return velocityFilter;
    }

    /**
     * @brief Retrieves the filter for the displacement data
     */
    Butterworth getDisplacementFilter() {
        return displacementFilter;
    }

    /**
     * @brief Calculates the event's Azimuth (Estimation)
     * @details \f$ \varphi ^{\circ}=\left\{\begin{matrix}
     * (\tan^{-1}(\frac{A_{NS}}{A_{EW}}))*\frac{180}{\pi} , A_{Z} >0\\
     * (\tan^{-1}(\frac{A_{NS}}{A_{EW}})+\pi)*\frac{180}{\pi} , A_{Z} <0
     * \end{matrix}\right. \f$ @param amp_ew Ampl it ude on E-W axis @param
     * amp_ns Amplitude on N-S axis @param amp_z Amplitude o n Z axis @return
     * Calculated Azimuth in degrees
     */
    private double calcAzimuth(double amp_ew, double amp_ns, double amp_z) {
        double az = Math.toDegrees(Math.atan2(amp_ns, amp_ew));
        if ((amp_z * amp_ns)> 0) {
            az = az + 180;
        }
        az = (az + 360) % 360;
        return az;
    }

    /**
     * @brief \f$\tau_{c}-P_{d}\f$ algorithm implementation
     * @param samplingPeriod the sampling period
     * @param tau0 The time (measured in ms)
     * @param acc Acceleration data array on Z axis
     * @param vel Velocity data array on Z axis
     * @param disp Displacement data array on Z axis
     * @param thes_tauc Tc threshold
     * @param thres_pd Pd threshold
     * @param amp_ew Amplitude on E-W axis
     * @param amp_ns Amplitude on N-S axis
     * @details \f$ P_{d}\f$ is the max Displacement between \f$ 0 - \tau_{0}
     * \f$, and \f$ \tau_{c} \f$ is calculated by, \f$
     * \tau_{c}=\sqrt{\frac{2\pi}{\int_{0}^{\tau_{0}}
     * \frac{x'^{2}(t)}{x^{2}(t)}}} \f$
     * @image html tcpdalg.png
     * @image latex tcpdalg.eps "Tauc Pd block diagram" width=\textwidth
     */
    GeoEvent taucPd(final float samplingPeriod, final int tau0, final double[] acc, final double[] vel,
            final double[] disp, final float thes_tauc, final float thres_pd,
            final double amp_ew, final double amp_ns) {

        Integral disp_in, vel_in;

        disp_in = new Integral(0, displacementFilter.filter((float) disp[0]), 2);
        vel_in = new Integral(0, velocityFilter.filter((float) vel[0]), 2);

        double pd = Arrays.stream(disp).max().getAsDouble();
        double pa = Arrays.stream(acc).max().getAsDouble();
        double pv = Arrays.stream(vel).max().getAsDouble();

        for (int i = 1; i < tau0; i++) {
            vel_in.addElement(velocityFilter.filter((float) vel[i]), samplingPeriod, 2); //calculate the definite integral of x'²(t) for range 0 to tau0
            disp_in.addElement(displacementFilter.filter((float) disp[i]), samplingPeriod, 2);//calculate the definite integral of x²(t) for range 0 to tau0
        }
        double integvel = vel_in.getIntegral();//calculated integral of x'²(t) for range 0 to tau0
        double integdisp = disp_in.getIntegral();//calculated integral of x²(t) for range 0 to tau0
        //now calculate tauc
        double azi = calcAzimuth(amp_ew, amp_ns, disp[0]);

        double tauc = (2 * Math.PI) / (Math.sqrt(Math.abs(integvel / integdisp))); // abs is to ensure that sqrt has no negative input!
        double mw = (Math.log10(tauc) + 1.462) / 0.296; //Development of an Earthquake Early Warning System Using Real-Time Strong Motion Signals Wu Y-M and Kanamori H.

        int type = GeoEvent.TYPE_0;
        if ((Math.abs(pd) < thres_pd) && (tauc > thes_tauc)) {
            type = GeoEvent.TYPE_1;
        }
        if ((Math.abs(pd) > thres_pd) && (tauc < thes_tauc)) {
            type = GeoEvent.TYPE_2;
        }
        if ((Math.abs(pd) > thres_pd) && (tauc > thes_tauc)) {
            type = GeoEvent.TYPE_3;
        }
        if ((Math.abs(pd) < thres_pd) && (tauc < thes_tauc)) {
            type = GeoEvent.TYPE_4;
        }
        GeoEvent event = new GeoEventImpl(type, pd, pa, pv, tauc, azi, mw);
        return event;
    }
}
