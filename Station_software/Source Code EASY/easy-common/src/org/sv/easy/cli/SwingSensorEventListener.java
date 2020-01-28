/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that provides events for
 * the cli
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.cli;

import org.sv.easy.engine.api.GeoEvent;
import org.sv.easy.engine.api.GeoEventListener;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.engine.api.SensorEventListener;
import org.sv.easy.engine.api.StaltaEvent;
import org.sv.easy.engine.api.StaltaEventListener;

public class SwingSensorEventListener implements SensorEventListener, StaltaEventListener, GeoEventListener {

    private final MessageDialog dialog;

    public SwingSensorEventListener() {
        dialog = new MessageDialog(new javax.swing.JFrame(), true);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(false);
            }
        });
    }

    String text = "";

    @Override
    public void sensorEvent(SensorEvent e) {
        text += e.toString();
        text += "\n";
        dialog.getPane().setText(text);
    }

    @Override
    public void staltaEvent(StaltaEvent e) {
        text += e.toString();
        text += "\n";
        dialog.getPane().setText(text);
    }

    @Override
    public void taucPdEvent(GeoEvent e) {
        text += e.toString();
        text += "\n";
        dialog.getPane().setText(text);
    }

}
