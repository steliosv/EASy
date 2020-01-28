/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that contains methods for
 * the Reed - Solomon Encoder / Decoder
 * @author Mike Lubinets, Java version stevo
 * @bug No known bugs.
 */
package org.sv.easy.fec;

import java.util.Arrays;

/**
 * @brief This contains the Reed solomon encode - Decode methods
 */
public class Reedsolomon {

    private static final int ID_MSG_IN = 0;
    private static final int ID_MSG_OUT = 1;
    private static final int ID_GENERATOR = 2;   // 3
    private static final int ID_TPOLY1 = 3;      // T for Temporary
    private static final int ID_TPOLY2 = 4;
    private static final int ID_MSG_E = 5;       // 5
    private static final int ID_TPOLY3 = 6;     // 6
    private static final int ID_TPOLY4 = 7;
    private static final int ID_SYNDROMES = 8;
    private static final int ID_FORNEY = 9;
    private static final int ID_ERASURES_LOC = 10;
    private static final int ID_ERRORS_LOC = 11;
    private static final int ID_ERASURES = 12;
    private static final int ID_ERRORS = 13;
    private static final int ID_COEF_POS = 14;
    private static final int ID_ERR_EVAL = 15;
    private static final int MSG_CNT = 3;   // message-length polynomials count
    private static final int POLY_CNT = 14; // (ecc_length*2)-length polynomialc count
    private int msg_length;  // Message length without correction code
    private int ecc_length;
    private final Poly[] polynoms = new Poly[MSG_CNT + POLY_CNT];

    /**
     * @brief Class constructor
     * @param msg_length message length in characters
     * @param ecc_length ecc length in characters
     */
    public Reedsolomon(int msg_length, int ecc_length) {
        this.msg_length = msg_length;
        this.ecc_length = ecc_length;
        int enc_len = msg_length + ecc_length;
        int poly_len = ecc_length * 2;

        for (int i = 0; i < polynoms.length; i++) {
            polynoms[i] = new Poly();
        }

        /* Initialize first six polys manually cause their amount depends on template parameters */
        polynoms[0].Init(ID_MSG_IN, 0, enc_len, new int[enc_len]);

        polynoms[1].Init(ID_MSG_OUT, 0, enc_len, new int[enc_len]);

        for (int i = ID_GENERATOR; i < ID_MSG_E; i++) {
            polynoms[i].Init(i, 0, poly_len, new int[poly_len]);
        }

        polynoms[5].Init(ID_MSG_E, 0, enc_len, new int[enc_len]);

        for (int i = ID_TPOLY3; i < ID_ERR_EVAL + 2; i++) {
            polynoms[i].Init(i, 0, poly_len, new int[poly_len]);
        }
    }

    /**
     * @brief Message block encoding
     * @param src - input message buffer (msg_lenth size)
     * @param dst - output buffer for ecc (ecc_length size at least)
     */
    public void Encode(char[] src, char[] dst) {
        int[] s = new int[src.length];
        int[] d = new int[dst.length];
        for (int i = 0; i < src.length; i++) {
            s[i] = src[i] & 0xff;
        }
        Encode(s, d);
        for (int i = 0; i < d.length; i++) {
            dst[i] = (char) d[i];
        }
    }

    /**
     * @brief Message block encoding
     * @param src - input message buffer (msg_lenth size)
     * @param dst - output buffer for ecc (ecc_length size at least)
     * @param dst_offset - offset
     */
    public void EncodeBlock(int[] src, int[] dst, int dst_offset) {
        assert (msg_length + ecc_length < 256);
        /* Generator cache, it dosn't change for one template parameters */
        int[] generator_cache = new int[ecc_length + 1];
        boolean generator_cached = false;

        int[] src_ptr = src;
        int[] dst_ptr = dst;

        Poly msg_in = polynoms[ID_MSG_IN];
        Poly msg_out = polynoms[ID_MSG_OUT];
        Poly gen = polynoms[ID_GENERATOR];

        // Without reseting msg_in it simply doesn't work
        msg_in.Reset();
        msg_out.Reset();

        // Using cached generator or generating new one
        if (generator_cached) {
            gen.Set(generator_cache, generator_cache.length, 0);
        } else {
            GeneratorPoly();
            System.arraycopy(gen.ptr(), 0, generator_cache, 0, gen.length);
            generator_cached = true;
        }

        // Copying input message to internal polynomial
        msg_in.Set(src_ptr, msg_length, 0);
        msg_out.Set(src_ptr, msg_length, 0);
        msg_out.length = msg_in.length + ecc_length;

        int coef = 0; // cache// cache
        for (int i = 0; i < msg_length; i++) {
            coef = msg_out.at(i);
            if (coef != 0) {
                for (int j = 1; j < gen.length; j++) {
                    msg_out.ptr()[i + j]
                            ^= Gf.mul(gen.ptr()[j], coef);
                }
            }
        }

        // Copying ECC to the output buffer
        System.arraycopy(msg_out.ptr(), msg_length, dst_ptr, dst_offset, ecc_length);
    }

    /**
     * @brief Message encoding
     * @param src - input message buffer (msg_lenth size)
     * @param dst - output buffer (msg_length + ecc_length size at least)
     */
    public void Encode(int[] src, int[] dst) {
        int[] dst_ptr = dst;

        // Copying message to the output buffer
        System.arraycopy(src, 0, dst_ptr, 0, msg_length);

        // Calling EncodeBlock to write ecc to out[ut buffer
        EncodeBlock(src, dst_ptr, msg_length);
    }

    /**
     * @brief Message block decoding
     * @param src - encoded message buffer (msg_length size)
     * @param ecc - ecc buffer (ecc_length size)
     * @param dst - output buffer (msg_length size at least)
     * @param erase_pos - known errors positions
     * @param erase_count - count of known errors
     * @return RESULT_SUCCESS if successful, error code otherwise
     */
    public int DecodeBlock(int[] src, int[] ecc, int[] dst, int[] erase_pos, int erase_count) {
        assert (msg_length + ecc_length < 256);

        int[] src_ptr = src;
        int[] ecc_ptr = ecc;
        int[] dst_ptr = dst;

        int src_len = msg_length + ecc_length;
        int dst_len = msg_length;

        boolean ok;
        Poly msg_in = polynoms[ID_MSG_IN];
        Poly msg_out = polynoms[ID_MSG_OUT];
        Poly epos = polynoms[ID_ERASURES];

        // Copying message to polynomials memory
        msg_in.Set(src_ptr, msg_length, 0);
        msg_in.Set(ecc_ptr, ecc_length, msg_length);
        msg_out.Copy(msg_in);

        // Copying known errors to polynomial
        if (erase_pos == null) {
            epos.length = 0;
        } else {
            epos.Set(erase_pos, erase_count, 0);
            for (int i = 0; i < epos.length; i++) {
                msg_in.ptr()[epos.ptr()[i]] = 0;
            }
        }

        // Too many errors
        if (epos.length > ecc_length) {
            return 1;
        }

        Poly synd = polynoms[ID_SYNDROMES];
        Poly eloc = polynoms[ID_ERRORS_LOC];
        Poly reloc = polynoms[ID_TPOLY1];
        Poly err = polynoms[ID_ERRORS];
        Poly forney = polynoms[ID_FORNEY];

        // Calculating syndrome
        CalcSyndromes(msg_in);

        // Checking for errors
        boolean has_errors = false;
        for (int i = 0; i < synd.length; i++) {
            if (synd.ptr()[i] != 0) {
                has_errors = true;
                break;
            }
        }

        // Going to exit if no errors
        if (!has_errors) {
            msg_out.length = dst_len;
            System.arraycopy(msg_out.ptr(), 0, dst_ptr, 0, msg_out.length);
            return 0;
        }

        CalcForneySyndromes(synd, epos, src_len);
        FindErrorLocator(forney, null, epos.length);

        // Reversing syndrome
        // TODO optimize through special Poly flag
        reloc.length = eloc.length;
        for (int i = (eloc.length - 1), j = 0; i >= 0; i--, j++) {
            reloc.ptr()[j] = eloc.ptr()[i];
        }

        // Fing errors
        ok = FindErrors(reloc, src_len);
        if (!ok) {
            return 1;
        }

        // Error happened while finding errors (so helpfull :D)
        if (err.length == 0) {
            return 1;
        }

        /* Adding found errors with known */
        for (int i = 0; i < err.length; i++) {
            epos.Append(err.ptr()[i]);
        }

        // Correcting errors
        CorrectErrata(synd, epos, msg_in);

        //return_corrected_msg:
        // Wrighting corrected message to output buffer
        msg_out.length = dst_len;
        System.arraycopy(msg_out.ptr(), 0, dst_ptr, 0, msg_out.length);
        return 0;
    }

    /**
     * @brief Message block decoding
     * @param src - encoded message buffer (msg_length + ecc_length size)
     * @param dst - output buffer (msg_length size at least)
     * @param erase_pos - known errors positions
     * @param erase_count - count of known errors
     * @return RESULT_SUCCESS if successful, error code otherwise
     */
    public int Decode(int[] src, int[] dst, int[] erase_pos, int erase_count) {
        int[] src_ptr = src;
        int[] ecc_ptr = Arrays.copyOfRange(src_ptr, msg_length, src_ptr.length);
        return DecodeBlock(src, ecc_ptr, dst, erase_pos, erase_count);
    }

    /**
     * @brief Message block decoding
     * @param src - encoded message buffer (msg_length + ecc_length size)
     * @param dst - output buffer (msg_length size at least)
     * @param erase_pos - known errors positions
     * @param erase_count - count of known errors
     * @return RESULT_SUCCESS if successful, error code otherwise
     */
    public int Decode(char[] src, char[] dst, char[] erase_pos, int erase_count) {
        int[] s = new int[src.length];
        int[] d = new int[dst.length];
        int[] e = null;
        if (erase_pos != null) {
            e = new int[erase_pos.length];
        }
        for (int i = 0; i < src.length; i++) {
            s[i] = src[i] & 0xff;
        }
        int ret = Decode(s, d, e, erase_count);
        for (int i = 0; i < d.length; i++) {
            dst[i] = (char) d[i];
        }
        return ret;
    }

    /**
     * @brief Generates the polynomials
     */
    private void GeneratorPoly() {
        Poly gen = polynoms[ID_GENERATOR];
        gen.ptr()[0] = 1;
        gen.length = 1;

        Poly mulp = polynoms[ID_TPOLY1];
        Poly temp = polynoms[ID_TPOLY2];
        mulp.length = 2;

        for (int i = 0; i < ecc_length; i++) {
            mulp.ptr()[0] = 1;
            mulp.ptr()[1] = Gf.pow(2, i);

            Gf.poly_mul(gen, mulp, temp);

            gen.Copy(temp);
        }
    }

        /**
     * @brief Syndrome calculation
     * @param msg - source polynomial
     */
    private void CalcSyndromes(Poly msg) {
        Poly synd = polynoms[ID_SYNDROMES];
        synd.length = ecc_length + 1;
        synd.ptr()[0] = 0;
        for (int i = 1; i < ecc_length + 1; i++) {
            synd.ptr()[i] = Gf.poly_eval(msg, Gf.pow(2, i - 1));
        }
    }

    private void FindErrataLocator(Poly epos) {
        Poly errata_loc = polynoms[ID_ERASURES_LOC];
        Poly mulp = polynoms[ID_TPOLY1];
        Poly addp = polynoms[ID_TPOLY2];
        Poly apol = polynoms[ID_TPOLY3];
        Poly temp = polynoms[ID_TPOLY4];

        errata_loc.length = 1;
        errata_loc.ptr()[0] = 1;

        mulp.length = 1;
        addp.length = 2;

        for (int i = 0; i < epos.length; i++) {
            mulp.ptr()[0] = 1;
            addp.ptr()[0] = Gf.pow(2, epos.ptr()[i]);
            addp.ptr()[1] = 0;
            Gf.poly_add(mulp, addp, apol);
            Gf.poly_mul(errata_loc, apol, temp);
            errata_loc.Copy(temp);
        }
    }

    private void FindErrorEvaluator(Poly synd, Poly errata_loc, Poly dst, int ecclen) {
        Poly mulp = polynoms[ID_TPOLY1];
        Gf.poly_mul(synd, errata_loc, mulp);

        Poly divisor = polynoms[ID_TPOLY2];
        divisor.length = ecclen + 2;

        divisor.Reset();
        divisor.ptr()[0] = 1;
        Gf.poly_div(mulp, divisor, dst);
    }

    private void CorrectErrata(Poly synd, Poly err_pos, Poly msg_in) {
        Poly c_pos = polynoms[ID_COEF_POS];
        Poly corrected = polynoms[ID_MSG_OUT];
        c_pos.length = err_pos.length;

        for (int i = 0; i < err_pos.length; i++) {
            c_pos.ptr()[i] = msg_in.length - 1 - err_pos.ptr()[i];
        }

        /* uses t_poly 1, 2, 3, 4 */
        FindErrataLocator(c_pos);
        Poly errata_loc = polynoms[ID_ERASURES_LOC];

        /* reversing syndromes */
        Poly rsynd = polynoms[ID_TPOLY3];
        rsynd.length = synd.length;

        for (int i = synd.length - 1, j = 0; i >= 0; i--, j++) {
            rsynd.ptr()[j] = synd.ptr()[i];
        }

        /* getting reversed error evaluator polynomial */
        Poly re_eval = polynoms[ID_TPOLY4];

        /* uses T_POLY 1, 2 */
        FindErrorEvaluator(rsynd, errata_loc, re_eval, errata_loc.length - 1);

        /* reversing it back */
        Poly e_eval = polynoms[ID_ERR_EVAL];
        e_eval.length = re_eval.length;
        for (int i = re_eval.length - 1, j = 0; i >= 0; i--, j++) {
            e_eval.ptr()[j] = re_eval.ptr()[i];
        }

        Poly X = polynoms[ID_TPOLY1];
        /* this will store errors positions */
        X.length = 0;

        short l;
        for (int i = 0; i < c_pos.length; i++) {
            l = (short) (255 - c_pos.ptr()[i]);
            X.Append(Gf.pow(2, -l));
        }

        /* Magnitude polynomial*/
        Poly E = polynoms[ID_MSG_E];
        E.Reset();
        E.length = msg_in.length;

        int Xi_inv;

        Poly err_loc_prime_temp = polynoms[ID_TPOLY2];

        int err_loc_prime;
        int y;

        for (int i = 0; i < X.length; i++) {
            Xi_inv = Gf.inverse(X.ptr()[i]);

            err_loc_prime_temp.length = 0;
            for (int j = 0; j < X.length; j++) {
                if (j != i) {
                    err_loc_prime_temp.Append(Gf.sub(1, Gf.mul((short) Xi_inv, (short) X.ptr()[j])));
                }
            }

            err_loc_prime = 1;
            for (int j = 0; j < err_loc_prime_temp.length; j++) {
                err_loc_prime = Gf.mul((short) err_loc_prime, (short) err_loc_prime_temp.ptr()[j]);
            }

            y = Gf.poly_eval(re_eval, (short) Xi_inv);
            y = Gf.mul((short) Gf.pow(X.ptr()[i], 1), (short) y);

            E.ptr()[err_pos.ptr()[i]] = Gf.div(y, err_loc_prime);
        }

        Gf.poly_add(msg_in, E, corrected);
    }

    private boolean FindErrorLocator(Poly synd, Poly erase_loc, int erase_count) {

        Poly error_loc = polynoms[ID_ERRORS_LOC];
        Poly err_loc = polynoms[ID_TPOLY1];
        Poly old_loc = polynoms[ID_TPOLY2];
        Poly temp = polynoms[ID_TPOLY3];
        Poly temp2 = polynoms[ID_TPOLY4];

        if (erase_loc != null) {
            err_loc.Copy(erase_loc);
            old_loc.Copy(erase_loc);
        } else {
            err_loc.length = 1;
            old_loc.length = 1;
            err_loc.ptr()[0] = 1;
            old_loc.ptr()[0] = 1;
        }

        int synd_shift = 0;
        if (synd.length > ecc_length) {
            synd_shift = synd.length - ecc_length;
        }

        int K = 0;
        int delta = 0;
        int index;

        for (int i = 0; i < ecc_length - erase_count; i++) {
            if (erase_loc != null) {
                K = erase_count + i + synd_shift;
            } else {
                K = i + synd_shift;
            }

            delta = synd.ptr()[K];
            for (int j = 1; j < err_loc.length; j++) {
                index = err_loc.length - j - 1;
                delta ^= Gf.mul((short) err_loc.ptr()[index], (short) synd.ptr()[K - j]);
            }

            old_loc.Append(0);

            if (delta != 0) {
                if (old_loc.length > err_loc.length) {
                    Gf.poly_scale(old_loc, temp, (short) delta);
                    Gf.poly_scale(err_loc, old_loc, (short) Gf.inverse(delta));
                    err_loc.Copy(temp);
                }
                Gf.poly_scale(old_loc, temp, (short) delta);
                Gf.poly_add(err_loc, temp, temp2);
                err_loc.Copy(temp2);
            }
        }

        int shift = 0;
        while ((err_loc.length == 0) && (err_loc.ptr()[shift] == 0)) {
            shift++;
        }

        int errs = err_loc.length - shift - 1;
        if (((errs - erase_count) * 2 + erase_count) > ecc_length) {
            return false;
            /* Error count is greater then we can fix! */
        }

        System.arraycopy(err_loc.ptr(), shift, error_loc.ptr(), 0, err_loc.length - shift);
        error_loc.length = err_loc.length - shift;
        return true;
    }

    private boolean FindErrors(Poly error_loc, int msg_in_size) {
        Poly err = polynoms[ID_ERRORS];

        int errs = error_loc.length - 1;
        err.length = 0;

        for (int i = 0; i < msg_in_size; i++) {
            if (Gf.poly_eval(error_loc, Gf.pow(2, i)) == 0) {
                err.Append(msg_in_size - 1 - i);
            }
        }

        if (err.length != errs) /* couldn't find error locations */ {
            return false;
        }
        return true;
    }

    private void CalcForneySyndromes(Poly synd, Poly erasures_pos, int msg_in_size) {
        Poly erase_pos_reversed = polynoms[ID_TPOLY1];
        Poly forney_synd = polynoms[ID_FORNEY];
        erase_pos_reversed.length = 0;

        for (int i = 0; i < erasures_pos.length; i++) {
            erase_pos_reversed.Append(msg_in_size - 1 - erasures_pos.ptr()[i]);
        }

        forney_synd.Reset();

        forney_synd.Set(synd.ptr(), 1, synd.length - 1, 0);

        int x;
        for (int i = 0; i < erasures_pos.length; i++) {
            x = Gf.pow(2, erase_pos_reversed.ptr()[i]);
            for (int j = 0; j < forney_synd.length - 1; j++) {
                forney_synd.ptr()[j] = (Gf.mul(forney_synd.ptr()[j], x) ^ forney_synd.ptr()[j + 1]);
            }
        }
    }
}
