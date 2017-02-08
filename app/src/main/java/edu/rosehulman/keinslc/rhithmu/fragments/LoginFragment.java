package edu.rosehulman.keinslc.rhithmu.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;

import edu.rosehulman.keinslc.rhithmu.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    private View mProgressSpinner;
    private boolean mLoggingIn;
    private OnLoginListener mListener;
    private SignInButton mGoogleSignInButton;
    private View rosefireLoginButton;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoggingIn = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mProgressSpinner = rootView.findViewById(R.id.login_progress);
        mGoogleSignInButton = (SignInButton) rootView.findViewById(R.id.google_sign_in_button);
        rosefireLoginButton = rootView.findViewById(R.id.rosefire_sign_in_button);

        mGoogleSignInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithGoogle();
            }
        });
        rosefireLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithRosefire();
            }
        });
        return rootView;
    }

    private void loginWithRosefire() {
        if (mLoggingIn) {
            return;
        }

        showProgress(true);
        mLoggingIn = true;
        mListener.onRosefireLogin();
    }


    private void loginWithGoogle() {
        if (mLoggingIn) {
            return;
        }

        showProgress(true);
        mLoggingIn = true;
        mListener.onGoogleLogin();
    }

    public void onLoginError(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.login_error))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();

        showProgress(false);
        mLoggingIn = false;
    }

    private void showProgress(boolean show) {
        mProgressSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        mGoogleSignInButton.setVisibility(show ? View.GONE : View.VISIBLE);
        rosefireLoginButton.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnLoginListener {
        void onLogin(String email, String password);

        void onGoogleLogin();

        void onRosefireLogin();
    }
}
