package de.uni_ulm.uberuniulm;

import android.content.Intent;
import android.location.Location;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TextBalloonViewAdapter;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.MyOffersFragment;
import de.uni_ulm.uberuniulm.model.ParkingSpot;
import de.uni_ulm.uberuniulm.model.ParkingSpots;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import android.view.Window;
import android.view.WindowManager;


public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, TomtomMapCallback.OnMapLongClickListener {
    private TomtomMap tomtomMap;
    private SearchApi searchApi;
    private Fragment mapFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Boolean parkingMode=false;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager=getSupportFragmentManager();


        Window window = getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(Color.BLACK);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (parkingMode) {
            LinearLayout mapFragment=(LinearLayout) findViewById(R.id.mainPageMapContainer);
            mapFragment.setVisibility(View.INVISIBLE);
            parkingMode=true;
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id==R.id.action_parking){
            Log.d("I'm in", "SETTINGS MENU");
            LinearLayout mapFragment=(LinearLayout) findViewById(R.id.mainPageMapContainer);
            mapFragment.setVisibility(View.VISIBLE);
            parkingMode=true;

            initTomTomServices();
            initUIViews();
            setupUIViewListeners();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            LinearLayout mapFragment=(LinearLayout) findViewById(R.id.mainPageMapContainer);
            mapFragment.setVisibility(View.INVISIBLE);
            // Handle the camera action
        } else if (id == R.id.booked) {

        } else if (id == R.id.offers) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.mainPageContentContainer, new MyOffersFragment());
            fragmentTransaction.commit();

        } else if (id == R.id.settings) {

        } else if (id == R.id.logout) {

        } else if (id == R.id.profile) {

        } else if (id == R.id.ratings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(@NonNull TomtomMap map) {
        ParkingSpots parkings=new ParkingSpots();
        tomtomMap = map;
        tomtomMap.setMyLocationEnabled(true);
        tomtomMap.addOnMapLongClickListener(this);
        tomtomMap.getMarkerSettings().setMarkersClustering(true);
        tomtomMap.removeMarkers();
        tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(new TypedBallonViewAdapter());
        ArrayList<ParkingSpot> parkingSpots=parkings.getAll();
        for(ParkingSpot spot: parkingSpots){
            SimpleMarkerBalloon balloon = new SimpleMarkerBalloon(spot.getName());
            MarkerBuilder markerBuilder = new MarkerBuilder(spot.getPosition())
                    .markerBalloon(balloon);

            Marker m = tomtomMap.addMarker(markerBuilder);
        }
        tomtomMap.getUiSettings().setCameraPosition(
                CameraPosition
                        .builder(parkings.getSpotByName("P26").getPosition())
                        .zoom(14)
                        .build()
        );
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {}

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
        Toast.makeText(MainPage.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

    public void onMyRidesNewRideBttn(View view){
        Intent intent = new Intent(MainPage.this, NewOfferPage.class);
        startActivity(intent);
    }
}
