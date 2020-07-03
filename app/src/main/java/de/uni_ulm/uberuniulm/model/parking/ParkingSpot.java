package de.uni_ulm.uberuniulm.model.parking;

import com.tomtom.online.sdk.common.location.LatLng;

public class ParkingSpot {
    LatLng position;
    int capacity=80, fill=60;
    String name, description;

    public ParkingSpot(String name, LatLng pos, String description){
        this.name=name;
        position=pos;
        this.description= description;
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setDescription(String description){ this.description=description;}

    public String getDescription(){return description;}

    public int getFill() {
        return fill;
    }

    public void setFill(int fill) {
        this.fill = fill;
    }
}
