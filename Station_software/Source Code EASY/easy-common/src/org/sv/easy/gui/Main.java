/**
 * @mainpage E.A.SY. Application.
 * @version v20
 * @author Stelios Voutsinas CreateDate 2020
 */
package org.sv.easy.gui;

import processing.core.PApplet;
import static processing.core.PApplet.concat;

@SuppressWarnings("ClassWithoutLogger")
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String[] appletArgs = new String[]{"org.sv.easy.gui.Easyplot"};
        if (args != null) {
            PApplet.main(concat(appletArgs, args));
        } else {
            PApplet.main(appletArgs);
        }
    }

}
