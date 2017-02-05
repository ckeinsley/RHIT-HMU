package edu.rosehulman.keinslc.rhithmu.Utils;

/**
 * Created by keinslc on 1/28/2017.
 */

public class Constants {
    /*Used for transition from weekView to AddEditDeleteEventFragment*/
    public static final String ARG_EVENT = "myEventArgument";
    //public static final String ARG_KEY = "myEventKey";
    public static final String ARG_PATH = "userPath";
    /*Used for transition from login fragment to weekView*/
    public static final String FIREBASE_PATH = "myFirebasePath";
    public static final int RC_ROSEFIRE_LOGIN = 34;
    public static final int RC_GOOGLE_LOGIN = 56;
    /*Log Tags*/
    public static final String TAG_WEEK_VIEW = "WEEK_VIEW";
    public static final String TAG_MAIN_ACTIVITY = "MAIN_ACTIVITY";
    public static final String TAG_EDIT_FRAG = "EDIT_FRAG";
    /*Bluetooth*/
    public static final String TAG_ACCEPT_THREAD = "ACCEPT_THREAD";
    public static final String TAG_CONNECT_THREAD = "CONNECT_THREAD";
    public static final String TAG_CONNECTED_THREAD = "CONNECTED_THREAD";
    public static final int REQUEST_CONNECT_DEVICE = 61;
    public static final int REQUEST_ENABLE_BT = 62;
    public static final int REQUEST_BLUETOOTH_CONNECT = 1001;
}
