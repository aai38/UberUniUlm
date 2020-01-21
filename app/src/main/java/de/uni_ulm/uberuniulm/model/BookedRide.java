package de.uni_ulm.uberuniulm.model;

import java.sql.Date;
import java.sql.Time;

public class BookedRide {

    private String destination;
    private String departure;
    private String route;
    private ParkingSpot parkingspot;
    private int price;
    private Date date;
    private Time time;
    private int places;
    private String offering_person;

    public BookedRide(String destination, String departure, String route, ParkingSpot parkingspot, int price, Date date, Time time, int places, String offering_person) {
        this.destination = destination;
        this.departure = departure;
        this.route = route;
        this.parkingspot = parkingspot;
        this.price = price;
        this.date = date;
        this.time = time;
        this.places = places;
        this.offering_person = offering_person;
    }
}


