package edu.rosehulman.keinslc.rhithmu;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;
import edu.rosehulman.keinslc.rhithmu.fragments.AddEditDeleteEventDialogFragment;

public class MainActivity extends AppCompatActivity implements AddEditDeleteEventDialogFragment.EditEventDialogListener, ChildEventListener {
//public class MainActivity extends AppCompatActivity{

    private WeekView mWeekView;
    private Button mMatchScheduleButton;
    private Button mTodayButton;
    private Button mOneDayButton;
    private Button mThreeDayButton;
    private Button mWeekButton;
    private List<Event> mEvents;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private OnCompleteListener mOnCompleteListener;

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
        initializeWeekViewListeners();

        //Fill mEvents
        mEvents = new ArrayList<>();
        EventUtils.createDefaultEvents(mEvents);
        if (savedInstanceState != null) {
            // TODO Persist data
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = new Event();
                DialogFragment df = AddEditDeleteEventDialogFragment.newInstance(event);
                df.show(getSupportFragmentManager(), "add/edit/delete fragment");
            }
        });

        //TODO: Implement complete firebase login procedure
        //temp: hardcoded in Authentication, will be resolve in later milestone
        mFirebaseAuth = FirebaseAuth.getInstance();
        intializeFirebaseListeners();
        mFirebaseAuth.signInWithEmailAndPassword("default@rhit.edu","password")
                .addOnCompleteListener(mOnCompleteListener);

    }

    private void initializeWeekViewListeners() {
        // Display the Description of the event
        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                Event currentEvent = null;
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).getId() == event.getId()) {
                        currentEvent = mEvents.get(i);
                    }
                }
                builder.setTitle(currentEvent.getName());
                builder.setMessage(currentEvent.niceToStringNoName());
                builder.show();
            }
        });

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                Log.d("MAIN", "Month Changed");
                // TODO generate the events in the given month/year
                return mEvents;
            }
        });

        // Pass the event to the dialog fragment for editing or deletion
        mWeekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                Log.d("MAIN", "On Event Clicked Long Press");
                Event mEvent = null;
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).getId() == event.getId()) {
                        mEvent = mEvents.get(i);
                    }
                }
                DialogFragment df = AddEditDeleteEventDialogFragment.newInstance(mEvent);
                df.show(getSupportFragmentManager(), "add/edit/delete fragment");
            }
        });
    }

    private void intializeFirebaseListeners() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    //TODO: Implement
            }
        };
        mOnCompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()){
                    //TODO: Implement a proper login failed catch
                    Log.e("OnComplete", "login failed");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected  void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
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
                Calendar day = mWeekView.getFirstVisibleDay();
                mWeekView.setNumberOfVisibleDays(1);
                mWeekView.goToDate(day);
            }
        });

        mThreeDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar day = mWeekView.getFirstVisibleDay();
                mWeekView.setNumberOfVisibleDays(3);
                mWeekView.goToDate(day);
            }
        });

        mWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar day = mWeekView.getFirstVisibleDay();
                mWeekView.setNumberOfVisibleDays(7);
                mWeekView.goToDate(day);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFinishEditDialog(int editCode, Event event) {
        if (editCode == AddEditDeleteEventDialogFragment.ADD_CODE) {
            mEvents.add(event);
            mWeekView.notifyDatasetChanged();
        }
        if (editCode == AddEditDeleteEventDialogFragment.DELETE_CODE) {
            mEvents.remove(event);
            mWeekView.notifyDatasetChanged();
        }
        if (editCode == AddEditDeleteEventDialogFragment.EDIT_CODE) {
            //TODO Handle rotations during an edit
            mWeekView.notifyDatasetChanged();
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Event event = dataSnapshot.getValue(Event.class);
        event.setKey(dataSnapshot.getKey());
        mEvents.add(event);
        mWeekView.notifyDatasetChanged();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String keyChanged = dataSnapshot.getKey();
        Event changed = dataSnapshot.getValue(Event.class);
        for(Event event : mEvents){
            if(event.getKey().equals(changed.getKey())){
                event.setStartTime(changed.getStartTime());
                event.setEndTime(changed.getEndTime());
                event.setName(changed.getName());
                event.setLocation(changed.getLocation());
                event.setDescription(changed.getDescription());
                event.setInvitees(changed.getInvitees());
                mWeekView.notifyDatasetChanged();
                return;
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String keyRemoved = dataSnapshot.getKey();
        for(int i = 0; i < mEvents.size(); i++ ){
            if(mEvents.get(i).getKey().equals(keyRemoved)){
                mEvents.remove(i);
                mWeekView.notifyDatasetChanged();
                return;
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        //not used
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e("WeekViewMan", "onCancelled called");
    }

}
