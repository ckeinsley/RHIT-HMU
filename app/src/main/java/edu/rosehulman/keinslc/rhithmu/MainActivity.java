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
import android.view.Gravity;

import com.google.firebase.database.DatabaseReference;

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

    public void redirectToBannerWeb() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://prodweb.rose-hulman.edu/regweb-cgi/reg-sched.pl"));
        startActivity(browserIntent);
    }



    // In your activity...
    public void onRosefireLogin() {
        // Really shouldn't be here

        Intent signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefire_key));
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN);
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
