package edu.rosehulman.keinslc.rhithmu.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.ARG_KEY;
import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.ARG_PATH;

/**
 * Created by keinslc on 1/15/2017.
 */

public class AddEditDeleteEventFragment extends Fragment {
    public Calendar mStartTime;
    public Calendar mEndTime;
    private OnEventEditedListener mOnEditFinishedListener;
    private Event mEvent;
    private String mPath;

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

    private Button mNuetralButton;
    private Button mNegativeButton;
    private Button mPositiveButton;

    public static AddEditDeleteEventFragment newInstance(Event event, String path) {
        AddEditDeleteEventFragment frag = new AddEditDeleteEventFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);
        args.putString(ARG_KEY, event.getKey());
        args.putString(ARG_PATH, path);
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
                R.layout.dialog_fragment_add_edit_delete_event, null);

        // The arguments cannot be null, new event must be passed in at least
        if (getArguments() != null) {
            String key = getArguments().getString(ARG_KEY);
            mPath = getArguments().getString(ARG_PATH);
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
        mNuetralButton = (Button) view.findViewById(R.id.neutralButton);
        mNegativeButton = (Button) view.findViewById(R.id.negativeButton);
        mPositiveButton = (Button) view.findViewById(R.id.positiveButton);

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
        mNuetralButton = (Button) view.findViewById(R.id.neutralButton);
        mNegativeButton = (Button) view.findViewById(R.id.negativeButton);
        mPositiveButton = (Button) view.findViewById(R.id.positiveButton);

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
        endDateTextView.setText(getString(R.string.endDateButtonFirstHalf) + EventUtils.getDateStringFromCalendar(mEndTime));
        endTimeTextView.setText(getString(R.string.endTimeFirstHalf) + EventUtils.getTimeStringFromCalendar(mEndTime));
    }

    /**
     * Assigns a listener to the date and time buttons to launch the fragments
     */
    private void setupButtonListeners() {
        //TODO: FIX button appearance, chris thinks this should be in the XML but it makes me cringe
        mNuetralButton.setOnClickListener(new View.OnClickListener() {
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
                if (mEvent.getId() == -1) {
                    mEvent.setId(EventUtils.getNewId());
                    mEventRef.push().setValue(mEvent);
                } else {
                    mEventRef.child(mEvent.getKey()).setValue(mEvent);
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

    public interface OnEventEditedListener{
        void onEventEditFinished(String path);
    }
}
