package com.thanasis.firespeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser currentUser;
    private ProgressBar progressBar;
    ListView list;
    private String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        currentUser = mAuth.getCurrentUser();
        // Check if user is logged in
        if (currentUser==null) {
            startActivity(new Intent(ListActivity.this, LoginActivity.class));
            finish();
        }
        userID = currentUser.getUid();
        myRef = database.getReference().child("users").child(userID).child("Points");

        list = findViewById(R.id.pointsList);
        ArrayList<SpeedPoint> pointsList = new ArrayList<>();

        ValueEventListener pointListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pointsList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    SpeedPoint point = new SpeedPoint();
                    point.setSpeed(ds.child("Speed").getValue(String.class));
                    point.setTimestamp(ds.child("timestamp").getValue(String.class));
                    point.setLat(ds.child("Lat").getValue(double.class));
                    point.setLng(ds.child("Lng").getValue(double.class));
                    pointsList.add(point);

                    SpeedPointListAdapter adapter = new SpeedPointListAdapter(ListActivity.this, R.layout.adapter_view_layout, pointsList);
                    list.setAdapter(adapter);
                }
                long count = dataSnapshot.getChildrenCount();
                Toast.makeText(ListActivity.this, "Παραβάσεις Χρήστη: "+ count, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ListActivity.this, "Database is offline. Try later.", Toast.LENGTH_LONG).show();
            }
        };
        myRef.addValueEventListener(pointListener);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.ic_home:
                    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_left, R.anim.slide_out_right).toBundle();
                    Intent intentHome = new Intent(ListActivity.this, MainActivity.class);
                    startActivity(intentHome, bundle);
                    break;
                case R.id.ic_speeder:
                    Bundle bundleList = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_left, R.anim.slide_out_right).toBundle();
                    Intent intentList = new Intent(ListActivity.this, SpeedometerActivity.class);
                    startActivity(intentList, bundleList);
                    break;
                case R.id.ic_list:
                    break;
                case R.id.ic_map:
                    Bundle bundleMap = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
                    Intent intentMap = new Intent(ListActivity.this, MapsActivity.class);
                    startActivity(intentMap, bundleMap);
                    break;
            }

            return false;
        });

    }
}
