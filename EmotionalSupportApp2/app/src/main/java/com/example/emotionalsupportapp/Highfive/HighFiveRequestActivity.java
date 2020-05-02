package com.example.emotionalsupportapp.Highfive;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.emotionalsupportapp.MainActivity;
import com.example.emotionalsupportapp.Member.Registration.LoginActivity;
import com.example.emotionalsupportapp.R;

import com.example.emotionalsupportapp.Service.CancelHighFiveDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighFiveRequestActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent, R.color.primary_dark_material_light};
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private GoogleMap mMap;
    private Location lastLocation;
    private LocationCallback locationCallback;
    private MarkerOptions currentUserLocationMarker;
    private MarkerOptions volunteerLocationMarker;
    private static final int REQUEST_CODE = 101;
    private int interval = 5000;
    private Handler handler;
    private ProgressDialog progressDialog;
    private Boolean userFound;
    private String userID;
    private String volunteerID;
    private  Location dest;
    private Button cancelButton;
    private CancelHighFiveDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_five_request);

        polylines = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        currentUserLocationMarker = new MarkerOptions();
        volunteerLocationMarker = new MarkerOptions();
        userFound = false;
        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            userID = b.getString("EXTRA_USER_ID");
        }else{
            Intent login = new Intent(this,LoginActivity.class);
            startActivity(login);
        }

        dialog = new CancelHighFiveDialog(userID);
        handler = new Handler();
        progressDialog = new ProgressDialog(this);
        lastLocation = new Location("");
        dest = new Location("");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new UpdateLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_high_five);
        mapFragment.getMapAsync(this);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(userID != null){
                        dialog.show(getSupportFragmentManager(),"Cancel High Five Dialog");
                    }
            }
        });
    }


    private class UpdateLocation extends LocationCallback{
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            lastLocation = locationResult.getLastLocation();
            currentUserLocationMarker.position(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()));
            Log.d("Location Updating", lastLocation + "");
            if (userFound) {
                getVolunteerLocation();
            }
        }

    }

    //Gets volunteer's location and updates the route on the maps
    public void getVolunteerLocation(){
        String phpfile = "updateCoord.php";

        StringBuilder fullURL = new StringBuilder();
        fullURL.append(getString(R.string.database_url));
        fullURL.append(phpfile);

        StringRequest updateUserLocation = new StringRequest(Request.Method.POST, fullURL.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response",response + "");
                }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley update Error",error + "");

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> query = new HashMap<>();
                query.put("userID",userID);
                query.put("xCord",Double.toString(lastLocation.getLatitude()));
                query.put("yCord",Double.toString(lastLocation.getLongitude()));
                return query;
            }
        };
        phpfile = "retrieveUpdatedCoord.php";
        fullURL = new StringBuilder();
        fullURL.append(getString(R.string.database_url));
        fullURL.append(phpfile);
        StringRequest getUserLocation = new StringRequest(Request.Method.GET,fullURL.toString(),new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("Response",response);
                try {
                    JSONObject userdata = new JSONObject(response);
                    if(userdata.getString("xCord").equals("Canceled")){
                        AlertDialog.Builder canceled = new AlertDialog.Builder(HighFiveRequestActivity.this);
                        canceled.setTitle("Request Canceled");
                        canceled.setMessage("The user has canceled the high five request");
                        canceled.setCancelable(false);
                        canceled.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                returnToMain();
                            }
                        });
                        canceled.show();
                    }
                    else{
                        dest.setLatitude(Double.parseDouble(userdata.getString("xCord")));
                        dest.setLongitude(Double.parseDouble(userdata.getString("yCord")));
                    }
                } catch (JSONException e) {
                    Log.e("Retrive Error JSON",e + "");
                }

            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error Get Cord",error + "");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                HashMap<String,String> query = new HashMap<>();
                query.put("userID",volunteerID);
                return query;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(updateUserLocation);
        requestQueue.add(getUserLocation);
        updateDistance();
    }
    public void returnToMain(){
        stopRepeatingTask();
        Intent main = new Intent(this,HighFiveActivity.class);
        main.putExtra("EXTRA_USER_ID",userID);
        startActivity(main);
    }

    @Override
    protected void onStart() {
        startLocationUpdates();

        if(!userFound){
            startRepeatingTask();
        }else{
            updateDistance();
        }
        super.onStart();
    }

    public void startRepeatingTask(){
        progressDialog.setMessage("Finding a high five...");
        progressDialog.show();
        databaseChecker.run();
    }

    public void stopRepeatingTask(){
        handler.removeCallbacks(databaseChecker);
        progressDialog.dismiss();

    }

    @Override
    protected void onResume() {
        startLocationUpdates();
        if(!userFound){
            startRepeatingTask();
        }else{
            updateDistance();
        }
        super.onResume();
    }

    Runnable databaseChecker  = new Runnable() {
        @Override
        public void run() {
            try{
                if(!userFound){
                    matchedUser();
                    //
                }else{
                    updateDistance();
                }

            }finally {
                if (userFound) {//dont stop until get matched user info
                    stopRepeatingTask();
                    //pull
                } else {
                    handler.postDelayed(databaseChecker, interval);
                }
            }
        }
    };

    //Update the marker on the map and request a new route between the points
    private void updateDistance(){

        float distance = lastLocation.distanceTo(dest);
        if(distance<75){
            Intent ratings = new Intent(this,HighFiveRatingActivity.class);
            ratings.putExtra("EXTRA_USER_ID",userID);
            ratings.putExtra("EXTRA_VOLUNTEER_ID",volunteerID);
            stopLocationUpdates();
            startActivity(ratings);
        }
        LatLng origin = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        LatLng destination = new LatLng(dest.getLatitude(),dest.getLongitude());

        mMap.addMarker(currentUserLocationMarker);
        mMap.addMarker(volunteerLocationMarker);
        Log.e("Location Found",dest + " " + origin);
        requestDirections(origin, destination);

    }

    //Check the match user table for to see if user was matched
    private void matchedUser() {

        String phpfile = "retrieveMatchedUser.php";

        StringBuilder fullURL = new StringBuilder();
        fullURL.append(getString(R.string.database_url));
        fullURL.append(phpfile);

        StringRequest jsonArrayRequest = new StringRequest(Request.Method.POST, fullURL.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.equals("No such user exist in the MatchedUsers table")){
                    try {

                        JSONObject userdata = new JSONObject(response);
                        Log.e("Matched Table",userdata + "");
                        if(!userdata.getString("userID1").equals(userID)){
                            volunteerID = userdata.getString("userID1");
                            dest.setLatitude(Double.parseDouble(userdata.getString("xCord1")));
                            dest.setLongitude(Double.parseDouble(userdata.getString("yCord1")));
                        }else{
                            volunteerID = userdata.getString("userID2");
                            dest.setLatitude(Double.parseDouble(userdata.getString("xCord2")));
                            dest.setLongitude(Double.parseDouble(userdata.getString("yCord2")));
                        }
                        volunteerLocationMarker.position(new LatLng(dest.getLatitude(),dest.getLongitude()));
                        volunteerLocationMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        userFound = true;

                    } catch (JSONException e) {
                        Log.e("JSON Exception",e + "");
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error",error + "");

            }
        }){

            @Override
            protected Map<String, String> getParams(){
                HashMap<String,String> query = new HashMap<>();
                query.put("userID",userID);
                return query;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }


    //Add dialog for when user tries to go back asking if they want to
    @Override
    public void onBackPressed() {
        dialog.show(getSupportFragmentManager(),"Cancel High Five Dialog");
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdates();
        updateLocationUI();

    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
                if(lastLocation == null){
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {

                                    if (location != null) {
                                        lastLocation = location;
                                        mMap.setMyLocationEnabled(true);
                                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),12));
                                        currentUserLocationMarker.position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())).title("You are Here");
                                        mMap.addMarker(currentUserLocationMarker);

                                    }else{
                                        startLocationUpdates();
                                    }
                                }
                            });
                }else{
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),12));
                    currentUserLocationMarker.position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())).title("You are Here");
                    mMap.addMarker(currentUserLocationMarker);
                }


        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //Makes a request using the origin and the dest to create a route on the map
    private void requestDirections(LatLng origin, LatLng dest) {
        Routing routing = new Routing.Builder()
                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(origin, dest)
                .build();
        routing.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationUpdates();
                }
                break;
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }
    }

    @Override
    public void onRoutingCancelled() {

    }


    @Override
    protected void onPause() {
        stopRepeatingTask();
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopRepeatingTask();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        stopRepeatingTask();
        super.onDestroy();
    }

}

