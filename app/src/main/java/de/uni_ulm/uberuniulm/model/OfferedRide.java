package de.uni_ulm.uberuniulm.model;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

public class OfferedRide {
    private String destination;
    private String departure;
    private String route;
    private ParkingSpot parkingspot;
    private int price;
    private Date date;
    private Time time;
    private int places;
    private int places_open;
    private ArrayList<User> bookedUsers;


    public OfferedRide(String destination, String departure, String route, ParkingSpot parkingspot, int price, Date date, Time time, int places, int places_open, ArrayList<User> bookedUsers) {
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
