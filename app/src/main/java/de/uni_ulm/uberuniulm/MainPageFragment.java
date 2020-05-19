package de.uni_ulm.uberuniulm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.map.Route;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.ParkingSpot;
import de.uni_ulm.uberuniulm.model.ParkingSpots;
import de.uni_ulm.uberuniulm.model.Settings;
import de.uni_ulm.uberuniulm.model.User;
import timber.log.Timber;


public class MainPageFragment extends Fragment {

    public View fragmentView;
    ArrayList<OfferedRide> offeredRides;
    ListView listView;
    private static CustomAdapter adapter;
    private DatabaseReference myRef;
    private OfferedRide offeredRide;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);




        LinearLayout mapFragment= fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);



        listView=(ListView)fragmentView.findViewById(R.id.list);
        offeredRides = new ArrayList<>();
        ParkingSpots parkingSpots = new ParkingSpots();
        //BookedRide exampleRide = new BookedRide("P21", 3, "Ehinger Tor", "2", parkingSpots.getSpotByName("P09"), 2, new Date(120,2,5),
              //  new Time(12, 00, 00), 5, new User(null, "blabla", "female", "bla.png", "Mueller", "Nina", null, null, 2, new Settings("de", "black"), "ninabina"));
        //offeredRides.add(new BookedRide("P21", 3, "Ehinger Tor", "2", parkingSpots.getSpotByName("P09"), 2, new Date(120,2,5),
             //   new Time(12, 00, 00), 5, new User(null, "blabla", "female", "bla.png", "Mueller", "Nina", null, null, 2, new Settings("de", "black"), "ninabina")));
       // offeredRides.add(new BookedRide("P25", 3, "Neu-Ulm ZUP", "2", parkingSpots.getSpotByName("P09"), 5, new Date(120,2,6),
        //        new Time(15, 00, 00), 3, new User(null, "blabla", "female", "bla.png", "Mueller", "Nina", null, null, 2, new Settings("de", "black"), "ninabina")));

        SharedPreferences pref = getContext().getSharedPreferences("UserKey", 0);
        String userId = pref.getString("UserKey", "");
        Log.i("userid", ""+userId);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        int ridesTotal = pref.getInt("RideId", 0);

        for (int i = 0; i<= ridesTotal; i++) {
            myRef = database.getReference().child(userId).child("offeredRides").child(""+i);


            ArrayList<Object> values = new ArrayList<>();

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        values.add(childSnapshot.getValue());
                    }
                    if(values.size()==0) {
                        Log.i("onDataChange", "no values");
                    } else {
                        //Route route = (Route) values.get(0);
                        Log.i("TAG", "values" + values.get(3).toString());
                        long price = (long) (values.get(5));
                        String date =  values.get(0).toString();
                        String time = values.get(6).toString();
                        long places = (long) values.get(3);
                        long places_open = (long) values.get(4);
                        String departure = values.get(1).toString();
                        String destination = values.get(2).toString();

                        offeredRide = new OfferedRide(null, (int) price, date, time, (int)places, (int)places_open, departure, destination);
                        offeredRides.add(offeredRide);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }




        adapter= new CustomAdapter(offeredRides,fragmentView.getContext());

        listView.setAdapter(adapter);

        return fragmentView;
    }
}
