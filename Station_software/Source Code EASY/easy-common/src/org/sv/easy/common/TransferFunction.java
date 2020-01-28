/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Class that deconvolves the
 * pole-zero from the digitized data
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.seisFile.sac.SacPoleZero;

/**
 * @brief Contain methods that deconvolve the pole-zero from the digitized data
 */
@SuppressWarnings("ClassWithoutLogger")
public class TransferFunction {

    static final Cmplx ZERO = new Cmplx(0, 0);

    private final SacPoleZero pz;
    private final double sampFreq;
    private final float lowCut;
    private final float lowPass;
    private final float highPass;
    private final float highCut;
    /**
     * @brief Constructor
     */
    public TransferFunction(SacPoleZero pz, double sampFreq, float lowCut,
            float lowPass, float highPass, float highCut) {
        this.pz = pz;
        this.sampFreq = sampFreq;
        this.lowCut = lowCut;
        this.lowPass = lowPass;
        this.highPass = highPass;
        this.highCut = highCut;
    }

    /**
     * @brief Deconvolves the pole-zero from the digitized data
     * @param datain Counts to be processed
     * @return The deconvoluted output data represented as counts
     */
    public float[] transferFunction(int[] datain) {
        float[] counts = new float[datain.length];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = (float) (datain[i] / sampFreq);
        }
        Cmplx[] freqValues = Cmplx.fft(counts);
        freqValues = combine(freqValues, sampFreq, new PoleZeroTranslator(pz), lowCut, lowPass, highPass, highCut);
        counts = Cmplx.fftInverse(freqValues, counts.length);
        for (int i = 0; i < counts.length; i++) {
            counts[i] *= freqValues.length;
        }
        return counts;
    }

    /**
     * @brief Deconvolves the pole-zero from the digitized data
     * @param pz pole zero file
     * @param datain Counts to be processed
     * @param sampFreq The sampling frequency
     * @param lowCut High-pass filter at low frequency
     * @param lowPass High-pass filter at low frequency
     * @param highPass Low-pass filter at low frequency
     * @param highCut Low-pass filter at low frequency
     * @return The deconvoluted output data represented as counts
     */
    public static float[] transferFunction(SacPoleZero pz, int[] datain,
            double sampFreq, float lowCut, float lowPass, float highPass, float highCut) {
        float[] counts = new float[datain.length];
        //int[] out = new int[datain.length];  
        for (int i = 0; i < counts.length; i++) {
            counts[i] = (float) (datain[i] / sampFreq);
        }
        Cmplx[] freqValues = Cmplx.fft(counts);
        freqValues = combine(freqValues, sampFreq, new PoleZeroTranslator(pz), lowCut, lowPass, highPass, highCut);
        counts = Cmplx.fftInverse(freqValues, counts.length);
        for (int i = 0; i < counts.length; i++) {
            counts[i] *= freqValues.length;
        }
        return counts;
    }

    /**
     * @brief Write data to mSEED file
     * @param freqValues
     * @param sampFreq Sampling Frequency
     * @param poleZero The pole zero translator
     * @param lowCut High-pass filter at low frequency
     * @param lowPass High-pass filter at low frequency
     * @param highPass Low-pass filter at low frequency
     * @param highCut Low-pass filter at low frequency
     * @return Cmplx[]
     */
    static private Cmplx[] combine(Cmplx[] freqValues, double sampFreq,
            PoleZeroTranslator poleZero, float lowCut, float lowPass,
            float highPass, float highCut) {
        double deltaF = sampFreq / freqValues.length;
        double freq;
        // handle zero freq
        freqValues[0] = ZERO;
        // handle nyquist
        freq = sampFreq / 2;
        Cmplx respAtS = evalPoleZeroInverse(poleZero, freq);
        respAtS = Cmplx.mul(respAtS, deltaF * freqTaper(freq,
                lowCut,
                lowPass,
                highPass,
                highCut));
        freqValues[freqValues.length / 2] = Cmplx.mul(freqValues[freqValues.length / 2],
                respAtS);
        for (int i = 1; i < freqValues.length / 2; i++) {
            freq = i * deltaF;
            respAtS = evalPoleZeroInverse(poleZero, freq);
            // fft in sac has opposite sign on imag, so take conjugate to make same
            respAtS = Cmplx.mul(respAtS, deltaF * freqTaper(freq,
                    lowCut,
                    lowPass,
                    highPass,
                    highCut));
            freqValues[i] = Cmplx.mul(freqValues[i], respAtS);
            freqValues[freqValues.length - i] = freqValues[i];//.conjg();
        }
        return freqValues;
    }

    /**
     * @return @brief Evaluates the poles and zeros at the given value. The
     * return value is 1/(pz(s) to avoid divide by zero issues. If there is a
     * divide by zero situation, then the response is set to be 0+0i.
     * @param pz PoleZero file to be analysed as a complex
     * @param freq Frequency to apply
     */
    static private Cmplx evalPoleZeroInverse(PoleZeroTranslator pz, double freq) {
        Cmplx s = new Cmplx(0, 2 * Math.PI * freq);
        Cmplx zeroOut = new Cmplx(1, 0);
        Cmplx poleOut = new Cmplx(1, 0);
        for (int i = 0; i < pz.getPoles().size(); i++) {
            poleOut = Cmplx.mul(poleOut, Cmplx.sub(s, pz.getPoles().get(i)));
        }
        for (int i = 0; i < pz.getZeros().size(); i++) {
            if (s.real() == pz.getZeros().get(i).real()
                    && s.imag() == pz.getZeros().get(i).imag()) {
                return ZERO;
            }
            zeroOut = Cmplx.mul(zeroOut, Cmplx.sub(s, pz.getZeros().get(i)));
        }
        Cmplx out = Cmplx.div(poleOut, zeroOut);
        // sac uses opposite sign in imag, so take conjugate
        return Cmplx.div(out, pz.getConstant());//.conjg();
    }

    /**
     * @brief Applies tapper to get rid of noise amplification.
     * @details Frequencies f1 and f2 specify the high-pass filter at low
     * frequencies, while frequencies f3 and f4 specify the low-pass filter at
     * high frequencies. The taper is unity between f2 and f3 and zero below f1
     * and above f4 To avoid ringing in the output time series, a suggestr
     * rule-of-thumb is f1 ,= f2/2 and 0.5/delta >= f4 >= 2*f3.
     * @param freq Frequency to apply tapper
     * @param lowCut High-pass filter at low frequency
     * @param lowPass High-pass filter at low frequency
     * @param highPass Low-pass filter at low frequency
     * @param highCut Low-pass filter at low frequency
     * @return
     */
    static private double freqTaper(double freq, float lowCut, float lowPass,
            float highPass, float highCut) {
        if (lowCut > lowPass || lowPass > highPass || highPass > highCut) {
            throw new IllegalArgumentException("Must be lowCut > lowPass > highPass > highCut: "
                    + lowCut + " " + lowPass + " " + highPass + " " + highCut);
        }
        if (freq <= lowCut || freq >= highCut) {
            return 0;
        }
        if (freq >= lowPass && freq <= highPass) {
            return 1;
        }
        if (freq > lowCut && freq < lowPass) {
            return 0.5e0 * (1.0e0 + Math.cos(Math.PI * (freq - lowPass)
                    / (lowCut - lowPass)));
        }
        // freq > highPass && freq < highCut
        return 0.5e0 * (1.0e0 - Math.cos(Math.PI * (freq - highCut)
                / (highPass - highCut)));
    }

}
