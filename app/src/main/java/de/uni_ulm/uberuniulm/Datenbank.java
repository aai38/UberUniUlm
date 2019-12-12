package de.uni_ulm.uberuniulm;

/**
 * Created by karo on 12.12.19.
 */
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Timer;

public class Datenbank {

    Connection conn;

    public Datenbank(){

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //TODO: find out password and user and right url
            conn = DriverManager.getConnection("jdbc:mysql://localhost:8080/uberUniUlm" +
                            "user=minty&password=greatsqldb");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println("cant establish connection");
        }
    }

    //creating an user and returning the id
    public int createUser(String prename, String lastname, String username, String gender, String image, String email, String password, int rating) {
        return 0;
    }

    //creating a ride and returning the id
    public int createRide(String departue, String destination, String route, String parkingspot, int price, Date date, Time time, int places) {
        return 0;
    }

    //creating entry for booked ride

    //TODO: update places for the ride
    public void createBookedRide(int userID, int rideID) {

    }

    //creating entry for offered ride

    public void createOfferedRide(int userID, int rideID) {

    }

    //TODO: lookup if you get a list or array when catching all
    //look up the booked rides for one user
    public int[] getBookedRides(int userID) {
        return null;
    }

    //look up the offered rides for one user
    public int[] getOfferedRides(int userID) {
        return null;
    }

    //setters and getters for users
    public String getUserPrename(int userID) {
        return "";
    }

    public String getUserLastname(int userID) {
        return "";
    }

    public String getUserUsername(int userID) {
        return "";
    }

    public String getUserGender(int userID) {
        return "";
    }

    public String getUserImage(int userID) {
        return "";
    }

    public String getUserEmail(int userID) {
        return "";
    }

    public int getUserRating(int userID) {
        return 0;
    }

    public void setUserPrename(int userID) {

    }

    public void setUserLastname(int userID) {

    }

    public void setUserUsername(int userID) {
    }

    public void setUserGender(int userID) {

    }

    public void setUserImage(int userID) {

    }

    public void setUserEmail(int userID) {

    }

    public void setUserRating(int userID) {
    }

    public void setUserPassword(int userID) {

    }

    //setters and getters for rides
    public void setRideDeparture(int rideID) {

    }

    public void setRideDestination(int rideID) {

    }

    public void setRideRoute(int rideID) {

    }

    public void setRideParkingspot(int rideID) {

    }

    public void setRidePrice(int RideID) {

    }

    public void setRideDate(int rideID) {

    }

    public void setRideTime(int rideID) {

    }

    public void setRidePlaces(int rideID) {

    }

    public String getRideDeparture(int rideID) {
        return "";
    }

    public String getRideDestination(int rideID) {
        return "";
    }

    public String getRideRoute(int rideID) {
        return "";
    }

    public String getRideParkingspot(int rideID) {
        return "";
    }

    public int getRidePrice(int RideID) {
        return 0;
    }

    public Date getRideDate(int rideID) {
        return null;
    }

    public Time getRideTime(int rideID) {
        return null;
    }

    public int getRidePlaces(int rideID) {
        return 0;
    }

    //delete ride
    public void deleteRide(int rideID) {

    }

    //delete user
    public void deleteUser(int userID) {

    }

    //testing the database
    public static void main (String args[]){
        Datenbank database = new Datenbank();
    }
}
