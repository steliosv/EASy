package org.sv.easy.gui;

import controlP5.Canvas;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.gwoptics.graphics.GWColour;
import org.gwoptics.graphics.graph2D.Graph2D;
import org.gwoptics.graphics.graph2D.backgrounds.GridBackground;
import org.gwoptics.graphics.graph2D.traces.ILine2DEquation;
import org.gwoptics.graphics.graph2D.traces.RollingLine2DTrace;
import org.sv.easy.api.sampling.SampleConverter;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.spi.sampling.TemperatureConverter;
import processing.core.PApplet;
import org.sv.easy.engine.api.SensorEventListener;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the canvas class of the temperature
 * plots of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the canvas class of the temperature plots of EASY app
 */
class TempCanvas extends Canvas implements SensorEventListener {

    private static final Logger LOGGER = Logger.getLogger(TempCanvas.class.getName());

    private final RollingLine2DTrace trace;
    private Graph2D graph;
    private final SampleConverter converter = new TemperatureConverter();
    //lock
    private final Object lock = new Object();
    private int sampleCount = 0;
    private double sampleValue = 0;
    private double avgSampleValue = 0;
    private long startTimestamp = -1;

    TempCanvas(int samplingFrequency) {
        long spst = 1000 / samplingFrequency; //sps to samplerate in ms
        float srin = 1 / (float) samplingFrequency;
        trace = new RollingLine2DTrace(new TemperatEq(), spst, srin);
        trace.setTraceColour(38, 77, 2);
        trace.setLineWidth(2);
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        if (startTimestamp == -1) {
            startTimestamp = System.currentTimeMillis();
            DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
            graph.setXAxisLabel("Seconds after: " + stamp.format(startTimestamp));
            graph.addTrace(trace);
        }
        double value = converter.convert((int) e.getTemperature());
        synchronized (lock) {
            sampleCount++;
            sampleValue += value;
            avgSampleValue = sampleValue / sampleCount;
        }
    }

    private double getSampleValue() {
        synchronized (lock) {
            sampleCount = 0;
            sampleValue = 0;
            return avgSampleValue;
        }
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup(PApplet p) {
        GridBackground bg = new GridBackground(new GWColour(230));
        bg.setGridColour(180, 180, 180, 180, 180, 180);
        graph = new Graph2D(p, 900, 400, false);
        graph.setXAxisMin(-70f);
        graph.setXAxisMax(0f);
        graph.setYAxisMax(110.0f);
        graph.setYAxisMin(-20.0f);
        graph.setYAxisLabel("Ambient temperature (\u00b0C)");
        graph.setXAxisLabel("Seconds after: ...");
        graph.setXAxisTickSpacing(100f);
        graph.setXAxisMinorTicks(3);
        graph.setXAxisLabelAccuracy(0);
        graph.setBackground(bg);
        graph.position.y = 30;
        graph.position.x = 330;
        graph.setYAxisTickSpacing(10);
    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     * @exception RollingTraceTooFastException This exception is thrown when the
     * graph is trying to update too fast. You must pick values for
     * msRefreshRate and xIncr in the RollingLine2DTrace constructor
     */
    @Override
    public void draw(PApplet p) {
        DateFormat stamp = org.sv.easy.common.DateUtils.getDateFormat();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        graph.setXAxisLabel(stamp.format(calendar.getTime()));
        graph.draw();
    }

    private void start() {
        trace.unpause();
    }

    private void stop() {
        trace.pause();
    }

    /**
     * @brief Implements an object that is going to be represented by a trace on
     * a Graph2D object
     * @details Temperature plot
     */
    private class TemperatEq implements ILine2DEquation {

        /**
         * @brief Plots a pixel with a value of x on the graph
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            return getSampleValue();
        }
    }

}
