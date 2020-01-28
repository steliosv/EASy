package org.sv.easy.gui;

import ddf.minim.analysis.FFT;
import java.util.ArrayList;
import org.gicentre.utils.multisketch.EmbeddedSketch;
import org.gwoptics.graphics.GWColour;
import org.gwoptics.graphics.graph2D.Graph2D;
import org.gwoptics.graphics.graph2D.backgrounds.GridBackground;
import org.gwoptics.graphics.graph2D.traces.ILine2DEquation;
import org.gwoptics.graphics.graph2D.traces.Line2DTrace;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.engine.api.SensorEventListener;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class for the fft window of
 * EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief Applies Fast Fourrier Transform on the selected axis
 * @details Uses Minim's audio library fft method, forms data appropriate, so
 * they can be processed properly
 */
@SuppressWarnings({"serial", "ClassWithoutLogger", "CollectionWithoutInitialCapacity"})
@Deprecated
class FFTspect extends EmbeddedSketch implements SensorEventListener {

    private final Easyplot outer;
    private final String[] yLabels = {
        "Acceleration EW Axis Amplitude(db)",
        "Acceleration NS Axis Amplitude(db)",
        "Acceleration Z Axis Amplitude(db)",
        "Velocity EW Axis Amplitude(db)",
        "Velocity NS Axis Amplitude(db)",
        "Velocity Z Axis Amplitude(db)",
        "Displacement EW Axis Amplitude(db)",
        "Displacement NS Axis Amplitude(db)",
        "Displacement Z Axis Amplitude(db)"
    };
    private final int[][] traceColors = {
        {38, 77, 2}, {255, 0, 0}, {0, 0, 255},
        {38, 77, 2}, {255, 0, 0}, {0, 0, 255},
        {38, 77, 2}, {255, 0, 0}, {0, 0, 255}
    };
    private final int FFTWindowSize = 1024;
    private int selectedFFtAxis;
    private Graph2D graph;
    private GridBackground gb;
    private Line2DTrace rfft;
    private final int bufferSize = FFTWindowSize;
    private final float[] sample = new float[FFTWindowSize];
    private int sampleRate;
    private final ArrayList<Float> DataFFT = new ArrayList<>();
    private final double[] yin = new double[FFTWindowSize];
    private FFT fft;
    private final Object lock = new Object();

    FFTspect(final Easyplot outer) {
        this.outer = outer;
    }

    public int getSelectedFFtAxis() {
        return selectedFFtAxis;
    }

    public void setSelectedFFtAxis(int selectedFFtAxis) {
        this.selectedFFtAxis = selectedFFtAxis;
        if (selectedFFtAxis >= 0 && selectedFFtAxis <= 8) {
            graph.setYAxisLabel(yLabels[selectedFFtAxis]);
            rfft.setTraceColour(traceColors[selectedFFtAxis][0], traceColors[selectedFFtAxis][1], traceColors[selectedFFtAxis][2]);
        }
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        float SensorsData;
        switch (selectedFFtAxis) {
            case 0:
                SensorsData = (float) e.getAccelerationX();
                break;
            case 1:
                SensorsData = (float) e.getAccelerationY();
                break;
            case 2:
                SensorsData = (float) e.getAccelerationZ();
                break;
            case 3:
                SensorsData = (float) e.getVelocityX();
                break;
            case 4:
                SensorsData = (float) e.getVelocityY();
                break;
            case 5:
                SensorsData = (float) e.getVelocityZ();
                break;
            case 6:
                SensorsData = (float) e.getDisplacementX();
                break;
            case 7:
                SensorsData = (float) e.getDisplacementY();
                break;
            case 8:
                SensorsData = (float) e.getDisplacementZ();
                break;
            default:
                return;
        }
        float t = SensorsData;
        synchronized (lock) {
            DataFFT.remove(0);
            DataFFT.add(t);
        }
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup() {
        size(1124, 260);
        initFFT(DataFFT, FFTWindowSize);
        sampleRate = Integer.parseInt(outer.nodeConfig.getSampleFrequency());
        textFont(createFont("SanSerif", 12));
        rfft = new Line2DTrace(new ffteq());
        rfft.setLineWidth(2);
        fft = new FFT(bufferSize, sampleRate);
        graph = new Graph2D(this, FFTWindowSize, 180, true);
        graph.setXAxisMin(0);
        graph.setXAxisMax(sampleRate);
        graph.setXAxisTickSpacing(5);
        graph.setXAxisMinorTicks(10);
        graph.setXAxisLabel("Hz");
        graph.setXAxisLabelAccuracy(0);
        graph.setYAxisMax(-120.0f);
        graph.setYAxisMin(-150.0f);
        graph.setYAxisTickSpacing(50);
        graph.setYAxisMinorTicks(5);
        graph.setYAxisLabelAccuracy(0);
        gb = new GridBackground(new GWColour(230));
        gb.setGridColour(180, 180, 180, 180, 180, 180);
        graph.setBackground(gb);
        graph.addTrace(rfft);
        graph.position.y = 30;
        graph.position.x = 60;
    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     */
    @Override
    public void draw() {
        background(240);
        synchronized (lock) {
            for (int i = 0; i < DataFFT.size(); i++) {
                sample[i] = DataFFT.get(i);
            }
        }
        float max = graph.getYAxis().getMaxValue();
        if (sample.length == bufferSize) {
            // perform a forward FFT on the samples
            fft.window(FFT.HAMMING);
            fft.forward(sample);
            for (int i = 0; i < fft.specSize(); i++) {
                yin[i] = fft.getBand(i); //get amplitude for the ith freq band
                if (Math.abs(yin[i]) > max) {
                    graph.setYAxisMax((float) yin[i] + (float) Math.abs(yin[i]) * 0.2f);
                }
            }
        }
        graph.removeTrace(rfft);
        graph.addTrace(rfft);
        graph.draw();
    }

    /**
     * @brief Initialises a float arraylist for the fft calculation
     * @param DataIn Data for initialisation
     * @param FFTWindowSize Window length
     */
    private void initFFT(ArrayList<Float> DataIn, int FFTWindowSize) {
        if (DataIn.isEmpty()) {
            for (int i = 0; i < FFTWindowSize; i++) {
                DataIn.add(0f);
            }
        }
    }

    /**
     * @brief This contains the canvas class of the fft plots of EASY app
     */
    private class ffteq implements ILine2DEquation {

        /**
         * @brief Plots the amplitudes of the freq. bands of the spectrum note
         * that each band has a central frequency calculated by((num of
         * band)/FFTWindowSize)*samplerate
         * @param x value
         * @param pos pixel
         * @return value to be plotted
         */
        @Override
        public double computePoint(double x, int pos) {
            double val;
            if (yin[pos] > 0) {
                val = 20.0 * Math.log10(yin[pos]);
            } else {
                val = -200; // avoid log(0)
            }
            return val;
        }
    }

}
