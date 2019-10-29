package bustracker.project.inglese.npbuslocator;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;


public class AlertsActivity extends Activity implements View.OnClickListener {
    final int ROUTECYCLECOUNT = 15;
    final int DESTINATIONCOUNT = 8;
    final int BUTTONTEXTWIDTH = 300;
    String[] titleArray = new String[]{"Main & Prospect Streets",
            "Ulster Poughkeepsie Link (UPL)", "Shoprite Plaza", "Stop & Shop Plaza",
            "SUNY Campus: HAB Circle", "SUNY Huguenot Ct. & South Side Rd.",
            "SUNY Campus: South Rd. at Hawk Dr.", "Southside Ave. & Rt. 208", "aa", "aa"};
    String[][] minsTimeArray = new String[][] {{"00","","05","10","15","16","17","18","19"}
            , {"30","40","35","40","45","46","47","48","49"}};
    String[] hoursTimeArray = new String[] {"11","12","2","3","4","5","6","7"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        HorizontalScrollView horizontalScroll = new HorizontalScrollView(this);
        ScrollView verticalScroll = new ScrollView(this);
        TableLayout layout = new TableLayout(this);
        TableLayout layout2 = new TableLayout(this);
        TableLayout layoutOutter = new TableLayout(this);
        horizontalScroll.setLayoutParams( new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        verticalScroll.setLayoutParams( new ScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setLayoutParams( new TableLayout.LayoutParams(15,1) );
        layout.setPadding(1,1,1,1);
        layout.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorStar, null));
        for (int f=0; f<ROUTECYCLECOUNT + 1; f++) {
            TableRow tr = new TableRow(this);
            tr.setGravity(Gravity.CENTER);
            for (int c=0; c<DESTINATIONCOUNT; c++) {
                if(f==0){
                    TextView tv = new TextView(this);
                    tv.setText(titleArray[c]);
                    tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                    tv.setWidth(BUTTONTEXTWIDTH);
                    tv.setPadding(10, 0, 10, 1);
                    tr.addView(tv, c);
                } else {
                    if((f==4 || f==6 || f==10) || c!=1) {
                        ToggleButton b = new ToggleButton(this);
                        b.setText(hoursTimeArray[(f-1)/2] + ":" + minsTimeArray[(f-1)%2][c]);
                        b.setId(Integer.parseInt(""+(f-1) + c));
                        b.setTextSize(21.0f);
                        b.setWidth(BUTTONTEXTWIDTH);
                        b.setOnClickListener((View.OnClickListener) this);
                        tr.addView(b, c);

                    } else if(c==1) {

                        TextView tv = new TextView(this);
                        tv.setWidth(BUTTONTEXTWIDTH);
                        tv.setId(Integer.parseInt(""+(f-1) + c));
                        tr.addView(tv, c);
                    }
                }
            }
            if(f == 0) {
                layout2.addView(tr);
            } else {
                layout.addView(tr);
            }
        }
        verticalScroll.addView(layout, new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutOutter.addView(layout2, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutOutter.addView(verticalScroll, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        horizontalScroll.addView(layoutOutter, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.setContentView(horizontalScroll);
    }






    boolean[][] showTitle = new boolean[ROUTECYCLECOUNT][DESTINATIONCOUNT];
    @Override
    public void onClick(View view) {
        ToggleButton tb = (ToggleButton)view;
        int id = tb.getId();
        System.out.println(id);
        showTitle[id/10][id%10] = !showTitle[id/10][id%10] ;
        if(!showTitle[id/10][id%10]) {
            tb.setText(hoursTimeArray[(id / 10) / 2] + ":" + minsTimeArray[(id / 10) % 2][(id % 10)]);
            killAlert(id);
        } else {
            scheduleAlert(id);
            //saveAlert(id);
        }
    }

    private void scheduleAlert(int id){
        int hour = Integer.parseInt(hoursTimeArray[(id/10)/2]);        //add option: customize time of alert
        if(!(hour == 11 || hour == 12))
            hour += 12;
        int mins = Integer.parseInt(minsTimeArray[(id/10)%2][id%10]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, mins);
        System.out.println("hours : " +hour + " + mins : " +mins);


        Intent intent = new Intent(this, AlertsService.class);
        intent.putExtra("destination", titleArray[id%10]);
        intent.putExtra("id", id);

        PendingIntent pintent = PendingIntent.getService(this, id, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()-600000, pintent);  //setRepeating --> options interval   -> should be calendar.getMilis()
        System.out.println("test1");                                                            //changed for debugging
    }                                                                                           //-10 minutes for warning

    private void killAlert(int id) {
        Intent intent = new Intent(this, AlertsService.class);          //pray the intent 'id' field stops all alerts from being deleted
        intent.putExtra("destination", titleArray[id%10]);
        intent.putExtra("id", id);

        PendingIntent pintent = PendingIntent.getService(this, id, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
        System.out.println("test2");
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alerts, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_schedule:
                showSchedule();
                return true;
            case R.id.action_home:
                showMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showSchedule(){
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }
    private void showMap(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}