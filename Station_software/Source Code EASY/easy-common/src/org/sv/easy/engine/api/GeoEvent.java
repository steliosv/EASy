/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the GeoEvent interface
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.engine.api;

public interface GeoEvent {

    int TYPE_0 = 0;
    int TYPE_1 = 1;
    int TYPE_2 = 2;
    int TYPE_3 = 3;
    int TYPE_4 = 4;

    double getAzi();

    double getMw();

    double getPa();

    double getPd();

    double getPv();

    double getTauc();

    int getType();

}
