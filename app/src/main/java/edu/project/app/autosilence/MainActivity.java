package edu.project.app.autosilence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnCompleteListener {

    public static final String ACTIVITY_FROM = "activityFrom";

    //The main RecyclerView to display all registered geofences.
    private RecyclerView locationList;
    //The recyclerview adapter for the locationList.
    private LocationListAdapter listAdapter;
    //Database instance to store the added geofences.
    private LocationDBHelper locationDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //Setting main content
            setContentView(R.layout.activity_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Setting up floating action button
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivityForResult(intent, 777);
                }
            });

            //Requesting the permissions
            if ((Build.VERSION.SDK_INT >= 23)) {
                while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.ACCESS_FINE_LOCATION};
                    ActivityCompat.requestPermissions(this, permissions, 777);
                }
            }

            //Preparing the recyclerview
            locationDBHelper = new LocationDBHelper(this);

            //Adding dummy data
            //insertDummyData();

            locationList = findViewById(R.id.rv_list_locations);
            locationList.setLayoutManager(new LinearLayoutManager(this));
            locationList.setAdapter((listAdapter = new LocationListAdapter(this, locationDBHelper.getAllData())));
        } catch (Exception e) {
            Toast.makeText(this, Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            //Started activity is completed with result code RESULT_OK. If not just ignore.
            if (resultCode == RESULT_OK) {
                String fromActivity = data.getStringExtra(ACTIVITY_FROM);

                //Check if the resulted activity is Maps activity or the ConfirmDialogActivity
                if (fromActivity != null && fromActivity.equals(MapsActivity.FROM_MAPS_ACTIVITY)) {
                    //Retrieve selected latitude and longitude
                    double lat = data.getDoubleExtra(ConfirmDialogActivity.GEO_LATITUDE, 0.0);
                    double lng = data.getDoubleExtra(ConfirmDialogActivity.GEO_LONGITUDE, 0.0);
                    float rad = 100.0f;
                    Intent intent = new Intent(this, ConfirmDialogActivity.class)
                            .putExtra(ConfirmDialogActivity.GEO_LATITUDE, lat)
                            .putExtra(ConfirmDialogActivity.GEO_LONGITUDE, lng)
                            .putExtra(ConfirmDialogActivity.GEO_RADIUS, rad);
                    startActivityForResult(intent, 777);

                } else {
                    //Activity resulted is the ConfirmDialogActivity.
                    Double lat = data.getDoubleExtra(ConfirmDialogActivity.GEO_LATITUDE,0.0);
                    Double lng = data.getDoubleExtra(ConfirmDialogActivity.GEO_LONGITUDE, 0.0);
                    Float rad = data.getFloatExtra(ConfirmDialogActivity.GEO_RADIUS, 0.0f);
                    String name = data.getStringExtra(ConfirmDialogActivity.GEO_NAME);
                    String address = data.getStringExtra(ConfirmDialogActivity.GEO_ADDRESS);

                    //Adding selected location to the geofencing client
                    GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);
                    ArrayList<Geofence> geofenceList = new ArrayList<>();
                    geofenceList.add(new Geofence.Builder()
                            .setCircularRegion(lat, lng, rad)
                            .setRequestId("REQUEST_GEOFENCES")
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .build());
                    GeofencingRequest request = new GeofencingRequest.Builder()
                            .addGeofences(geofenceList)
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build();
                    try {
                        geofencingClient.addGeofences(request, getPendingIntent()).addOnCompleteListener(MainActivity.this);
                        locationDBHelper.insert(name, lat, lng, rad, address);
                    } catch (SecurityException e) {
                        Toast.makeText(this, Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            Toast.makeText(this, Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
        }
    }

    private PendingIntent getPendingIntent() {
        try {
            Intent intent = new Intent(this, GeofenceReceiver.class);
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (Exception e) {
            Toast.makeText(this, Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    public void onComplete(@NonNull Task task) {
        Toast.makeText(this, "Geofences added!", Toast.LENGTH_SHORT).show();
    }

}