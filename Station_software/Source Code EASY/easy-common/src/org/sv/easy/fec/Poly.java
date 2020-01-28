/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that contains methods for
 * the Polynomial manipulation
 * @author Mike Lubinets, Java version stevo
 * @bug No known bugs.
 */
package org.sv.easy.fec;

import java.util.Arrays;

/**
 * @brief This contains the methods for the Polynomial manipulation
 */
public class Poly {

    public int length;
    private int _id;
    //private int _size;    // Size of reserved memory for this polynomial
    //private int _offset;  // Offset in memory
    private int[] _memory;  // Pointer to pointer to memory

    /**
     * @brief Class constructor
     */
    public Poly() {
        length = 0;
        _memory = null;
    }

    /**
     * @brief Append number at the end of polynomial
     * @param num - number to append
     * @return false if polynomial can't be stretched
     */
    public boolean Append(int num) {
        assert (length + 1 < _memory.length);
        ptr()[length++] = num;
        return true;
    }

    /**
     * @brief Polynomial initialization
     * @param id - id
     * @param offset - offset
     * @param size - length
     * @param memory_ptr - array to store data
     */
    public void Init(int id, int offset, int size, int[] memory_ptr) {
        this._id = id;
        //this._offset = offset;
        //this._size = size;
        this.length = 0;
        this._memory = memory_ptr;
    }

    /**
     * @brief Polynomial memory zeroing
     */
    public void Reset() {
        Arrays.fill(ptr(), 0, _memory.length, 0);
    }

    /**
     * @brief Copy polynomial to memory
     * @param src - source byte-sequence
     * @param len - size of polynomial
     * @param offset - write offset
     */
    public void Set(int[] src, int len, int offset) {
        //offset = (int)0;
        assert (src != null && len <= (this._memory.length - offset));
        for (int i = 0; i < len; i++) {
            ptr()[offset + i] = src[i];
        }
        length = len + offset;
    }

    /**
     * @brief Copy polynomial to memory
     * @param src - source byte-sequence
     * @param src_offset - offset
     * @param len - size of polynomial
     * @param offset - write offset
     */
    public void Set(int[] src, int src_offset, int len, int offset) {
        //offset = (int)0;
        assert (src != null && len <= (this._memory.length - offset));
        for (int i = 0; i < len; i++) {
            ptr()[offset + i] = src[i + src_offset];
        }
        length = len + offset;
    }

    /**
     * @brief Copy polynomial to memory
     * @param src - source byte-sequence
     */
    public void Copy(Poly src) {
        length = Math.max(length, src.length);
        Set(src.ptr(), length, 0);
    }

    /**
     * @brief Sets value at a specific memory address
     * @param i - address
     */
    public int at(int i) {
        assert (i < _memory.length);
        return ptr()[i];
    }

    /**
     * @brief Sets value at a specific memory address
     * @param i - address
     * @param v - value
     */
    public void at(int i, int v) {
        assert (i < _memory.length);
        ptr()[i] = v;
    }

    public int id() {
        return _id;
    }

    /**
     * @brief Returns the internal memory length
     */
    public int size() {
        return _memory.length;
    }

    /**
     * @brief Returns the internal memory
     */
    public int[] ptr() {
        return _memory;
    }
}
