package de.uni_ulm.uberuniulm.model.filter;

import android.util.Pair;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.ride.OfferedRide;

public class FilterOfferer {
    public FilterOfferer(){

    }

    public Boolean filterByOffererName(String offerer, String nameSearch){
        if(offerer.contains(nameSearch)){
            return true;
        }else if(nameSearch.length()>5 && offerer.length()>=5){
            for(int i=0; i<nameSearch.length()-4;i++){
                if(offerer.contains(nameSearch.substring(i, i+4))){
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList filterOffersByOfferer(ArrayList<Pair<ArrayList, OfferedRide>> offers, String searchedName){
        ArrayList offersFiltered= new ArrayList();
        for(int i=0; i<offers.size(); i++){
            if(filterByOffererName((String) offers.get(i).first.get(1),searchedName)){
                offersFiltered.add(offers.get(i));
            }
        }
        return offersFiltered;
    }
}
