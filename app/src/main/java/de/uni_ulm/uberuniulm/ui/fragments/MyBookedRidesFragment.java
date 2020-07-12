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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.model.Rating;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.OfferListAdapter;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.ui.main.ClickListener;
import kotlin.Triple;

public class MyBookedRidesFragment extends Fragment {
    public View fragmentView;
    private RecyclerView mybookingsRecyclerView;
    private static OfferListAdapter adapter;
    private DatabaseReference myRef;
    ArrayList<Triple<ArrayList, OfferedRide, Float>> bookedRides;
    private FragmentTransaction fragmentTransaction;
    ArrayList<BookedRide> bookedRidesData = new ArrayList<>();
    private FirebaseAuth mAuth;
    private long zIndexBooking;
    private String userIdBooking;
    private RideLoader rideLoader;
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


        rideLoader= new RideLoader(getContext());
        rideLoader.getBookedRides(this);


        return fragmentView;
    }

    public void updateOffers(ArrayList<Triple<ArrayList, OfferedRide, Float>> rides){
        bookedRides= rides;
        if(adapter!=null){
            adapter.notifyDataSetChanged();
            mybookingsRecyclerView.setAdapter(adapter);
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
                Triple clickedRidePair = bookedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                intent.putExtra("RATING", (float) clickedRidePair.getThird());
                intent.putExtra("USER", (ArrayList) clickedRidePair.getFirst());
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }

            @Override
            public void onMarkClicked(View view, int position){
                Boolean notMarkedYet=bookedRides.get(position).getSecond().getObservers().contains(userId);
                OfferedRide ride=bookedRides.get(position).getSecond();
                if (!userId.equals(ride.getUserId())) {
                    TextView markBttn = (TextView) view;
                    if (!notMarkedYet) {
                        ride.markRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer);
                    } else {
                        ride.unmarkRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer_deselected);
                    }

                    myRef.child(bookedRides.get(position).getFirst().get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(ride);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onEditClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                Triple clickedRidePair = bookedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                intent.putExtra("RATING", (float) clickedRidePair.getThird());
                intent.putExtra("USER", (ArrayList) clickedRidePair.getFirst());
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "EDITOFFER");
                startActivity(intent);
            }
        });
        mybookingsRecyclerView.setAdapter(adapter);
    }
    public void lookForRating(ArrayList<Triple<ArrayList, OfferedRide, Float>> bookedRides, ArrayList<BookedRide> bookedRide) {

        for(int i = 0; i<bookedRides.size(); i++) {
            final int hold = i;
            Date date1= null;
            try {
                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(bookedRides.get(i).getSecond().getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(date1.compareTo(Calendar.getInstance().getTime()) < 0 && !bookedRide.get(i).isRated()) {
                Log.e("time is less", "");

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Rating");
                builder.setMessage("Please rate this ride");
                // set the custom layout
                final View customLayout = getLayoutInflater().inflate(R.layout.rating_dialogue, null);
                builder.setView(customLayout);
                // add a button
                builder.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // send data from the AlertDialog to the Activity
                        EditText editText = customLayout.findViewById(R.id.editTextRating);
                        RatingBar ratingBar = customLayout.findViewById(R.id.ratingBar);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference().child("Users");
                        rideLoader.setRatedValue(true, hold);
                        Rating rating = new Rating ((int)ratingBar.getRating(), editText.getText().toString());

                        DatabaseReference ref = myRef.child(bookedRide.get(hold).getUserKey()).child("Rating").push();
                        String key = ref.getKey();
                        ref.setValue(rating);

                    }
                });

                builder.setNeutralButton("Rate later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rideLoader.setRatedValue(false, hold);
                    }
                });
                // create and show the alert dialog
                builder.setNegativeButton("Never Rate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                        rideLoader.setRatedValue(true, hold);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }
    }


}
