package de.uni_ulm.uberuniulm.model.filter;

import android.util.Log;
import android.util.Pair;

import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;
import java.util.List;

import de.uni_ulm.uberuniulm.model.OfferedRide;

public class FilterDistance {
    public FilterDistance(){

    }

    public Boolean distanceTwoPoints(LatLng coord1, LatLng coord2, double filterDistance){
        double lat1= coord1.getLatitude(), lat2=coord2.getLatitude(), lon1=coord1.getLongitude(), lon2=coord2.getLongitude();
        Boolean isWithinDistance;

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);
        Log.d("CALCULATED DISTANCE", String.valueOf(Math.sqrt(distance)));

        if(filterDistance>= Math.sqrt(distance)){
            return true;
        }else{
            return false;
        }
    }

    public ArrayList filterListByDistanceDeparture(ArrayList<Pair<ArrayList, OfferedRide>> offers, LatLng filterPosition, double filterDistance){
        ArrayList<Pair<ArrayList, OfferedRide>> offersFiltered= new ArrayList<>();
        for(int i=0; i<offers.size(); i++){
            Boolean isInDistance=distanceTwoPoints(offers.get(i).second.getRoute().get(0), filterPosition, filterDistance);
            if(isInDistance){
                offersFiltered.add(offers.get(i));
            }
        }

        return offersFiltered;
    }

    public ArrayList filterListByDistanceDestination(ArrayList<Pair<ArrayList, OfferedRide>> offers, LatLng filterPosition, double filterDistance){
        ArrayList<Pair<ArrayList, OfferedRide>> offersFiltered= new ArrayList<>();
        for(int i=0; i<offers.size(); i++){
            List<LatLng> routeCoords=offers.get(i).second.getRoute();
            Boolean isInDistance=distanceTwoPoints(routeCoords.get(routeCoords.size()-1), filterPosition, filterDistance);
            if(isInDistance){
                offersFiltered.add(offers.get(i));
            }
        }

        return offersFiltered;
    }

    public ArrayList FilterListByDistanceTotalRoute(ArrayList<Pair<ArrayList, OfferedRide>> offers, LatLng filterPosition, double filterDistance){
        ArrayList<Pair<ArrayList, OfferedRide>> offersFiltered= new ArrayList<>();

        for(int i=0; i<offers.size(); i++){
            List<LatLng> routeCoords=offers.get(i).second.getRoute();
            for(int j=0; j<routeCoords.size(); j++){
                if(distanceTwoPoints(routeCoords.get(j), filterPosition, filterDistance)){
                    offersFiltered.add(offers.get(i));
                    break;
                }
            }
        }

        return offersFiltered;
    }

}
