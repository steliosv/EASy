package org.sv.easy.gui;

import controlP5.Canvas;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.gwoptics.ValueType;
import org.gwoptics.graphics.GWColour;
import org.gwoptics.graphics.graph2D.Graph2D;
import org.gwoptics.graphics.graph2D.backgrounds.GridBackground;
import org.gwoptics.graphics.graph2D.traces.ILine2DEquation;
import org.gwoptics.graphics.graph2D.traces.RollingLine2DTrace;
import org.sv.easy.common.EasyDweetListener;
import org.sv.easy.common.SeismicEventListener;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.StaltaEvent;
import org.sv.easy.engine.api.StaltaEventListener;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.engine.api.SensorEventListener;
import processing.core.PApplet;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Canvas class of the STA/LTA
 * trigger plots of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the canvas class of the triggering algorithm plots of
 * EASY app
 */
@SuppressWarnings("ClassWithoutLogger")
class STALTACanvas extends Canvas implements StaltaEventListener, SensorEventListener {

    private static final Logger log = Logger.getLogger(STALTACanvas.class);
    private RollingLine2DTrace rc;
    private RollingLine2DTrace rd;
    private RollingLine2DTrace re;
    private RollingLine2DTrace rf;
    private RollingLine2DTrace r10;
    private RollingLine2DTrace r11;
    private RollingLine2DTrace r12;
    private RollingLine2DTrace r13;
    private RollingLine2DTrace r14;
    private RollingLine2DTrace r15;
    private Graph2D staltaX;
    private Graph2D staltaY;
    private Graph2D staltaZ;
    private Graph2D azi;
    private GridBackground gbx;
    private GridBackground gby;
    private GridBackground gbz;
    private GridBackground az;
    private double staltaValueX;
    private double staltaValueY;
    private double staltaValueZ;
    private double dispValueX;
    private double dispValueY;
    private double dispValueZ;
    private double pgax = 0;
    private double pgay = 0;
    private double pgaz = 0;
    private double pga = 0;
    private final int samplingFrequency;
    private final int increment = 1;
    private final Easyplot outer;
    private float STALTAYaxisx = 10.0f;
    private float STALTAYaxisy = 10.0f;
    private float STALTAYaxisz = 10.0f;
    private final EasyDweetListener dweet = new EasyDweetListener();
    private final NodeConfig nodeConfig;

    STALTACanvas(int samplingFrequency, final Easyplot outer, final NodeConfig nodeConfig) {
        this.outer = outer;
        this.samplingFrequency = samplingFrequency;
        this.nodeConfig = nodeConfig;
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        dispValueX = e.getDisplacementX();
        dispValueY = e.getDisplacementY();
        dispValueZ = e.getDisplacementZ();
    }

    @Override
    public void staltaEvent(StaltaEvent event) {
        if (event.getType() == StaltaEvent.EVENT_SAMPLE) {
            staltaValueX = event.getX();
            staltaValueY = event.getY();
            staltaValueZ = event.getZ();
            return;
        }
        if (event.getType() == StaltaEvent.EVENT_END_AXIS) {
            if (event.getAxis() == StaltaEvent.AXIS_X) {
                if (event.isTrigger()) {
                    outer.l17.setText("P-S time diff(Z-EW): " + Math.abs(event.getDuration()) / 1000 + " s");
                }
            }
            if (event.getAxis() == StaltaEvent.AXIS_Y) {
                if (event.isTrigger()) {
                    outer.l18.setText("P-S time diff(Z-NS): " + Math.abs(event.getDuration()) / 1000 + " s");
                }
            }
            return;
        }
        if (event.getType() == StaltaEvent.EVENT_TRIGGER) {
            if (event.getAxis() == StaltaEvent.AXIS_X) {
                if (event.isTrigger()) {
                    outer.l10.setText("Triggered!");
                    outer.l10.setColor(outer.veraman);
                } else {
                    outer.l10.setText("Not-Triggered!");
                    outer.l10.setColor(outer.yellow);
                    outer.l14.setText("Event Duration on EW: " + (float) (event.getDuration()) / 1000 + " s");
                    pgax = outer.getPGAx();
                }
            }
            if (event.getAxis() == StaltaEvent.AXIS_Y) {
                if (event.isTrigger()) {
                    outer.l11.setText("Triggered!");
                    outer.l11.setColor(outer.veraman);
                } else {
                    outer.l11.setText("Not-Triggered!");
                    outer.l11.setColor(outer.yellow);
                    outer.l13.setText("Event Duration on NS: " + (float) (event.getDuration()) / 1000 + " s");
                    pgay = outer.getPGAy();
                }
            }
            if (event.getAxis() == StaltaEvent.AXIS_Z) {
                if (event.isTrigger()) {
                    outer.l12.setText("Triggered!");
                    outer.l12.setColor(outer.veraman);
                } else {
                    outer.l12.setText("Not-Triggered!");
                    outer.l12.setColor(outer.yellow);
                    outer.resetLabelsZ();
                    outer.l15.setText("Event Duration on Z:  " + (float) event.getDuration() / 1000 + " s");
                    outer.sql.sqlDetrigger(outer.nodeConfig);
                    outer.sql.setTriggered(false);
                }
            }
            if (Math.abs(pgax) > Math.abs(pgay) && Math.abs(pgax) > Math.abs(pgaz)) {
                pga = pgax;
            } else if (Math.abs(pgay) > Math.abs(pgaz)) {
                pga = pgay;
            } else {
                pga = pgaz;
            }
            dweet.sendPGA(outer.nodeConfig, pga);
            outer.resetLabelsX();
            outer.resetLabelsY();
            outer.resetLabelsZ();
        }
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup(PApplet p) {
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        int sr = 1000 / samplingFrequency;
        float srincr = 1 / Float.parseFloat(nodeConfig.getSampleFrequency());
        rc = new RollingLine2DTrace(new STALTAEqX(), sr, increment);
        rc.setTraceColour(38, 77, 2);
        rc.setLineWidth(2);
        rd = new RollingLine2DTrace(new STALTAEqY(), sr, increment);
        rd.setTraceColour(255, 0, 0);
        rd.setLineWidth(2);
        re = new RollingLine2DTrace(new STALTAEqZ(), sr, increment);
        re.setTraceColour(0, 0, 255);
        re.setLineWidth(2);
        rf = new RollingLine2DTrace(new TrigThresEqX(), sr, increment);
        rf.setTraceColour(255, 126, 51);
        rf.setLineWidth(2);
        r10 = new RollingLine2DTrace(new TrigThresEqX(), sr, increment);
        r10.setTraceColour(255, 126, 51);
        r10.setLineWidth(2);
        r11 = new RollingLine2DTrace(new TrigThresEqX(), sr, increment);
        r11.setTraceColour(255, 126, 51);
        r11.setLineWidth(2);
        r12 = new RollingLine2DTrace(new DeTrigThresEqX(), sr, increment);
        r12.setTraceColour(97, 3, 4);
        r12.setLineWidth(2);
        r13 = new RollingLine2DTrace(new DeTrigThresEqX(), sr, increment);
        r13.setTraceColour(97, 3, 4);
        r13.setLineWidth(2);
        r14 = new RollingLine2DTrace(new DeTrigThresEqX(), sr, increment);
        r14.setTraceColour(97, 3, 4);
        r14.setLineWidth(2);
        r15 = new RollingLine2DTrace(new AziEq(), sr, srincr);
        r15.setTraceColour(0, 0, 0);
        r15.setLineWidth(2);
        staltaX = new Graph2D(p, 900, 135, false);
        staltaX.setXAxisMin(-(60 * Integer.parseInt(nodeConfig.getSampleFrequency())));
        staltaX.setXAxisMax(0);
        staltaX.setYAxisLabel("STA/LTA - EW axis");
        staltaX.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        staltaX.setXAxisTickSpacing(1000);
        staltaX.setXAxisMinorTicks(10);
        staltaX.setXAxisLabelAccuracy(0);
        gbx = new GridBackground(new GWColour(230));
        gbx.setGridColour(180, 180, 180, 180, 180, 180);
        staltaX.setBackground(gbx);
        staltaX.addTrace(rc);
        staltaX.addTrace(rf);
        staltaX.addTrace(r12);
        staltaX.position.y = 30;
        staltaX.position.x = 330;
        staltaX.setYAxisTickSpacing(10);
        staltaY = new Graph2D(p, 900, 135, false);
        staltaY.setXAxisMin(-(60 * Integer.parseInt(nodeConfig.getSampleFrequency())));
        staltaY.setXAxisMax(0);
        staltaY.setYAxisLabel("STA/LTA - NS axis");
        staltaY.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        staltaY.setXAxisTickSpacing(1000);
        staltaY.setXAxisMinorTicks(10);
        staltaY.setXAxisLabelAccuracy(0);
        gby = new GridBackground(new GWColour(230));
        gby.setGridColour(180, 180, 180, 180, 180, 180);
        staltaY.setBackground(gby);
        staltaY.addTrace(rd);
        staltaY.addTrace(r10);
        staltaY.addTrace(r13);
        staltaY.position.y = 200;
        staltaY.position.x = 330;
        staltaY.setYAxisTickSpacing(10);
        staltaZ = new Graph2D(p, 900, 135, false);
        staltaZ.setXAxisMin(-(60 * Integer.parseInt(nodeConfig.getSampleFrequency())));
        staltaZ.setXAxisMax(0);
        staltaZ.setYAxisLabel("STA/LTA - Z axis");
        staltaZ.setXAxisLabel("Seconds after: " + stamp.format(outer.seedListener.getCalendar().getTime()));
        staltaZ.setXAxisTickSpacing(1000);
        staltaZ.setXAxisMinorTicks(10);
        staltaZ.setXAxisLabelAccuracy(0);
        gbz = new GridBackground(new GWColour(230));
        gbz.setGridColour(180, 180, 180, 180, 180, 180);
        staltaZ.setBackground(gbz);
        staltaZ.addTrace(re);
        staltaZ.addTrace(r11);
        staltaZ.addTrace(r14);
        staltaZ.position.y = 370;
        staltaZ.position.x = 330;
        staltaZ.setYAxisTickSpacing(10);
        azi = new Graph2D(p, 900, 135, false);
        azi.setXAxisMin(-60);
        azi.setXAxisMax(0);
        azi.setYAxisMin(-0);
        azi.setYAxisMax(360);
        azi.setYAxisLabel("Azimuth (°N)");
        azi.setXAxisTickSpacing(10);
        azi.setXAxisMinorTicks(10);
        azi.setXAxisLabelAccuracy(0);
        az = new GridBackground(new GWColour(230));
        az.setGridColour(180, 180, 180, 180, 180, 180);
        azi.setBackground(az);
        azi.addTrace(r15);
        azi.position.y = 540;
        azi.position.x = 330;
        azi.setYAxisTickSpacing(60);
        stop();
    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     * Dynamically scales up/down Y axis according to the represented values
     * @exception RollingTraceTooFastException This exception is thrown when the
     * graph is trying to update too fast. You must pick values for
     * msRefreshRate and xIncr in the RollingLine2DTrace constructor
     */
    @Override
    public void draw(PApplet p) {
        outer.staltaOptions_unused();
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        staltaX.setXAxisLabel(stamp.format(calendar.getTime()));
        staltaY.setXAxisLabel(stamp.format(calendar.getTime()));
        staltaZ.setXAxisLabel(stamp.format(calendar.getTime()));

        if (Math.abs(staltaValueX) > STALTAYaxisx) {
            STALTAYaxisx = (float) (Math.abs(staltaValueX) + Math.abs(staltaValueX) * 0.2f);
        } else if (Math.abs(staltaValueX) < STALTAYaxisx * 0.001f) {
            STALTAYaxisx = (float) (Math.abs(staltaValueX) + Math.abs(staltaValueX) * 0.2f);
        }
        if (STALTAYaxisx < 1f) {
            staltaX.getYAxis().setTickLabelType(ValueType.EXPONENT);
        } else {
            staltaX.getYAxis().setTickLabelType(ValueType.DECIMAL);
        }
        staltaX.setYAxisMax(STALTAYaxisx);
        staltaX.setYAxisMin(0.0f);
        staltaX.setYAxisTickSpacing(STALTAYaxisx / 5);
        staltaX.setYAxisMinorTicks(10);
        if (Math.abs(staltaValueY) > STALTAYaxisy) {
            STALTAYaxisy = (float) (Math.abs(staltaValueY) + Math.abs(staltaValueY) * 0.2f);
        } else if (Math.abs(staltaValueY) < STALTAYaxisy * 0.001f) {
            STALTAYaxisy = (float) (Math.abs(staltaValueY) + Math.abs(staltaValueY) * 0.2f);
        }
        if (STALTAYaxisy < 1f) {
            staltaY.getYAxis().setTickLabelType(ValueType.EXPONENT);
        } else {
            staltaY.getYAxis().setTickLabelType(ValueType.DECIMAL);
        }
        staltaY.setYAxisMax(STALTAYaxisy);
        staltaY.setYAxisMin(0.0f);
        staltaY.setYAxisTickSpacing(STALTAYaxisy / 5);
        staltaY.setYAxisMinorTicks(10);
        if (Math.abs(staltaValueZ) > STALTAYaxisz) {
            STALTAYaxisz = (float) (Math.abs(staltaValueZ) + Math.abs(staltaValueZ) * 0.2f);
        } else if (Math.abs(staltaValueZ) < STALTAYaxisz * 0.001f) {
            STALTAYaxisz = (float) (Math.abs(staltaValueZ) + Math.abs(staltaValueZ) * 0.2f);
        }
        if (STALTAYaxisz < 1f) {
            staltaZ.getYAxis().setTickLabelType(ValueType.EXPONENT);
        } else {
            staltaZ.getYAxis().setTickLabelType(ValueType.DECIMAL);
        }
        staltaZ.setYAxisMax(STALTAYaxisz);
        staltaZ.setYAxisMin(0.0f);
        staltaZ.setYAxisTickSpacing(STALTAYaxisz / 5);
        staltaZ.setYAxisMinorTicks(10);
        staltaX.draw();
        staltaY.draw();
        staltaZ.draw();
        azi.draw();
    }

    void start() {
        rc.unpause();
        rd.unpause();
        re.unpause();
        r15.unpause();
    }

    private void stop() {
        rc.pause();
        rd.pause();
        re.pause();
        r15.pause();
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots Characteristic function of the selected algorithm for the
     * X-Axis
     */
    private class STALTAEqX implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @SuppressWarnings("override")
        public double computePoint(double x, int pos) {
            return staltaValueX;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots Characteristic function of the selected algorithm for the
     * Y-Axis
     */
    private class STALTAEqY implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            return staltaValueY;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Plots Characteristic function of the selected algorithm for the
     * Z-Axis
     */
    private class STALTAEqZ implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            return staltaValueZ;
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Threshold plot
     */
    private class TrigThresEqX implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            return outer.seedListener.getStaltaTrigger();
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Threshold plot
     */
    private class DeTrigThresEqX implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            return outer.seedListener.getStaltaDeTrigger();
        }
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Threshold plot
     */
    private class AziEq implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            double az = Math.toDegrees(Math.atan2(dispValueY, dispValueX));
            if ((dispValueZ * dispValueY) > 0) {
                az = az + 180;
            }
            az = (az + 360) % 360;
            return az;
        }
    }

}
