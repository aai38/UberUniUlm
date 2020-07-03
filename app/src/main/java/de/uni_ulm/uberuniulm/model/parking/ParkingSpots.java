package de.uni_ulm.uberuniulm.model.parking;

import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.parking.ParkingSpot;

public class ParkingSpots {
    ArrayList<ParkingSpot> parkingSpots;

    public ParkingSpots(){
        parkingSpots=new ArrayList<>();
        parkingSpots.add(new ParkingSpot("P09",new LatLng(48.418618, 9.942304), "börner dorm"));
        parkingSpots.add(new ParkingSpot("P10",new LatLng(48.419291, 9.944084), "west, main"));
        parkingSpots.add(new ParkingSpot("P11",new LatLng(48.420014, 9.945747), "47 west, internally "));
        parkingSpots.add(new ParkingSpot("P12",new LatLng(48.420641, 9.946508), "45 west, internally"));
        parkingSpots.add(new ParkingSpot("P13",new LatLng(48.421175, 9.947934), "43 west, internally"));
        parkingSpots.add(new ParkingSpot("P16",new LatLng(48.422228, 9.948180), "library I"));
        parkingSpots.add(new ParkingSpot("P17",new LatLng(48.422033, 9.949043), "library II"));
        parkingSpots.add(new ParkingSpot("P20",new LatLng(48.424193, 9.951338), "parking garage clinic"));
        parkingSpots.add(new ParkingSpot("P21",new LatLng(48.425123, 9.952772), "kindergarten"));
        parkingSpots.add(new ParkingSpot("P22",new LatLng(48.425350, 9.954471), "line, albert-einstein-allee"));
        parkingSpots.add(new ParkingSpot("P23",new LatLng(48.426304, 9.954959), "north, near roundabout"));
        parkingSpots.add(new ParkingSpot("P24",new LatLng(48.424272, 9.953381), "north, main"));
        parkingSpots.add(new ParkingSpot("P25",new LatLng(48.425183, 9.955120), "line, medical school"));
        parkingSpots.add(new ParkingSpot("P26",new LatLng(48.423944, 9.952292), "clinic/ north"));
        parkingSpots.add(new ParkingSpot("P30",new LatLng(48.421870, 9.951710), "clinic south"));
        parkingSpots.add(new ParkingSpot("P32",new LatLng(48.423958, 9.957477),"botanical garden"));
        parkingSpots.add(new ParkingSpot("P40",new LatLng(48.426308, 9.957366), "parking garage, helmholtz"));
        parkingSpots.add(new ParkingSpot("P41",new LatLng(48.426504, 9.958686), "east, main"));
        parkingSpots.add(new ParkingSpot("P42",new LatLng(48.426066, 9.961253), "across from east main, small"));
        parkingSpots.add(new ParkingSpot("P43",new LatLng(48.424557, 9.960632), "across from east main, big"));
        parkingSpots.add(new ParkingSpot("P44",new LatLng(48.425425, 9.962753), "small, on right main east "));
        parkingSpots.add(new ParkingSpot("P49",new LatLng(48.426393, 9.960506), "helmholtz turning space"));
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

    public ArrayList<ParkingSpot> getSpotsBySubString(String substring){
        ArrayList<ParkingSpot> result= new ArrayList<>();
        for(ParkingSpot spot:parkingSpots){
            if(spot.getName().contains(substring)){
                result.add(spot);
            }
        }
        return result;
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

            latitudeNorth = Math.max(origin.getLatitude(), destination.getLatitude());

            latitudeSouth = Math.min(origin.getLatitude(), destination.getLatitude());

            longitudeEast = Math.max(origin.getLongitude(), destination.getLongitude());

            longitudeWest = Math.min(origin.getLongitude(), destination.getLongitude());

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
