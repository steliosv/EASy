/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains a utility app that fixes sacpz formatting
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.sacpzformatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class Main {
	/**
     * @brief This utility strips down an IRIS Wilber3 pzc file from its comment 
     * section, and it transforms Zeros and Poles section from scientific notation
     * to plain signed numbers.      
     * @param args args[0] input file args[1] Output file 
     * @exception  IOException when wrong file is given for I/O
     */
	public static void main(String[] args) throws IOException {
		// Check how many arguments were passed in
	    if(args.length == 0)
	    {
	        System.out.println("Proper Usage is: Executable.jar file_in file_out");
	        System.exit(0);
	    }
	    
		String source = args[0];
		String dest = args[1];

		File fin = new File(source);
		FileInputStream fis = new FileInputStream(fin);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));

		FileWriter fstream = new FileWriter(dest, true);
		BufferedWriter out = new BufferedWriter(fstream);

		String dataLineRd = null;
		while ((dataLineRd = in.readLine()) != null) {
			if (dataLineRd.startsWith("*")) {
				continue; // skip info lines
			}
			if ((dataLineRd.startsWith("ZEROS")) || (dataLineRd.startsWith("POLES")) || (dataLineRd.startsWith("CONSTANT"))) {
				out.write(dataLineRd);
				out.newLine();
			} else {
				String[] valueWithSciFormt = dataLineRd.split("\\s+");
				int length = valueWithSciFormt.length;
				for (int l = 1; l < length; l++) { // set l to zero if no indent
					String valueNoSciFormt = new BigDecimal(valueWithSciFormt[l]).toPlainString();
					if (l != length - 1) {
						out.write(valueNoSciFormt + "\t");
					} else {
						out.write(valueNoSciFormt);
					}
				}
				out.newLine();
			}
		}
		// close the buffer reader/writer
		in.close();
		out.close();
	}
}