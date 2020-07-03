package de.uni_ulm.uberuniulm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.location.LocationUpdateListener;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.RouteStyle;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.Report;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderFullAddress;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.uni_ulm.uberuniulm.ui.fragments.NewOfferHeaderFragment;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.parking.ParkingSpots;
import de.uni_ulm.uberuniulm.ui.fragments.RideOverviewHeaderFragment;
import de.uni_ulm.uberuniulm.ui.map.TypedBallonViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MapPage extends AppCompatActivity implements LocationUpdateListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, TomtomMapCallback.OnMapLongClickListener{
    private TomtomMap tomtomMap;
    private SearchApi searchApi;
    private LatLng latLngCurrentPosition, latLngDeparture, latLngDestination, wayPointPosition;
    private Route route;
    private ArrayList userData;
    private OfferedRide ride;
    private RoutingApi routingApi;
    private static final LatLng DEFAULT_DEPARTURE_LATLNG = new LatLng(48.418618, 9.942304);
    private NewOfferHeaderFragment fragment;
    private RideOverviewHeaderFragment overviewHeaderFragment;
    private static final int MAX_ROUTE_ALTERNATIVES=2;
    private static final RouteType[] ROUTETYPES_LIST={RouteType.FASTEST,RouteType.SHORTEST};

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private List<String>  waypoints;
    private List<LatLng> waypointList;
    private long currrentlySelectedRoute;
    private Boolean waypointsInitiated=false;
    private WaypointArrayAdapter waypointArrayAdapter;
    private AutoCompleteTextView atvWaypointLocation;
    private String viewType, userId;

    private Icon departureIcon, destinationIcon, waypointIcon;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_ride);

        LinearLayout mapFragment=(LinearLayout) findViewById(R.id.newOfferActivityMapContainer);


        mapFragment.setVisibility(View.VISIBLE);
        searchApi=OnlineSearchApi.create(this);
        routingApi=OnlineRoutingApi.create(this);

        departureIcon=Icon.Factory.fromResources(MapPage.this, R.drawable.ic_map_route_departure);
        destinationIcon=Icon.Factory.fromResources(MapPage.this, R.drawable.ic_map_route_destination);
        waypointIcon=Icon.Factory.fromResources(MapPage.this, R.drawable.ic_markedlocation);

        waypoints=new ArrayList<>();

        viewType= getIntent().getExtras().getString("VIEWTYPE");
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
            if(viewType.equals("NEWOFFER")) {
                fragment = new NewOfferHeaderFragment();
                fragmentTransaction.replace(R.id.newOfferActivityHeaderFragmentContainer, fragment);
            }else if(viewType.equals("RIDEOVERVIEW")){
                userData= (ArrayList) getIntent().getExtras().get("USER");
                ride = (OfferedRide) getIntent().getSerializableExtra("RIDE");
                overviewHeaderFragment = new RideOverviewHeaderFragment();
                fragmentTransaction.replace(R.id.newOfferActivityHeaderFragmentContainer, overviewHeaderFragment);
            }else if(viewType.equals("EDITOFFER")){
                userData= (ArrayList) getIntent().getExtras().get("USER");
                ride = (OfferedRide) getIntent().getSerializableExtra("RIDE");
                fragment = new NewOfferHeaderFragment();
                fragmentTransaction.replace(R.id.newOfferActivityHeaderFragmentContainer, fragment);
            }
        fragmentTransaction.commit();

        initTomTomServices();
        initTomTomServices();
        initUIViews();
        setupUIViewListeners();

        waypoints= new ArrayList<>();
        waypointList= new ArrayList<>();
        wayPointPosition=DEFAULT_DEPARTURE_LATLNG;
        waypointArrayAdapter = new WaypointArrayAdapter(this);

    }

    public SearchApi getSearchApi(){
        return searchApi;
    }

    public void cancel(){
        Intent intent = new Intent(MapPage.this, MainPage.class);
        startActivity(intent);
    }

    public void onNewOfferActivityConfirmBttn(int price, String date, String time, int places, String departure, String destination){
        if(route!=null){
            final SharedPreferences pref = new ObscuredSharedPreferences(
                        MapPage.this, MapPage.this.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
            userId = pref.getString("UserKey", "");

            int zIndex = pref.getInt("RideId", 0);

            ArrayList<String> observers= new ArrayList<>();

            OfferedRide offer=new OfferedRide(route.getCoordinates(), price, date, time, places, places, departure, destination, userId, zIndex, waypointList, observers);

            database = FirebaseDatabase.getInstance();
            myRef = database.getReference();

            if(viewType.equals("EDITOFFER")){
                myRef.child(userId).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(offer);
            }else{
                SharedPreferences.Editor editor = pref.edit();
                myRef.child(userId).child("offeredRides").child(String.valueOf(zIndex)).setValue(offer);
                editor.putInt("RideId", zIndex +1);
                editor.apply();
            }
            Intent intent = new Intent(MapPage.this, MainPage.class);
            startActivity(intent);

        }else{
            Toast.makeText(this, getResources().getString(R.string.newOffer_route_invalid_error), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapReady(@NonNull TomtomMap map) {
        tomtomMap = map;
        tomtomMap.setMyLocationEnabled(true);
        tomtomMap.addOnMapLongClickListener(this);
        tomtomMap.getMarkerSettings().setMarkersClustering(true);
        tomtomMap.removeMarkers();
        tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(new TypedBallonViewAdapter());

        LatLng currentPosition=getCurrentPosition();
        tomtomMap.getUiSettings().setCameraPosition(
                CameraPosition
                        .builder(currentPosition)
                        .zoom(14)
                        .build()
        );

        if(viewType.equals("RIDEOVERVIEW")|| viewType.equals("EDITOFFER")){
            route=tomtomMap.addRoute(new RouteBuilder(ride.getRoute()).startIcon(departureIcon).endIcon(destinationIcon).style(RouteStyle.DEFAULT_ROUTE_STYLE));
            if(ride.getWaypoints().size()>0) {
                waypointList = ride.getWaypoints();
                waypoints=new ArrayList<>();
                for (int i = 0; i < waypointList.size(); i++) {
                    waypoints.add(waypointList.get(i).getLatitudeAsString()+ "; "+ waypointList.get(i).getLongitudeAsString());
                    SimpleMarkerBalloon balloon = new SimpleMarkerBalloon(waypoints.get(i));
                    MarkerBuilder markerBuilder = new MarkerBuilder(waypointList.get(i))
                            .markerBalloon(balloon);

                    Marker m = tomtomMap.addMarker(markerBuilder);
                }
            }
        }

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (isDeparturePositionSet() && isDestinationPositionSet()) {
            clearMap();
        } else {
            handleLongClick(latLng);
        }
    }

    private void handleLongClick(@NonNull LatLng latLng) {
        searchApi.reverseGeocoding(new ReverseGeocoderSearchQueryBuilder(latLng.getLatitude(), latLng.getLongitude()).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ReverseGeocoderSearchResponse>() {
                    @Override
                    public void onSuccess(ReverseGeocoderSearchResponse response) {
                        processResponse(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("MISTAKE LONG CLICK", "MISTAKE WAS HERE");
                        handleApiError(e);
                    }

                    private void processResponse(ReverseGeocoderSearchResponse response) {
                        if (response.hasResults()) {
                            processFirstResult(response.getAddresses().get(0).getPosition());
                        }
                        else {
                            Toast.makeText(MapPage.this, getString(R.string.geocode_no_results), Toast.LENGTH_SHORT).show();
                        }
                    }

                    private void processFirstResult(LatLng geocodedPosition) {
                        if (!isDeparturePositionSet()) {
                            setAndDisplayDeparturePosition(geocodedPosition);
                        } else {
                            latLngDestination = geocodedPosition;
                            tomtomMap.removeMarkers();
                            drawRoute(latLngDeparture, latLngDestination);
                        }
                    }

                    private void setAndDisplayDeparturePosition(LatLng geocodedPosition) {
                        latLngDeparture = geocodedPosition;
                        createMarkerIfNotPresent(latLngDeparture, departureIcon);
                    }
                });
    }

    private void createMarkerIfNotPresent(LatLng position, Icon icon) {
        Optional optionalMarker = tomtomMap.findMarkerByPosition(position);
        if (!optionalMarker.isPresent()) {
            tomtomMap.addMarker(new MarkerBuilder(position)
                    .icon(icon));
        }
    }

    private boolean isDestinationPositionSet() {
        return latLngDestination != null;
    }

    private boolean isDeparturePositionSet() {
        return latLngDeparture != null;
    }

    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
        searchApi = OnlineSearchApi.create(this);
    }

    private void initUIViews() {}

    private void setupUIViewListeners() {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void handleApiError(Throwable e) {
        Toast.makeText(MapPage.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private LatLng getCurrentPosition(){
        ParkingSpots parkings=new ParkingSpots();
        Location currentUserLocation=tomtomMap.getUserLocation();
        if(currentUserLocation!=null) {
            return new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getAltitude());
        }else{
            return parkings.getSpotByName("P26").getPosition();
        }
    }


    private RouteQuery createRouteQuery(LatLng start, LatLng stop, LatLng[] wayPoints) {
        return (wayPoints != null) ?
                new RouteQueryBuilder(start, stop).withWayPoints(wayPoints).withMaxAlternatives(MAX_ROUTE_ALTERNATIVES)
                        .withReport(Report.EFFECTIVE_SETTINGS)
                        .withInstructionsType(InstructionsType.TEXT)
                        .withConsiderTraffic(false).build():
                new RouteQueryBuilder(start, stop).withWayPoints(wayPoints).withMaxAlternatives(MAX_ROUTE_ALTERNATIVES)
                        .withReport(Report.EFFECTIVE_SETTINGS)
                        .withInstructionsType(InstructionsType.TEXT)
                        .withRouteType(ROUTETYPES_LIST[0])
                        .withRouteType(ROUTETYPES_LIST[1])
                        .withConsiderTraffic(false).build();
    }

    public void hideKeyboard(View view) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (in != null) {
            in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (latLngCurrentPosition == null) {
            latLngCurrentPosition = new LatLng(location);
            fragment.deactivateLocationSource();
        }
    }


    public void drawRoute(LatLng start, LatLng stop) {
        tomtomMap.clear();
        LatLng[] waypointList=new LatLng[this.waypointList.size()];
        for(int i=0; i<this.waypointList.size();i++){
            waypointList[i]=this.waypointList.get(i);
        }
        drawRouteWithWayPoints(start, stop, waypointList);
    }

    public void setAddressForLocation(LatLng location, final AutoCompleteTextView autoCompleteTextView) {
        searchApi.reverseGeocoding(new ReverseGeocoderSearchQueryBuilder(location.getLatitude(), location.getLongitude()).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ReverseGeocoderSearchResponse>() {
                    @Override
                    public void onSuccess(ReverseGeocoderSearchResponse reverseGeocoderSearchResponse) {
                        List addressesList = reverseGeocoderSearchResponse.getAddresses();
                        if (!addressesList.isEmpty()) {
                            String address = ((ReverseGeocoderFullAddress) addressesList.get(0)).getAddress().getFreeformAddress();
                            autoCompleteTextView.setText(address);
                            autoCompleteTextView.dismissDropDown();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MapPage.this, "Error setting address", Toast.LENGTH_LONG).show();
                        Log.e("newOfferActivity", "Error setting address", e);
                    }
                });
    }


    private void drawRouteWithWayPoints(LatLng start, LatLng stop, LatLng[] wayPoints) {
        RouteQuery routeQuery = createRouteQuery(start, stop, wayPoints);
        routingApi.planRoute(routeQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResponse>() {

                    @Override
                    public void onSuccess(RouteResponse routeResponse) {
                        displayRoutes(routeResponse.getRoutes());
                        tomtomMap.displayRoutesOverview();
                    }

                    private void displayRoutes(List<FullRoute> routes) {
                        for (FullRoute fullRoute : routes) {
                            if(routes.indexOf(fullRoute)==0){
                                route = tomtomMap.addRoute(new RouteBuilder(
                                        fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).style(RouteStyle.DEFAULT_ROUTE_STYLE));
                                currrentlySelectedRoute=route.getId();
                            }else {
                                route = tomtomMap.addRoute(new RouteBuilder(
                                        fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).style(RouteStyle.DEFAULT_INACTIVE_ROUTE_STYLE));
                            }
                        }

                        for(int i=0; i<waypointList.size();i++){
                            SimpleMarkerBalloon balloon = new SimpleMarkerBalloon(waypoints.get(i));
                            MarkerBuilder markerBuilder = new MarkerBuilder(waypointList.get(i))
                                    .markerBalloon(balloon);

                            Marker m = tomtomMap.addMarker(markerBuilder);
                        }
                        tomtomMap.getRouteSettings().addOnRouteClickListener(new TomtomMapCallback.OnRouteClickListener() {
                            @Override
                            public void onRouteClick(@NonNull Route route) {
                                tomtomMap.updateRouteStyle(currrentlySelectedRoute, RouteStyle.DEFAULT_INACTIVE_ROUTE_STYLE);
                                tomtomMap.updateRouteStyle(route.getId(), RouteStyle.DEFAULT_ROUTE_STYLE);
                                currrentlySelectedRoute=route.getId();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleApiError(e);
                        clearMap();
                    }
                });
    }

    private void clearMap() {
        tomtomMap.clear();
        latLngDeparture = null;
        latLngDestination = null;
        route = null;
    }

    public void addWaypoint(){
        LinearLayout fragmentContainer = findViewById(R.id.newOfferFragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        atvWaypointLocation = findViewById(R.id.dialogWaypointSearch);

        if (!waypointsInitiated) {
            fragment.setTextWatcherToAutoCompleteField(atvWaypointLocation);
            ListView waypointList = findViewById(R.id.dialogManageWaypointsList);
            waypointList.setAdapter(waypointArrayAdapter);
            waypointsInitiated = true;
        }

        fragment.setWayPointTextField(atvWaypointLocation);
    }

    public void onAddWaypointDialogButton(View view){
        if(!waypointList.contains(wayPointPosition)&& wayPointPosition!=DEFAULT_DEPARTURE_LATLNG){
            waypointList.add(wayPointPosition);
            waypoints.add(atvWaypointLocation.getText().toString());
            atvWaypointLocation.setText("");
            if(waypointList.size()==3){
                TextView waypointAddTitle= findViewById(R.id.dialogManageWaypointsTitle);
                LinearLayout addWaypointContainer= findViewById(R.id.dialogWaypointAddWaypointContainer);
                waypointAddTitle.setText(R.string.dialog_manage_waypoints_max_number_hint);
                addWaypointContainer.setVisibility(View.INVISIBLE);
            }
            waypointArrayAdapter.notifyDataSetChanged();
        }
    }



    public void onCloseWaypointDialogButton(View view){
        onAddWaypointDialogButton(view);
        fragment.updateCheck();
        LinearLayout fragmentContainer= findViewById(R.id.newOfferFragmentContainer);
        fragmentContainer.setVisibility(View.INVISIBLE);

    }

    private class WaypointArrayAdapter extends ArrayAdapter<String> {
        Context mContext;

        public WaypointArrayAdapter(@NonNull Context context) {
            super(context, 0 , waypoints);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(MapPage.this).inflate(R.layout.item_waypoint,parent,false);

            TextView waypointName = (TextView)listItem.findViewById(R.id.waypointNameTextfield);
            waypointName.setText(waypoints.get(position));

            ImageView deleteBttn= (ImageView) listItem.findViewById(R.id.waypointDeleteBttn);
            deleteBttn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    waypointList.remove(position);
                    waypoints.remove(position);
                    wayPointPosition=DEFAULT_DEPARTURE_LATLNG;
                    LinearLayout addWaypointContainer= findViewById(R.id.dialogWaypointAddWaypointContainer);
                    waypointArrayAdapter.notifyDataSetChanged();

                    if(waypoints.size()==2) {
                        addWaypointContainer.setVisibility(View.VISIBLE);
                        TextView waypointAddTitle= findViewById(R.id.dialogManageWaypointsTitle);
                        waypointAddTitle.setText(R.string.dialog_manage_waypoints_title);
                    }
                }
            });

            return listItem;
        }
    }

    public void markRide(Boolean notMarkedYet){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        final SharedPreferences pref = new ObscuredSharedPreferences(
                MapPage.this, MapPage.this.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        if(notMarkedYet) {
            ride.markRide(userId);
        }else{
            ride.unmarkRide(userId);
        }
        
        myRef.child(userData.get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(ride);
    }

    public void closeMapView(){
        this.cancel();
    }

    public void setWayPointPosition(LatLng waypoint){
        this.wayPointPosition=waypoint;
        //waypointList.add(waypoint);
    }

    public Pair<ArrayList,OfferedRide> getRideData(){
        Pair<ArrayList, OfferedRide> rideData= new Pair<>(userData, ride);
        return rideData;
    }

    public String getViewType(){return viewType;}
}
