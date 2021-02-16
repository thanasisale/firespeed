package com.thanasis.firespeed;

public class SpeedPoint {
    private String name;
    private String speed;
    private String timestamp;
    private double lat;
    private double lng;

    public SpeedPoint(String speed, String timestamp, double lat, double lng) {
        this.speed = speed;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
    }

    public SpeedPoint(){}

    public SpeedPoint(String name, String speed, String timestamp, double lat, double lng) {
        this.name = name;
        this.speed = speed;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
