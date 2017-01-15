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
import android.widget.EditText;

import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.MainActivity;
import edu.rosehulman.keinslc.rhithmu.R;

/**
 * Created by keinslc on 1/15/2017.
 */

public class AddEditDeleteEventDialogFragment extends DialogFragment {
    private MainActivity mActivity;
    private Event mEvent;

    public static AddEditDeleteEventDialogFragment newInstance(Event event) {
        AddEditDeleteEventDialogFragment frag = new AddEditDeleteEventDialogFragment();
        Bundle args = new Bundle();
        // TODO: Set up bundle

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
        // TODO: Capture views and grab mEvent from bundle
        final EditText messageEditText = (EditText) view.findViewById(R.id.event_name_editText);


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("FRAG", "OK Clicked");
            }
        });
        return builder.create();
    }
}
