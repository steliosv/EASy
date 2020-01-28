package org.sv.easy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains classes and methods of the
 * Observer-Observable pattern
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the SplashScreen class and methods
 */
@SuppressWarnings({"serial", "ClassWithoutLogger"})
class SplashScreen extends JWindow {

    private final int duration;

    /**
     * @brief Class constructor
     * @param ms Duration of the splash screen in milliseconds
     */
    SplashScreen(int ms) {
        duration = ms;
    }

    /**
     * @brief Creates a splashscreen on start
     */
    public void showSplash() {

        JPanel content = (JPanel) getContentPane();
        content.setBackground(Color.white);
        // Set the window's bounds, centering the window
        int width = 840;
        int height = 620;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;
        setBounds(x, y, width, height);

        JLabel label = new JLabel(new ImageIcon(SplashScreen.class.getResource("resources/splash.png")));
        setOpacity(1f);
        JLabel copyrt = new JLabel("Earthquake Alert SYstem 2016", JLabel.CENTER);
        content.add(label, BorderLayout.CENTER);
        content.add(copyrt, BorderLayout.SOUTH);
        content.setOpaque(false);
        setVisible(true);

        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {
            //ignore
        }

        setVisible(false);
    }

}
