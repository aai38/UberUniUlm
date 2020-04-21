package de.uni_ulm.uberuniulm;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.common.base.Optional;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.permission.AndroidPermissionChecker;
import com.tomtom.online.sdk.location.LocationSource;
import com.tomtom.online.sdk.location.LocationSourceFactory;
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
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderFullAddress;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.uni_ulm.uberuniulm.model.MyOffersFragment;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.ParkingSpot;
import de.uni_ulm.uberuniulm.model.ParkingSpots;
import de.uni_ulm.uberuniulm.model.Settings;
import de.uni_ulm.uberuniulm.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NewOfferPage extends AppCompatActivity implements LocationUpdateListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, TomtomMapCallback.OnMapLongClickListener {
    private TomtomMap tomtomMap;
    private SearchApi searchApi;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng latLngCurrentPosition, latLngDeparture, latLngDestination, wayPointPosition;
    private Route route;
    private RoutingApi routingApi;
    private AutoCompleteTextView atvDepartureLocation;
    private AutoCompleteTextView atvDestinationLocation;
    private AutoCompleteTextView atvWaypointLocation;
    private final Handler searchTimerHandler = new Handler();
    private LocationSource locationSource;

    private static final int AUTOCOMPLETE_SEARCH_DELAY_MILLIS = 600;
    private static final int AUTOCOMPLETE_SEARCH_THRESHOLD = 2;
    private static final LatLng DEFAULT_DEPARTURE_LATLNG = new LatLng(48.418618, 9.942304);
    private static final LatLng DEFAULT_DESTINATION_LATLNG = new LatLng(48.426393, 9.960506);
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int MAX_ROUTE_ALTERNATIVES=2;
    private static final RouteType[] ROUTETYPES_LIST={RouteType.FASTEST,RouteType.SHORTEST};

    private static final int SEARCH_FUZZY_LVL_MIN = 2;

    private ArrayAdapter<String> searchAdapter;
    private List<String> searchAutocompleteList, waypoints;
    private List<LatLng> waypointList;
    private Map<String, LatLng> searchResultsMap;
    private Runnable searchRunnable;
    private EditText startTextField, goalTextField, dateTextField, timeTextField, placesTextField, priceTextField;
    public static ArrayList<OfferedRide> offeredRides;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private long currrentlySelectedRoute;
    private Boolean waypointsInitiated=false;
    private WaypointArrayAdapter waypointArrayAdapter;

    private Icon departureIcon, destinationIcon, waypointIcon;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_ride);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LinearLayout mapFragment=(LinearLayout) findViewById(R.id.newOfferActivityMapContainer);
        startTextField= findViewById(R.id.newOfferActivityStartEditText);
        goalTextField= findViewById(R.id.newOfferActivityDestinationEditText);
        dateTextField= findViewById(R.id.newOfferActivityDateTextField);
        timeTextField= findViewById(R.id.newOfferActivityTimeTextField);
        placesTextField= findViewById(R.id.newOfferActivityPlacesTextField);
        priceTextField= findViewById(R.id.newOfferActivityPriceTextField);

        dateTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        NewOfferPage.this, R.style.spinnerDatePickerStyle,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorSlightlyTransparentBlack)));
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        mDateSetListener= new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = month + "/" + dayOfMonth + "/" + year;
                dateTextField.setText(date);
            }
        };

        timeTextField.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final Calendar myCalender = Calendar.getInstance();
                int hour = myCalender.get(Calendar.HOUR_OF_DAY);
                int minute = myCalender.get(Calendar.MINUTE);
                mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {
                            myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            myCalender.set(Calendar.MINUTE, minute);
                            String time=hourOfDay+":"+minute;
                            timeTextField.setText(time);
                        }
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewOfferPage.this,R.style.timePickerStyle , mTimeSetListener, hour, minute, true);
                timePickerDialog.setTitle("Choose departure:");
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorSlightlyTransparentBlack)));
                timePickerDialog.show();
                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        mapFragment.setVisibility(View.VISIBLE);
        searchApi=OnlineSearchApi.create(this);
        routingApi=OnlineRoutingApi.create(this);

        departureIcon=Icon.Factory.fromResources(NewOfferPage.this, R.drawable.ic_map_route_departure);
        destinationIcon=Icon.Factory.fromResources(NewOfferPage.this, R.drawable.ic_map_route_destination);
        waypointIcon=Icon.Factory.fromResources(NewOfferPage.this, R.drawable.ic_markedlocation);

        fragmentManager=getSupportFragmentManager();

        initTomTomServices();
        initSearchFieldsWithDefaultValues();
        initWhereSection();
        initTomTomServices();
        initUIViews();
        setupUIViewListeners();


        Button btnRouteShow = findViewById(R.id.newOfferActivityGoBttn);
        btnRouteShow.setOnClickListener(v -> {
            tomtomMap.clear();
            drawRoute(latLngDeparture, latLngDestination);
        });

        waypoints= new ArrayList<>();
        waypointList= new ArrayList<>();
        wayPointPosition=DEFAULT_DEPARTURE_LATLNG;
        waypointArrayAdapter = new WaypointArrayAdapter(this);
    }

    public void onNewOfferActivityCancelBttn(View view){
        Intent intent = new Intent(NewOfferPage.this, MainPage.class);
        startActivity(intent);
    }

    public void onNewOfferActivityConfirmBttn(View view){
        Integer price= Integer.parseInt(priceTextField.getText().toString());
        //if(route!=null&& price!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            java.util.Date utilDate;
            Date date;
            String time;
            try {
                utilDate = sdf.parse(dateTextField.getText().toString());
                date= new java.sql.Date(utilDate.getTime());
                time = timeTextField.getText().toString();
            } catch (ParseException e) {
                e.printStackTrace();
                utilDate=Calendar.getInstance().getTime();
                date =new java.sql.Date(utilDate.getTime());
                time=Calendar.getInstance().getTime().toString();
            }
            route = null;
            Integer places= (Integer.parseInt(placesTextField.getText().toString()));
            OfferedRide offer=new OfferedRide(route, price, date, time, places, places );
            offeredRides = new ArrayList<>();
            offeredRides.add(offer);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();


            SharedPreferences pref = getApplicationContext().getSharedPreferences("UserKey", 0);
            String userId = pref.getString("UserKey", "");
            Log.i("userkey", ""+userId);




            if(offeredRides.size() == 1) {
                DatabaseReference refRide = myRef.child(userId).push();
                Log.i("size=1 new offer", ""+ refRide.getKey());

                refRide.setValue(offer);
                offer.setKey(refRide.getKey());
            } else {
                DatabaseReference refRide = myRef.child(userId).child("offeredRides").push();

                Log.i("size>1 new offer", ""+ refRide.getKey());
                refRide.setValue(offer);
                offer.setKey(refRide.getKey());

            }

        //}

        //fragmentTransaction = fragmentManager.beginTransaction();
       // fragmentTransaction.replace(R.id.fragment_offers, new MainPageFragment());
        //fragmentTransaction.commit();
        Intent intent = new Intent(NewOfferPage.this, MainPage.class);
        startActivity(intent);
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
                        handleApiError(e);
                    }

                    private void processResponse(ReverseGeocoderSearchResponse response) {
                        if (response.hasResults()) {
                            processFirstResult(response.getAddresses().get(0).getPosition());
                        }
                        else {
                            Toast.makeText(NewOfferPage.this, getString(R.string.geocode_no_results), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(NewOfferPage.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
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

    private void initSearchFieldsWithDefaultValues() {
        atvDepartureLocation = findViewById(R.id.newOfferActivityStartEditText);
        atvDestinationLocation = findViewById(R.id.newOfferActivityDestinationEditText);
        atvWaypointLocation = findViewById(R.id.dialogWaypointSearch);

        initLocationSource();
        initDepartureWithDefaultValue();
        initDestinationWithDefaultValue();
    }

    private void initLocationSource() {
        AndroidPermissionChecker permissionChecker = AndroidPermissionChecker.createLocationChecker(this);
        if(permissionChecker.ifNotAllPermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
        LocationSourceFactory locationSourceFactory = new LocationSourceFactory();
        locationSource = locationSourceFactory.createDefaultLocationSource(this, this,  LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(2000)
                .setInterval(5000));
    }

    private void initDepartureWithDefaultValue() {
        latLngDeparture = DEFAULT_DEPARTURE_LATLNG;
        setAddressForLocation(latLngDeparture, atvDepartureLocation);
    }

    private void initDestinationWithDefaultValue() {
        latLngDestination = DEFAULT_DESTINATION_LATLNG;
        setAddressForLocation(latLngDestination, atvDestinationLocation);
    }

    private void setAddressForLocation(LatLng location, final AutoCompleteTextView autoCompleteTextView) {
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
                        Toast.makeText(NewOfferPage.this, "Error setting address", Toast.LENGTH_LONG).show();
                        Log.e("newOfferActivity", "Error setting address", e);
                    }
                });
    }

    private void searchAddress(final String searchWord, final AutoCompleteTextView autoCompleteTextView) {
        searchApi.search(new FuzzySearchQueryBuilder(searchWord)
                .withLanguage(Locale.getDefault().toLanguageTag())
                .withTypeAhead(true)
                .withMinFuzzyLevel(SEARCH_FUZZY_LVL_MIN).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {
                    @Override
                    public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                        if (!fuzzySearchResponse.getResults().isEmpty()) {
                            searchAutocompleteList.clear();
                            searchResultsMap.clear();
                            if (autoCompleteTextView == atvDepartureLocation && latLngCurrentPosition != null) {
                                String currentLocationTitle = "Current position";
                                searchAutocompleteList.add(currentLocationTitle);
                                searchResultsMap.put(currentLocationTitle, latLngCurrentPosition);
                            }
                            for (FuzzySearchResult result : fuzzySearchResponse.getResults()) {
                                String addressString = result.getAddress().getFreeformAddress();
                                searchAutocompleteList.add(addressString);
                                searchResultsMap.put(addressString, result.getPosition());
                            }
                            searchAdapter.clear();
                            searchAdapter.addAll(searchAutocompleteList);
                            searchAdapter.getFilter().filter("");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(NewOfferPage.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initWhereSection() {
        searchAutocompleteList = new ArrayList<>();
        waypoints=new ArrayList<>();
        searchResultsMap = new HashMap<>();
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, searchAutocompleteList);

        setTextWatcherToAutoCompleteField(atvDepartureLocation);
        setTextWatcherToAutoCompleteField(atvDestinationLocation);
    }

    private void setTextWatcherToAutoCompleteField(final AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setAdapter(searchAdapter);
        autoCompleteTextView.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTimerHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    if (s.length() >= AUTOCOMPLETE_SEARCH_THRESHOLD) {
                        searchRunnable = () -> searchAddress(s.toString(), autoCompleteTextView);
                        searchAdapter.clear();
                        searchTimerHandler.postDelayed(searchRunnable, AUTOCOMPLETE_SEARCH_DELAY_MILLIS);
                    }
                }
            }
        });
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if (autoCompleteTextView == atvDepartureLocation) {
                latLngDeparture = searchResultsMap.get(item);
            } else if (autoCompleteTextView == atvDestinationLocation) {
                latLngDestination = searchResultsMap.get(item);
            }else if(autoCompleteTextView == atvWaypointLocation){
                wayPointPosition = searchResultsMap.get(item);
            }
            hideKeyboard(view);
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (in != null) {
            in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (latLngCurrentPosition == null) {
            latLngCurrentPosition = new LatLng(location);
            locationSource.deactivate();
        }
    }

    private abstract class BaseTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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



    private void drawRoute(LatLng start, LatLng stop) {
        LatLng[] waypointList=new LatLng[this.waypointList.size()];
        for(int i=0; i<this.waypointList.size();i++){
            waypointList[i]=this.waypointList.get(i);
        }
        drawRouteWithWayPoints(start, stop, waypointList);
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
                        Boolean initialRouteSelected=false;
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

    public void onNewWaypoint(View view){
        LinearLayout fragmentContainer= findViewById(R.id.newOfferFragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);
        setTextWatcherToAutoCompleteField(atvWaypointLocation);

        if(!waypointsInitiated){
            ListView waypointList= findViewById(R.id.dialogManageWaypointsList);
            waypointList.setAdapter(waypointArrayAdapter);
            waypointsInitiated=true;
        }
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
                listItem = LayoutInflater.from(NewOfferPage.this).inflate(R.layout.item_waypoint,parent,false);

            String currentWaypoint= waypoints.get(position);

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
}
