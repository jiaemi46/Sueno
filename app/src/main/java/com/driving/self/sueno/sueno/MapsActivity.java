package com.driving.self.sueno.sueno;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
//        GoogleMap.OnMarkerClickListener,
//        GoogleMap.OnMarkerDragListener
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    int PROXIMITY_RADIUS = 1000;
    double latitude, longitude;
    double end_latitude, end_longitude;
    public List<LatLng> markers = new ArrayList<LatLng>();
    private List<LatLng> drivingRoute; //ToDelete.
    GroundOverlay overlayMap;
    GroundOverlay overlayTemp;

    private static MapsActivity mapsActivity;
    private MapAnimator mapAnimator = new MapAnimator();

    private static final int TRANSPARENCY_MAX = 100;
    private static final LatLng NEWARK = new LatLng(1.375895, 103.8358343);
    private static final LatLng NEAR_NEWARK =
            new LatLng(NEWARK.latitude - 0.001, NEWARK.longitude - 0.025);
    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();
//    private com.example.user.kuruma.GroundOverlay mGroundOverlay;
//    private com.example.user.kuruma.GroundOverlay mGroundOverlayRotated;
    private SeekBar mTransparencyBar;
    private int mCurrentEntry = 0;

    public float oldBearing = 0;
    public float newBearing = 0;


    double latest_latitude, latest_longitude; //To delete
    LatLng latestLatLng;

    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private ImageView geo_autocomplete_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        geo_autocomplete_clear = findViewById(R.id.geo_autocomplete_clear);
        geo_autocomplete = findViewById(R.id.geo_autocomplete);
        geo_autocomplete.setThreshold(THRESHOLD);
        geo_autocomplete.setAdapter(new GeoAutoCompleteAdapter(this));

        geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                geo_autocomplete.setText(result.getAddress());
            }
        });

        geo_autocomplete.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0)
                {
                    geo_autocomplete_clear.setVisibility(View.VISIBLE);
                }
                else
                {
                    geo_autocomplete_clear.setVisibility(View.GONE);
                }
            }
        });

        geo_autocomplete_clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // ToDo Auto-generated method stub
                geo_autocomplete.setText("");
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public static MapsActivity getInstance(){
        if (mapsActivity == null) mapsActivity = new MapsActivity();
        return mapsActivity;
    }

    //Moving a custom marker along with the polyline
    public void movingMarker(LatLng newLatLng){
//        GroundOverlayOptions overlayMap = new GroundOverlayOptions()
//                .image(BitmapDescriptorFactory.fromResource(R.drawable.mazda_50px))
//                .position(newLatLng, 8600f, 6500f);
//        if (overlayMap != null){
//            overlayMap.remove();
//        }
        LatLng oldLatLng = newLatLng;
        //float mazdaBearing = 0;

        if (overlayMap != null){
            overlayTemp = overlayMap;
            oldLatLng = overlayTemp.getPosition();
        }
        oldBearing = newBearing;

        //Calculate bearing to face next
        Double angle = Math.atan2((oldLatLng.longitude - newLatLng.longitude),(oldLatLng.latitude - newLatLng.latitude));
        angle = Math.toDegrees(angle);

        if (Math.abs(oldBearing - angle) > 20){
            newBearing = Float.valueOf(String.valueOf(angle));
        }


//        Float angle = Math.atan2((userstartPoint.getX() - userendPoint.getX()), userstartPoint.getY() - userendPoint.getY());
//        angle = Math.toDegrees(angle);
//        map.setRotationAngle(angle);

        //Log.i("Position", "overlayTemp.getPosition() value :  " + oldLatLng);
        overlayMap = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.mazda_150px))
                .bearing(newBearing)
                .zIndex(1)
                .position(newLatLng, 250f, 250f)); //LatLng of marker and Height x Width of marker
        overlayTemp.remove();
        //overlayMap.remove();

    }



    //Start the auto-driving animation
    private void startAnimation(){
        if(mMap != null) {
//            MapAnimator.getInstance().animateRoute(mMap, markers);
//            MapAnimator.getInstance().animateRoute(mMap, markers);
            mapAnimator.animateRoute(mMap, markers);
            //Start moving custom market here too
            movingMarker(latestLatLng);
        } else {
            Toast.makeText(getApplicationContext(), "Map not ready", Toast.LENGTH_LONG).show();
        }
    }

    //Stop the auto-driving animation (When reach destination or stop abruptly)
    private void stopAnimation(){
        if(mMap != null) {
            mapAnimator.stopAnimator();
        } else {
            Toast.makeText(getApplicationContext(), "Map not ready", Toast.LENGTH_LONG).show();
        }
    }

    //Pause the auto-driving animation (When car momentarily pauses, Eg: Traffic light)
    //Slightly different usage scenarios as stopAnimation()
    private void pauseAnimation(){
        if(mMap != null) {
            mapAnimator.pauseAnimator();
        } else {
            Toast.makeText(getApplicationContext(), "Map not ready", Toast.LENGTH_LONG).show();
        }
    }


    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
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

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                //mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            //mMap.setMyLocationEnabled(true);
        }
//        mMap.setOnMarkerDragListener(this);
//        mMap.setOnMarkerClickListener(this);

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NEWARK, 11));
//        mImages.clear();
//        mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.mazda_50px));

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onClick(View v)
    {
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();


        switch(v.getId()) {
            case R.id.btn_SearchAddr: {
                mMap.clear();
                EditText tf_location = findViewById(R.id.geo_autocomplete);
                String location = tf_location.getText().toString();
                List<Address> addressList = null;
                MarkerOptions markerOptions = new MarkerOptions();
                Log.d("location = ", location);

                if (!location.equals("")) {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 5);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addressList != null) {
                        for (int i = 0; i < addressList.size(); i++) {
                            Address myAddress = addressList.get(i);
                            LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                            markerOptions.position(latLng);
                            mMap.addMarker(markerOptions);
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                            end_latitude = myAddress.getLatitude();
                            end_longitude =  myAddress.getLongitude();
                        }
                    }

                }
            }
            break;

            case R.id.btn_nearbyRestaurants:
                mMap.clear();
                dataTransfer = new Object[2];
                String restaurant = "restaurant";
                String url = getUrl(latitude, longitude, restaurant);
                getNearbyPlacesData = new GetNearbyPlacesData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Restaurants", Toast.LENGTH_LONG).show();
                break;

            case R.id.btn_CancelAnimation:
                stopAnimation();
                break;

            case R.id.btn_autoDrive:
                //Auto Drive (Press this button only after enter postalCode -> Click 'Search' -> Click 'To' button -> Click Auto Drive .. FOR NOW!!)
                final LatLng START_POINT = markers.get(0);
                final LatLng END_POINT = markers.get(markers.size()-1);

                latestLatLng = START_POINT;

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(START_POINT); //Later store values of latlong of start & end pts from arraylist
                builder.include(END_POINT);
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);

                mMap.moveCamera(cu);
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 5000, null); //Middle number 5000 = pan-out speed delay; higher=slower;
                startAnimation();

                break;

            case R.id.btn_toDestination:
                mMap.clear();
                dataTransfer = new Object[3];
                url = getDirectionsUrl();
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);
                getDirectionsData.execute(dataTransfer);

                //mMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(end_latitude, end_longitude));
                markerOptions.title("Destination");
                float results[] = new float[10];
                Location.distanceBetween(latitude, longitude, end_latitude, end_longitude, results);
                markerOptions.snippet("Distance = " + results[0] + " metres");
                mMap.addMarker(markerOptions);
                Toast.makeText(MapsActivity.this, "~Hoseibo~", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyCAcfy-02UHSu2F6WeQ1rhQhkCr51eBL9g");

        return googleDirectionsUrl.toString();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBj-cnmMUY21M0vnIKz0k3tD3bRdyZea-Y");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.draggable(true);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        Toast.makeText(MapsActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the contacts-related task if needed
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        //Disable googlemaps location enable bluedot
                        //mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request.
            // Add here other case statements according to your requirement.
        }
    }


            public class GetDirectionsData extends AsyncTask<Object,String,String> {

                String url;
                String googleDirectionsData;
                String duration, distance;
                LatLng latLng;

                @Override
                protected String doInBackground(Object... objects) {
                    mMap = (GoogleMap)objects[0];
                    url = (String)objects[1];
                    latLng = (LatLng)objects[2];

                    DownloadUrl downloadUrl = new DownloadUrl();
                    try {
                        googleDirectionsData = downloadUrl.readUrl(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return googleDirectionsData;
                }

                @Override
                protected void onPostExecute(String s) {
                    Log.i("String s", "onPostExecute String S:  " + s);
                    String[] directionsList;
                    DataParser parser = new DataParser();
                    directionsList = parser.parseDirections(s);
                    displayDirection(directionsList);
                }

                //On post execute method
                public void displayDirection(String[] directionsList)
                {
                    int count = directionsList.length;
                    for(int i = 0;i<count;i++)
                    {
                        PolylineOptions options = new PolylineOptions();
                        options.color(Color.BLUE);
                        options.width(5);
                        options.addAll(PolyUtil.decode(directionsList[i]));

                        //1st Regex: Clean all alphabets and special characters except (.) and (,)
                        String str = options.getPoints().toString().replaceAll("[^\\d.,]", "");
                        //Log.i("New", "Now, i wanna get ALL curves in the polyline!!  :: " + str);

                        //2nd Clean-up: Put 1st regex into a list and only take 1st two values for latlng
                        ArrayList latlngList = new ArrayList(Arrays.asList(str.split(",")));
//                        Log.i("New", "Splitted string array's contents :: " + latlngList);
//                        Log.i("New", "Splitted string array's SIZE :: " + latlngList.size());

                        //Loop through each iteration to get the entire curve of the polyline's coordinates
                        for (int j=0; j<latlngList.size(); j+=2){
                            double listLat = Double.parseDouble(latlngList.get(j).toString());
                            double listLong = Double.parseDouble(latlngList.get(j+1).toString());
                            LatLng listLatLong = new LatLng(listLat, listLong);
                            markers.add(listLatLong);
//                            mMap.addPolyline(options);
                        }
                        mMap.addPolyline(options);
                    }
                }
            }

            // animator class bookmark start

    public class MapAnimator {
        //private MapAnimator mapAnimator = new MapAnimator();
        //private static MapAnimator mapAnimator;
        private AnimatorSet RunAnimatorSet;

        private Polyline backgroundPolyline;
        private Polyline foregroundPolyline;
        private PolylineOptions optionsForeground;
        int counter = 0;

        private MapAnimator(){

        }

//    public static MapAnimator getInstance(){
//        if(mapAnimator == null) mapAnimator = new MapAnimator();
//        return mapAnimator;
//    }

        public void stopAnimator(){
            RunAnimatorSet.removeAllListeners();
            RunAnimatorSet.cancel();
        }

        public void pauseAnimator(){
            RunAnimatorSet.removeAllListeners();
            RunAnimatorSet.cancel();
        }


        public void animateRoute(GoogleMap googleMap, List<LatLng> drivingRoute) {
            if (RunAnimatorSet == null){
                RunAnimatorSet = new AnimatorSet();
            } else {
                RunAnimatorSet.removeAllListeners();
                RunAnimatorSet.end();
                RunAnimatorSet.cancel();

                RunAnimatorSet = new AnimatorSet();
            }

            //Reset the polylines
            if (foregroundPolyline != null) foregroundPolyline.remove();
            if (backgroundPolyline != null) backgroundPolyline.remove();

            PolylineOptions optionsBackground = new PolylineOptions().add(drivingRoute.get(0)).color(Color.BLUE).width(5);
            backgroundPolyline = googleMap.addPolyline(optionsBackground);

            optionsForeground = new PolylineOptions().add(drivingRoute.get(0)).color(Color.BLUE).width(5);
            foregroundPolyline = googleMap.addPolyline(optionsForeground);

            final ValueAnimator percentageCompletion = ValueAnimator.ofInt(0, 100);
            percentageCompletion.setDuration(2000);
            percentageCompletion.setInterpolator(new DecelerateInterpolator());
            percentageCompletion.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    List<LatLng> foregroundPoints = backgroundPolyline.getPoints();

                    int percentageValue = (int) animation.getAnimatedValue();
                    int pointcount = foregroundPoints.size();
                    int countTobeRemoved = (int) (pointcount * (percentageValue / 100.0f));
                    List<LatLng> subListTobeRemoved = foregroundPoints.subList(0, countTobeRemoved);
                    subListTobeRemoved.clear();

                    foregroundPolyline.setPoints(foregroundPoints);
                }
            });
            percentageCompletion.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    foregroundPolyline.setColor(Color.BLUE);
                    foregroundPolyline.setPoints(backgroundPolyline.getPoints());
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });


            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLUE, Color.BLUE);
            colorAnimation.setInterpolator(new AccelerateInterpolator());
            colorAnimation.setDuration(1200); // milliseconds

            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    foregroundPolyline.setColor((int) animator.getAnimatedValue());
                }

            });

            ObjectAnimator foregroundRouteAnimator = ObjectAnimator.ofObject(this, "routeIncreaseForward", new RouteEvaluator(), drivingRoute.toArray());
            foregroundRouteAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            foregroundRouteAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    backgroundPolyline.setPoints(foregroundPolyline.getPoints());
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            //------Value to adjust the speed of the polyline
            foregroundRouteAnimator.setDuration(16000);
//        foregroundRouteAnimator.start();

            RunAnimatorSet.playSequentially(foregroundRouteAnimator,
                    percentageCompletion);
            RunAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //secondLoopRunAnimSet.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            RunAnimatorSet.start();
        }

        /**
         * This will be invoked by the ObjectAnimator multiple times. Mostly every 16ms.
         **/
        public void setRouteIncreaseForward(LatLng endLatLng) {
            List<LatLng> foregroundPoints = foregroundPolyline.getPoints();
            foregroundPoints.add(endLatLng);
            foregroundPolyline.setPoints(foregroundPoints);

            movingMarker(endLatLng);
            //MapsActivity.getInstance().movingMarker(endLatLng);
        }
    }

    // animator class bookmark end

    // Animator async class bookmark start

    //AsyncTask for quick stops: Emergency brake / sudden stop, etc
    public class animatorAsync extends AsyncTask<LatLng, Integer, Integer>{

        //TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
        LatLng asyncLatLng;

        @Override
        protected void onPreExecute(){
            //tvCurrentSpd.setText("onPre-> Current Speed: " + currentKmph);
        }

        @Override
        protected Integer doInBackground(LatLng... params){
            try {
                //MapsActivity.getInstance().movingMarker(asyncLatLng);
                movingMarker(asyncLatLng);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... update){
            //tvCurrentSpd.setText("onProgUpdate -> Current Speed: " + currentKmph);
        }

//        @Override
//        protected void onPostExecute(String s){
//            TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
//            tvCurrentSpd.setText("Current Speed: " + currentKmph);
//        }
    }


    // Animator async class bookmark end


        }


