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
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.uni_ulm.uberuniulm.ChatPage;
import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.notifications.NotificationsManager;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.ClickListener;
import de.uni_ulm.uberuniulm.ui.main.OfferListAdapter;
import kotlin.Triple;

public class WatchListFragment extends Fragment {
    public View fragmentView;
    private RecyclerView watchListRecyclerView;
    private static OfferListAdapter adapter;
    private ArrayList<Triple<ArrayList, OfferedRide, Float>> observedRides;
    private String userId;
    private ConstraintLayout noEntrysLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_bookings, container, false);

        watchListRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewMyBookings);
        watchListRecyclerView.setHasFixedSize(true);
        watchListRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        observedRides = new ArrayList();

        noEntrysLayout = (ConstraintLayout) fragmentView.findViewById(R.id.noEntryContainer);
        if (observedRides.isEmpty()) {
            watchListRecyclerView.setVisibility(View.GONE);
            noEntrysLayout.setVisibility(View.VISIBLE);
        }
        else {
            watchListRecyclerView.setVisibility(View.VISIBLE);
            noEntrysLayout.setVisibility(View.GONE);
        }

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        RideLoader rideLoader= new RideLoader(getContext());
        rideLoader.getWatchedRides(this);
        return fragmentView;
    }

    public void updateOffers(ArrayList<Triple<ArrayList, OfferedRide, Float>> rides){
        observedRides = rides;
        if(adapter!=null){
            adapter.notifyDataSetChanged();
            watchListRecyclerView.setAdapter(adapter);
        }else{
            setOfferAdapter();
        }

        if (observedRides.isEmpty()) {
            watchListRecyclerView.setVisibility(View.GONE);
            noEntrysLayout.setVisibility(View.VISIBLE);
        }
        else {
            watchListRecyclerView.setVisibility(View.VISIBLE);
            noEntrysLayout.setVisibility(View.GONE);
        }
    }

    private void setOfferAdapter(){
        adapter = new OfferListAdapter(fragmentView.getContext(), observedRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

                Triple<ArrayList,OfferedRide, Float> clickedRideTriple = observedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRideTriple.getSecond();
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

                DatabaseReference myRef= FirebaseDatabase.getInstance().getReference().child("Users");


                if (!clickedRide.getBookedUsers().contains(userId)) {
                    if(clickedRide.getPlaces_open()>0) {
                        dialog.setItems(getResources().getStringArray(R.array.bookoptions), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if (pos == 0) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                                    alert.setMessage("You want to book this ride?");
                                    alert.setTitle("Book Ride");


                                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            if (clickedRide.getPlaces_open() > 0) {
                                                Date date = Calendar.getInstance().getTime();
                                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                                                myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("bookedUsers").child(userId).setValue(formatter.format(date));
                                                int places = clickedRide.getPlaces_open() - 1;
                                                myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("places_open").setValue(places);

                                                BookedRide bookedRide = new BookedRide(clickedRide.getUserId(), clickedRide.getzIndex());
                                                final SharedPreferences pref = new ObscuredSharedPreferences(
                                                        fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                                                int obookedRideId = pref.getInt("obookedRideId", 0);

                                                myRef.child(userId).child("obookedRides").child(String.valueOf(obookedRideId)).setValue(bookedRide);
                                                SharedPreferences.Editor editor = pref.edit();
                                                editor.putInt("BookedRideId", obookedRideId + 1);
                                                editor.apply();
                                                NotificationsManager notificationManager = new NotificationsManager();
                                                notificationManager.setUp(getContext());
                                                notificationManager.subscribeToTopic(clickedRide.getUserId() + "_" + clickedRide.getzIndex());
                                            } else {
                                                Toast.makeText(fragmentView.getContext(), "Oops, looks like somebody was a little faster than you.", Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        }
                                    });

                                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    });

                                    alert.show();
                                } else {
                                    Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                    intent.putExtra("SENDERID", userId);
                                    intent.putExtra("RECEIVERNAME", clickedRideTriple.getFirst().get(1).toString());
                                    intent.putExtra("RECEIVERID", clickedRideTriple.getFirst().get(0).toString());
                                    startActivity(intent);

                                }
                            }
                        });

                        dialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = dialog.create();
                        alert.show();
                    }else{
                        AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                        alert.setMessage("Ride already booked up. Do you want to send the rider a message?");
                        alert.setTitle("Booked up!");


                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                intent.putExtra("SENDERID", userId);
                                intent.putExtra("RECEIVERNAME", clickedRideTriple.getFirst().get(1).toString());
                                intent.putExtra("RECEIVERID", clickedRideTriple.getFirst().get(0).toString());
                                startActivity(intent);
                            }

                        });

                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //TODO what ever you want to do with No option.
                            }
                        });

                        alert.show();

                    }

                } else {
                    dialog.setItems(getResources().getStringArray(R.array.cancelbookingoptions), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            if (pos == 0) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                                alert.setMessage("Do you really want to cancel your booking?");
                                alert.setTitle("Book Ride");


                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference().child("Users");
                                        final SharedPreferences pref = new ObscuredSharedPreferences(
                                                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                                        int obookedRidesId = pref.getInt("oBookedRidesId", 0);

                                        //TODO hier eigentlich Benachrichtigung an Fahrer
                                        myRef.child(userId).child("obookedRides").child(String.valueOf(obookedRidesId)).removeValue();
                                        myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("bookedUsers").child(userId).removeValue();
                                        int places = clickedRide.getPlaces_open() + 1;
                                        myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("places_open").setValue(places);
                                        clickedRide.getBookedUsers().remove(userId);

                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putInt("BookedRideId", obookedRidesId - 1);
                                        editor.apply();
                                        NotificationsManager notificationManager=new NotificationsManager();
                                        notificationManager.setUp(getContext());
                                        notificationManager.unsubscribeTopic(clickedRide.getUserId()+"_"+clickedRide.getzIndex());
                                    }
                                });

                                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //TODO what ever you want to do with No option.
                                    }
                                });

                                alert.show();
                            } else {
                                Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                intent.putExtra("SENDERID", userId);
                                intent.putExtra("RECEIVERNAME", clickedRideTriple.getFirst().get(1).toString());
                                intent.putExtra("RECEIVERID", clickedRideTriple.getFirst().get(0).toString());
                                startActivity(intent);
                            }
                        }
                    });

                    dialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = dialog.create();
                    alert.show();

                }
            }

            @Override
            public void onOfferClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                Triple clickedRidePair = observedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                intent.putExtra("USER", (ArrayList) clickedRidePair.getFirst());
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("RATING", (float)clickedRidePair.getThird());
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }

            @Override
            public void onEditClicked(int position) {

            }

            @Override
            public void onMarkClicked(View view, int position){
                OfferedRide ride= observedRides.get(position).getSecond();
                Boolean notMarkedYet= !ride.getObservers().contains(userId);
                if (!userId.equals(ride.getUserId())) {
                    TextView markBttn = (TextView) view;
                    if (notMarkedYet) {
                        ride.markRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_marked);
                    } else {
                        ride.unmarkRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_unmarked);
                    }
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child("Users");
                    myRef.child(userId).child("offeredRides").child(String.valueOf(observedRides.get(position).getSecond().getzIndex())).removeValue();
                    myRef.child(observedRides.get(position).getFirst().get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).child("observers").setValue(ride.getObservers());
                    adapter.notifyDataSetChanged();
                }
            }

        });
        watchListRecyclerView.setAdapter(adapter);
    }
}
