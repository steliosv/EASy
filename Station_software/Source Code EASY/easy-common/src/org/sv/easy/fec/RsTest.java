/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the test program for the reed solomon encoder/ decoder
 * @author Mike Lubinets, Java version stevo
 * @bug No known bugs.
 */
package org.sv.easy.fec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @brief This contains the test class
 */
public class RsTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        // TODO code application logic here
        int ECC_LENGTH = 10;
        String message = "1206 1206 1206 1206 1206 1206 1206 1206 0 ";

        int msglen = 80;
        char[] message_frame = new char[msglen];
        char[] repaired = new char[msglen];
        char[] repaired2 = new char[msglen];
        char[] encoded = new char[msglen + ECC_LENGTH];
        char[] encoded2 = "1206 1206 1206 1206 1206 1206 1206 1206 0                                       0a2ØÄ s".toCharArray();
        Arrays.fill(message_frame, (char)0);
        System.arraycopy(message.toCharArray(), 0, message_frame,  0, message.toCharArray().length); 
        
        Reedsolomon rs = new Reedsolomon(msglen, ECC_LENGTH);
        rs.Encode(message_frame, encoded);
        int a = rs.Decode(encoded2, repaired2, null, (char) 0);
        int b = rs.Decode(encoded, repaired, null, (char) 0);
        System.out.println("original message:\t" + message);
        System.out.println("encoded by java:\t" + new String(encoded));
        System.out.println("encoded by msp432:\t" + new String(encoded2));
        System.out.println("decoded msp432 -> java:\t" + new String(repaired2));
        System.out.println("decoded by java:\t" + new String(repaired));

    }
}
