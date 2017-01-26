package edu.rosehulman.keinslc.rhithmu;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;

import edu.rosehulman.keinslc.rhithmu.fragments.AddEditDeleteEventFragment;
import edu.rosehulman.keinslc.rhithmu.fragments.WeekViewFragment;

//public class MainActivity extends AppCompatActivity implements ChildEventListener {
public class MainActivity extends AppCompatActivity implements WeekViewFragment.OnEventSelectedListener, AddEditDeleteEventFragment.OnEventEditedListener {

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

    @Override
    public void onEventSelected(Event event, String path) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, AddEditDeleteEventFragment.newInstance(event, path));
        ft.commit();
    }

    @Override
    public void onEventEditFinished() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new WeekViewFragment());
        ft.commit();
    }
}
