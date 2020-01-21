package de.uni_ulm.uberuniulm.model;

import java.util.ArrayList;

public class User {
    private ArrayList<BookedRide> bookedRides;
    private String email;
    private String gender;
    private String image;
    private String lastname;
    private String prename;
    private String message;
    private ArrayList<OfferedRide> offeredRides;
    private float rating;
    private Settings setting;
    private String username;


    public User(ArrayList<BookedRide> bookedRides, String email, String gender, String image, String lastname, String prename, String message, ArrayList<OfferedRide> offeredRides, float rating, Settings setting, String username) {
        this.bookedRides = bookedRides;
        this.email = email;
        this.gender = gender;
        this.image = image;
        this.lastname = lastname;
        this.prename = prename;
        this.message = message;
        this.offeredRides = offeredRides;
        this.rating = rating;
        this.setting = setting;
        this.username = username;
    }
}
