package de.uni_ulm.uberuniulm.model;

import com.tomtom.online.sdk.map.Route;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

public class OfferedRide {
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    public ArrayList<User> getBookedUsers() {
        return bookedUsers;
    }

    public void setBookedUsers(ArrayList<User> bookedUsers) {
        this.bookedUsers = bookedUsers;
    }

    private String destination;
    private String departure;
    private Route route;
    private ParkingSpot parkingspot;
    private int price;
    private Date date;
    private String time;
    private int places;
    private int places_open;
    private ArrayList<User> bookedUsers;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;


    public OfferedRide(Route route, int price, Date date, String time, int places, int places_open) {
        this.route = route;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
        this.places_open = places_open;
        this.bookedUsers = new ArrayList<>();
    }

    public OfferedRide(String destination, String departure, Route route, ParkingSpot parkingspot, int price, Date date, String time, int places, int places_open, ArrayList<User> bookedUsers) {
        this.destination = destination;
        this.departure = departure;
        this.route = route;
        this.parkingspot = parkingspot;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
        this.places_open = places_open;
        this.bookedUsers = bookedUsers;
    }
}
