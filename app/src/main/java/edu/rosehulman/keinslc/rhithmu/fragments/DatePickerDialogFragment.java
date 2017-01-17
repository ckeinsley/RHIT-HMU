package edu.rosehulman.keinslc.rhithmu.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

import edu.rosehulman.keinslc.rhithmu.R;

/**
 * Created by gilmordw on 1/15/2017.
 */

public class DatePickerDialogFragment extends DialogFragment {

    public static final int END_DATE_REQUEST_CODE = 3;
    public static final int START_DATE_REQUEST_CODE = 2;
    public static final String KEY_MONTH = "MONTH KEY";
    public static final String KEY_DAY_OF_MONTH = "DAY OF MONTH KEY";
    public static final String KEY_YEAR = "YEAR KEY";
    private DatePicker mDatePicker;


    public static DatePickerDialogFragment newInstance(int day, int month, int year) {
        DatePickerDialogFragment df = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_YEAR, year);
        args.putInt(KEY_DAY_OF_MONTH, day);
        args.putInt(KEY_MONTH, month);
        df.setArguments(args);
        return df;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_fragment_date_picker, null);
        builder.setView(view);

        mDatePicker = (DatePicker) view.findViewById(R.id.date_picker);

        Calendar cal = Calendar.getInstance();
        int year = getArguments().getInt(KEY_YEAR, cal.get(Calendar.YEAR));
        int month = getArguments().getInt(KEY_MONTH, cal.get(Calendar.MONTH));
        int day = getArguments().getInt(KEY_DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));

        mDatePicker.updateDate(year, month, day);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(getTargetRequestCode());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    // When finished with the okay button, we send a result back to the AddEditDeleteEventDialogFragment to let it know
    // that there is new data to populate the button with
    private void sendResult(int request_code) {
        Intent intent = new Intent();

        intent.putExtra(KEY_MONTH, mDatePicker.getMonth());
        intent.putExtra(KEY_DAY_OF_MONTH, mDatePicker.getDayOfMonth());
        intent.putExtra(KEY_YEAR, mDatePicker.getYear());

        getTargetFragment().onActivityResult(getTargetRequestCode(), request_code, intent);
    }

}
