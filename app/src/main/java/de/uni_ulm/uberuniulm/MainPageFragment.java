package de.uni_ulm.uberuniulm;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.ParkingSpot;
import de.uni_ulm.uberuniulm.model.ParkingSpots;
import de.uni_ulm.uberuniulm.model.Settings;
import de.uni_ulm.uberuniulm.model.User;


public class MainPageFragment extends Fragment {

    public View fragmentView;
    ArrayList<BookedRide> bookedRides;
    ListView listView;
    private static CustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);


        LinearLayout mapFragment= fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);

        listView=(ListView)fragmentView.findViewById(R.id.list);
        bookedRides = new ArrayList<>();
        ParkingSpots parkingSpots = new ParkingSpots();
        bookedRides.add(new BookedRide("P21", 3, "Ehinger Tor", "2", parkingSpots.getSpotByName("P09"), 2, new Date(120,2,5),
                new Time(12, 00, 00), 5, new User(null, "blabla", "female", "bla.png", "Mueller", "Nina", "", null, 2, new Settings("de", "black"), "ninabina")));
        bookedRides.add(new BookedRide("P25", 3, "Neu-Ulm ZUP", "2", parkingSpots.getSpotByName("P09"), 5, new Date(120,2,6),
                new Time(15, 00, 00), 3, new User(null, "blabla", "female", "bla.png", "Mueller", "Nina", "", null, 2, new Settings("de", "black"), "ninabina")));


        adapter= new CustomAdapter(bookedRides,fragmentView.getContext());

        listView.setAdapter(adapter);

        return fragmentView;
    }
}
