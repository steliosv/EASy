package org.sv.easy.gui;

import org.gicentre.utils.multisketch.EmbeddedSketch;
import processing.core.PFont;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class that shows the
 * application's info
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the class showing the about window of EASY app
 */
@SuppressWarnings({"ClassWithoutLogger"})
class About extends EmbeddedSketch {

    private static final long serialVersionUID = 1L;

    private final float ang = 0.0f;
    private float y = 600;
    private final float z = -200;
    private final float x = 300;

    About() {
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup() {
        size(600, 600, P3D);
        PFont font = loadFont(getClass().getResource("resources/Verdana-Bold-48.vlw").getFile());
        textFont(font, 20);
        textMode(MODEL);
    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     */
    @Override
    public void draw() {
        background(0);
        rotateX(PI / 6);
        stroke(0);
        strokeWeight(2);
        directionalLight(250, 207, 63, 0, -200, -200);
        textAlign(CENTER);
        text("About E.A.Sy application", x, y, z);
        text("NATIONAL AND KAPODISTRIAN\n UNIVERSITY OF ATHENS\nSCHOOL OF SCIENCES\nFaculty of Geology \n and Geoenvironment", x, y + 100, z);
        text("Developer: Stylianos Voutsinas, \n steliosvo@teipir.gr", x, y + 350, z);
        text("Athens, 2015-2018", x, y + 450, z);
        y--;
    }

}
