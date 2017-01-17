package edu.rosehulman.keinslc.rhithmu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

import edu.rosehulman.keinslc.rhithmu.R;

/**
 * Created by gilmordw on 1/15/2017.
 */

public class TimePickerDialogFragment extends DialogFragment {

    public static final int START_TIME_REQUEST_CODE = 11;
    public static final int END_TIME_REQUEST_CODE = 12;
    public static final String KEY_HOUR = "HOUR KEY";
    public static final String KEY_MINUTE = "MINUTE KEY";

    private TimePicker mTimePicker;

    public static TimePickerDialogFragment newInstance(int minute, int hour) {
        TimePickerDialogFragment df = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_HOUR, hour);
        args.putInt(KEY_MINUTE, minute);
        df.setArguments(args);
        return df;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_fragment_time_picker, null);
        builder.setView(view);
        mTimePicker = (TimePicker) view.findViewById(R.id.time_picker);
        Calendar cal = Calendar.getInstance();
        int hour = getArguments().getInt(KEY_HOUR, cal.get(Calendar.HOUR_OF_DAY));
        int min = getArguments().getInt(KEY_MINUTE, cal.get(Calendar.MINUTE));

        // SDK version check becaues of deprecated methods
        if (Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(min);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(min);
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(getTargetRequestCode());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private void sendResult(int request_code) {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT >= 23) {
            intent.putExtra(KEY_HOUR, mTimePicker.getHour());
            intent.putExtra(KEY_MINUTE, mTimePicker.getMinute());
        } else {
            intent.putExtra(KEY_MINUTE, mTimePicker.getCurrentMinute());
            intent.putExtra(KEY_HOUR, mTimePicker.getCurrentHour());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), request_code, intent);
    }

}
