package com.gtp.hunter.wms.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.rfid.BluetoothDeviceActivity;
import com.gtp.hunter.rfid.RFIDModelBase;
import com.gtp.hunter.rfid.WeakHandler;
import com.gtp.hunter.rfid.asciiprotocol.AsciiCommander;
import com.gtp.hunter.rfid.asciiprotocol.DeviceProperties;
import com.gtp.hunter.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.gtp.hunter.rfid.asciiprotocol.enumerations.QuerySession;
import com.gtp.hunter.rfid.asciiprotocol.enumerations.TriState;
import com.gtp.hunter.rfid.asciiprotocol.parameters.AntennaParameters;
import com.gtp.hunter.rfid.asciiprotocol.responders.LoggerResponder;
import com.gtp.hunter.wms.client.ThingClient;
import com.gtp.hunter.wms.inventory.InventoryRFIDModel;
import com.gtp.hunter.wms.model.Thing;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

public class InventoryActivity extends BluetoothDeviceActivity implements Observer {
    private static final String DOCUMENT_ID = "DOCUMENT_ID";

    // Debug control
    private static final boolean D = BuildConfig.DEBUG;

    // The list of results from actions
    private ArrayAdapter<String> mResultsArrayAdapter;
    private ListView mResultsListView;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    // Error report
    private TextView mResultTextView;

    //List of seen tags to avoid duplicates
    private final List<String> seenTags = new CopyOnWriteArrayList<>();

    //hunter Thing Rest Client
    private ThingClient tClient;

    private Handler uiHandler;

    // Custom adapter for the session values to display the description rather than the toString() value
    public class SessionArrayAdapter extends ArrayAdapter<QuerySession> {

        private final QuerySession[] mValues;

        SessionArrayAdapter(Context context, int textViewResourceId, QuerySession[] objects) {
            super(context, textViewResourceId, objects);
            mValues = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setText(mValues[position].getDescription());
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setText(mValues[position].getDescription());
            return view;
        }
    }

    // The session
    private final QuerySession[] mSessions = new QuerySession[]{
            QuerySession.SESSION_0,
            QuerySession.SESSION_1,
            QuerySession.SESSION_2,
            QuerySession.SESSION_3
    };

    // All of the reader inventory tasks are handled by this class
    private InventoryRFIDModel mModel;

    //----------------------------------------------------------------------------------------------
    // OnCreate life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        uiHandler = new Handler();
        //
        // An AsciiCommander has been created by the base class
        //
        AsciiCommander commander = HunterMobileWMS.isRFIDAvailable() ? getCommander() : null;

        mResultsArrayAdapter = new ArrayAdapter<>(this, R.layout.item_inventory_result);

        mResultTextView = findViewById(R.id.resultTextView);

        // Find and set up the results ListView
        mResultsListView = findViewById(R.id.resultListView);
        mResultsListView.setAdapter(mResultsArrayAdapter);
        mResultsListView.setFastScrollEnabled(true);

        MaterialButton cButton = findViewById(R.id.clearButton);
        cButton.setOnClickListener(mClearButtonListener);

        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = findViewById(R.id.powerTextView);
        mPowerSeekBar = findViewById(R.id.powerSeekBar);
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);

        // Set the seek bar current value and to cover the range of the power settings 
        setPowerBarLimits();
        mPowerLevel = commander == null ? 0 : getCommander().getDeviceProperties().getMaximumCarrierPower();
        mPowerSeekBar.setProgress(commander == null ? 0 : mPowerLevel - getCommander().getDeviceProperties().getMinimumCarrierPower());

        // The list of sessions that can be selected
        SessionArrayAdapter mSessionArrayAdapter = new SessionArrayAdapter(this, android.R.layout.simple_spinner_item, mSessions);
        // Find and set up the sessions spinner
        Spinner spinner = findViewById(R.id.sessionSpinner);
        mSessionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mSessionArrayAdapter);
        spinner.setOnItemSelectedListener(mActionSelectedListener);
        spinner.setSelection(0);

        // Set up Fast Id check box listener
        CheckBox cb = findViewById(R.id.fastIdCheckBox);
        cb.setOnClickListener(mFastIdCheckBoxListener);

        if (commander != null) {
            // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
            // and passes the line onto the next responder
            // This is added first so that no other responder can consume received lines before they are logged.
            commander.addResponder(new LoggerResponder());

            // Add a synchronous responder to handle synchronous commands
            commander.addSynchronousResponder();

        }

        //Create a (custom) model and configure its commander and handler
        mModel = new InventoryRFIDModel();
        mModel.setCommander(commander);
        mModel.setHandler(mGenericModelHandler);


        mResultsListView.setEnabled(HunterMobileWMS.isRFIDAvailable());
        mPowerSeekBar.setEnabled(HunterMobileWMS.isRFIDAvailable());
        cb.setEnabled(HunterMobileWMS.isRFIDAvailable());
        spinner.setEnabled(HunterMobileWMS.isRFIDAvailable());
        cButton.setEnabled(HunterMobileWMS.isRFIDAvailable());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        String docId = getIntent().getStringExtra(DOCUMENT_ID);

        tClient = new ThingClient(getBaseContext());
        tClient.addObserver(this);
    }

    //----------------------------------------------------------------------------------------------
    // Pause & Resume life cycle
    //----------------------------------------------------------------------------------------------

    @Override
    public synchronized void onPause() {
        super.onPause();

        mModel.setEnabled(false);

        // Unregister to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommanderMessageReceiver);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        mModel.setEnabled(true);

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommanderMessageReceiver,
                new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        displayReaderState();
        UpdateUI();
    }


    //----------------------------------------------------------------------------------------------
    // Menu
    //----------------------------------------------------------------------------------------------

    private MenuItem mReconnectMenuItem;
    private MenuItem mConnectMenuItem;
    private MenuItem mDisconnectMenuItem;
    private MenuItem mResetMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reader, menu);

        mResetMenuItem = menu.findItem(R.id.reset_reader_menu_item);
        mReconnectMenuItem = menu.findItem(R.id.reconnect_reader_menu_item);
        mConnectMenuItem = menu.findItem(R.id.insecure_connect_reader_menu_item);
        mDisconnectMenuItem = menu.findItem(R.id.disconnect_reader_menu_item);
        return true;
    }


    /**
     * Prepare the menu options
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean isConnected = HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected();
        mResetMenuItem.setEnabled(isConnected);
        mDisconnectMenuItem.setEnabled(isConnected);

        mReconnectMenuItem.setEnabled(!isConnected);
        mConnectMenuItem.setEnabled(!isConnected);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Respond to menu item selections
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.reconnect_reader_menu_item:
                if (HunterMobileWMS.isRFIDAvailable()) {
                    Toast.makeText(this.getApplicationContext(), getText(R.string.msg_reader_reconnecting), Toast.LENGTH_LONG).show();
                    reconnectDevice();
                    UpdateUI();
                } else
                    Toast.makeText(this.getApplicationContext(), getText(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
                return true;

            case R.id.insecure_connect_reader_menu_item:
                if (HunterMobileWMS.isRFIDAvailable()) {
                    // Choose a device and connect to it
                    selectDevice();
                } else
                    Toast.makeText(this.getApplicationContext(), getText(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
                return true;

            case R.id.disconnect_reader_menu_item:
                if (HunterMobileWMS.isRFIDAvailable()) {
                    Toast.makeText(this.getApplicationContext(), getText(R.string.msg_reader_disconnecting), Toast.LENGTH_SHORT).show();
                    disconnectDevice();
                    displayReaderState();
                } else
                    Toast.makeText(this.getApplicationContext(), getText(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
                return true;

            case R.id.reset_reader_menu_item:
                if (HunterMobileWMS.isRFIDAvailable()) {
                    resetReader();
                    UpdateUI();
                } else
                    Toast.makeText(this.getApplicationContext(), getText(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------
    // Model notifications
    //----------------------------------------------------------------------------------------------
    private final WeakHandler<InventoryActivity> mGenericModelHandler = new WeakHandler<InventoryActivity>(this) {

        @Override
        public void handleMessage(Message msg, InventoryActivity thisActivity) {
            try {
                switch (msg.what) {
                    case RFIDModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        //TODO: process change in model busy state
                        break;

                    case RFIDModelBase.MESSAGE_NOTIFICATION:
                        // Examine the message for prefix
                        String message = (String) msg.obj;
                        if (message.startsWith("ER:")) {
                            mResultTextView.setText(message.substring(3));
                        } else if (message.startsWith("BC:")) {
                            //TODO: Barcode
                        } else if (message.startsWith("EPC:")) {
                            String epc = message.replace("EPC: ", "").substring(0, 24);

                            if (!seenTags.contains(epc)) {
                                tClient.asyncFindByTagId(epc);
                                mResultsArrayAdapter.add(epc);
                                scrollResultsListViewToBottom();
                                seenTags.add(epc);
                            }
                        } else {
//                            mResultsArrayAdapter.add(message);
//                            scrollResultsListViewToBottom();
                        }
                        UpdateUI();
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };


    //----------------------------------------------------------------------------------------------
    // UI state and display update
    //----------------------------------------------------------------------------------------------

    private void displayReaderState() {
        String connectionMsg = "Reader: " + (HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Disconnected");
        setTitle(connectionMsg);
    }


    //
    // Set the state for the UI controls
    //
    private void UpdateUI() {
        //boolean isConnected = getCommander().isConnected();
        //TODO: configure UI control state
    }


    private void scrollResultsListViewToBottom() {
        mResultsListView.post(() -> {
            // Select the last row so it will scroll into view...
            mResultsListView.setSelection(mResultsArrayAdapter.getCount() - 1);
        });
    }

    //----------------------------------------------------------------------------------------------
    // AsciiCommander message handling
    //----------------------------------------------------------------------------------------------

    //
    // Handle the messages broadcast from the AsciiCommander
    //
    private final BroadcastReceiver mCommanderMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) {
                Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected());
            }

            String connectionStateMsg = intent.getStringExtra(AsciiCommander.REASON_KEY);
            Toast.makeText(context, connectionStateMsg, Toast.LENGTH_SHORT).show();

            displayReaderState();
            if (getCommander().isConnected()) {
                // Update for any change in power limits
                setPowerBarLimits();
                // This may have changed the current power level setting if the new range is smaller than the old range
                // so update the model's inventory command for the new power value
                mModel.getCommand().setOutputPower(mPowerLevel);

                mModel.resetDevice();
                mModel.updateConfiguration();
            }

            UpdateUI();
        }
    };

    //----------------------------------------------------------------------------------------------
    // Reader reset
    //----------------------------------------------------------------------------------------------

    //
    // Handle reset controls
    //
    private void resetReader() {
        try {
            // Reset the reader
            FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
            getCommander().executeCommand(fdCommand);
            String msg = "Reset " + (fdCommand.isSuccessful() ? "succeeded" : "failed");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            UpdateUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //----------------------------------------------------------------------------------------------
    // Power seek bar
    //----------------------------------------------------------------------------------------------

    //
    // Set the seek bar to cover the range of the currently connected device
    // The power level is capped when necessary
    //
    private void setPowerBarLimits() {
        if (HunterMobileWMS.isRFIDAvailable()) {
            DeviceProperties deviceProperties = getCommander().getDeviceProperties();

            mPowerSeekBar.setMax(deviceProperties.getMaximumCarrierPower() - deviceProperties.getMinimumCarrierPower());

            if (mPowerLevel > deviceProperties.getMaximumCarrierPower()) {
                mPowerLevel = deviceProperties.getMaximumCarrierPower();
            } else if (mPowerLevel < deviceProperties.getMinimumCarrierPower()) {
                mPowerLevel = deviceProperties.getMinimumCarrierPower();
            }

            mPowerSeekBar.setProgress(mPowerLevel - deviceProperties.getMinimumCarrierPower());
        }
    }


    //
    // Handle events from the power level seek bar. Update the mPowerLevel member variable for use in other actions
    //
    private final OnSeekBarChangeListener mPowerSeekBarListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Nothing to do here
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            // Update the reader's setting only after the user has finished changing the value
            if (HunterMobileWMS.isRFIDAvailable())
                updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + seekBar.getProgress());
            mModel.getCommand().setOutputPower(mPowerLevel);
            mModel.updateConfiguration();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (HunterMobileWMS.isRFIDAvailable())
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + progress);
        }
    };

    private void updatePowerSetting(int level) {
        String powerLevelText = String.valueOf(level) + getText(R.string.power_level_label_text);

        mPowerLevel = level;
        mPowerLevelTextView.setText(powerLevelText);
    }


    //----------------------------------------------------------------------------------------------
    // Button event handlers
    //----------------------------------------------------------------------------------------------

    // Clear action
    private final OnClickListener mClearButtonListener = new OnClickListener() {
        public void onClick(View v) {
            try {
                // Clear the list
                mResultsArrayAdapter.clear();
                seenTags.clear();
                UpdateUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //----------------------------------------------------------------------------------------------
    // Handler for changes in session
    //----------------------------------------------------------------------------------------------

    private final AdapterView.OnItemSelectedListener mActionSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (mModel.getCommand() != null) {
                QuerySession targetSession = (QuerySession) parent.getItemAtPosition(pos);
                mModel.getCommand().setQuerySession(targetSession);
                mModel.updateConfiguration();
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for changes in FastId
    //----------------------------------------------------------------------------------------------

    private final OnClickListener mFastIdCheckBoxListener = new OnClickListener() {
        public void onClick(View v) {
            try {
                CheckBox fastIdCheckBox = (CheckBox) v;
                mModel.getCommand().setUsefastId(fastIdCheckBox.isChecked() ? TriState.YES : TriState.NO);
                mModel.updateConfiguration();

                UpdateUI();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void update(Observable observable, Object o) {
        if (o != null) {
            if (o instanceof Thing) {
                Thing t = (Thing) o;

                mResultsArrayAdapter.add(t.getName());
                scrollResultsListViewToBottom();
            }
        } else {
            runOnUiThread(() -> Toast.makeText(InventoryActivity.this, getText(R.string.msg_no_tag_found), Toast.LENGTH_SHORT).show());
        }
    }
}
