package edu.rosehulman.keinslc.rhithmu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.rosehulman.keinslc.rhithmu.fragments.AddEditDeleteEventFragment;
import edu.rosehulman.keinslc.rhithmu.fragments.LoginFragment;
import edu.rosehulman.keinslc.rhithmu.fragments.WeekViewFragment;
import edu.rosehulman.rosefire.Rosefire;
import edu.rosehulman.rosefire.RosefireResult;

//public class MainActivity extends AppCompatActivity implements ChildEventListener {
public class MainActivity extends AppCompatActivity implements WeekViewFragment.OnEventSelectedListener, AddEditDeleteEventFragment.OnEventEditedListener, LoginFragment.OnLoginListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String FIREBASE_PATH = "myFirebasePath";
    private static final int RC_ROSEFIRE_LOGIN = 34;
    private FirebaseAuth mFirebaseAuth;
    private OnCompleteListener mOnCompleteListener;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        initializeListeners();
        setupGoogleSignIn();

    }

    public void logOut() {
        mFirebaseAuth.signOut();
    }

    private void initializeListeners() {
        // Called during log in or log out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d("MAIN", "Current User: " + user);
                if (user != null) {
                    switchToWeekViewFragment("users/" + user.getUid());
                } else {
                    switchToLoginFragment();
                }
            }
        };

        mOnCompleteListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    //TODO: Implement a proper login failed catch
                    Log.e("OnComplete", "login failed");
                }
            }
        };
    }

    private void setupGoogleSignIn() {
        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

    }

    @Override
    public void onLogin(String email, String password) {
        mFirebaseAuth.signInWithEmailAndPassword("default@rhit.edu", "password")
                .addOnCompleteListener(mOnCompleteListener);
    }

    @Override
    public void onGoogleLogin() {

    }

    @Override
    public void onRosefireLogin() {
        // Really shouldn't be here
        Intent signInIntent = Rosefire.getSignInIntent(this, getString(R.string.rosefire_key));
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN);
    }


    /*FRAGMENT SWAPS AND CALLBACKS*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_ROSEFIRE_LOGIN) {
            RosefireResult result = Rosefire.getSignInResultFromIntent(data);
            if (!result.isSuccessful()) {
                // The user cancelled login
            }
            //Success
        }
    }

    private void switchToLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new LoginFragment(), "Login");
        ft.commit();
    }

    private void switchToWeekViewFragment(String path) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment weekViewFragment = new WeekViewFragment();
        Bundle args = new Bundle();
        args.putString(FIREBASE_PATH, path);
        Log.d("Passing in path:", path);
        weekViewFragment.setArguments(args);
        ft.replace(R.id.fragment_container, weekViewFragment, "Passwords");
        ft.commit();
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

    /*LIFECYCLE METHODS*/
    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Woops
    }
}

