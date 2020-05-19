package de.uni_ulm.uberuniulm.model;

import java.util.ArrayList;

public class User {
    public ArrayList<BookedRide> getBookedRides() {
        return bookedRides;
    }

    public void setBookedRides(ArrayList<BookedRide> bookedRides) {
        this.bookedRides = bookedRides;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }



    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPrename() {
        return prename;
    }

    public void setPrename(String prename) {
        this.prename = prename;
    }



    public ArrayList<OfferedRide> getOfferedRides() {
        return offeredRides;
    }

    public void setOfferedRides(ArrayList<OfferedRide> offeredRides) {
        this.offeredRides = offeredRides;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    /*public Settings getSetting() {
        return setting;
    }

    public void setSetting(Settings setting) {
        this.setting = setting;
    }*/

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    private ArrayList<BookedRide> bookedRides;
    private String email;
    private String gender;
    private String image;
    private String lastname;
    private String prename;
    private ArrayList<OfferedRide> offeredRides;



    private ArrayList<Message> messages;
    private float rating;
    //private Settings setting;
    private String username;


    public User(ArrayList<BookedRide> bookedRides, String email, String gender,  ArrayList<OfferedRide> offeredRides, ArrayList<Message> messages, float rating, String username) {
        this.bookedRides = bookedRides;
        this.email = email;
        this.gender = gender;
        this.image = image;
        this.offeredRides = offeredRides;
        this.rating = rating;
        //this.setting = setting;
        this.username = username;
        this.messages = messages;
    }
}
