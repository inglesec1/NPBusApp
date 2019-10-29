//changed SDK build version to 22 (right click project in package explorere -> sdk version)
//changed build tools from 24.0.22
//permissions new for version 24 android -> must request permission

//firebase modifications:
//classpath + dependency; in gradles
//init + button + import; method in this java code

package bustracker.project.inglese.npbustracker;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener{
    private Button trackerButton;
    private Firebase myFirebaseRef;
    final int REQUEST_GPS = 1;
    private GoogleApiClient mGoogleApiClient;

    //keep array in sync with titleArray in AlertsActivity.class from Client App
    private LatLng[] destinationArray = new LatLng[]{ new LatLng(41.74765438, -74.08266664),  new LatLng(41.7408581, -74.06046331), new LatLng(41.74153456, -74.06888813),
            new LatLng(41.74470862, -74.0698269), new LatLng(41.74078105, -74.08158101), new LatLng(41.73770687, -74.08432759),
            new LatLng(41.73896979, -74.08639021), new LatLng(41.73764082, -74.08785872), new LatLng(41.74254924, -74.08896044)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initButton();
        initGoogleAPI();
        initFirebase();
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS);
    }


    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnected(Bundle bundle) throws SecurityException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentTime = sdf.format(new Date());
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


        if (mLastLocation != null) {
            Firebase databaseRef = myFirebaseRef.child("coordinateTable").child("coordinateDatabase");
            Firebase coordinateRef = myFirebaseRef.child("coordinateTable").child("coordinateSet");
            double mLatitude = mLastLocation.getLatitude();
            double mLongitude = mLastLocation.getLongitude();

            //update 'LastDestination' Field
            for(int i = 0; i < destinationArray.length; i++) {
                if ((mLatitude < destinationArray[i].latitude + .0005 && mLatitude > destinationArray[i].longitude - .0005)
                        && (mLongitude < destinationArray[i].longitude + .0005 && mLongitude > destinationArray[i].latitude - .0005)) {
                    databaseRef.child(currentTime).child("LastDestination").setValue(i);
                    coordinateRef.child("LastDestination").setValue(i);
                }
                else if ( i == destinationArray.length-1){
                    databaseRef.child(currentTime).child("LastDestination").setValue(-1);
                }
            }


            databaseRef.child(currentTime).child("Long").setValue(mLongitude);
            databaseRef.child(currentTime).child("Lat").setValue(mLatitude);
            coordinateRef.child("Long").setValue(mLongitude);
            coordinateRef.child("Lat").setValue(mLatitude);
        }
        onStop();
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }
    @Override
    public void onConnectionFailed(com.google.android.gms.common.ConnectionResult connectionResult) {
    }
    /**
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GPS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                    super.onStart();
                }
                else {
                    // Disable functionality that depends on this permission.
                }
                return;
            }
        }
    }
    */




    private void initButton() {
        trackerButton = (Button) findViewById(R.id.trackerButton);
        trackerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGoogleApiClient.connect();
            }
        });
    }
    private void initGoogleAPI() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    private void initFirebase(){
        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://np-bus-tracker.firebaseio.com/");
    }
}