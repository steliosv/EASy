/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the SeismicEvent Listener Class
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import org.sv.easy.config.NodeConfig;
import org.sv.easy.engine.api.EasyEngine;
import org.sv.easy.engine.api.GeoEvent;

public interface SeismicEventListener {

    public void alert(EasyEngine seedListener ,NodeConfig nodeConfig, String trigger,
            double mag, double threat, double azimuth, GeoEvent event);
}
