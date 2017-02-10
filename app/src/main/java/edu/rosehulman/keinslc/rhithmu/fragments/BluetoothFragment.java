package edu.rosehulman.keinslc.rhithmu.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.rosehulman.keinslc.rhithmu.DeviceListActivity;
import edu.rosehulman.keinslc.rhithmu.Event;
import edu.rosehulman.keinslc.rhithmu.R;
import edu.rosehulman.keinslc.rhithmu.Utils.Constants;
import edu.rosehulman.keinslc.rhithmu.Utils.EventUtils;
import edu.rosehulman.keinslc.rhithmu.Utils.SyncCalendarService;

import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.PREFS_NAME;
import static edu.rosehulman.keinslc.rhithmu.Utils.Constants.PREF_MPATH;
import static edu.rosehulman.keinslc.rhithmu.Utils.SyncCalendarService.STATE_CONNECTED;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 * This includes a handler to handle passing events from the threaded SyncCalendarService and this UI thread
 * This also requires that it's context implements a callback interface for passing events back once done.
 */
public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private String recievedEvents;
    private boolean hasSentEvents;
    private boolean hasRecievedEvents;
    private bluetoothCallbackListener mCallbackListener;

    private List<Event> mList = new ArrayList<>();

    // Layout Views
    private ListView mConversationView;
    private Button mSendButton;
    private Button mMatchButton;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            // If someone mashes the back very quickly, this is nulled
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case SyncCalendarService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case SyncCalendarService.STATE_LISTEN:
                        case SyncCalendarService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    // construct a string from the buffer
                    setHasSentEvents();
                    mConversationArrayAdapter.add("Me:  " + "Sent Events");
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    recievedEvents += readMessage;
                    setHasReceivedEvents();
                    mConversationArrayAdapter.add("Received Event from " + mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(activity, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Member object for the chat services
     */
    private SyncCalendarService mChatService = null;


    /*------------------------------------------Recieved Events Setter------------------------------------------------*/
    private void setHasReceivedEvents() {
        hasRecievedEvents = true;
        if (hasSentEvents) {
            mMatchButton.setClickable(true);
            Toast.makeText(getActivity(), "Ready to Match", Toast.LENGTH_LONG).show();
        }
    }

    private void setHasSentEvents() {
        hasSentEvents = true;
        if (hasRecievedEvents) {
            mMatchButton.setClickable(true);
            Toast.makeText(getActivity(), "Ready to Match", Toast.LENGTH_LONG).show();
        }
    }

    /*-------------LIFE CYCLE METHODS-------------------------------*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Activity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String mPath = prefs.getString(PREF_MPATH, "NoUid");
        //Only Query Firebase for the next two weeks of stuff
        // TODO: Fix this firebase Query (Probably need to fix the indexing since it seems to be broken)
        Calendar cal = Calendar.getInstance();
        long l = cal.getTimeInMillis();
        long TWO_WEEKS_IN_MILIS = 1209600000;
        l += TWO_WEEKS_IN_MILIS;
        Query mQuery = FirebaseDatabase.getInstance().getReference().child(mPath).orderByChild("startTimeInMilis").endAt(l);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mList.add(ds.getValue(Event.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Don't care
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mMatchButton = (Button) view.findViewById(R.id.button_match);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof bluetoothCallbackListener) {
            mCallbackListener = (bluetoothCallbackListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventSelectedListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    /*Helps prevent crashing when going backwards since the fragment is not destroyed*/
    @Override
    public void onPause() {
        super.onPause();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == SyncCalendarService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /*--------------------HELPERS / SETUP METHODS-----------------------------------*/

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    if (mChatService.getState() == STATE_CONNECTED) {
                        Toast.makeText(getActivity(), "Working", Toast.LENGTH_SHORT).show();
                    }
                    String message = EventUtils.getJSONifiedString(mList);
                    sendMessage(message);
                }
            }
        });
        // Parse events from JSON, Match Them, then send them back to mainactivity for processing
        mMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatService.stop();
                // Init event list list
                List<List<Event>> eventListList = new ArrayList<List<Event>>();
                //Gson sucks so make a list from it
                List<Event> thisIsTheEventListIHadToMakeBecauseGsonSucks = EventUtils.getEventListfromJSON(recievedEvents);
                //Fix the stupid calendar bugs
                for (Event e : thisIsTheEventListIHadToMakeBecauseGsonSucks) {
                    e.setStartTimeInMilis(e.getStartTimeInMilis());
                    e.setEndTimeInMilis(e.getEndTimeInMilis());
                }
                //Stick that list in our event list list
                eventListList.add(thisIsTheEventListIHadToMakeBecauseGsonSucks);
                //Stick our events in the list list
                eventListList.add(mList);
                // Match that list list and get a list back
                List<Event> matched = EventUtils.match(eventListList);
                //Parcleable balks at the idea of a generic list so we make that matched list into a slightly different list
                ArrayList<Event> output = new ArrayList<>(matched);
                // Pass that there list along
                mCallbackListener.onSchedulesMatch(output);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new SyncCalendarService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }


    /*-----------------ON ACTIVITY RESULT----------------------------------------*/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
        mSendButton.setClickable(true);
        mMatchButton.setClickable(false);
        hasRecievedEvents = false;
        hasSentEvents = false;
    }

    /*-------------------OPTIONS MENU STUFF-------------------------------------------*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    public interface bluetoothCallbackListener {
        void onSchedulesMatch(ArrayList<Event> events);
    }

}
