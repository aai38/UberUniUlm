package de.uni_ulm.uberuniulm.model;

import android.util.Pair;

import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;

public class ParkingSpots {
    ArrayList<ParkingSpot> parkingSpots;

    public ParkingSpots(){
        parkingSpots=new ArrayList<>();
        parkingSpots.add(new ParkingSpot("P09",new LatLng(48.418618, 9.942304)));
        parkingSpots.add(new ParkingSpot("P10",new LatLng(48.419291, 9.944084)));
        parkingSpots.add(new ParkingSpot("P11",new LatLng(48.420014, 9.945747)));
        parkingSpots.add(new ParkingSpot("P12",new LatLng(48.420641, 9.946508)));
        parkingSpots.add(new ParkingSpot("P13",new LatLng(48.421175, 9.947934)));
        parkingSpots.add(new ParkingSpot("P16",new LatLng(48.422228, 9.948180)));
        parkingSpots.add(new ParkingSpot("P17",new LatLng(48.422033, 9.949043)));
        parkingSpots.add(new ParkingSpot("P20",new LatLng(48.424193, 9.951338)));
        parkingSpots.add(new ParkingSpot("P21",new LatLng(48.425123, 9.952772)));
        parkingSpots.add(new ParkingSpot("P22",new LatLng(48.425350, 9.954471)));
        parkingSpots.add(new ParkingSpot("P23",new LatLng(48.426304, 9.954959)));
        parkingSpots.add(new ParkingSpot("P24",new LatLng(48.424272, 9.953381)));
        parkingSpots.add(new ParkingSpot("P25",new LatLng(48.425183, 9.955120)));
        parkingSpots.add(new ParkingSpot("P26",new LatLng(48.423944, 9.952292)));
        parkingSpots.add(new ParkingSpot("P30",new LatLng(48.421870, 9.951710)));
        parkingSpots.add(new ParkingSpot("P32",new LatLng(48.423958, 9.957477)));
        parkingSpots.add(new ParkingSpot("P40",new LatLng(48.426308, 9.957366)));
        parkingSpots.add(new ParkingSpot("P41",new LatLng(48.426504, 9.958686)));
        parkingSpots.add(new ParkingSpot("P42",new LatLng(48.426066, 9.961253)));
        parkingSpots.add(new ParkingSpot("P43",new LatLng(48.424557, 9.960632)));
        parkingSpots.add(new ParkingSpot("P44",new LatLng(48.425425, 9.962753)));
        parkingSpots.add(new ParkingSpot("P49",new LatLng(48.426393, 9.960506)));
    }

    public ArrayList<ParkingSpot> getAll(){
        return parkingSpots;
    }

    public ParkingSpot getSpotByName(String name){
        for(ParkingSpot spot:parkingSpots){
            if(spot.getName().equals(name)){
                return spot;
            }
        }
        return parkingSpots.get(0);
    }

    public static class CenterLocation {

        private double latitudeNorth;
        private double latitudeSouth;
        private double longitudeWest;
        private double longitudeEast;

        public static CenterLocation create(LatLng origin, LatLng destination) {
            return new CenterLocation(origin, destination);
        }


        private CenterLocation(LatLng origin, LatLng destination) {

            latitudeNorth = origin.getLatitude() < destination.getLatitude() ?
                    destination.getLatitude() : origin.getLatitude();

            latitudeSouth = origin.getLatitude() < destination.getLatitude() ?
                    origin.getLatitude() : destination.getLatitude();

            longitudeEast = origin.getLongitude() < destination.getLongitude() ?
                    destination.getLongitude() : origin.getLongitude();

            longitudeWest = origin.getLongitude() < destination.getLongitude() ?
                    origin.getLongitude() : destination.getLongitude();

        }

        public double getLatitudeNorth() {
            return latitudeNorth;
        }

        public double getLatitudeSouth() {
            return latitudeSouth;
        }

        public double getLongitudeWest() {
            return longitudeWest;
        }

        public double getLongitudeEast() {
            return longitudeEast;
        }


    }
}
