package de.uni_ulm.uberuniulm.model;

import com.tomtom.online.sdk.common.location.LatLng;

public class ParkingSpot {
    LatLng position;
    int capacity;
    String name;

    public ParkingSpot(String name, LatLng pos){
        this.name=name;
        position=pos;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
