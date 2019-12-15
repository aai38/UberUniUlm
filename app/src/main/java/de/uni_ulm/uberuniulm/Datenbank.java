package de.uni_ulm.uberuniulm;

/**
 * Created by karo on 12.12.19.
 */
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import de.uni_ulm.uberuniulm.model.Ride;

public class Datenbank {

    private Connection conn;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    static String template = "jdbc:mysql://localhost/%s?useEncoding=true&characterEncoding=UTF-8&user=%s&password=%s";

    public Datenbank(){

        try {
            conn = DriverManager.getConnection(String.format(template, "uberUniUlm", "uberuser", "uberuniulm"));
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            System.out.println("cant establish connection");
        }
    }

    //creating an user and returning the id
    public int createUser(String prename, String lastname, String username, String gender, String image, String email, String password, int rating) {
        try {
            preparedStatement = conn.prepareStatement("INSERT INTO user (prename, lastname, username, gender, image, email, password, rating) VALUES (?,?,?,?,?,?,?,?)");
            preparedStatement.setString(1, prename);
            preparedStatement.setString(2, lastname);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, gender);
            preparedStatement.setString(5, image);
            preparedStatement.setString(6, email);
            preparedStatement.setString(7, password);
            preparedStatement.setInt(8, rating);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getUserID(username);
    }

    //creating a ride and returning the id
    public int createRide(String departure, String destination, String route, String parkingspot, int price, Date date, Time time, int places) {
        try {
            preparedStatement = conn.prepareStatement("INSERT INTO rides (departure, destination, route, parkingspot, price, date, time, places) VALUES (?,?,?,?,?,?,?,?)");
            preparedStatement.setString(1, departure);
            preparedStatement.setString(2, destination);
            preparedStatement.setString(3, route);
            preparedStatement.setString(4, parkingspot);
            preparedStatement.setInt(5, price);
            preparedStatement.setDate(6, date);
            preparedStatement.setTime(7, time);
            preparedStatement.setInt(8, places);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getRideID(route);
    }

    //creating entry for booked ride

    //TODO: update places for the ride
    public void createBookedRide(int userId, int rideId) {
        try {
            preparedStatement = conn.prepareStatement("INSERT INTO bookedRides (userId, rideId) VALUES (?,?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //creating entry for offered ride

    public void createOfferedRide(int userId, int rideId) {
        try {
            preparedStatement = conn.prepareStatement("INSERT INTO offeredRides (userId, rideId) VALUES (?,?)");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO: lookup if you get a list or array when catching all
    //look up the booked rides for one user
    public List<Ride> getBookedRides(int userId) {
        List<Ride> bookedRides = new ArrayList<>();
        try {
            preparedStatement = conn.prepareStatement("SELECT *\n" +
                    "  FROM rides r\n" +
                    "  JOIN bookedRides b ON b.ridesId = r.id\n" +
                    "  WHERE userId =?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ride ride = new Ride(resultSet.getString("destination"), resultSet.getString("departure"),
                        resultSet.getString("route"), resultSet.getString("parkingspot"),
                        resultSet.getInt("price"), resultSet.getDate("date"),
                        resultSet.getTime("time"), resultSet.getInt("places"));
                bookedRides.add(ride);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedRides;
    }

    public int getUserID(String username) {
        int id = -1;
        try {
            preparedStatement = conn.prepareStatement("SELECT id FROM user WHERE username=?");
            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();
            id = resultSet.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    //look up the offered rides for one user
    public List<Ride> getOfferedRides(int userId) {
        List<Ride> bookedRides = new ArrayList<>();
        try {
            preparedStatement = conn.prepareStatement("SELECT *\n" +
                    "  FROM rides r\n" +
                    "  JOIN offeredRides o ON o.ridesId = r.id\n" +
                    "  WHERE userId =?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ride ride = new Ride(resultSet.getString("destination"), resultSet.getString("departure"),
                        resultSet.getString("route"), resultSet.getString("parkingspot"),
                        resultSet.getInt("price"), resultSet.getDate("date"),
                        resultSet.getTime("time"), resultSet.getInt("places"));
                bookedRides.add(ride);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedRides;
    }

    //setters and getters for users
    public String getUserPrename(int userId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT prename FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("prename");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getUserLastname(int userId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT lastname FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("lastname");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getUserUsername(int userId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT username FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("username");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getUserGender(int userId) {
        String gender = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT gender FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            gender = resultSet.getString("gender");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gender;
    }

    public String getUserImage(int userId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT image FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("image");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getUserEmail(int userId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT email FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("email");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public int getUserRating(int userId) {
        int rating = -1;
        try {
            preparedStatement = conn.prepareStatement("SELECT rating FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();
            rating = resultSet.getInt("rating");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rating;
    }

    public void setUserPrename(int userId, String prename) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET prename=? WHERE id=?");
            preparedStatement.setString(1, prename);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserLastname(int userId, String lastname) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET lastname=? WHERE id=?");
            preparedStatement.setString(1, lastname);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserUsername(int userId, String username) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET username=? WHERE id=?");
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserGender(int userId, String gender) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET gender=? WHERE id=?");
            preparedStatement.setString(1, gender);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserImage(int userId, String image) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET image=? WHERE id=?");
            preparedStatement.setString(1, image);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserEmail(int userId, String email) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET email=? WHERE id=?");
            preparedStatement.setString(1, email);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserRating(int userId, int rating) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET rating=? WHERE id=?");
            preparedStatement.setInt(1, rating);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUserPassword(int userId, String password) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE user SET password=? WHERE id=?");
            preparedStatement.setString(1, password);
            preparedStatement.setInt(2, userId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //setters and getters for rides
    public void setRideDeparture(int rideId, String departure) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET departure=? WHERE id=?");
            preparedStatement.setString(1, departure);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRideDestination(int rideId, String destination) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET destination=? WHERE id=?");
            preparedStatement.setString(1, destination);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRideRoute(int rideId, String route) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET route=? WHERE id=?");
            preparedStatement.setString(1, route);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRideParkingspot(int rideId, String parkingspot) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET parkingspot=? WHERE id=?");
            preparedStatement.setString(1, parkingspot);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRidePrice(int rideId, int price) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET price=? WHERE id=?");
            preparedStatement.setInt(1, price);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRideDate(int rideId, Date date) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET date=? WHERE id=?");
            preparedStatement.setDate(1, date);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRideTime(int rideId, Time time) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET time=? WHERE id=?");
            preparedStatement.setTime(1, time);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRidePlaces(int rideId, int places) {
        try {
            preparedStatement = conn.prepareStatement("UPDATE rides SET departure=? WHERE id=?");
            preparedStatement.setInt(1, places);
            preparedStatement.setInt(2, rideId);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getRideID(String route) {
        int id = -1;
        try {
            preparedStatement = conn.prepareStatement("SELECT id FROM rides WHERE route=?");
            preparedStatement.setString(1, route);

            resultSet = preparedStatement.executeQuery();
            id = resultSet.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }
    public String getRideDeparture(int rideId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT departure FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("departure");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getRideDestination(int rideId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT destination FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("destination");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getRideRoute(int rideId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT route FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("route");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public String getRideParkingspot(int rideId) {
        String name = "";
        try {
            preparedStatement = conn.prepareStatement("SELECT parkingspot FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            name = resultSet.getString("parkingspot");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    public int getRidePrice(int rideId) {
        int price = -1;
        try {
            preparedStatement = conn.prepareStatement("SELECT price FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            price = resultSet.getInt("price");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return price;
    }

    public Date getRideDate(int rideId) {
        Date date = null;
        try {
            preparedStatement = conn.prepareStatement("SELECT date FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            date = resultSet.getDate("date");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return date;
    }

    public Time getRideTime(int rideId) {
        Time time = null;
        try {
            preparedStatement = conn.prepareStatement("SELECT time FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            time = resultSet.getTime("time");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return time;
    }

    public int getRidePlaces(int rideId) {
        int places = -1;
        try {
            preparedStatement = conn.prepareStatement("SELECT places FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            resultSet = preparedStatement.executeQuery();
            places = resultSet.getInt("places");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return places;
    }

    //delete ride
    public void deleteRide(int rideId) {
        try {
            preparedStatement = conn.prepareStatement("DELETE FROM rides WHERE rideId=?");
            preparedStatement.setInt(1, rideId);

            preparedStatement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //delete user
    public void deleteUser(int userId) {
        try {
            preparedStatement = conn.prepareStatement("DELETE FROM user WHERE userId=?");
            preparedStatement.setInt(1, userId);

            preparedStatement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //testing the database
    public static void main (String args[]){
        Datenbank database = new Datenbank();
        database.createBookedRide(1,2);
    }
}
