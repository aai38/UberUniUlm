package de.uni_ulm.uberuniulm.model.ride;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.model.Rating;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.notifications.NotificationsManager;
import de.uni_ulm.uberuniulm.ui.fragments.MainPageFragment;
import de.uni_ulm.uberuniulm.ui.fragments.MyBookedRidesFragment;
import de.uni_ulm.uberuniulm.ui.fragments.MyOffersFragment;
import de.uni_ulm.uberuniulm.ui.fragments.RatingsFragment;
import de.uni_ulm.uberuniulm.ui.fragments.WatchListFragment;
import kotlin.Triple;

public class RideLoader {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private DatabaseReference myRef;
    private ArrayList<OfferedRide> offers;
    private DataSnapshot dataSnapshot;
    private ParseType parseType;
    private ArrayList bookings = new ArrayList();
    private NotificationsManager notificationsManager;
    private ArrayList<Triple<ArrayList, OfferedRide, Float>> ridesParsed;


    private String userId;

    public RideLoader(Context context){
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref = new ObscuredSharedPreferences(
                context, context.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");
        myRef = database.getReference().child("Users");

        ridesParsed= new ArrayList<>();
    }

    public void getOfferedRides(MainPageFragment mainFrag){

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ridesParsed.clear();
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    float total = 0;
                    float rating = 0;
                    int number = 0;
                    DataSnapshot usersRating = user.child("Rating");
                    for (DataSnapshot ratings : usersRating.getChildren()) {
                        long ratingValue = (long)ratings.child("stars").getValue();
                            total += Float.valueOf(ratingValue);
                            number +=1;

                    }
                    rating = total/ number;

                    DataSnapshot usersOfferedRides = user.child("offeredRides");
                    ArrayList userData = new ArrayList();
                    userData.add(user.getKey());
                    userData.add(user.child("username").getValue());
                    userData.add(user.child("Rating").getValue());
                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
                        HashMap<String, Object> values = new HashMap<>();
                        values.put("snapKey", dataSnapshot.getKey());
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.put(rideValue.getKey(), rideValue.getValue());
                        }
                        parseData(userData, values, rating);
                    }
                }
                mainFrag.updateOffers(ridesParsed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getBookedRides(MyBookedRidesFragment bookFrag){
        bookings= new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> valuesBookings = new HashMap<>();
                ridesParsed.clear();

                float total = 0;
                float rating = 0;
                int number = 0;
                DataSnapshot usersRating = dataSnapshot.child(userId).child("Rating");
                for (DataSnapshot ratings : usersRating.getChildren()) {

                    long ratingValue = (long)ratings.child("stars").getValue();
                    total += Float.valueOf(ratingValue);
                    number +=1;

                }
                rating = total /number;
                for (DataSnapshot ride : dataSnapshot.child(userId).child("obookedRides").getChildren()) {
                    String userIdBooking = ride.child("userKey").getValue().toString();
                    long zIndexBooking= (long) ride.child("zIndex").getValue();
                    boolean rated = (boolean) ride.child("rated").getValue();


                    if (userIdBooking==null ) {
                        Log.e("onDataChange", "no booked rides found");
                    } else {
                        BookedRide bookedRide = new BookedRide(userIdBooking, (int) zIndexBooking);
                        bookedRide.setRated(rated);
                        valuesBookings.put(userIdBooking, (int) zIndexBooking);
                        bookings.add(bookedRide);
                    }
                }
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        DataSnapshot usersOfferedRides = user.child("offeredRides");
                        if (valuesBookings.containsKey(user.getKey())) {
                            ArrayList userData = new ArrayList();
                            userData.add(user.getKey());
                            userData.add(user.child("username").getValue());
                            for (DataSnapshot offeredride : usersOfferedRides.getChildren()) {
                                HashMap<String, Object> values = new HashMap<>();

                                long zINDEX=(long) offeredride.child("zIndex").getValue();
                                Boolean bool2=valuesBookings.containsValue((int)zINDEX);
                                if (bool2) {
                                    for (DataSnapshot rideValue : offeredride.getChildren()) {
                                        values.put(rideValue.getKey(), rideValue.getValue());
                                    }
                                    parseData(userData, values, rating);
                                }
                            }
                        }
                    }
                bookFrag.updateOffers(ridesParsed);
                bookFrag.lookForRating(ridesParsed, bookings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void getWatchedRides(WatchListFragment watchFrag){
        parseType= ParseType.WATCHING;

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ridesParsed.clear();
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    DataSnapshot usersOfferedRides= user.child("offeredRides");
                    ArrayList userData = new ArrayList();
                    userData.add(user.getKey());
                    userData.add(user.child("username").getValue());

                    float total = 0;
                    float rating = 0;
                    int number = 0;
                    DataSnapshot usersRating = user.child("Rating");
                    for (DataSnapshot ratings : usersRating.getChildren()) {

                        long ratingValue = (long)ratings.child("stars").getValue();
                        total += Float.valueOf(ratingValue);
                        number +=1;

                    }
                    rating = total/ number;

                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
                        for(DataSnapshot observer: ride.child("observers").getChildren()){
                            Log.d("WATCHING", observer.getValue().toString()+"  "+ userId);
                            if(observer.getValue().equals(userId)){
                                HashMap<String, Object> values = new HashMap<>();
                                for (DataSnapshot rideValue : ride.getChildren()) {
                                    values.put(rideValue.getKey(), rideValue.getValue());
                                }
                                parseData(userData, values, rating);
                            }
                        }

                    }
                }
                watchFrag.updateOffers(ridesParsed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getSpecificRide(String offererId, String zIndex, NotificationsManager notificationManager){
        parseType=ParseType.NOTIFICATION;
        notificationsManager=notificationManager;
        ridesParsed .clear();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    if (user.getKey().equals(offererId)) {
                        ArrayList userData = new ArrayList();
                        userData.add(user.getKey());
                        userData.add(user.child("username").getValue());

                        float total = 0;
                        float rating = 0;
                        int number = 0;
                        DataSnapshot usersRating = user.child("Rating");
                        if(usersRating.getChildrenCount()>0) {
                            for (DataSnapshot ratings : usersRating.getChildren()) {

                                long ratingValue = (long) ratings.child("stars").getValue();
                                total += Float.valueOf(ratingValue);
                                number += 1;

                            }
                            if(number!=0)
                            rating = total / number;
                        }
                        DataSnapshot ride = user.child("offeredRides").child(zIndex);
                        HashMap<String, Object> values = new HashMap<>();
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.put(rideValue.getKey(), rideValue.getValue());
                        }
                        parseData(userData, values, rating);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getUsersOfferedRides(MyOffersFragment myOffersFragment){
        parseType=ParseType.USERSOFFERS;

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ridesParsed.clear();
                ArrayList userData = new ArrayList();
                userData.add(dataSnapshot.child(userId).getKey());
                userData.add(dataSnapshot.child(userId).child("username").getValue());

                float total = 0;
                float rating = 0;
                int number = 0;
                DataSnapshot usersRating = dataSnapshot.child(userId).child("Rating");
                for (DataSnapshot ratings : usersRating.getChildren()) {

                    long ratingValue = (long)ratings.child("stars").getValue();
                    total += Float.valueOf(ratingValue);
                    number +=1;

                }

                rating = total/number;
                for (DataSnapshot ride : dataSnapshot.child(userId).child("offeredRides").getChildren()) {
                    HashMap<String, Object> values = new HashMap<>();
                    for (DataSnapshot rideValue : ride.getChildren()) {
                        values.put(rideValue.getKey(), rideValue.getValue());
                    }
                    parseData(userData, values, rating);
                }
                myOffersFragment.updateOffers(ridesParsed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void setRatedValue(boolean isRated, int position) {
        BookedRide ratedRide = (BookedRide) bookings.get(position);
        ratedRide.setRated(isRated);
        myRef.child(userId).child("obookedRides").child(String.valueOf(position)).setValue(ratedRide);
    }

    private void parseData(ArrayList userData, HashMap<String, Object> values, float rating){
        List<LatLng> coordinates = new ArrayList<>();
        try {
            List<HashMap> coordinatesHash = (List<HashMap>) values.get("route");
            if (coordinatesHash != null) {
                for (HashMap coordinate : coordinatesHash) {
                    LatLng coord = new LatLng((Double) coordinate.get("latitude"), (Double) coordinate.get("longitude"));
                    coordinates.add(coord);
                }
            }
        } catch (ClassCastException e) {
            coordinates = new ArrayList<>();
        }

        long price = (long) (values.get("price"));
        String date = values.get("date").toString();
        String time = values.get("time").toString();
        String userkey = values.get("userId").toString();
        long zIndex = (long) values.get("zIndex");
        long places = (long) values.get("places");
        long places_open = (long) values.get("places_open");
        String departure = values.get("departure").toString();
        String destination = values.get("destination").toString();
        List<LatLng> waypoints = new ArrayList<>();
        List<String> bookedUsers = new ArrayList<>();
        List<String> observers = new ArrayList<>();

        try {
            List<String> observersHash = (List<String>) values.get("observers");
            if (observersHash != null) {
                for (String observer : observersHash) {
                    observers.add(observer);
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            observers = new ArrayList<>();
        }

        try {
            List<HashMap> waypointsHash = (List<HashMap>) values.get("waypoints");
            if (waypointsHash != null) {
                for (HashMap waypoint : waypointsHash) {
                    LatLng coord = new LatLng((Double) waypoint.get("latitude"), (Double) waypoint.get("longitude"));
                    waypoints.add(coord);
                }
            }
        } catch (ClassCastException e) {
            waypoints = new ArrayList<>();
        }

        try {
            HashMap<String, String> bookedUsersHash = (HashMap<String, String>) values.get("bookedUsers");
            if (bookedUsersHash != null) {
                    bookedUsers= new ArrayList<String>(bookedUsersHash.keySet());
            }
        } catch (ClassCastException e) {
            bookedUsers = new ArrayList<>();
        }

        OfferedRide offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int) places, (int) places_open, departure, destination, userkey, (int) zIndex, waypoints, observers);
        offeredRide.setBookedUsers((ArrayList) bookedUsers);

        if(parseType==ParseType.NOTIFICATION){
            notificationsManager.setRideNotification(new Triple(userData, offeredRide, rating));
        }
        ridesParsed.add(new Triple(userData, offeredRide, rating));
    }

    public void getRatings (RatingsFragment fragment) {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Rating> ratings = new ArrayList();


                    int rating = 0;
                    DataSnapshot usersRating = dataSnapshot.child(userId).child("Rating");
                    for (DataSnapshot ratingsValue : usersRating.getChildren()) {
                        String comment = ratingsValue.child("comment").getValue().toString();
                        long ratingValue = (long) ratingsValue.child("stars").getValue();
                        Rating rate = new Rating((float)ratingValue, comment);
                        ratings.add(rate);
                    }

                fragment.updateRatings(ratings);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    enum ParseType {
        BOOKEDRIDES, USERSOFFERS, OFFERS, WATCHING, NOTIFICATION
    }
}
