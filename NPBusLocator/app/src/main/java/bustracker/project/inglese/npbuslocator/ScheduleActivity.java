package bustracker.project.inglese.npbuslocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ScheduleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        TouchImageView tiv = (TouchImageView)findViewById(R.id.schedule);
        tiv.setZoom(1.2f);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                showMap();
                return true;
            case R.id.action_alerts:
                showAlerts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showMap(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void showAlerts(){
        Intent intent = new Intent(this, AlertsActivity.class);
        startActivity(intent);
    }
}