package com.sustbus.driver.util;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.routing.WaypointType;

import java.util.ArrayList;
import java.util.List;

public class PathInformation {

    public boolean isRouteAvailable = true;
    private Waypoint dest;
    private List<Waypoint> wayPoints = new ArrayList<>();

    public Waypoint getDest() {
        return dest;
    }

    public void setDest(GeoCoordinates gdest) {
        dest = new Waypoint(gdest);
        dest.type = WaypointType.STOPOVER;
        wayPoints.add(dest);
    }

    public void addWayPoint(GeoCoordinates geoCoordinates) {
        Waypoint tmp = new Waypoint(geoCoordinates);
        tmp.type = WaypointType.STOPOVER;
        wayPoints.add(tmp);
    }

    public List<Waypoint> getWayPoints() {
        return wayPoints;
    }

    public void kill() {
        wayPoints.clear();
    }

}
