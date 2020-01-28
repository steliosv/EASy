package org.sv.easy.gui;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.EsriProvider;
import de.fhpotsdam.unfolding.utils.DebugDisplay;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.ArrayList;
import java.util.List;
import org.gicentre.utils.multisketch.EmbeddedSketch;
import org.sv.easy.api.map.MapNode;
import org.sv.easy.api.map.MapNodeProvider;

/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the class for the map window of
 * EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
/**
 * @brief This contains the class for the map window of EASY app
 */
@SuppressWarnings({"serial", "ClassWithoutLogger", "Convert2Diamond", "CollectionWithoutInitialCapacity"})
class DisplayMap extends EmbeddedSketch {

    private final List<MapNode> nodes = new ArrayList<MapNode>();
    private UnfoldingMap map;
    private DebugDisplay debugDisplay;
    private final MapNodeProvider provider;
    private final int orange = color(255, 142, 03);

    DisplayMap(final MapNodeProvider provider) {
        this.provider = provider;
    }

    /**
     * @brief Initialisation of the application's forms and settings
     */
    @Override
    public void setup() {
        size(1250, 700, P3D);
        noStroke();
        List<MapNode> list = provider.getMapNodes();
        this.nodes.addAll(list);

        map = new UnfoldingMap(this, "Node list", new EsriProvider.WorldTopoMap());
        map.setTweening(true);
        map.zoomToLevel(3);
        map.panTo(new Location(40f, 8f));
        MapUtils.createDefaultEventDispatcher(this, map);
        debugDisplay = new DebugDisplay(this, map);
    }

    /**
     * @brief Runs continuously from top to bottom until the program is stopped
     */
    @Override
    public void draw() {
        background(0);
        map.draw();
        debugDisplay.draw();
        if (provider.isTriggered()) {
            List<MapNode> list = provider.getMapNodes();
            this.nodes.addAll(list);
        }

        for (int i = 0; i < nodes.size(); i++) {
            MapNode node = nodes.get(i);
            Location nodeLocation = new Location(node.getLatitude(), node.getLongitude()); // lat,long
            ScreenPosition posnode = map.getScreenPosition(nodeLocation);
            fill(0, 200, 0, 100);
            ellipse(posnode.x, posnode.y, 10, 10);
            float s = map.getZoom();
            //equation for circle range in meters:
            //4range=262141*s (262144 is the zoom factor 2^18)
            if ("U".equals(node.getLocation())) {
                float m = s * 0.00152587890625f;
                //println(m/4);
                if (node.isTriggered()) {
                    fill(orange);
                    ellipse(posnode.x, posnode.y, m, m);
                } else {
                    fill(200, 0, 0, 100);
                    ellipse(posnode.x, posnode.y, m, m);
                }
            }
            if ("R".equals(node.getLocation())) {
                //m=s*0.0152587890625;
                float m = s * 0.0245565795898f;
                //println(m/4);
                if (node.isTriggered()) {
                    fill(orange);
                    ellipse(posnode.x, posnode.y, m, m);
                } else {
                    fill(255, 255, 0, 100);
                    ellipse(posnode.x, posnode.y, m, m);
                }
            }
        }
    }

}
