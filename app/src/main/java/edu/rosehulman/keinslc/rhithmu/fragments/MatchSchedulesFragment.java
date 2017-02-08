package edu.rosehulman.keinslc.rhithmu.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.List;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.R;

import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.PREFS_NAME;
import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.PREF_MPATH;

/**
 * Created by keinslc on 2/8/2017.
 */

public class MatchSchedulesFragment extends Fragment {

    public static final String ARG_EVENTS_LIST = "myPossibleEventsList";
    private Button mCommitButton;
    private WeekView mWeekView;
    private String mPath;
    private List<Event> mPossibleEvents;
    private List<Event> mSelectedEvents;
    private boolean loadedFirstSetOfEvents;

    public static Fragment newInstance(ArrayList<Event> possibleEvents) {
        Fragment frag = new MatchSchedulesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_EVENTS_LIST, possibleEvents);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPath = prefs.getString(PREF_MPATH, "NoUid");
        loadedFirstSetOfEvents = false;
        if (getArguments() == null) {
            onDestroy(); // Commit Suicide
        }
        mPossibleEvents = getArguments().getParcelableArrayList(ARG_EVENTS_LIST);
        mSelectedEvents = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_schedules, container, false);

        mCommitButton = (Button) view.findViewById(R.id.button_commit_schedules);
        mWeekView = (WeekView) view.findViewById(R.id.commitWeekView);

        mWeekView.notifyDatasetChanged();

        return view;
    }

    private void initializeWeekViewListeners() {
        // Display the Description of the event
        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                Event event1 = (Event) event; //kinda sketch
                for (int i = 0; i < mPossibleEvents.size(); i++) {
                    if (mPossibleEvents.get(i).getKey().equals(event1.getKey())) {
                        event1 = mPossibleEvents.get(i);
                    }
                }
            }
        });

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                // My hacky way to avoid figuring out which month we are dealing with since we have limited
                //events
                if (loadedFirstSetOfEvents) {
                    return null;
                }
                loadedFirstSetOfEvents = true;
                return mPossibleEvents;
            }
        });

        // Pass the event to the dialog fragment for editing or deletion
        mWeekView.setEventLongPressListener(new WeekView.EventLongPressListener() {
            @Override
            public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                Event event1 = (Event) event;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                Event currentEvent = null;
                for (int i = 0; i < mPossibleEvents.size(); i++) {
                    if (mPossibleEvents.get(i).getKey().equals(event1.getKey())) {
                        currentEvent = mPossibleEvents.get(i);
                    }
                }
                builder.setTitle(currentEvent.getName());
                builder.setMessage(currentEvent.niceToStringNoName());
                builder.show();
            }
        });
    }
}
