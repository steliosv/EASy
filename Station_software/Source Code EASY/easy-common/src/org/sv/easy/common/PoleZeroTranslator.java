/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the threaded class for Pole zero
 * translation
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.seisFile.sac.Complex;
import edu.sc.seis.seisFile.sac.SacPoleZero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @brief This is to translate from a SacPoleZero file, which uses the
 * seisFile.sac.Complex class into something that uses the Cmplx class.
 */
@SuppressWarnings("ClassWithoutLogger")
public class PoleZeroTranslator {

    private final float constant;
    private final List<Cmplx> poles;
    private final List<Cmplx> zeros;

    /**
     * @brief Class constructor
     * @param spz sac pole zero
     */
    public PoleZeroTranslator(SacPoleZero spz) {
        this.constant = spz.getConstant();
        this.poles = transArray(spz.getPoles());
        this.zeros = transArray(spz.getZeros());
    }

    /**
     * @brief This returns the Real and the Imaginary parts of a Complex number
     * @param array Input data
     * @return returns the Real and the Imaginary parts of a Complex number
     */
    private List<Cmplx> transArray(Complex[] array) {
        List<Cmplx> list = new ArrayList<>(array.length);
        for (Complex e : array) {
            list.add(new Cmplx(e.getReal(), e.getImaginary()));
        }
        return list;
    }

    /**
     * @brief This returns the poles
     * @return The poles
     */
    public List<Cmplx> getPoles() {
        return Collections.unmodifiableList(poles);
    }

    /**
     * @brief This returns the zeros
     * @return The zeros
     */
    public List<Cmplx> getZeros() {
        return Collections.unmodifiableList(zeros);
    }

    /**
     * @brief This returns the constant
     * @return The constant
     */
    public float getConstant() {
        return constant;
    }

}
