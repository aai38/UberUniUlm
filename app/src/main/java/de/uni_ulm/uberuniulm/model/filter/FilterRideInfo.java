package de.uni_ulm.uberuniulm.model.filter;

import android.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.uni_ulm.uberuniulm.model.ride.OfferedRide;

public class FilterRideInfo {
    public FilterRideInfo(){

    }

    public Boolean filterByTime(String time1, String time2){

        try{
            if(!time1.equals("")&& !time2.equals("")) {
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                Date timeParsed1 = timeFormatter.parse(time1);
                Date timeParsed2 = timeFormatter.parse(time2);

                Calendar cal = Calendar.getInstance();
                cal.setTime(timeParsed2);
                cal.add(Calendar.HOUR_OF_DAY, -5);

                Date lowerBound= cal.getTime();

                cal.add(Calendar.HOUR_OF_DAY, 10);

                Date upperBound= cal.getTime();
                if (timeParsed1.getHours()>=lowerBound.getHours()&&timeParsed1.getHours()<=upperBound.getHours()) {
                    return true;
                } else {
                    return false;
                }
            }
        }catch(ParseException e){
            e.printStackTrace();
        }

        return true;
    }

    public ArrayList filterByTime(ArrayList<Pair<ArrayList, OfferedRide>> offers, String time){
        ArrayList offersFiltered= new ArrayList();
        for(int i=0; i<offers.size();i++){
            if(filterByTime(offers.get(i).second.getDate(), time)){
                offersFiltered.add(offers.get(i));
            }
        }

        return offers;
    }

    public Boolean filterByDate(String date1, String date2){
        try {
            if(!date1.equals("")& !date2.equals("")) {
                Date date1parsed = new SimpleDateFormat("MM/dd/yyy").parse(date1);
                Date date2parsed = new SimpleDateFormat("MM/dd/yyy").parse(date2);

                if (date1parsed.getDay() == date2parsed.getDay()&& date1parsed.getMonth() == date2parsed.getMonth()&& date1parsed.getYear() == date2parsed.getYear()) {
                    return true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList filterByDate(ArrayList<Pair<ArrayList, OfferedRide>> offers, String date, String time){
        ArrayList offersFiltered= new ArrayList();
        for(int i=0; i<offers.size();i++){
            if(filterByDate(offers.get(i).second.getDate(), date)) {
                if (filterByTime(offers.get(i).second.getTime(), time)) {
                    offersFiltered.add(offers.get(i));
                }
            }
        }

        return offersFiltered;
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
