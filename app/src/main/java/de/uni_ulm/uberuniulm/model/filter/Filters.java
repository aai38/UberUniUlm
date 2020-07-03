package de.uni_ulm.uberuniulm.model.filter;

import android.content.Context;

import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.R;

public class Filters {
    Boolean userNameFilterSet=false, priceFilterSet=false, placesFilterSet=false, distanceFilterSet=false, dateFilterSet=false;
    int price, distance;
    String userName, date, time;
    LatLng userPosition;
    ArrayList offers;
    FilterRideInfo infoFilter;
    FilterDistance distanceFilter;
    FilterOfferer usernameFilter;
    Context context;

    public Filters(Context mContext){
        context=mContext;
        infoFilter = new FilterRideInfo();
        distanceFilter= new FilterDistance(mContext);
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
            offersFiltered=distanceFilter.FilterListByDistanceTotalRoute(offersFiltered, distance);

        }

        if(dateFilterSet){
            offersFiltered= infoFilter.filterByDate(offersFiltered, date, time);
        }

        return offersFiltered;
    }

    public ArrayList newFilter(ArrayList dataSet, int filterType, int contentIndex){
        offers=dataSet;
        switch(filterType){
            case 0:
                setDistanceFilter(contentIndex);
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
                deleteDistanceFilter();
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
            case 4:
                deleteDateFilter();
                break;
        }
        return filter();
    }

    private void setPriceFilter(int priceIndex){
        String priceString= context.getResources().getStringArray(R.array.Price)[priceIndex];
        price= Integer.parseInt(priceString.substring(0,priceString.length()-1));
        priceFilterSet=true;
    }

    private void deletePriceFilter(){
        priceFilterSet=false;
    }

    private void setPlacesFilter(){
        placesFilterSet=true;
    }

    private void deletePlacesFilter(){
        placesFilterSet=false;
    }

    private ArrayList setDistanceFilter(int distanceIndex){
        String distanceString= context.getResources().getStringArray(R.array.Distance)[distanceIndex];
        distance= Integer.parseInt(distanceString.substring(0,distanceString.length()-1));
        distanceFilterSet=true;
        return filter();
    }
    private void deleteDistanceFilter(){
        distanceFilterSet=false;
    }

    public ArrayList setUsernameFilter(ArrayList offers, String username){
        this.userName=username;
        userNameFilterSet=true;
        this.offers=offers;
        return filter();
    }

    private void deleteUsernameFilter(){
        userNameFilterSet=false;
    }

    public ArrayList setDateFilter(ArrayList offers, String date, String time){
        this.offers=offers;
        dateFilterSet=true;
        this.date=date;
        this.time=time;
        return filter();
    }

    private void deleteDateFilter(){
        dateFilterSet=false;
    }
}
