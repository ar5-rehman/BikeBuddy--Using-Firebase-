package com.techo.bikebuddy.Models;

public class RidePojo {

    String rideID;
    String rideName;
    String locationImage;
    String startAddress;
    String destinationAddress;
    String time;
    String distance;
    String date;

    public RidePojo(String rideID,String rideName, String locationImage, String startAddress, String destinationAddress, String time, String distance, String date) {
        this.rideName = rideName;
        this.locationImage = locationImage;
        this.startAddress = startAddress;
        this.destinationAddress = destinationAddress;
        this.time = time;
        this.distance = distance;
        this.date = date;
        this.rideID = rideID;
    }


    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getRideName() {
        return rideName;
    }

    public void setRideName(String rideName) {
        this.rideName = rideName;
    }

    public String getLocationImage() {
        return locationImage;
    }

    public void setLocationImage(String locationImage) {
        this.locationImage = locationImage;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
