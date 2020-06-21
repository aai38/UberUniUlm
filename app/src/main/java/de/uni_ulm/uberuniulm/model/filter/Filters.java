package de.uni_ulm.uberuniulm.model.filter;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.tomtom.online.sdk.common.location.LatLng;

import java.text.ParseException;
import java.util.ArrayList;

import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.OfferedRide;

public class Filters {
    Boolean userNameFilterSet=false, priceFilterSet=false, placesFilterSet=false, distanceFilterSet=false;
    int price, distance;
    String userName;
    LatLng userPosition;
    ArrayList<Pair<ArrayList, OfferedRide>> offers;
    FilterRideInfo infoFilter;
    FilterDistance distanceFilter;
    FilterOfferer usernameFilter;
    Context context;

    public Filters(Context mContext){
        context=mContext;
        infoFilter = new FilterRideInfo();
        distanceFilter= new FilterDistance();
        usernameFilter= new FilterOfferer();
    }

    private ArrayList filter(){
        ArrayList offersFiltered=offers;
        if(userNameFilterSet){
            offersFiltered=usernameFilter.filterOffersByOfferer(offersFiltered, userName);
        }

        if(priceFilterSet){
            offersFiltered=infoFilter.filterOffersByPrice(offersFiltered, (float) price);
        }

        if(placesFilterSet){
            offersFiltered=infoFilter.filterByPlaces(offersFiltered);
        }

        if(distanceFilterSet){
            offersFiltered=distanceFilter.FilterListByDistanceTotalRoute(offersFiltered, userPosition, distance);

        }

        return offersFiltered;
    }

    public ArrayList newFilter(ArrayList dataSet, int filterType, int contentIndex){
        offers=dataSet;
        switch(filterType){
            case 0:
                //user position just needs to be implemented
                break;
            case 2:
                setPriceFilter(contentIndex);
                break;
            case 3:
                setPlacesFilter();
                break;

        }
        return filter();
    }

    public ArrayList deleteFilter(ArrayList dataSet, int filter){
        this.offers=dataSet;
        switch(filter){
            case 0:
                //user position just needs to be implemented
                break;
            case 1:
                deleteUsernameFilter();
                break;
            case 2:
                deletePriceFilter();
                break;
            case 3:
                deletePlacesFilter();
                break;

        }
        return filter();
    }

    public void setPriceFilter(int priceIndex){
        String priceString= context.getResources().getStringArray(R.array.Price)[priceIndex];
        Log.d("PRICE BEFORE PARSE: ",priceString);
        price= Integer.parseInt(priceString.substring(0,priceString.length()-1));
        priceFilterSet=true;
    }

    public void deletePriceFilter(){
        priceFilterSet=false;
    }

    public void setPlacesFilter(){
        placesFilterSet=true;
    }

    public void deletePlacesFilter(){
        placesFilterSet=false;
    }

    public ArrayList setDistanceFilter(ArrayList offers, int distanceIndex, LatLng userPosition){
        String distanceString= context.getResources().getStringArray(R.array.Distance)[distanceIndex];
        distance= Integer.parseInt(distanceString.substring(0,distanceString.length()-2));
        this.userPosition=userPosition;
        this.offers=offers;
        distanceFilterSet=true;
        return filter();
    }
    public void deleteDistanceFilter(){
        distanceFilterSet=false;
    }

    public ArrayList setUsernameFilter(ArrayList offers, String username){
        this.userName=username;
        userNameFilterSet=true;
        this.offers=offers;
        return filter();
    }

    public void deleteUsernameFilter(){
        userNameFilterSet=false;
    }
}
