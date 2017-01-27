package edu.rosehulman.keinslc.rhithmu.fragments;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.MainActivity;
import edu.rosehulman.keinslc.rhithmu.R;

/**
 * Created by keinslc on 1/26/2017.
 */

public class WeekViewFragment extends Fragment implements ChildEventListener {

    private WeekView mWeekView;
    private Button mMatchScheduleButton;
    private Button mTodayButton;
    private Button mOneDayButton;
    private Button mThreeDayButton;
    private Button mWeekButton;
    private List<Event> mEvents;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mEventRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private OnCompleteListener mOnAuthCompleteListener;
    private OnEventSelectedListener mOnEventSelectedListener;
    private String mPath;
    private MainActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
        if (context instanceof OnEventSelectedListener) {
            mOnEventSelectedListener = (OnEventSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_view, container, false);
        // Capture references
        mWeekView = (WeekView) view.findViewById(R.id.weekView);
        mMatchScheduleButton = (Button) view.findViewById(R.id.matchSyncSchedule);
        // Day Buttons
        mTodayButton = (Button) view.findViewById(R.id.todayButton);
        mOneDayButton = (Button) view.findViewById(R.id.oneDayButton);
        mThreeDayButton = (Button) view.findViewById(R.id.threeDayButton);
        mWeekButton = (Button) view.findViewById(R.id.sevenDayButton);

        setButtonListeners();
        initializeWeekViewListeners();

        //Fill mEvents
        mEvents = new ArrayList<>();

        //EventUtils.createDefaultEvents(mEvents);

        //TODO: Implement complete firebase login procedure
        //temp: hardcoded in Authentication, will be resolve in later milestone
        mFirebaseAuth = FirebaseAuth.getInstance();
        intializeFirebaseListeners();
        mFirebaseAuth.signInWithEmailAndPassword("default@rhit.edu", "password")
                .addOnCompleteListener(mOnAuthCompleteListener);
        //mEventRef = FirebaseDatabase.getInstance().getReference().child(mPath);


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = new Event();
                mOnEventSelectedListener.onEventSelected(event, mPath);
            }
        });
        mWeekView.notifyDatasetChanged();
        return view;
    }

    /**
     * Sets the WeekView Listeners
     */
    private void initializeWeekViewListeners() {
        // Display the Description of the event
        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Event event1 = (Event) event;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                Event currentEvent = null;
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).getKey().equals(event1.getKey())) {
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
                Log.d("MAIN", "Month Changed Year: " + newYear + " newMonth: " + newMonth);
                int month = newMonth;
                int year = newYear;
                if (newMonth == 1) {
                    month = 13;
                    year = newYear - 1;
                }
                Calendar lastMonth = Calendar.getInstance();
                lastMonth.set(year, month - 1, 1);
                if (newMonth == 12) {
                    month = 0;
                    year = newYear + 1;
                }
                Calendar nextMonth = Calendar.getInstance();
                nextMonth.set(year, month + 1, 1);
                long lowEnd = lastMonth.getTimeInMillis();
                long highEnd = nextMonth.getTimeInMillis();
                List<Event> list = new ArrayList<Event>();
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).getStartTimeInMilis() >= lowEnd && mEvents.get(i).getEndTimeInMilis() <= highEnd) {
                        list.add(mEvents.get(i));
                    }
                }
                Log.d("WEEK", Arrays.deepToString(list.toArray()));
                return list;
            }
        });

        // Pass the event to the dialog fragment for editing or deletion
        mWeekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                Event event1 = (Event) event; //kinda sketch
                Log.d("MAIN", "On Event Clicked Long Press");
                Event mEvent = null;
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).getKey().equals(event1.getKey())) {
                        mEvent = mEvents.get(i);
                    }
                }
                mOnEventSelectedListener.onEventSelected(mEvent, mPath);
            }
        });
    }

    /**
     * Sets up the FireBase Listeners
     */
    private void intializeFirebaseListeners() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //TODO: Implement
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mPath = "users/" + user.getUid();
                    mEventRef = FirebaseDatabase.getInstance().getReference().child(mPath);
                    mEventRef.addChildEventListener(WeekViewFragment.this);
                }
            }
        };
        mOnAuthCompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    //TODO: Implement a proper login failed catch
                    Log.e("OnComplete", "login failed");
                }
            }
        };
    }

    /*LIFECYCLE METHODS FOR FIREBASE*/
    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventRef.removeEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEventRef != null) {
            mEventRef.addChildEventListener(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /*OPTION MENU METHODS*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
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
                mActivity.onRosefireLogin();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the button listeners on the weekview and schedule button
     */
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
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            }
        });

        mThreeDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar day = mWeekView.getFirstVisibleDay();
                mWeekView.setNumberOfVisibleDays(3);
                mWeekView.goToDate(day);
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            }
        });

        mWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar day = mWeekView.getFirstVisibleDay();
                mWeekView.setNumberOfVisibleDays(7);
                mWeekView.goToDate(day);
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
            }
        });
    }

    /*FIREBASE CHILD LISTENER METHODS*/
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
        for (Event event : mEvents) {
            if (event.getKey().equals(changed.getKey())) {
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
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).getKey().equals(keyRemoved)) {
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

    //TODO: Move auth into main activity and remove the passing of the Path
    public interface OnEventSelectedListener {
        void onEventSelected(Event event, String userPath);
    }

}
