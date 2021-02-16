package com.thanasis.firespeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpeedometerActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "SpeedometerActivity";

    SwitchCompat sw_metric;
    TextView tv_speed;
    TextView tv_limit_speed;

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef, limitRef;
    FirebaseUser currentUser;

    final String[] limit_speed = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer);

        sw_metric = findViewById(R.id.sw_metric);
        tv_speed = findViewById(R.id.tv_speed);
        tv_limit_speed = findViewById(R.id.limit_speed);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        // Check if user is logged in
        if (currentUser==null) {
            startActivity(new Intent(SpeedometerActivity.this, LoginActivity.class));
            finish();
        }

        myRef = database.getReference("users");
        limitRef = database.getReference("limit");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String limit = dataSnapshot.getValue().toString();

                limit_speed[0] = limit;
                tv_limit_speed.setText(limit_speed[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SpeedometerActivity.this, "Error with the DB, Event Canceled.", Toast.LENGTH_SHORT).show();
            }
        };
        limitRef.addValueEventListener(postListener);

        // Check for gps permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            // start the program if permission granted
            startSpeedometer();
        }
        this.updateSpeed(null);

        sw_metric.setOnCheckedChangeListener((buttonView, isChecked) -> updateSpeed(null));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.ic_home:
                    Bundle bundleHome = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_left, R.anim.slide_out_right).toBundle();
                    Intent intentHome = new Intent(SpeedometerActivity.this, MainActivity.class);
                    startActivity(intentHome, bundleHome);
                    break;
                case R.id.ic_speeder:
                    break;
                case R.id.ic_list:
                    Bundle bundleList = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
                    Intent intentList = new Intent(SpeedometerActivity.this, ListActivity.class);
                    startActivity(intentList, bundleList);
                    break;
                case R.id.ic_map:
                    Bundle bundleMap = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_out_right, R.anim.slide_in_left).toBundle();
                    Intent intentMap = new Intent(SpeedometerActivity.this, MapsActivity.class);
                    startActivity(intentMap, bundleMap);
                    break;
            }

            return false;
        });
    }

    @SuppressLint("MissingPermission")
    private void startSpeedometer() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Toast.makeText(this, "Reading Speed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if(location != null) {
            location.setUserMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%.2f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");
        if(useMetricUnits()){
            strCurrentSpeed = strCurrentSpeed + " Km/h";
        }else {
            strCurrentSpeed = strCurrentSpeed + " Miles/h";
        }

        if(limit_speed[0] != null && nCurrentSpeed > Float.parseFloat(limit_speed[0]))  {
            String timestamp = new SimpleDateFormat("yyMMddHHmmssZ").format(new Date());
            String key = myRef.push().getKey();
            Map<String, Object> value = new HashMap<>();
            value.put("name", currentUser.getDisplayName());
            value.put("Speed", strCurrentSpeed);
            value.put("Lat", location.getLatitude());
            value.put("Lng", location.getLongitude());
            value.put("timestamp", timestamp);
            myRef.child(currentUser.getUid()).child("Points").child(key).setValue(value);
            tv_speed.setTextColor(Color.RED);
        } else if(limit_speed == null) {
            Toast.makeText(this, "Database not working properly. Please check your connection!", Toast.LENGTH_LONG).show();
            tv_speed.setTextColor(Color.BLACK);
        }
        else {
            tv_speed.setTextColor(Color.BLACK);
        }

        if(this.useMetricUnits()) {
            tv_speed.setText(strCurrentSpeed);
        }else {
            tv_speed.setText(strCurrentSpeed);
        }
    }

    private boolean useMetricUnits() {
        return sw_metric.isChecked();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1000) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startSpeedometer();
            }else {
                finish();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
