/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains classes and methods for the
 * spectrogram
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.gui;

import ddf.minim.analysis.FFT;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.sv.easy.engine.api.SensorEvent;
import org.sv.easy.engine.api.SensorEventListener;

@SuppressWarnings({"CollectionWithoutInitialCapacity", "ClassWithoutLogger", "Convert2Diamond"
})
public class SwingSpectrogram extends javax.swing.JFrame implements SensorEventListener {

    private static final long serialVersionUID = 1L;
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
    private final int type;
    private final int channel;
    private final int FFTWindowSize = 1024;
    private int selectedFFtAxis = 0;
    private int sampleRate = 0;

    private final float[] sample = new float[FFTWindowSize];
    private final ArrayList<Float> DataFFT = new ArrayList<>();
    private float SensorsData;
    private final FFT fft;
    private final XChartPanel<XYChart> chartPanel;
    private final Object lock = new Object();

    /**
     * Creates new form SwingSpectrogram
     *
     * @param type the type 
     * @param channel the channel 
     * @param sampleRate sample rate
     */
    public SwingSpectrogram(int type, int channel, int sampleRate) throws HeadlessException {
        this.type = type;
        this.channel = channel;
        this.sampleRate = sampleRate;
        for (int i = 0; i < FFTWindowSize; i++) {
            DataFFT.add(0f);
        }
        fft = new FFT(FFTWindowSize, sampleRate);

        super.setMinimumSize(new Dimension(640, 480));
        initComponents();
        XYChart chart = new XYChartBuilder()
                .xAxisTitle("Hz").yAxisTitle(yLabels[type * 3 + channel])
                .width(800).height(600).title("Spectrogram").theme(Styler.ChartTheme.Matlab).build();
        // Customize Chart
        chart.getStyler().setChartTitleVisible(true);
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setXAxisTickMarkSpacingHint(10);
        chart.getStyler().setXAxisMin(0.0);
        SwingWrapper<XYChart> w = new SwingWrapper<XYChart>(chart);
        chartPanel = new XChartPanel<XYChart>(chart);
        jPanel1.add(chartPanel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("Convert2Lambda")
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SwingSpectrogram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SwingSpectrogram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SwingSpectrogram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SwingSpectrogram.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingSpectrogram(0, 0, 100).setVisible(true);
            }
        });
    }

    @Override
    public void sensorEvent(SensorEvent e) {
        //start = true;
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
        synchronized (lock) {
            draw();
        }
    }

    private void draw() {
        //if(!start) return;
        float t = SensorsData;
        DataFFT.remove(0);
        DataFFT.add(t);
        for (int i = 0; i < DataFFT.size(); i++) {
            sample[i] = DataFFT.get(i);
        }
//        float max = graph.getYAxis().getMaxValue();
        if (true) {
            // perform a forward FFT on the samples
            fft.window(FFT.HAMMING);
            fft.forward(sample);

            double[] yin = new double[fft.specSize()];

            for (int i = 0; i < fft.specSize(); i++) {
                yin[i] = fft.getBand(i); //get amplitude for the ith freq band
            }
            ArrayList<Double> yData = new ArrayList<Double>();
            for (int i = 0; i < yin.length; i++) {
                yData.add(normalize(yin[i]));
            }

            if (chartPanel.getChart().getSeriesMap().isEmpty()) {
                XYSeries series = chartPanel.getChart().addSeries("data", xValues(), yData);
                series.setMarker(SeriesMarkers.NONE);

            } else {
                chartPanel.getChart().updateXYSeries("data", xValues(), yData, null);
            }
            chartPanel.updateUI();
        }
    }

    private List<Double> xValues() {
        List<Double> list = new ArrayList<>(this.FFTWindowSize / 2 + 1);
        list.add(0.0);
        for (int i = 1; i <= (this.FFTWindowSize / 2); i++) {
            double v = i;
            v *= this.sampleRate;
            v /= this.FFTWindowSize;
            list.add(v);
        }
        return list;
    }

    private double normalize(double v) {
        double val;
        if (v > 0) {
            val = 20.0 * Math.log10(v);
        } else {
            val = -200; // avoid log(0)
        }
        return val;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
