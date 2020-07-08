package de.uni_ulm.uberuniulm.model.ride;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.ui.fragments.MainPageFragment;
import de.uni_ulm.uberuniulm.ui.fragments.MyBookedRidesFragment;
import de.uni_ulm.uberuniulm.ui.fragments.MyOffersFragment;
import de.uni_ulm.uberuniulm.ui.fragments.WatchListFragment;

public class RideLoader {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private DatabaseReference myRef;
    private ArrayList<Pair<ArrayList, OfferedRide>> ridesParsed;
    private ArrayList<OfferedRide> offers;
    private DataSnapshot dataSnapshot;
    private ParseType parseType;
    private ArrayList bookings = new ArrayList();

    private String userId;

    public RideLoader(Context context){
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();


        SharedPreferences pref = new ObscuredSharedPreferences(
                context, context.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");
        myRef = database.getReference();

        ridesParsed= new ArrayList<>();
    }

    public void getOfferedRides(MainPageFragment mainFrag){
        parseType= ParseType.OFFERS;

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren()) {

                    DataSnapshot usersOfferedRides = user.child("offeredRides");
                    ArrayList userData = new ArrayList();
                    userData.add(user.getKey());
                    userData.add(user.child("username").getValue());
                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
                        HashMap<String, Object> values = new HashMap<>();
                        values.put("snapKey", dataSnapshot.getKey());
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.put(rideValue.getKey(), rideValue.getValue());
                        }
                        parseData(userData, values);
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
        parseType=ParseType.BOOKEDRIDES;

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> valuesBookings = new HashMap<>();
                for (DataSnapshot ride : dataSnapshot.child(userId).child("obookedRides").getChildren()) {
                    String userIdBooking = ride.child("userKey").getValue().toString();
                    long zIndexBooking= (long) ride.child("zIndex").getValue();

                    if (userIdBooking==null ) {
                        Log.e("onDataChange", "no booked rides found");
                    } else {
                        BookedRide bookedRide = new BookedRide(userIdBooking, (int) zIndexBooking);
                        valuesBookings.put(userIdBooking, (int) zIndexBooking);
                        bookings.add(bookedRide);
                        Log.i("bookedRidesData", bookings.toString());
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
                                    parseData(userData, values);
                                }
                            }
                        }
                    }

                bookFrag.updateOffers(ridesParsed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public ArrayList<Pair<ArrayList, OfferedRide>> getWatchedRides(WatchListFragment watchFrag){
        parseType= ParseType.WATCHING;
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    DataSnapshot usersOfferedRides= user.child("offeredRides");
                    ArrayList userData = new ArrayList();
                    userData.add(dataSnapshot.child(userId).getKey());
                    userData.add(dataSnapshot.child(userId).child("username").getValue());
                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
                        HashMap<String, Object> values = new HashMap<>();
                        for(DataSnapshot observer: ride.child("observers").getChildren()){
                            if(observer.getValue().equals(userId)){
                                for (DataSnapshot rideValue : ride.getChildren()) {
                                    values.put(rideValue.getKey(), rideValue.getValue());
                                    Log.d("WATCHED", rideValue.getKey().toString()+" "+ rideValue.getValue());
                                }
                                parseData(userData, values);
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

        return ridesParsed;
    }

    public ArrayList<Pair<ArrayList, OfferedRide>> getUsersOfferedRides(MyOffersFragment myOffersFragment){
        parseType=ParseType.USERSOFFERS;

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList userData = new ArrayList();
                userData.add(dataSnapshot.child(userId).getKey());
                userData.add(dataSnapshot.child(userId).child("username").getValue());
                for (DataSnapshot ride : dataSnapshot.child(userId).child("offeredRides").getChildren()) {
                    HashMap<String, Object> values = new HashMap<>();
                    for (DataSnapshot rideValue : ride.getChildren()) {
                        values.put(rideValue.getKey(), rideValue.getValue());
                    }
                    parseData(userData, values);
                }
                myOffersFragment.updateOffers(ridesParsed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return ridesParsed;
    }

    public void setRatedValue(boolean isRated, int position) {
        BookedRide ratedRide = (BookedRide) bookings.get(position);
        ratedRide.setRated(isRated);
        myRef.child(userId).child("obookedRides").child(String.valueOf(position)).setValue(ratedRide);
    }

    private void parseData(ArrayList userData, HashMap<String, Object> values){
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

        OfferedRide offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int) places, (int) places_open, departure, destination, userkey, (int) zIndex, waypoints, observers);
        Float rating = -2.0f;
        userData.add(rating);

        ridesParsed.add(new Pair(userData, offeredRide));
    }

    enum ParseType {
        BOOKEDRIDES, USERSOFFERS, OFFERS, WATCHING
    }
}
