package edu.rosehulman.keinslc.rhithmu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.MainActivity;
import edu.rosehulman.keinslc.rhithmu.R;
import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;

/**
 * Created by keinslc on 1/15/2017.
 */

public class AddEditDeleteEventDialogFragment extends DialogFragment {
    private MainActivity mActivity;
    private Event mEvent;
    public static final String ARG_EVENT = "myEventArgument";

    public Calendar mStartTime;
    public Calendar mEndTime;

    private Button dateButton;
    private Button startTimeButton;
    private Button endTimeButton;
    private EditText eventNameEditText;
    private EditText eventLocationEditText;
    private EditText eventInviteesEditText;
    private EditText eventDescriptionEditText;

    public static AddEditDeleteEventDialogFragment newInstance(Event event) {
        AddEditDeleteEventDialogFragment frag = new AddEditDeleteEventDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);
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

        // Buttons
        dateButton = (Button) view.findViewById(R.id.event_calendar_edit_button);
        startTimeButton = (Button) view.findViewById(R.id.start_time_edit_button);
        endTimeButton = (Button) view.findViewById(R.id.end_time_edit_button);
        // EditTexts
        eventNameEditText = (EditText) view.findViewById(R.id.event_name_editText);
        eventLocationEditText = (EditText) view.findViewById(R.id.event_location_editText);
        eventInviteesEditText = (EditText) view.findViewById(R.id.event_invitees_editText);
        eventDescriptionEditText = (EditText) view.findViewById(R.id.event_description_editText);

        // The arguments cannot be null, new event must be passed in at least
        if (getArguments() != null) {
            mEvent = getArguments().getParcelable(ARG_EVENT);
        } else {
            // Should never happen
            mEvent = new Event();
        }
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
                // TODO: Delete the event from MainActivity
            }
        });
        // Edit Event
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("FRAG", "OK Clicked");
                // TODO: Update the event information with the information provided
            }
        });
        return builder.create();
    }

    private void updateView() {
        dateButton.setText(getString(R.string.startDateButtonFirstHalf) + EventUtils.getDateStringFromCalendar(mStartTime));
        startTimeButton.setText(getString(R.string.startTimeFirstHalf) + EventUtils.getTimeStringFromCalendar(mStartTime));
        endTimeButton.setText(getString(R.string.endTimeFirstHalf) + EventUtils.getTimeStringFromCalendar(mEndTime));
        eventDescriptionEditText.setText(mEvent.getDescription());
        eventInviteesEditText.setText(mEvent.getInvitees());
        eventLocationEditText.setText(mEvent.getLocation());
        eventNameEditText.setText(mEvent.getName());
    }

    private void setupButtonListeners() {
        // Create a new fragment and prepopulate the current data
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = DatePickerDialogFragment.newInstance(mStartTime.get(Calendar.DAY_OF_MONTH), mStartTime.get(Calendar.MONTH), mStartTime.get(Calendar.YEAR));
                df.setTargetFragment(AddEditDeleteEventDialogFragment.this, DatePickerDialogFragment.START_DATE_REQUEST_CODE);
                df.show(getActivity().getSupportFragmentManager(), "DATE_PICKER");
            }
        });
        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = TimePickerDialogFragment.newInstance(mStartTime.get(Calendar.MINUTE), mStartTime.get(Calendar.HOUR_OF_DAY));
                df.setTargetFragment(AddEditDeleteEventDialogFragment.this, TimePickerDialogFragment.START_TIME_REQUEST_CODE);
                df.show(getFragmentManager(), "TIME_PICK");
            }
        });
        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = TimePickerDialogFragment.newInstance(mEndTime.get(Calendar.MINUTE), mEndTime.get(Calendar.HOUR_OF_DAY));
                df.setTargetFragment(AddEditDeleteEventDialogFragment.this, TimePickerDialogFragment.END_TIME_REQUEST_CODE);
                df.show(getFragmentManager(), "TIME_PICK");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DatePickerDialogFragment.START_DATE_REQUEST_CODE) {
            // Use the year, day, month returned
            int year = data.getIntExtra(DatePickerDialogFragment.KEY_YEAR, mStartTime.get(Calendar.YEAR));
            int day = data.getIntExtra(DatePickerDialogFragment.KEY_DAY_OF_MONTH, mStartTime.get(Calendar.DAY_OF_MONTH));
            int month = data.getIntExtra(DatePickerDialogFragment.KEY_MONTH, mStartTime.get(Calendar.MONTH));
            mStartTime.set(year, month, day);
            updateView();
        } else if (requestCode == TimePickerDialogFragment.START_TIME_REQUEST_CODE) {
            // Use the minute and hour returned, and keep the year, month, date the same
            int minute = data.getIntExtra(TimePickerDialogFragment.KEY_MINUTE, mStartTime.get(Calendar.MINUTE));
            int hour = data.getIntExtra(TimePickerDialogFragment.KEY_HOUR, mStartTime.get(Calendar.HOUR_OF_DAY));
            mStartTime.set(mStartTime.get(Calendar.YEAR), mStartTime.get(Calendar.MONTH), mStartTime.get(Calendar.DAY_OF_MONTH), hour, minute);
            updateView();
        } else if (requestCode == TimePickerDialogFragment.END_TIME_REQUEST_CODE) {
            // Use the minute and hour returned, and keep the year, month, date the same
            int minute = data.getIntExtra(TimePickerDialogFragment.KEY_MINUTE, mEndTime.get(Calendar.MINUTE));
            int hour = data.getIntExtra(TimePickerDialogFragment.KEY_HOUR, mEndTime.get(Calendar.HOUR_OF_DAY));
            mEndTime.set(mEndTime.get(Calendar.YEAR), mEndTime.get(Calendar.MONTH), mEndTime.get(Calendar.DAY_OF_MONTH), hour, minute);
            updateView();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
