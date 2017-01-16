package edu.rosehulman.keinslc.rhithmu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.MainActivity;
import edu.rosehulman.keinslc.rhithmu.R;

/**
 * Created by keinslc on 1/15/2017.
 */

public class AddEditDeleteEventDialogFragment extends DialogFragment {
    private MainActivity mActivity;
    private Event mEvent;
    public static final String ARG_EVENT = "myEventArgument";

    public Calendar mStartTime;
    public Calendar mEndTime;

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

        mEvent = new Event();
        if(getArguments() != null) {
            mEvent = getArguments().getParcelable(ARG_EVENT);
        }

        // Buttons
        Button dateButton = (Button) view.findViewById(R.id.event_calendar_edit_button);
        Button startTimeButton = (Button) view.findViewById(R.id.start_time_edit_button);
        Button endTimeButton = (Button) view.findViewById(R.id.end_time_edit_button);
        // EditTexts
        EditText eventNameEditText = (EditText) view.findViewById(R.id.event_name_editText);
        EditText eventLocationEditText = (EditText) view.findViewById(R.id.event_location_editText);
        EditText eventInviteesEditText = (EditText) view.findViewById(R.id.event_invitees_editText);
        EditText eventDescriptionEditText = (EditText) view.findViewById(R.id.event_description_editText);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = new DatePickerDialogFragment();
                df.show(getFragmentManager(), "Date Pickin Fragin");
            }
        });
        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = new TimePickerDialogFragment();
                df.show(getFragmentManager(), "Time Pickin Fragin");
            }
        });
        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment df = new TimePickerDialogFragment();
                df.show(getFragmentManager(), "Time Pickin Fragin");
            }
        });


        String date = mEvent.getStartTime().toString();
        if( date != null) {
            dateButton.setText(date);
        }
        String start = mEvent.getStartTime().toString();
        if(start != null) {
            startTimeButton.setText(mEvent.getStartTime().toString());
        }
        String end = mEvent.getEndTime().toString();
        if(end != null) {
            endTimeButton.setText(mEvent.getEndTime().toString());
        }




        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("FRAG", "OK Clicked");
            }
        });
        return builder.create();
    }


}
