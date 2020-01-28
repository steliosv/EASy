package org.sv.easy.gui;

import controlP5.Canvas;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.gwoptics.ValueType;
import org.gwoptics.graphics.GWColour;
import org.gwoptics.graphics.graph2D.Graph2D;
import org.gwoptics.graphics.graph2D.backgrounds.GridBackground;
import org.gwoptics.graphics.graph2D.traces.ILine2DEquation;
import org.gwoptics.graphics.graph2D.traces.RollingLine2DTrace;
import org.sv.easy.engine.api.SensorEvent;
import processing.core.PApplet;
import org.sv.easy.engine.api.SensorEventListener;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the canvas class of the velocity
 * plots of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the canvas class of the velocity plots of EASY app
 */
@SuppressWarnings("ClassWithoutLogger")
class VelCanvas extends Canvas implements SensorEventListener {

    private double VELByteX = 0.0f;
    private double VELByteY = 0.0f;
    private double VELByteZ = 0.0f;
    private float velYaxisx = 6e-12f;
    private float velYaxisy = 6e-12f;
    private float velYaxisz = 6e-12f;
    private double maxrecvx = 0;
    private double maxrecvy = 0;
    private double maxrecvz = 0;
    private RollingLine2DTrace r4;
    private RollingLine2DTrace r5;
    private RollingLine2DTrace r6;
    private GridBackground gbx;
    private GridBackground gby;
    private GridBackground gbz;
    private Graph2D velx;
    private Graph2D vely;
    private Graph2D velz;
    private final int samplingFrequency;
    private final Easyplot outer;

    VelCanvas(int samplingFrequency, final Easyplot outer) {
        this.outer = outer;
        this.samplingFrequency = samplingFrequency;
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        VELByteX = e.getVelocityX();
        VELByteY = e.getVelocityY();
        VELByteZ = e.getVelocityZ();
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup(PApplet p) {
        int sr = samplingFrequency;
        long spst = 1000 / sr; //sps to samplerate in ms
        float srin = 1 / (float) sr;
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        r4 = new RollingLine2DTrace(new VelEqX(), spst, srin);
        r4.setTraceColour(38, 77, 2);
        r4.setLineWidth(2);
        r5 = new RollingLine2DTrace(new VelEqY(), spst, srin);
        r5.setTraceColour(255, 0, 0);
        r5.setLineWidth(2);
        r6 = new RollingLine2DTrace(new VelEqZ(), spst, srin);
        r6.setTraceColour(0, 0, 255);
        r6.setLineWidth(2);
        velx = new Graph2D(p, 900, 180, false);
        velx.setXAxisMin(-70);
        velx.setXAxisMax(0f);
        velx.setYAxisLabel("Velocity (m/s) - EW axis");
        velx.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        velx.setXAxisTickSpacing(100f);
        velx.setXAxisMinorTicks(3);
        velx.setXAxisLabelAccuracy(0);
        gbx = new GridBackground(new GWColour(230));
        gbx.setGridColour(180, 180, 180, 180, 180, 180);
        velx.setBackground(gbx);
        velx.addTrace(r4);
        velx.position.y = 30;
        velx.position.x = 330;
        vely = new Graph2D(p, 900, 180, false);
        vely.setXAxisMin(-70);
        vely.setXAxisMax(0f);
        vely.setYAxisLabel("Velocity (m/s) - NS axis");
        vely.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        vely.setXAxisTickSpacing(100f);
        vely.setXAxisMinorTicks(3);
        vely.setXAxisLabelAccuracy(0);
        //vely.setYAxisLabelAccuracy(3);
        gby = new GridBackground(new GWColour(230));
        gby.setGridColour(180, 180, 180, 180, 180, 180);
        vely.setBackground(gby);
        vely.addTrace(r5);
        vely.position.y = 250;
        vely.position.x = 330;
        velz = new Graph2D(p, 900, 180, false);
        velz.setXAxisMin(-70);
        velz.setXAxisMax(0f);
        velz.setYAxisLabel("Velocity (m/s) - Z axis");
        velz.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        velz.setXAxisTickSpacing(100f);
        velz.setXAxisMinorTicks(3);
        velz.setXAxisLabelAccuracy(0);
        //velz.setYAxisLabelAccuracy(3);
        gbz = new GridBackground(new GWColour(230));
        gbz.setGridColour(180, 180, 180, 180, 180, 180);
        velz.setBackground(gbz);
        velz.addTrace(r6);
        velz.position.y = 470;
        velz.position.x = 330;
        stop();
    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     * Dynamically scales up/down Y axis according to the represented values
     *
     * @exception RollingTraceTooFastException This exception is thrown when the
     * graph is trying to update too fast. You must pick values for
     * msRefreshRate and xIncr in the RollingLine2DTrace constructor
     */
    @Override
    public void draw(PApplet p) {
        DecimalFormat df = new DecimalFormat("0.000E00");
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        velx.setXAxisLabel(stamp.format(calendar.getTime()));
        vely.setXAxisLabel(stamp.format(calendar.getTime()));
        velz.setXAxisLabel(stamp.format(calendar.getTime()));
        velx.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(VELByteX) > velYaxisx) {
            velYaxisx = (float) (Math.abs(VELByteX) + Math.abs(VELByteX) * 0.2f);
        } else if (Math.abs(VELByteX) < velYaxisx * 0.001f) {
            velYaxisx = (float) (Math.abs(VELByteX) + Math.abs(VELByteX) * 0.2f);
        }
        velx.setYAxisMax(velYaxisx);
        velx.setYAxisMin(-velYaxisx);
        velx.setYAxisTickSpacing(velYaxisx / 5);
        velx.setYAxisMinorTicks(5);
        vely.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(VELByteY) > velYaxisy) {
            velYaxisy = (float) (Math.abs(VELByteY) + Math.abs(VELByteY) * 0.2f);
        } else if (Math.abs(VELByteY) < velYaxisy * 0.001f) {
            velYaxisy = (float) (Math.abs(VELByteY) + Math.abs(VELByteY) * 0.2f);
        }
        vely.setYAxisMax(velYaxisy);
        vely.setYAxisMin(-velYaxisy);
        vely.setYAxisTickSpacing(velYaxisy / 5);
        vely.setYAxisMinorTicks(5);
        velz.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(VELByteZ) > velYaxisz) {
            velYaxisz = (float) (Math.abs(VELByteZ) + Math.abs(VELByteZ) * 0.2f);
        } else if (Math.abs(VELByteZ) < velYaxisz * 0.001f) {
            velYaxisz = (float) (Math.abs(VELByteZ) + Math.abs(VELByteZ) * 0.2f);
        }
        velz.setYAxisMax(velYaxisz);
        velz.setYAxisMin(-velYaxisz);
        velz.setYAxisTickSpacing(velYaxisz / 5);
        velz.setYAxisMinorTicks(5);
        outer.l24.setText("Max Vel-EW: " + df.format(maxrecvx).toLowerCase() + "m/s");
        outer.l25.setText("Max Vel-NS: " + df.format(maxrecvy).toLowerCase() + "m/s");
        outer.l26.setText("Max Vel-Z: " + df.format(maxrecvz).toLowerCase() + "m/s");
        velx.draw();
        vely.draw();
        velz.draw();
    }

     void resetX() {
        maxrecvx = 0;
    }

     void resetY() {
        maxrecvy = 0;
    }

     void resetZ() {
        maxrecvz = 0;
    }

    void start() {
        r4.unpause();
        r5.unpause();
        r6.unpause();
    }

    private void stop() {
        r4.pause();
        r5.pause();
        r6.pause();
    }


    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots velocity data on the X-Axis
     */
    private class VelEqX implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecvx < Math.abs(VELByteX)) {
                maxrecvx = VELByteX;
            }
            return VELByteX;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots velocity data on the Y-Axis
     */
    private class VelEqY implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecvy < Math.abs(VELByteY)) {
                maxrecvy = VELByteY;
            }
            return VELByteY;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots velocity data on the Z-Axis
     */
    private class VelEqZ implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecvz < Math.abs(VELByteZ)) {
                maxrecvz = VELByteZ;
            }
            return VELByteZ;
        }
    }

}
