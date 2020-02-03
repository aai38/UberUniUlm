package de.uni_ulm.uberuniulm;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainPageFragment extends Fragment {

    public View fragmentView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);


        LinearLayout mapFragment= fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);

        return fragmentView;
    }
}
