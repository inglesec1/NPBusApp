//firebase modifications:
//classpath + dependency(; in gradles
//init + updateData + import; method in this java code
//import packages

package bustracker.project.inglese.npbuslocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class MainActivity extends Activity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //firebase init
        Firebase.setAndroidContext(this);
        Firebase myFirebaseRef = new Firebase("https://np-bus-tracker.firebaseio.com/");
        attachListeners(myFirebaseRef);
    }




    private LatLng mainAndProspect = new LatLng(41.74765438, -74.08266664);
    private String mainAndProspectS = "1: Main & Prospect St";
    private LatLng UPL = new LatLng(41.7408581, -74.06046331);              //may be inaccurate
    private String UPLS = "C: Ulster-Poughkeepsie Link";
    private LatLng shopritePlaza = new LatLng(41.74153456, -74.06888813);
    private String shopritePlazaS = "2: Shoprite Plaza";
    private LatLng stopandshopPlaza = new LatLng(41.74470862, -74.0698269);
    private String stopandshopPlazaS = "3: Shop & Stop Plaza";
    private LatLng SunyHabCircle = new LatLng(41.74078105, -74.08158101);
    private String SunyHabCircleS = "4: SUNY HAB Circle at Rt.32";
    private LatLng SunyHuguenot = new LatLng(41.73770687, -74.08432759);
    private String SunyHuguenotS = "5: SUNY Huguenot & South Side Rd.";
    private LatLng SunySouthsideLoop = new LatLng(41.73896979, -74.08639021);
    private String SunySouthsideLoopS = "6: SUNY Southside Loop & South Rd.";
    private LatLng SunySouthRd = new LatLng(41.73764082, -74.08785872);
    private String SunySouthRdS = "7: SUNY South Rd. & Hawk Dr.";
    private LatLng SoutsideAve = new LatLng(41.74254924, -74.08896044);
    private String SouthsideAveS = "8: Southside Ave. at Rt.208";


    private GoogleMap map;
    private LatLng BUS = mainAndProspect;
    private Marker bus;
    boolean updatedLat = false;
    boolean updatedLong = false;
    public void onMapReady(GoogleMap map) {
        bus = map.addMarker(new MarkerOptions().position(BUS).title("BUS LOCATION").draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        bus.showInfoWindow();

        BitmapDescriptor markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        float markerOpacity = 0.3f;
        map.addMarker(new MarkerOptions().position(mainAndProspect).title(mainAndProspectS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(UPL).title(UPLS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(shopritePlaza).title(shopritePlazaS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(stopandshopPlaza).title(stopandshopPlazaS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(SunyHabCircle).title(SunyHabCircleS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(SunyHuguenot).title(SunyHuguenotS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(SunySouthsideLoop).title(SunySouthsideLoopS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(SunySouthRd).title(SunySouthRdS).draggable(false).icon(markerColor).alpha(markerOpacity));
        map.addMarker(new MarkerOptions().position(SoutsideAve).title(SouthsideAveS).draggable(false).icon(markerColor).alpha(markerOpacity));

        this.map = map;
        updateMap();
    }
    private void updateMap() {
        LatLng updatedPosition = bus.getPosition();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(updatedPosition, (float)15.5));
    }

    private void updateBusMarker() {
        if(updatedLat && updatedLong) {
            updatedLat = false;
            updatedLong = false;
            bus.setPosition(BUS);
            updateMap();
        }
    }
    //ERROR --> LAT & LONG WILL BE OF TYPE 'LONG' IF NOT STORED AS DECIMALS IN FIREBASE
    //WONT be an issue, but account for it--> add +.00001 to .00000 values
    private void updateBusLat(double latitude) {
        updatedLat = true;
        BUS = new LatLng(latitude, BUS.longitude);
        updateBusMarker();
    }
    private void updateBusLong(double longitude) {
        updatedLong = true;
        BUS = new LatLng(BUS.latitude, longitude);
        updateBusMarker();
    }
    private void attachListeners(Firebase myFirebaseRef) {
        Firebase newRef = myFirebaseRef.child("coordinateTable").child("coordinateSet");
        newRef.child("Lat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateBusLat((double)snapshot.getValue());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
        newRef.child("Long").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateBusLong((double)snapshot.getValue());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_schedule:
                showSchedule();
                return true;
            case R.id.action_alerts:
                showAlerts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showSchedule(){
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }
    private void showAlerts(){
        Intent intent = new Intent(this, AlertsActivity.class);
        startActivity(intent);
    }
}
