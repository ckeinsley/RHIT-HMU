package edu.rosehulman.keinslc.rhithmu;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WeekView mWeekView;
    private Button mMatchScheduleButton;
    private Button mTodayButton;
    private Button mOneDayButton;
    private Button mThreeDayButton;
    private Button mWeekButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Capture references
        mWeekView = (WeekView) findViewById(R.id.weekView);
        mMatchScheduleButton = (Button) findViewById(R.id.matchSyncSchedule);
        // Day Buttons
        mTodayButton = (Button) findViewById(R.id.todayButton);
        mOneDayButton = (Button) findViewById(R.id.oneDayButton);
        mThreeDayButton = (Button) findViewById(R.id.threeDayButton);
        mWeekButton = (Button) findViewById(R.id.sevenDayButton);
        setButtonListeners();

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Log.d("MAIN", "On Event Clicked");
                // TODO generate alert dialog with description of event
            }
        });

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                Log.d("MAIN", "Month Changed");
                // TODO generate the events in the given month/year
                ArrayList<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
                events.add(new WeekViewEvent(5, "Test", 2017, 1, 14, 6, 25, 2017, 1, 14, 8, 5));
                return events;
            }
        });

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                Log.d("MAIN", "On Event Clicked Long Press");
                // TODO Load information about event and launch add/edit event activity
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case (R.id.action_settings):
                Log.d("MAIN", "Settings Pressed");
                return true;
            case (R.id.action_importClasses):
                Log.d("MAIN", "Import Classes Pressed");
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setButtonListeners() {
        mMatchScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch a bluetooth connect fragment
                Log.d("MAIN", "Match Schedules Clicked");
            }
        });
        mTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeekView.goToToday();
            }
        });
        mOneDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeekView.setNumberOfVisibleDays(1);
            }
        });
        mThreeDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeekView.setNumberOfVisibleDays(3);
            }
        });
        mWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeekView.setNumberOfVisibleDays(7);
            }
        });
    }
}
