package edu.rosehulman.keinslc.rhithmu.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.MainActivity;
import edu.rosehulman.keinslc.rhithmu.R;
import edu.rosehulman.keinslc.rhithmu.Utils.Constants;
import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;

import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.FIREBASE_PATH;

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
    private DatabaseReference mEventRef;
    private OnEventSelectedListener mOnEventSelectedListener;
    private String mPath;
    private MainActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPath = getArguments().getString(FIREBASE_PATH);
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


    /*-----LIFECYCLE METHODS FOR FIREBASE-----*/
    /* Don't need on resume since oncreate is called to recreate the fragment */
    @Override
    public void onPause() {
        super.onPause();
        if (mEventRef != null) {
            mEventRef.removeEventListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPath == null || mPath.isEmpty()) {
            mEventRef = FirebaseDatabase.getInstance().getReference();
            mEventRef.addChildEventListener(this);
        } else {
            mEventRef = FirebaseDatabase.getInstance().getReference().child(mPath);
            mEventRef.addChildEventListener(this);
        }
    }

    /*-----OPTION MENU METHODS-----*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case (R.id.action_settings):
                Log.d(Constants.TAG_WEEK_VIEW, "Settings Pressed");
                return true;
            case (R.id.action_importClasses):
                Log.d(Constants.TAG_WEEK_VIEW, "Import Classes Pressed");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Do you have your calendar file to import?");
                builder.setMessage("Hitting no will launch a browser to download it.\nHitting yes will prompt you to select it.\n" +
                        "It is most likely in your Download folder.");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchFilePicker();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        redirectToBannerWeb();
                    }
                });
                builder.setNeutralButton(android.R.string.cancel, null);
                builder.show();

                return true;
            case (R.id.action_logout):
                mActivity.logOut();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void redirectToBannerWeb() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://prodweb.rose-hulman.edu/regweb-cgi/reg-sched.pl"));
        startActivity(browserIntent);
    }

    private void launchFilePicker() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        final FilePickerDialog dialog = new FilePickerDialog(getActivity(), properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                String filePath = files[0];
                if (!filePath.contains("ics")) {
                    Toast.makeText(getContext(), "Should be a .ics file.", Toast.LENGTH_LONG);
                    launchFilePicker();
                } else {
                    try {
                        addBulkEvents(EventUtils.parseScheduleLookupEvent(filePath));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        dialog.show();
    }

    private void addBulkEvents(List<Event> eventList) {
        mEventRef = FirebaseDatabase.getInstance().getReference().child(mPath);
        for (Event event : eventList) {
            mEventRef.push().setValue(event);
        }
    }


    /*----- SETUP METHODS -----*/
    private void setButtonListeners() {
        mMatchScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.TAG_WEEK_VIEW, "Match Schedules Clicked");
                mActivity.launchDeviceList();
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
                Calendar lastMonth = Calendar.getInstance();
                Calendar nextMonth = Calendar.getInstance();
                lastMonth.set(lastMonth.get(Calendar.YEAR), lastMonth.get(Calendar.MONTH), lastMonth.get(Calendar.DAY_OF_MONTH), 0, 0);
                nextMonth.set(lastMonth.get(Calendar.YEAR), lastMonth.get(Calendar.MONTH), lastMonth.get(Calendar.DAY_OF_MONTH), 0, 0);
                //TODO: will cause funny bugs, woo;
                if (newMonth == 1) {
                    lastMonth.set(newYear - 1, 12, 31);
                    nextMonth.set(newYear, 2, 1);
                } else if (newMonth == 12) {
                    lastMonth.set(newYear, 11, 30);
                    nextMonth.set(newYear + 1, 1, 1);
                } else {
                    lastMonth.set(newYear, newMonth - 1, 30);
                    nextMonth.set(newYear, newMonth + 1, 1);
                }

                long lowEnd = lastMonth.getTimeInMillis();
                long highEnd = nextMonth.getTimeInMillis();

                List<Event> list = new ArrayList<>();
                for (int i = 0; i < mEvents.size(); i++) {
                    if (lowEnd <= mEvents.get(i).getStartTimeInMilis() && mEvents.get(i).getEndTimeInMilis() <= highEnd) {
                        list.add(mEvents.get(i));
                    }
                }
                return list;
            }
        });

        // Pass the event to the dialog fragment for editing or deletion
        mWeekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                Event event1 = (Event) event; //kinda sketch
                for (int i = 0; i < mEvents.size(); i++) {
                    if (mEvents.get(i).getKey().equals(event1.getKey())) {
                        event1 = mEvents.get(i);
                    }
                }
                mOnEventSelectedListener.onEventSelected(event1, mPath);
            }
        });
    }

    /*-----FIREBASE CHILD LISTENER METHODS-----*/
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
            if (event.getKey().equals(keyChanged)) {
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
        Log.e(Constants.TAG_WEEK_VIEW, databaseError.toString());
    }

    public interface OnEventSelectedListener {
        void onEventSelected(Event event, String userPath);
    }

}
