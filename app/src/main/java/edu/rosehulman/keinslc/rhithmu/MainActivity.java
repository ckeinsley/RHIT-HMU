package edu.rosehulman.keinslc.rhithmu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import edu.rosehulman.keinslc.rhithmu.fragments.AddEditDeleteEventFragment;
import edu.rosehulman.keinslc.rhithmu.fragments.WeekViewFragment;
import edu.rosehulman.rosefire.Rosefire;
import edu.rosehulman.rosefire.RosefireResult;

//public class MainActivity extends AppCompatActivity implements ChildEventListener {
public class MainActivity extends AppCompatActivity implements WeekViewFragment.OnEventSelectedListener, AddEditDeleteEventFragment.OnEventEditedListener {

    private static final int RC_ROSEFIRE_LOGIN = 34;
    private DatabaseReference mEventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        //TODO: Implement complete firebase login procedure
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new WeekViewFragment());
            ft.commit();
        }


    }


    // In your activity...
    public void onRosefireLogin() {
        // Really shouldn't be here
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                // TODO: Need to make sure its a .ics file
                // TODO: Move this elsewhere, ask the user if they have the calendar downloaded, else have them go get it
                Log.d("MAIN ACTIVITY", "Files :" + Arrays.toString(files));
                File file = new File(files[0]);
                Scanner input = null;
                try {
                    input = new Scanner(file);
                    while (input.hasNextLine()) {
                        Log.d("MAIN", input.nextLine());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
        dialog.show();
//        Intent signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefire_key));
//        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_ROSEFIRE_LOGIN) {
            RosefireResult result = Rosefire.getSignInResultFromIntent(data);
            if (!result.isSuccessful()) {
                // The user cancelled login
            }
            // Now use this token to send to your server for authentication
            // Check out the server libraries to learn more
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://prodweb.rose-hulman.edu/regweb-cgi/reg-sched.pl"));
            startActivity(browserIntent);


        }
    }

    @Override
    public void onEventSelected(Event event, String path) {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStackImmediate();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = AddEditDeleteEventFragment.newInstance(event, path);
        ft.addToBackStack("EventFragment");
        Slide slideTransition = new Slide(Gravity.RIGHT);
        slideTransition.setDuration(200);
        fragment.setEnterTransition(slideTransition);

        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    @Override
    public void onEventEditFinished() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStackImmediate();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new WeekViewFragment());
        ft.commit();
    }
}
