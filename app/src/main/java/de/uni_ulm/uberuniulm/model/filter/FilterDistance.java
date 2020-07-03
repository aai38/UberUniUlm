package de.uni_ulm.uberuniulm.model.filter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.uni_ulm.uberuniulm.model.ride.OfferedRide;

import static android.content.Context.LOCATION_SERVICE;

public class FilterDistance implements LocationListener {
    private LocationManager locationManager;
    private Context mContext;
    private static final LatLng DEFAULT__LATLNG = new LatLng(48.418618, 9.942304);

    public FilterDistance(Context context) {
        this.mContext = context;

    }

    public Location getCurrentLocation(){
        String[] permissions= new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        checkPermission(permissions,  0);
        Location location= DEFAULT__LATLNG.toLocation();


        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        if (!(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                return location;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
        return location;
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            locationManager.removeUpdates(this);
        }
    }

    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

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

    public ArrayList FilterListByDistanceTotalRoute(ArrayList<Pair<ArrayList, OfferedRide>> offers, double filterDistance){
        Location location=getCurrentLocation();
        LatLng filterPosition= new LatLng(location.getLatitude(), location.getLongitude());

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


    public void checkPermission(String[] permissions, int requestCode)
    {

        // Checking if permission is not granted
        for(String permission: permissions) {
            if (ContextCompat.checkSelfPermission(
                    mContext,
                    permission)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat
                        .requestPermissions(
                                (Activity) mContext,
                                new String[]{permission},
                                requestCode);
            } else {
                Log.i("Location permission", " already granted!");
            }
        }
    }
}
