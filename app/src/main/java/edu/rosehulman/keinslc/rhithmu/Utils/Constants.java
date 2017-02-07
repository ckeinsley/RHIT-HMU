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
    // Message types sent from the SyncCalendarService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the SyncCalendarService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}
