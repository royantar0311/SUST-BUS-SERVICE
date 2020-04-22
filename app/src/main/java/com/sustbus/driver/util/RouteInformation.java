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

    public String time;
    public String comparableEndTime;
    public String comparableStartTime;
    private String routeId;
    private String path;
    private String show;
    private String from,to;
    private String title;
    private String markerId;

    public void setTime(String time) {
        this.time = time;

        int hour=Integer.parseInt(time.substring(0,2));
        int min=Integer.parseInt(time.substring(3,5));
        String ampm=time.substring(6,8);
        if(ampm.equals("pm") && hour!=12){
            hour+=12;
        }

        min-=5;
        if(min<0){
            min+=60;
            hour--;
            if(hour<0)hour=23;
        }

        comparableStartTime=(hour<10?"0":"")+hour+":"+(min<10?"0":"")+min;
        min+=50;
        if(min>=60){
            min%=60;
            hour++;
            if(hour==24)hour=0;
        }

        comparableEndTime= "";
        comparableEndTime+=(hour<10?"0":"")+hour+":"+(min<10?"0":"")+min;
    }

    public void setTitle(String title) {
        this.title = title;

        for (int i = 0; i <title.length() ; i++) {
            if(title.charAt(i)=='-'){
                from=title.substring(0,i);
                to=title.substring(i+1);
                break;
            }
        }
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public void setShow(String show) {
        this.show = show;

    }

    public String showPath(){
       return show;
    }

}
