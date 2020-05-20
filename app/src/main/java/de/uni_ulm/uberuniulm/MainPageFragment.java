package de.uni_ulm.uberuniulm;

import android.content.Context;
import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.map.Route;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.ParkingSpots;


public class MainPageFragment extends Fragment {

    public View fragmentView;
    ArrayList<Pair<String,OfferedRide>> offeredRides;
    ArrayList<Pair<String, BookedRide>> availableRides;
    RecyclerView offerRecyclerView;
    private static OfferListAdapter adapter;
    private DatabaseReference myRef;
    private OfferedRide offeredRide;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);
        LinearLayout mapFragment = fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);


        offerRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.mainPageOfferRecyclerView);
        offerRecyclerView.setHasFixedSize(true);
        offerRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        offeredRides = new ArrayList();

        ParkingSpots parkingSpots = new ParkingSpots();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        String userId = pref.getString("UserKey", "");
        Log.i("userid", "" + userId);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int ridesTotal = pref.getInt("RideId", 0);

        myRef = database.getReference();


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    DataSnapshot usersOfferedRides= user.child("offeredRides");
                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
                        ArrayList<Object> values = new ArrayList();
                        values.add(dataSnapshot.getKey());
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.add(rideValue.getValue());
                        }

                        if(values.size()==0) {
                            Log.i("onDataChange", "no values");
                        } else {
                            //Route route = (Route) values.get(0);
                            Route route =null;
                            Log.i("TAG", "values" + values.get(2).toString());
                            long price = (long) (values.get(6));
                            String date =  values.get(1).toString();
                            String time = values.get(7).toString();
                            long places = (long) values.get(4);
                            long places_open = (long) values.get(5);
                            String departure = values.get(2).toString();
                            String destination = values.get(3).toString();

                            offeredRide = new OfferedRide(route, (int) price, date, time, (int)places, (int)places_open, departure, destination);

                            offeredRides.add(new Pair<>(user.,offeredRide));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        adapter = new OfferListAdapter(fragmentView.getContext(), offeredRides);
        Log.d("OFFEREDRIDES", String.valueOf(offeredRides.size()));
        offerRecyclerView.setAdapter(adapter);

        return fragmentView;
    }
}
