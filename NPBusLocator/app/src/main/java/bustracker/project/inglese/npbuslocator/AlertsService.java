package bustracker.project.inglese.npbuslocator;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;


public class AlertsService extends Service {
    @Override
    public void onCreate() {
        System.out.println("created");
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        System.out.println("started");
        destinationTitle = intent.getStringExtra("destination");
        id = intent.getIntExtra("id", -1);
        estimateTimeAndNotify();
        return START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    //keep array in sync with titleArray in AlertsActivity.class
    //must be kept in sync with bus-tracking 'LastDestination' array on bus-tracking, back-end App
    private LatLng[] destinationArray = new LatLng[]{ new LatLng(41.74765438, -74.08266664),  new LatLng(41.7408581, -74.06046331), new LatLng(41.74153456, -74.06888813),
                                                    new LatLng(41.74470862, -74.0698269), new LatLng(41.74078105, -74.08158101), new LatLng(41.73770687, -74.08432759),
                                                    new LatLng(41.73896979, -74.08639021), new LatLng(41.73764082, -74.08785872), new LatLng(41.74254924, -74.08896044)};
    private String destinationTitle = "[null]";
    private String approximatedTime = "[null]";
    private int id;
    private LatLng BUS = destinationArray[0];
    private int DESTINATION;
    private int LASTDESTINATION;
    boolean initOnce = true;
    boolean updatedLat = false;
    boolean updatedLong = false;
    boolean updatedLastDestination = false;




    private void estimateTimeAndNotify(){
        Firebase mRef = new Firebase("https://np-bus-tracker.firebaseio.com/");
        RetrieveLocationAndEstimate(mRef);
    }
    private void RetrieveLocationAndEstimate(Firebase mRef){
        Firebase coordinateRef = mRef.child("coordinateTable").child("coordinateSet");
        coordinateRef.child("Lat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateBusLat((double)snapshot.getValue());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
        coordinateRef.child("Long").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateBusLong((double)snapshot.getValue());
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
        coordinateRef.child("LastDestination").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateLastDestination((int)snapshot.getValue());
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) { }
        });
    }
    //ERROR --> LAT & LONG WILL BE OF TYPE 'LONG' IF NOT STORED AS DECIMALS IN FIREBASE
    //WONT be an issue, but account for it--> add +.00001 to .00000 values
    private void updateBusLat(double latitude) {
        updatedLat = true;
        BUS = new LatLng(latitude, BUS.longitude);
        checkReadyConditions();
    }
    private void updateBusLong(double longitude) {
        updatedLong = true;
        BUS = new LatLng(BUS.latitude, longitude);
        checkReadyConditions();
    }
    private void updateLastDestination(int destination) {
        updatedLastDestination = true;
        LASTDESTINATION = destination;
        checkReadyConditions();
    }
    private void checkReadyConditions() {
        if(updatedLat && updatedLong && updatedLastDestination && initOnce) {
            System.out.println("checking conditions - values updated successfully");
            updatedLat = false;
            updatedLong = false;
            updatedLastDestination = false;
            initOnce = false;
            EstimateTimeAndNotify();
        }
    }
    private void EstimateTimeAndNotify() {
        System.out.println("approximating time");
        Location source = new Location("");
        source.setLatitude(BUS.latitude);
        source.setLongitude(BUS.longitude);

        //if not '-1'
        DESTINATION = id%10;
        Location destination = new Location("");
        destination.setLatitude(destinationArray[DESTINATION].latitude);
        destination.setLongitude(destinationArray[DESTINATION].longitude);

        float distanceInMeters = source.distanceTo(destination);    //returns distance in meters
        float assumedAvgSpeed = (float)536.448;                     //assumes 20 miles per hour speed
        float floatTime = distanceInMeters/assumedAvgSpeed;
        String approxMin = Integer.toString((int)(floatTime/1)*2 + 1) + " Mins ";     //*2+1 for time potentially spent at stops
        String approxSec = Integer.toString((int)((floatTime%1)/.01)) + " Secs";
        approximatedTime = approxMin + approxSec;

        System.out.println("creating notification");
        createNotification();
    }







    private void createNotification() {
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_alarm_white_48dp)
                        .setContentTitle("NP Loop Location Alerts")
                        .setContentText("Destination: " + destinationTitle)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setStyle(new Notification.BigTextStyle().bigText("Destination: " + destinationTitle + "\nEstimated Arrival: " + approximatedTime));
        Intent resultIntent = new Intent(this, MainActivity.class);


        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        NotificationManager mNotificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.id.notificationID, mBuilder.build());
    }
}
