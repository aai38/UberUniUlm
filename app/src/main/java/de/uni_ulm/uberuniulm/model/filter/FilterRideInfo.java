package de.uni_ulm.uberuniulm.model.filter;

import android.util.Log;
import android.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.uni_ulm.uberuniulm.model.OfferedRide;

public class FilterRideInfo {
    public FilterRideInfo(){

    }

    public Boolean filterByTime(String time1, String time2){
        String[] t2=time2.split(":"), t1=time1.split(":");

        try{
            int hours1=Integer.parseInt(t1[0]);
            int hours2=Integer.parseInt(t2[0]);
            int minutes1=Integer.parseInt(t1[1]);
            int minutes2=Integer.parseInt(t2[1]);

            if(hours1<hours2){
                return true;
            }else if(hours1==hours2 && minutes1<=minutes2){
                return true;
            }else{
                return false;
            }
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        return true;
    }

    public ArrayList filterByTime(ArrayList<Pair<ArrayList, OfferedRide>> offers, String time){
        ArrayList offersFiltered= new ArrayList();
        for(int i=0; i<offers.size();i++){
            if(filterByDate(offers.get(i).second.getDate(), time)){
                offersFiltered.add(offers.get(i));
            }
        }

        return offers;
    }

    public Boolean filterByDate(String date1, String date2){
        try {
            Date date1parsed=new SimpleDateFormat("dd/MM/yyyy").parse(date1);
            Date date2parsed=new SimpleDateFormat("dd/MM/yyyy").parse(date2);

            if(date1parsed.before(date2parsed)){
                return true;
            }else{
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList filterByDate(ArrayList<Pair<ArrayList, OfferedRide>> offers, String date){
        ArrayList offersFiltered= new ArrayList();
        for(int i=0; i<offers.size();i++){
            if(filterByDate(offers.get(i).second.getDate(), date)){
                offersFiltered.add(offers.get(i));
            }
        }

        return offers;
    }

    public ArrayList filterByPlaces(ArrayList<Pair<ArrayList, OfferedRide>> offers){
        ArrayList offersFiltered= new ArrayList();
        for(int i=0; i<offers.size();i++){
            if(offers.get(i).second.getPlaces_open()>0){
                offersFiltered.add(offers.get(i));
            }
        }

        return offers;
    }

    public Boolean filterByPrice(OfferedRide offer, Float price){
        if(offer.getPrice()<=price){
            return true;
        }else{
            return false;
        }
    }

    public ArrayList filterOffersByPrice(ArrayList<Pair<ArrayList, OfferedRide>> offers, Float price) {
        ArrayList offersFiltered= new ArrayList();
        for (int i = 0; i < offers.size(); i++) {
            if (filterByPrice(offers.get(i).second, price)) {
                offersFiltered.add(offers.get(i));
            }
        }

        return offersFiltered;
    }
}
