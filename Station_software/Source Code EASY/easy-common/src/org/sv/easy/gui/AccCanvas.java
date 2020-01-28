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
import org.sv.easy.engine.api.SensorEventListener;
import processing.core.PApplet;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the canvas class of the
 * acceleration plots of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the canvas class of the acceleration plots of EASY app
 */
@SuppressWarnings("ClassWithoutLogger")
class AccCanvas extends Canvas implements SensorEventListener {

    private double ACCByteX = 0.0;
    private double ACCByteY = 0.0;
    private double ACCByteZ = 0.0;
    private float accYaxisx = 2.0f;
    private float accYaxisy = 2.0f;
    private float accYaxisz = 2.0f;
    private double maxrecax = 0;
    private double maxrecay = 0;
    private double maxrecaz = 0;
    private RollingLine2DTrace r;
    private RollingLine2DTrace r2;
    private RollingLine2DTrace r3;
    private Graph2D accx;
    private Graph2D accy;
    private Graph2D accz;
    private GridBackground gbx;
    private GridBackground gby;
    private GridBackground gbz;
    private final int samplingFrequency;
    private final Easyplot outer;

    AccCanvas(int samplingFrequency, final Easyplot outer) {
        this.outer = outer;
        this.samplingFrequency = samplingFrequency;
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        ACCByteX = e.getAccelerationX();
        ACCByteY = e.getAccelerationY();
        ACCByteZ = e.getAccelerationZ();
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
        r = new RollingLine2DTrace(new AccEqX(), spst, srin);
        r.setTraceColour(38, 77, 2);
        r.setLineWidth(2);
        r2 = new RollingLine2DTrace(new AccEqY(), spst, srin);
        r2.setTraceColour(255, 0, 0);
        r2.setLineWidth(2);
        r3 = new RollingLine2DTrace(new AccEqZ(), spst, srin);
        r3.setTraceColour(0, 0, 255);
        r3.setLineWidth(2);
        accx = new Graph2D(p, 900, 180, false);
        accx.setXAxisMin(-70);
        accx.setXAxisMax(0f);
        accx.setYAxisLabel("Acceleration (m/s\u00b2) - EW axis");
        accx.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        accx.setXAxisTickSpacing(100f);
        accx.setXAxisMinorTicks(3);
        accx.setXAxisLabelAccuracy(0);
        gbx = new GridBackground(new GWColour(230));
        gbx.setGridColour(180, 180, 180, 180, 180, 180);
        accx.setBackground(gbx);
        accx.addTrace(r);
        accx.position.y = 30;
        accx.position.x = 330;
        accy = new Graph2D(p, 900, 180, false);
        accy.setXAxisMin(-70);
        accy.setXAxisMax(0f);
        accy.setYAxisLabel("Acceleration (m/s\u00b2) - NS axis");
        accy.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        accy.setXAxisTickSpacing(100f);
        accy.setXAxisMinorTicks(3);
        //accy.setYAxisLabelAccuracy(4);
        accy.setXAxisLabelAccuracy(0);
        gby = new GridBackground(new GWColour(230));
        gby.setGridColour(180, 180, 180, 180, 180, 180);
        accy.setBackground(gby);
        accy.addTrace(r2);
        accy.position.y = 250;
        accy.position.x = 330;
        accz = new Graph2D(p, 900, 180, false);
        accz.setXAxisMin(-70);
        accz.setXAxisMax(0f);
        accz.setYAxisLabel("Acceleration (m/s\u00b2) - Z axis");
        accz.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        accz.setXAxisTickSpacing(100f);
        accz.setXAxisMinorTicks(3);
        accz.setXAxisLabelAccuracy(0);
        //accz.setYAxisLabelAccuracy(4);
        gbz = new GridBackground(new GWColour(230));
        gbz.setGridColour(180, 180, 180, 180, 180, 180);
        accz.setBackground(gbz);
        accz.addTrace(r3);
        accz.position.y = 470;
        accz.position.x = 330;
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
        accx.setXAxisLabel(stamp.format(calendar.getTime()));
        accy.setXAxisLabel(stamp.format(calendar.getTime()));
        accz.setXAxisLabel(stamp.format(calendar.getTime()));
        accx.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(ACCByteX) > accYaxisx) {
            accYaxisx = (float) (Math.abs(ACCByteX) + Math.abs(ACCByteX) * 0.2f);
        } else if (Math.abs(ACCByteX) < accYaxisx * 0.001f) {
            accYaxisx = (float) (Math.abs(ACCByteX) + Math.abs(ACCByteX) * 0.2f);
        }
        accx.setYAxisMax(accYaxisx);
        accx.setYAxisMin(-accYaxisx);
        accx.setYAxisTickSpacing(accYaxisx / 5);
        accx.setYAxisMinorTicks(5);
        accy.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(ACCByteY) > accYaxisy) {
            accYaxisy = (float) (Math.abs(ACCByteY) + Math.abs(ACCByteY) * 0.2f);
        } else if (Math.abs(ACCByteY) < accYaxisy * 0.001f) {
            accYaxisy = (float) (Math.abs(ACCByteY) + Math.abs(ACCByteY) * 0.2f);
        }
        accy.setYAxisMax(accYaxisy);
        accy.setYAxisMin(-accYaxisy);
        accy.setYAxisTickSpacing(accYaxisy / 5);
        accy.setYAxisMinorTicks(5);
        accz.getYAxis().setTickLabelType(ValueType.EXPONENT);
        if (Math.abs(ACCByteZ) > accYaxisz) {
            accYaxisz = (float) (Math.abs(ACCByteZ) + Math.abs(ACCByteZ) * 0.2f);
        } else if (Math.abs(ACCByteZ) < accYaxisz * 0.001f) {
            accYaxisz = (float) (Math.abs(ACCByteZ) + Math.abs(ACCByteZ) * 0.2f);
        }
        accz.setYAxisMax(accYaxisz);
        accz.setYAxisMin(-accYaxisz);
        accz.setYAxisTickSpacing(accYaxisz / 5);
        accz.setYAxisMinorTicks(5);
        outer.l21.setText("Max Acc-EW: " + df.format(maxrecax).toLowerCase() + "m/s\u00b2");
        outer.l22.setText("Max Acc-NS: " + df.format(maxrecay).toLowerCase() + "m/s\u00b2");
        outer.l23.setText("Max Acc-Z: " + df.format(maxrecaz).toLowerCase() + "m/s\u00b2");
        accx.draw();
        accy.draw();
        accz.draw();
    }

    void resetX() {
        maxrecax = 0;
    }

    void resetY() {
        maxrecay = 0;
    }

    void resetZ() {
        maxrecaz = 0;
    }

    void start() {
        r.unpause();
        r2.unpause();
        r3.unpause();
    }

    private void stop() {
        r.pause();
        r2.pause();
        r3.pause();
    }

    double getPGAx() {
        return maxrecax;
    }

    double getPGAy() {
        return maxrecay;
    }
    
    double getPGAz(){
        return maxrecaz;
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots acceleration data on the X-Axis
     */
    private class AccEqX implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecax < Math.abs(ACCByteX)) {
                maxrecax = ACCByteX;
            }
            return ACCByteX;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots acceleration data on the Y-Axis
     */
    private class AccEqY implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecay < Math.abs(ACCByteY)) {
                maxrecay = ACCByteY;
            }
            return ACCByteY;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots acceleration data on the Z-Axis
     */
    private class AccEqZ implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            if (maxrecaz < Math.abs(ACCByteZ)) {
                maxrecaz = ACCByteZ;
            }
            return ACCByteZ;
        }
    }
}
