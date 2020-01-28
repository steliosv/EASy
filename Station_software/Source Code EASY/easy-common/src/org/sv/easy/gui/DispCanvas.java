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
 * @brief E.A.SY. Application. This contains the Canvas class of the
 * displacement plots of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the canvas class of the displacement plots of EASY app
 */
@SuppressWarnings("ClassWithoutLogger")
class DispCanvas extends Canvas implements SensorEventListener {

    private double DISPByteX = 0.0f;
    private double DISPByteY = 0.0f;
    private double DISPByteZ = 0.0f;
    private float dispYaxisx = 6e-8f;
    private float dispYaxisy = 6e-8f;
    private float dispYaxisz = 6e-8f;
    private double maxrecdx = 0;
    private double maxrecdy = 0;
    private double maxrecdz = 0;
    private RollingLine2DTrace r7;
    private RollingLine2DTrace r8;
    private RollingLine2DTrace r9;
    private GridBackground gbx;
    private GridBackground gby;
    private GridBackground gbz;
    private Graph2D dispx;
    private Graph2D dispy;
    private Graph2D dispz;
    private final int samplingFrequency;
    private final Easyplot outer;

    DispCanvas(int samplingFrequency, final Easyplot outer) {
        this.outer = outer;
        this.samplingFrequency = samplingFrequency;
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        DISPByteX = e.getDisplacementX();
        DISPByteY = e.getDisplacementY();
        DISPByteZ = e.getDisplacementZ();
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup(PApplet p) {
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        int sr = samplingFrequency;
        long spst = 1000 / sr; //sps to samplerate in ms
        float srin = 1 / (float) sr;
        r7 = new RollingLine2DTrace(new DispEqX(), spst, srin);
        r7.setTraceColour(38, 77, 2);
        r7.setLineWidth(2);
        r8 = new RollingLine2DTrace(new DispEqY(), spst, srin);
        r8.setTraceColour(255, 0, 0);
        r8.setLineWidth(2);
        r9 = new RollingLine2DTrace(new DispEqZ(), spst, srin);
        r9.setTraceColour(0, 0, 255);
        r9.setLineWidth(2);
        dispx = new Graph2D(p, 900, 180, false);
        dispx.setXAxisMin(-70);
        dispx.setXAxisMax(0f);
        dispx.setYAxisLabel("Displacement (m) - EW axis");
        dispx.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        dispx.setXAxisTickSpacing(100f);
        dispx.setXAxisMinorTicks(3);
        dispx.setXAxisLabelAccuracy(0);
        gbx = new GridBackground(new GWColour(230));
        gbx.setGridColour(180, 180, 180, 180, 180, 180);
        dispx.setBackground(gbx);
        dispx.addTrace(r7);
        dispx.position.y = 30;
        dispx.position.x = 330;
        dispy = new Graph2D(p, 900, 180, false);
        dispy.setXAxisMin(-70);
        dispy.setXAxisMax(0f);
        dispy.setYAxisLabel("Displacement (m) - NS axis");
        dispy.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        dispy.setXAxisTickSpacing(100f);
        dispy.setXAxisMinorTicks(3);
        dispy.setXAxisLabelAccuracy(0);
        gby = new GridBackground(new GWColour(230));
        gby.setGridColour(180, 180, 180, 180, 180, 180);
        dispy.setBackground(gby);
        dispy.addTrace(r8);
        dispy.position.y = 250;
        dispy.position.x = 330;
        dispz = new Graph2D(p, 900, 180, false);
        dispz.setXAxisMin(-70);
        dispz.setXAxisMax(0f);
        dispz.setYAxisLabel("Displacement (m)  - Z axis");
        dispz.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        dispz.setXAxisTickSpacing(100f);
        dispz.setXAxisMinorTicks(3);
        dispz.setXAxisLabelAccuracy(0);
        gbz = new GridBackground(new GWColour(230));
        gbz.setGridColour(180, 180, 180, 180, 180, 180);
        dispz.setBackground(gbz);
        dispz.addTrace(r9);
        dispz.position.y = 470;
        dispz.position.x = 330;
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
        dispx.setXAxisLabel(stamp.format(calendar.getTime()));
        dispy.setXAxisLabel(stamp.format(calendar.getTime()));
        dispz.setXAxisLabel(stamp.format(calendar.getTime()));

        dispx.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(DISPByteX) > dispYaxisx) {
            dispYaxisx = (float) (Math.abs(DISPByteX) + Math.abs(DISPByteX) * 0.2f);
        } else if (Math.abs(DISPByteX) < dispYaxisx * 0.001f) {
            dispYaxisx = (float) (Math.abs(DISPByteX) + Math.abs(DISPByteX) * 0.2f);
        }
        dispx.setYAxisMax(dispYaxisx);
        dispx.setYAxisMin(-dispYaxisx);
        dispx.setYAxisTickSpacing(dispYaxisx / 5);
        dispx.setYAxisMinorTicks(5);
        dispy.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(DISPByteY) > dispYaxisy) {
            dispYaxisy = (float) (Math.abs(DISPByteY) + Math.abs(DISPByteY) * 0.2f);
        } else if (Math.abs(DISPByteY) < dispYaxisy * 0.001f) {
            dispYaxisy = (float) (Math.abs(DISPByteY) + Math.abs(DISPByteY) * 0.2f);
        }
        dispy.setYAxisMax(dispYaxisy);
        dispy.setYAxisMin(-dispYaxisy);
        dispy.setYAxisTickSpacing(dispYaxisy / 5);
        dispy.setYAxisMinorTicks(5);
        dispz.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(DISPByteZ) > dispYaxisz) {
            dispYaxisz = (float) (Math.abs(DISPByteZ) + Math.abs(DISPByteZ) * 0.2f);
        } else if (Math.abs(DISPByteZ) < dispYaxisz * 0.001f) {
            dispYaxisz = (float) (Math.abs(DISPByteZ) + Math.abs(DISPByteZ) * 0.2f);
        }
        dispz.setYAxisMax(dispYaxisz);
        dispz.setYAxisMin(-dispYaxisz);
        dispz.setYAxisTickSpacing(dispYaxisz / 5);
        dispz.setYAxisMinorTicks(5);
        outer.l27.setText("Max Disp-EW: " + df.format(maxrecdx).toLowerCase() + "m");
        outer.l28.setText("Max Disp-NS: " + df.format(maxrecdy).toLowerCase() + "m");
        outer.l29.setText("Max Disp-Z: " + df.format(maxrecdz).toLowerCase() + "m");
        dispx.draw();
        dispy.draw();
        dispz.draw();
    }

     void resetX() {
        maxrecdx = 0;
    }
     void resetY() {
        maxrecdy = 0;
    }
     void resetZ() {
        maxrecdz = 0;
    }

    void start() {
        r7.unpause();
        r8.unpause();
        r9.unpause();
    }

    private void stop() {
        r7.pause();
        r8.pause();
        r9.pause();
    }

    /**
     * @brief Implements an object that is going to be repred by a trace on a
     * Graph2D object
     * @details Plots displacement data on the X-Axis
     */
    private class DispEqX implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecdx < Math.abs(DISPByteX)) {
                maxrecdx = DISPByteX;
            }
            return DISPByteX;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots displacement data on the Y-Axis
     */
    private class DispEqY implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecdy < Math.abs(DISPByteY)) {
                maxrecdy = DISPByteY;
            }
            return DISPByteY;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots displacement data on the Z-Axis
     */
    private class DispEqZ implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecdz < Math.abs(DISPByteZ)) {
                maxrecdz = DISPByteZ;
            }
            return DISPByteZ;
        }
    }
}
