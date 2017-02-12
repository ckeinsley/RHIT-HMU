package edu.rosehulman.keinslc.rhithmu.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.R;
import edu.rosehulman.keinslc.rhithmu.Utils.Constants;
import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;

import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.ARG_EVENT;
import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.PREFS_NAME;
import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.PREF_MPATH;

/**
 * Created by keinslc on 1/15/2017.
 */

public class AddEditDeleteEventFragment extends Fragment {
    public Calendar mStartTime;
    public Calendar mEndTime;
    public Calendar mRecurringTime;
    private OnEventEditedListener mOnEditFinishedListener;
    private Event mEvent;
    private String mPath;

    private TextView startDateTextView;
    private TextView startTimeTextView;
    private DatePicker startDatePicker;
    private TimePicker startTimePicker;

    private TextView endTimeTextView;
    private TimePicker endTimePicker;

    private EditText eventNameEditText;
    private EditText eventLocationEditText;
    private EditText eventInviteesEditText;
    private EditText eventDescriptionEditText;
    private DatabaseReference mEventRef;

    private Button mNeutralButton;
    private Button mNegativeButton;
    private Button mPositiveButton;

    private Spinner RecurringYesNoSpinner;
    private Spinner RecurringFrequencySpinner;
    private TextView RecurringEndDateTextView;
    private DatePicker RecurringDatePicker;

    public static AddEditDeleteEventFragment newInstance(Event event, String path) {
        AddEditDeleteEventFragment frag = new AddEditDeleteEventFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEventEditedListener) {
            mOnEditFinishedListener = (OnEventEditedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventEditedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.fragment_add_edit_delete_event, null);
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPath = prefs.getString(PREF_MPATH, "NoUid");

        // The arguments cannot be null, new event must be passed in at least
        if (getArguments() != null) {
            //String key = getArguments().getString(ARG_KEY);
            mEvent = getArguments().getParcelable(ARG_EVENT);
            mEventRef = FirebaseDatabase.getInstance().getReference().child(mPath);
        } else {
            // Should never happen
            Log.e(Constants.TAG_EDIT_FRAG, "no arguments");
            //mEvent = new Event();
        }

        // Buttons (TextViews)
        startDateTextView = (TextView) view.findViewById(R.id.startDateTextView);
        startTimeTextView = (TextView) view.findViewById(R.id.startTimeTextView);
        startTimePicker = (TimePicker) view.findViewById(R.id.startTimePicker);
        startDatePicker = (DatePicker) view.findViewById(R.id.startDatePicker);

        //Buttons (Buttons)
        mNeutralButton = (Button) view.findViewById(R.id.neutralButton);
        mNegativeButton = (Button) view.findViewById(R.id.negativeButton);
        mPositiveButton = (Button) view.findViewById(R.id.positiveButton);

        endTimeTextView = (TextView) view.findViewById(R.id.endTimeTextView);
        endTimePicker = (TimePicker) view.findViewById(R.id.endTimePicker);

        // EditTexts
        eventNameEditText = (EditText) view.findViewById(R.id.event_name_editText);
        eventLocationEditText = (EditText) view.findViewById(R.id.event_location_editText);
        eventInviteesEditText = (EditText) view.findViewById(R.id.event_invitees_editText);
        eventDescriptionEditText = (EditText) view.findViewById(R.id.event_description_editText);

        //Recurring
        RecurringYesNoSpinner = (Spinner) view.findViewById(R.id.recurring_spinner_yn);
        RecurringFrequencySpinner = (Spinner) view.findViewById(R.id.recurring_spinner_frequency);
        RecurringDatePicker = (DatePicker) view.findViewById(R.id.recurring_datepicker);
        RecurringEndDateTextView = (TextView) view.findViewById(R.id.recurring_end_date_textview);

        // Do nothing
        mNeutralButton = (Button) view.findViewById(R.id.neutralButton);
        mNegativeButton = (Button) view.findViewById(R.id.negativeButton);
        mPositiveButton = (Button) view.findViewById(R.id.positiveButton);

        // Event ID -1 means its a new event
        if (mEvent.getId() == -1) {
            mStartTime = Calendar.getInstance();
            mEndTime = Calendar.getInstance();
            mEndTime.setTimeInMillis(mEndTime.getTimeInMillis() + 3600000);
            mNeutralButton.setVisibility(View.GONE);

        } else {
            mStartTime = (Calendar) mEvent.getStartTime().clone();
            mEndTime = (Calendar) mEvent.getEndTime().clone();
        }
        //recurring gives no cares
        mRecurringTime = Calendar.getInstance();
        updateView();
        setupButtonListeners();

        return view;
    }

    /**
     * Updates the view upon return from a time/date picker
     */
    private void updateView() {
        updateDateTimeView();
        eventDescriptionEditText.setText(mEvent.getDescription());
        eventInviteesEditText.setText(mEvent.getInvitees());
        eventLocationEditText.setText(mEvent.getLocation());
        eventNameEditText.setText(mEvent.getName());
    }

    private void updateDateTimeView() {
        startDateTextView.setText(getString(R.string.startDateButtonFirstHalf) + EventUtils.getDateStringFromCalendar(mStartTime));
        startTimeTextView.setText(getString(R.string.startTimeFirstHalf) + EventUtils.getTimeStringFromCalendar(mStartTime));
        endTimeTextView.setText(getString(R.string.endTimeFirstHalf) + EventUtils.getTimeStringFromCalendar(mEndTime));
        RecurringEndDateTextView.setText(getString(R.string.until) + EventUtils.getDateStringFromCalendar(mRecurringTime));
    }

    /**
     * Assigns a listener to the date and time buttons to launch the fragments
     */
    private void setupButtonListeners() {
        mNeutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventRef.child(mEvent.getKey()).removeValue();
                mOnEditFinishedListener.onEventEditFinished(mPath);
            }
        });
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEditFinishedListener.onEventEditFinished(mPath);
            }
        });
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEvent.setStartTime(mStartTime);
                mEvent.setEndTime(mEndTime);
                mEvent.setName(eventNameEditText.getText().toString());
                mEvent.setLocation(eventLocationEditText.getText().toString());
                mEvent.setDescription(eventDescriptionEditText.getText().toString());
                mEvent.setInvitees(eventInviteesEditText.getText().toString());
//                if (mEvent.getId() == -1) {
//                    mEvent.setId(EventUtils.getNewId());
//                    mEventRef.push().setValue(mEvent);
//                } else {
//                    mEventRef.child(mEvent.getKey()).setValue(mEvent);
//                }
                //so this might qualify as cheating the system but who cares
                if (RecurringFrequencySpinner.getVisibility() == View.GONE) {
                    addEvent();
                } else {
                    addRecurringEvents();
                }
                mOnEditFinishedListener.onEventEditFinished(mPath);
            }
        });

        startDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startDatePicker.getVisibility() == View.VISIBLE) {
                    startDatePicker.setVisibility(View.GONE);
                    mStartTime.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
                    mEndTime.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
                } else {
                    startDatePicker.updateDate(mStartTime.get(Calendar.YEAR), mStartTime.get(Calendar.MONTH), mStartTime.get(Calendar.DAY_OF_MONTH));
                    startDatePicker.setVisibility(View.VISIBLE);
                }
                updateDateTimeView();
            }
        });
        startTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, min;
                if (startTimePicker.getVisibility() == View.VISIBLE) {
                    // If visibile, make it gone and grab the info, update the time
                    startTimePicker.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= 23) {
                        hour = startTimePicker.getHour();
                        min = startTimePicker.getMinute();
                    } else {
                        hour = startTimePicker.getCurrentHour();
                        min = startTimePicker.getCurrentMinute();
                    }
                    mStartTime.set(mStartTime.get(Calendar.YEAR), mStartTime.get(Calendar.MONTH), mStartTime.get(Calendar.DAY_OF_MONTH), hour, min);
                } else {
                    // If gone, make visible, and update with current calendar info
                    hour = mStartTime.get(Calendar.HOUR_OF_DAY);
                    min = mStartTime.get(Calendar.MINUTE);
                    if (Build.VERSION.SDK_INT >= 23) {
                        startTimePicker.setHour(hour);
                        startTimePicker.setMinute(min);
                    } else {
                        startTimePicker.setCurrentHour(hour);
                        startTimePicker.setCurrentMinute(min);
                    }
                    startTimePicker.setVisibility(View.VISIBLE);
                }
                updateDateTimeView();
            }
        });

        endTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, min;
                if (endTimePicker.getVisibility() == View.VISIBLE) {
                    // If visibile, make it gone and grab the info, update the time
                    endTimePicker.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= 23) {
                        hour = endTimePicker.getHour();
                        min = endTimePicker.getMinute();
                    } else {
                        hour = endTimePicker.getCurrentHour();
                        min = endTimePicker.getCurrentMinute();
                    }
                    mEndTime.set(mEndTime.get(Calendar.YEAR), mEndTime.get(Calendar.MONTH), mEndTime.get(Calendar.DAY_OF_MONTH), hour, min);
                } else {
                    // If gone, make visible, and update with current calendar info
                    hour = mEndTime.get(Calendar.HOUR_OF_DAY);
                    min = mEndTime.get(Calendar.MINUTE);
                    if (Build.VERSION.SDK_INT >= 23) {
                        endTimePicker.setHour(hour);
                        endTimePicker.setMinute(min);
                    } else {
                        endTimePicker.setCurrentHour(hour);
                        endTimePicker.setCurrentMinute(min);
                    }
                    endTimePicker.setVisibility(View.VISIBLE);
                }
                updateDateTimeView();
            }
        });
        RecurringYesNoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String option = adapterView.getItemAtPosition(position).toString();
                if (option.equals("No")) {
                    RecurringFrequencySpinner.setVisibility(View.GONE);
                    RecurringEndDateTextView.setVisibility(View.GONE);
                } else {
                    RecurringFrequencySpinner.setVisibility(View.VISIBLE);
                    RecurringEndDateTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //default == No
                RecurringDatePicker.setVisibility(View.GONE);
                RecurringFrequencySpinner.setVisibility(View.GONE);
                RecurringEndDateTextView.setVisibility(View.GONE);
            }
        });
        RecurringEndDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RecurringDatePicker.getVisibility() == View.VISIBLE) {
                    RecurringDatePicker.setVisibility(View.GONE);
                    mRecurringTime.set(RecurringDatePicker.getYear(), RecurringDatePicker.getMonth(), RecurringDatePicker.getDayOfMonth());
                } else {
                    RecurringDatePicker.updateDate(mRecurringTime.get(Calendar.YEAR), mRecurringTime.get(Calendar.MONTH), mRecurringTime.get(Calendar.DAY_OF_MONTH));
                    RecurringDatePicker.setVisibility(View.VISIBLE);
                }
                updateDateTimeView();
            }
        });
    }

    //If recurring is true call this and let the madness begin
    private void addRecurringEvents() {
        long factor = 0;
        Calendar recurStart = (Calendar) mStartTime.clone();
        Calendar recurEnd = (Calendar) mEndTime.clone();
        //We add a day in case someone gives us the exact stop day
        long max = mRecurringTime.getTimeInMillis() + EventUtils.ONE_DAY_IN_MILLIS;
        String option = RecurringFrequencySpinner.getSelectedItem().toString();
        if (option.equals("Daily")) {
            factor = EventUtils.ONE_DAY_IN_MILLIS;
        } else if (option.equals("Weekly")) {
            factor = EventUtils.ONE_WEEK_IN_MILLIS;
        } else {
            //guess we have to do this the hard way
            while (recurStart.getTimeInMillis() < max) {
                mEvent.setStartTime(recurStart);
                mEvent.setEndTime(recurEnd);
                addEvent();
                //side note: if this causes an error tell IntelliJ to piss off because the code works
                if (recurStart.get(Calendar.MONTH) == Calendar.DECEMBER) {
                    recurStart.set(Calendar.MONTH, Calendar.JANUARY);
                    recurEnd.set(Calendar.MONTH, Calendar.JANUARY);
                } else {
                    recurStart.set(Calendar.MONTH, recurStart.get(Calendar.MONTH) + 1);
                    recurEnd.set(Calendar.MONTH, recurEnd.get(Calendar.MONTH) + 1);
                }

            }
            return;
        }
        while (recurStart.getTimeInMillis() < max) {
            mEvent.setStartTime(recurStart);
            mEvent.setEndTime(recurEnd);
            addEvent();
            int hour = recurStart.get(Calendar.HOUR);
            recurStart.setTimeInMillis(recurStart.getTimeInMillis() + factor);
            recurEnd.setTimeInMillis(recurEnd.getTimeInMillis() + factor);
            if(recurStart.get(Calendar.HOUR) != hour){
                //daylight savings!
                int falseHour = recurStart.get(Calendar.HOUR);
                if(falseHour > hour){
                    recurStart.setTimeInMillis(recurStart.getTimeInMillis() - EventUtils.ONE_HOUR_IN_MILLIS);
                    recurEnd.setTimeInMillis(recurEnd.getTimeInMillis() - EventUtils.ONE_HOUR_IN_MILLIS);
                } else {
                    recurStart.setTimeInMillis(recurStart.getTimeInMillis() + EventUtils.ONE_HOUR_IN_MILLIS);
                    recurEnd.setTimeInMillis(recurEnd.getTimeInMillis() + EventUtils.ONE_HOUR_IN_MILLIS);
                }
            }

        }

    }

    //export some work and isolate the firebase connecting
    private void addEvent() {
        if (mEvent.getId() == -1) {
            mEvent.setId(EventUtils.getNewId());
            mEventRef.push().setValue(mEvent);
        } else {
            mEventRef.child(mEvent.getKey()).setValue(mEvent);
        }
        //if we don't do this recurring will not work
        mEvent.setId(-1);
    }

    public interface OnEventEditedListener {
        void onEventEditFinished(String path);
    }
}
