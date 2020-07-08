package de.uni_ulm.uberuniulm.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.OfferListAdapter;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.ui.main.ClickListener;

public class MyBookedRidesFragment extends Fragment {
    public View fragmentView;
    private RecyclerView mybookingsRecyclerView;
    private static OfferListAdapter adapter;
    private DatabaseReference myRef;
    ArrayList<Pair<ArrayList, OfferedRide>> bookedRides;
    ArrayList<BookedRide> bookedRidesData = new ArrayList<>();
    private FirebaseAuth mAuth;
    private long zIndexBooking;
    private String userIdBooking;
    private RatingBar ratingBar;
    FirebaseDatabase database;
    SharedPreferences pref;
    String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_bookings, container, false);

        mybookingsRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewMyBookings);
        mybookingsRecyclerView.setHasFixedSize(true);
        mybookingsRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));




        database = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();


        pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");


        bookedRides = new ArrayList();


        RideLoader rideLoader= new RideLoader(getContext());
        rideLoader.getBookedRides(this);

        for(int i = 0; i<bookedRides.size(); i++) {
            final int hold = i;
            Date date1= null;
            try {
                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(bookedRides.get(i).second.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date1.compareTo(Calendar.getInstance().getTime()) > 0) {
                final RatingBar ratingBar = new RatingBar(getContext());
                final EditText editText = new EditText(getContext());
                editText.setHint("Write your comment here");

                AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                alert.setMessage("You want to rate this ride?");
                alert.setTitle("Rating");
                alert.setView(ratingBar);
                alert.setView(editText);



                alert.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        rideLoader.setRatedValue(true, hold);
                        //myRef.child(userId).child("obookedRides").child(String.valueOf(zIndex)).setValue(bookedRide);


                    }
                });
                alert.setNeutralButton("Rate later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rideLoader.setRatedValue(false, hold);
                    }
                });

                alert.setNegativeButton("Never Rate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                        rideLoader.setRatedValue(true, hold);
                    }
                });

                alert.show();
            }
        }

        return fragmentView;
    }

    public void updateOffers(ArrayList<Pair<ArrayList, OfferedRide>> rides){
        bookedRides= rides;
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }else{
            setOfferAdapter();
        }
    }

    private void setOfferAdapter(){
        adapter = new OfferListAdapter(fragmentView.getContext(), bookedRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onOfferClicked(int position) {
                Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                Pair clickedRidePair = bookedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }

            @Override
            public void onMarkClicked(View view, int position){
                Boolean notMarkedYet=bookedRides.get(position).second.getObservers().contains(userId);
                OfferedRide ride=bookedRides.get(position).second;
                if (!userId.equals(ride.getUserId())) {
                    TextView markBttn = (TextView) view;
                    if (!notMarkedYet) {
                        ride.markRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer);
                    } else {
                        ride.unmarkRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer_deselected);
                    }

                    myRef.child(bookedRides.get(position).first.get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(ride);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onEditClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                Pair clickedRidePair = bookedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "EDITOFFER");
                startActivity(intent);
            }
        });
        mybookingsRecyclerView.setAdapter(adapter);
    }
}
