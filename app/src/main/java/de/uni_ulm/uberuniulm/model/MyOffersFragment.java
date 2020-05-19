package de.uni_ulm.uberuniulm.model;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.uni_ulm.uberuniulm.R;

public class MyOffersFragment extends Fragment {
    public View fragmentView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_offers, container, false);
        fragmentView.findViewById(R.id.startActivityRegisterProfileImage);
        return fragmentView;
    }
}
