/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the Interface for listening to
 * taucpd Events
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.api;

public interface GeoEventListener {

    public void taucPdEvent(GeoEvent event);
}
