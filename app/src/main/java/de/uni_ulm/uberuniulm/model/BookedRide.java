package de.uni_ulm.uberuniulm.model;

import java.sql.Date;
import java.sql.Time;

public class BookedRide {

    private String destination;
    private String departure;
    private String route;
    private ParkingSpot parkingspot;
    private float price;
    private Date date;
    private Time time;
    private int places;
    private int places_open;
    private User offering_person;

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

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public ParkingSpot getParkingspot() {
        return parkingspot;
    }

    public void setParkingspot(ParkingSpot parkingspot) {
        this.parkingspot = parkingspot;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public User getOffering_person() {
        return offering_person;
    }

    public void setOffering_person(User offering_person) {
        this.offering_person = offering_person;
    }



    public int getPlaces_open() {
        return places_open;
    }

    public void setPlaces_open(int places_open) {
        this.places_open = places_open;
    }

    public BookedRide(String destination, int places_open, String departure, String route, ParkingSpot parkingspot, float price, Date date, Time time, int places, User offering_person) {
        this.destination = destination;
        this.departure = departure;
        this.route = route;
        this.parkingspot = parkingspot;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
        this.places_open = places_open;
        this.offering_person = offering_person;
    }
}


