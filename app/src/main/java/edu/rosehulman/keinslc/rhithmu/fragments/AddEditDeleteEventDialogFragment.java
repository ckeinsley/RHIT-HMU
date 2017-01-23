package edu.rosehulman.keinslc.rhithmu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.MainActivity;
import edu.rosehulman.keinslc.rhithmu.R;
import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;

/**
 * Created by keinslc on 1/15/2017.
 */

public class AddEditDeleteEventDialogFragment extends DialogFragment {
    public static final String ARG_EVENT = "myEventArgument";
    public static final String ARG_KEY = "myEventKey";
    public static final String ARG_PATH = "userPath";
    public static final int ADD_CODE = 21;
    public static final int EDIT_CODE = 22;
    public static final int DELETE_CODE = 23;
    public Calendar mStartTime;
    public Calendar mEndTime;
    private MainActivity mActivity;
    private Event mEvent;

    private TextView startDateTextView;
    private TextView startTimeTextView;
    private DatePicker startDatePicker;
    private TimePicker startTimePicker;

    private TextView endDateTextView;
    private TextView endTimeTextView;
    private DatePicker endDatePicker;
    private TimePicker endTimePicker;

    private EditText eventNameEditText;
    private EditText eventLocationEditText;
    private EditText eventInviteesEditText;
    private EditText eventDescriptionEditText;
    private DatabaseReference mEventRef;

    public static AddEditDeleteEventDialogFragment newInstance(Event event, String path) {
        AddEditDeleteEventDialogFragment frag = new AddEditDeleteEventDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);
        args.putString(ARG_KEY, event.getKey());
        args.putString(ARG_PATH, path);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_fragment_add_edit_delete_event, null);
        builder.setView(view);

        // The arguments cannot be null, new event must be passed in at least
        if (getArguments() != null) {
            String key = getArguments().getString(ARG_KEY);
            String path = getArguments().getString(ARG_PATH);
            mEvent = getArguments().getParcelable(ARG_EVENT);
            mEventRef = FirebaseDatabase.getInstance().getReference().child(path);
        } else {
            // Should never happen
            Log.e("CRUD Fragment", "no arguments");
            //mEvent = new Event();
        }



        // Buttons (TextViews)
        startDateTextView = (TextView) view.findViewById(R.id.startDateTextView);
        startTimeTextView = (TextView) view.findViewById(R.id.startTimeTextView);
        startTimePicker = (TimePicker) view.findViewById(R.id.startTimePicker);
        startDatePicker = (DatePicker) view.findViewById(R.id.startDatePicker);

        endDateTextView = (TextView) view.findViewById(R.id.endDateTextView);
        endTimeTextView = (TextView) view.findViewById(R.id.endTimeTextView);
        endTimePicker = (TimePicker) view.findViewById(R.id.endTimePicker);
        endDatePicker = (DatePicker) view.findViewById(R.id.endDatePicker);

        // EditTexts
        eventNameEditText = (EditText) view.findViewById(R.id.event_name_editText);
        eventLocationEditText = (EditText) view.findViewById(R.id.event_location_editText);
        eventInviteesEditText = (EditText) view.findViewById(R.id.event_invitees_editText);
        eventDescriptionEditText = (EditText) view.findViewById(R.id.event_description_editText);


        // Event ID -1 means its a new event
        if (mEvent.getId() == -1) {
            mStartTime = Calendar.getInstance();
            mEndTime = Calendar.getInstance();
            mEndTime.setTimeInMillis(mEndTime.getTimeInMillis() + 3600000);
        } else {
            mStartTime = (Calendar) mEvent.getStartTime().clone();
            mEndTime = (Calendar) mEvent.getEndTime().clone();
        }
        updateView();
        setupButtonListeners();

        /* Alert Dialog Buttons */
        // Do nothing
        builder.setNegativeButton(android.R.string.cancel, null);

        // Delete Event
        builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEventRef.child(mEvent.getKey()).removeValue();
            }
        });

              /* Update the event with the new information. If the event was
               new (as indicated by an ID of -1) tell main activity to
               add the event. Otherwise tell main activity to update it's week view  */
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // TODO add sanity checks for date/time information figure out how to not close the dialog

                mEvent.setStartTime(mStartTime);
                mEvent.setEndTime(mEndTime);
                mEvent.setName(eventNameEditText.getText().toString());
                mEvent.setLocation(eventLocationEditText.getText().toString());
                mEvent.setDescription(eventDescriptionEditText.getText().toString());
                mEvent.setInvitees(eventInviteesEditText.getText().toString());
                if (mEvent.getId() == -1) {
                    mEvent.setId(EventUtils.getNewId());
                    mEventRef.push().setValue(mEvent);
                } else {
                    mEventRef.child(mEvent.getKey()).setValue(mEvent);
                }
            }
        });

        //TODO: More Firebase based things
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventRef.removeEventListener((ChildEventListener) getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventRef.addChildEventListener((ChildEventListener) getActivity());
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
        endDateTextView.setText(getString(R.string.endDateButtonFirstHalf) + EventUtils.getDateStringFromCalendar(mEndTime));
        endTimeTextView.setText(getString(R.string.endTimeFirstHalf) + EventUtils.getTimeStringFromCalendar(mEndTime));
    }

    /**
     * Assigns a listener to the date and time buttons to launch the fragments
     */
    private void setupButtonListeners() {
        startDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startDatePicker.getVisibility() == View.VISIBLE) {
                    startDatePicker.setVisibility(View.GONE);
                    mStartTime.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
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
        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (endDatePicker.getVisibility() == View.VISIBLE) {
                    endDatePicker.setVisibility(View.GONE);
                    mEndTime.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth());
                } else {
                    endDatePicker.updateDate(mEndTime.get(Calendar.YEAR), mEndTime.get(Calendar.MONTH), mEndTime.get(Calendar.DAY_OF_MONTH));
                    endDatePicker.setVisibility(View.VISIBLE);
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
    }
}
