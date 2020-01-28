/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the interface that provides
 * information for the map's nodes
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.api.map;

import java.util.List;

public interface MapNodeProvider {

    public List<MapNode> getMapNodes();

    public boolean isTriggered();
}
