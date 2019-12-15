package de.uni_ulm.uberuniulm.model;

import java.sql.Date;
import java.sql.Time;

public class Ride {
    private String destination;
    private String departure;
    private String route;
    private String parkingspot;
    private int price;
    private Date date;
    private Time time;
    private int places;

    public Ride(String destination, String departure, String route, String parkingspot, int price, Date date, Time time, int places) {
        this.destination = destination;
        this.departure = departure;
        this.route = route;
        this.parkingspot = parkingspot;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
    }
}
