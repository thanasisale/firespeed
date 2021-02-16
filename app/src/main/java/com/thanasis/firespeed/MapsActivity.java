package com.thanasis.firespeed;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARCE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int DEFAULT_ZOOM = 15;

    private Boolean mLocationPermissionsGranted = false;

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser currentUser;
    private String userID;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    ArrayList<SpeedPoint> pointsList = new ArrayList<>();
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        currentUser = mAuth.getCurrentUser();
        // Check if user is logged in
        if (currentUser==null) {
            startActivity(new Intent(MapsActivity.this, LoginActivity.class));
            finish();
        }
        userID = currentUser.getUid();
//        myRef = database.getReference().child("users").child(userID).child("Points");
        myRef = database.getReference().child("users");

        ValueEventListener pointListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pointsList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    DataSnapshot userPoints = (DataSnapshot) ds.child("Points");
                    for(DataSnapshot us : userPoints.getChildren()){
                        SpeedPoint point = new SpeedPoint();
                        point.setSpeed(us.child("Speed").getValue(String.class));
                        point.setTimestamp(us.child("timestamp").getValue(String.class));
                        point.setLat(us.child("Lat").getValue(double.class));
                        point.setLng(us.child("Lng").getValue(double.class));
                        point.setName(us.child("name").getValue(String.class));
                        pointsList.add(point);
                    }

                }
                getLocationPermission();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Database is offline. Try later.", Toast.LENGTH_LONG).show();
            }
        };
        myRef.addValueEventListener(pointListener);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.ic_home:
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_left, R.anim.slide_out_right).toBundle();
                    Intent intentHome = new Intent(MapsActivity.this, MainActivity.class);
                    startActivity(intentHome, bundle);
                    break;
                case R.id.ic_speeder:
                    Bundle bundleSpeed = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_left, R.anim.slide_out_right).toBundle();
                    Intent intentSpeed = new Intent(MapsActivity.this, SpeedometerActivity.class);
                    startActivity(intentSpeed, bundleSpeed);
                    break;
                case R.id.ic_list:
                    Bundle bundleList = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_left, R.anim.slide_out_right).toBundle();
                    Intent intentList = new Intent(MapsActivity.this, ListActivity.class);
                    startActivity(intentList, bundleList);
                    break;
                case R.id.ic_map:
                    break;
            }

            return false;
        });
    }

    private void getDeviceLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if(mLocationPermissionsGranted){
                Task location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            // Location Found
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }else {
                            Toast.makeText(MapsActivity.this, "Unable to find your location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            // Security exception handler
        }

    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARCE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 ){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if(mLocationPermissionsGranted){
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // mMap.getUiSettings().setMyLocationButtonEnabled(false); // Disables the default FindMe button

            if(!pointsList.isEmpty()){
                mMap.clear();

                for(int i = 0; i < pointsList.size(); i++ ){
                    createMarker(pointsList.get(i).getLat(), pointsList.get(i).getLng(), pointsList.get(i));
                }
            }
        }

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    protected Marker createMarker(double latitude, double longitude, SpeedPoint point) {

        String snippet = "Speed: " + point.getSpeed() + ", " +
                "Timestamp: " + point.getTimestamp();
        String title = point.getName();

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(title)
                .snippet(snippet));
    }
}
