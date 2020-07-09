package de.uni_ulm.uberuniulm.model.ride;

import android.util.Log;

import com.tomtom.online.sdk.common.location.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.uni_ulm.uberuniulm.model.parking.ParkingSpot;
import de.uni_ulm.uberuniulm.model.User;

public class OfferedRide implements Serializable {
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public List<LatLng> getRoute() {
        return route;
    }

    public void setRoute(List<LatLng> route) {
        this.route = route;
    }

    public ParkingSpot getParkingspot() {
        return parkingspot;
    }

    public void setParkingspot(ParkingSpot parkingspot) {
        this.parkingspot = parkingspot;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public int getPlaces_open() {
        return places_open;
    }

    public void setPlaces_open(int places_open) {
        this.places_open = places_open;
    }

    public ArrayList<String> getBookedUsers() {
        return bookedUsers;
    }

    public void setBookedUsers(ArrayList<String> bookedUsers) {
        this.bookedUsers = bookedUsers;
    }

    private String destination;
    private String departure;
    private List<LatLng> route;
    private ParkingSpot parkingspot;
    private int price;
    private String date;
    private String time;
    private int places;
    private int places_open;
    private ArrayList<String> bookedUsers;
    private List<LatLng> waypoints;
    private List<String> observers;


    private String key;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public List<LatLng> getWaypoints(){return waypoints;}

    public void setWaypoints(List<LatLng> waypoints){this.waypoints=waypoints;}

    public void setObservers(List<String> observers){ this.observers=observers;}

    public List<String> getObservers(){return observers;}

    public void markRide(String userId){
        observers.add(userId);
    }

    public void unmarkRide(String userId){
        Log.d("observers", observers.toString()+ " "+ String.valueOf(observers.indexOf(userId))+ " " + userId);
        observers.remove(userId);
    }



    private String userId;
    private int zIndex;


    public OfferedRide(List<LatLng> route, int price, String date, String time, int places, int places_open, String departure, String destination, String userId, int zIndex, List<LatLng> waypoints, List<String> observers) {
        this.route = route;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
        this.places_open = places_open;
        this.bookedUsers = new ArrayList<>();
        this.departure = departure;
        this.destination = destination;
        this.userId = userId;
        this.zIndex = zIndex;
        this.waypoints=waypoints;
        this.observers=observers;
    }

    /*public OfferedRide(HashMap route, int price, String date, String time, int places, int places_open, String departure, String destination, String userId, int zIndex) {
        List<LatLng> coordinates;
        ArrayList<LatLng>[] coordinatesUnParsed=(ArrayList<LatLng>[]) route.get("coordinates");
        for(int i=0; i<route.size(); i++){

        }


        this.route = route;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
        this.places_open = places_open;
        this.bookedUsers = new ArrayList<>();
        this.departure = departure;
        this.destination = destination;
        this.userId = userId;
        this.zIndex = zIndex;
    }*/


}
