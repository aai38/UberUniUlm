package de.uni_ulm.uberuniulm;

import android.app.AlertDialog;
import android.content.Context;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.RouteStyle;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.ParkingSpots;
import de.uni_ulm.uberuniulm.ui.ClickListener;


public class MainPageFragment extends Fragment{

    public View fragmentView;
    ArrayList<Pair<ArrayList,OfferedRide>> offeredRides;
    RecyclerView offerRecyclerView;
    private OfferListAdapter adapter;
    private DatabaseReference myRef;
    private OfferedRide offeredRide;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);
        LinearLayout mapFragment = fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);
        SearchView departure = fragmentView.findViewById(R.id.searchViewDeparture);
        SearchView destination = fragmentView.findViewById(R.id.searchViewDestination);

        departure.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.filterDeparture(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filterDeparture(s);
                return true;
            }
        });

        destination.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.filterDestination(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filterDestination(s);
                return true;
            }
        });




        offerRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.mainPageOfferRecyclerView);
        offerRecyclerView.setHasFixedSize(true);
        offerRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        offeredRides = new ArrayList();

        ParkingSpots parkingSpots = new ParkingSpots();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int ridesTotal = pref.getInt("RideId", 0);

        myRef = database.getReference();


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    DataSnapshot usersOfferedRides= user.child("offeredRides");
                    Long rating= (Long) user.child("rating").getValue();
                    ArrayList userData = new ArrayList();
                    userData.add(user.getKey());
                    userData.add(user.child("username").getValue());
                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
                        ArrayList<Object> values = new ArrayList();
                        values.add(dataSnapshot.getKey());
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.add(rideValue.getValue());
                        }

                        if(values.size()==0) {
                            Log.i("onDataChange", "no values");
                        } else {
                            List<LatLng> coordinates=new ArrayList<>();
                            try {
                                List<HashMap> coordinatesHash = (List<HashMap>) values.get(7);
                                for(HashMap coordinate : coordinatesHash){
                                    LatLng coord=new LatLng((Double)coordinate.get("latitude"), (Double) coordinate.get("longitude"));
                                    coordinates.add(coord);
                                }
                            }catch(ClassCastException e){
                                coordinates= new ArrayList<>();
                            }

                            long price = (long) (values.get(6));
                            String date =  values.get(1).toString();
                            String time;
                            String userkey;
                            long zIndex;
                            if(values.size()==11){
                                 time= values.get(8).toString();
                                 userkey = values.get(9).toString();
                                 zIndex = (long) values.get(10);
                            }else{
                                time= values.get(8).toString();
                                userkey = values.get(9).toString();
                                zIndex = (long) values.get(10);
                            }
                            long places = (long) values.get(4);
                            long places_open = (long) values.get(5);
                            String departure = values.get(2).toString();
                            String destination = values.get(3).toString();


                            offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int)places, (int)places_open, departure, destination, userkey, (int) zIndex);
                            userData.add((float) rating);
                            offeredRides.add(new Pair(userData, offeredRide));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        adapter = new OfferListAdapter(fragmentView.getContext(), offeredRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                alert.setMessage("You want to book this ride?");
                alert.setTitle("Book Ride");


                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Pair clickedRidePair = offeredRides.get(position);
                        OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        BookedRide bookedRide = new BookedRide(clickedRide.getUserId(), clickedRide.getzIndex());
                        final SharedPreferences pref = new ObscuredSharedPreferences(
                                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                        int zIndex = pref.getInt("BookedRideId", 0);

                        //hier eigentlich Benachrichtigung an Fahrer
                        myRef.child(userId).child("obookedRides").child(String.valueOf(zIndex)).setValue(bookedRide);

                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("BookedRideId", zIndex +1);
                        editor.apply();

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }

            @Override
            public void onOfferClicked(int position) {
                Intent intent = new Intent(fragmentView.getContext(), MapActivity.class);
                Pair clickedRidePair = offeredRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }
        });


        Log.d("OFFEREDRIDES", String.valueOf(offeredRides.size()));
        offerRecyclerView.setAdapter(adapter);

        return fragmentView;
    }
}
