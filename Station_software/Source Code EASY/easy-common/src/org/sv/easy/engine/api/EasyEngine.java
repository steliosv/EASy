/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the EasyEngine interface
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.api;

import java.util.Calendar;
import org.sv.easy.config.NodeConfig;
import org.sv.easy.stalta.StaLta;

public interface EasyEngine {

    public void addGeoEventListener(GeoEventListener l);

    public void removeGeoEventListener(GeoEventListener l);

    public void addListener(SensorEventListener l);

    public void removeListener(SensorEventListener l);

    public void addStaltaListener(StaltaEventListener l);

    public void removeStaltaListener(StaltaEventListener l);

    public Calendar getCalendar();

    public int getSamplingFrequency();

    public float getNyquistFrequency();

    public void push(String sample);

    public void push(float displacement[],float counts[]);

    public long getSamplingPeriodMillis();

    public float getStaltaDeTrigger();

    public void setStaltaDeTrigger(float value);

    public float getStaltaTrigger();

    public void setStaltaTrigger(float value);

    void loadFilters();

    void setNlta(int value);
    
    public float getTHpfFc();
    
    public float getTLpfFc();

    void setNodeConfig(NodeConfig nodeConfig);

    void setNsta(float value);

    void setPd_m(float pd_m);

    void setStaLtaImpl(StaLta staLtaImpl);

    void setTc(float tc);

    public int getTau0();

    public void setGeoacc(boolean b);

    public int getNlta();

    public float getNsta();

    public void createTestFile();
    
    public void flush();

    public int getSTALTAWindowSize();
}
