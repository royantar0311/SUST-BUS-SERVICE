package com.sustbus.driver.util;

public class RouteInformation {
    public String getRouteId() {
        return routeId;
    }

    public String getPath() {
        return path;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String routeId;
    private String path;
    private String time;
    private String title;

}
