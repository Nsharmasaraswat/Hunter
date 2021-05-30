//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.gtp.hunter.rfid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.rfid.asciiprotocol.AsciiCommander;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothDeviceActivity extends AppCompatActivity {
    // Debugging
    private static final String TAG = "BluetoothDeviceActivity";
    //    private static final boolean D = true;
    private static final boolean D = BuildConfig.DEBUG;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mDevice = null;

    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander() {
        return ((HunterMobileWMS) getApplication()).getCommander();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            HunterMobileWMS.setRFIDDisabled(true);
            Toast.makeText(this, getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            HunterMobileWMS.setRFIDDisabled(true);
            startActivity(enableBtIntent);
        }

        if (HunterMobileWMS.isRFIDAvailable()) {
            // Create the AsciiCommander to talk to the reader (if it doesn't already exist)
            if (getCommander() == null) {
                try {
                    HunterMobileWMS app = (HunterMobileWMS) getApplication();
                    AsciiCommander commander = new AsciiCommander(getApplicationContext());
                    app.setCommander(commander);

                } catch (Exception e) {
                    fatalError("Unable to create AsciiCommander!");
                }
            }
        }
    }

    //
    // Terminate the app with the given message
    //
    private void fatalError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                finish();
            }
        }, 1800);
    }

    /**
     * Override this method to provide custom handling of the (fatal)
     * situation when bluetooth is not available
     *
     * @param message the message describing the cause of the error
     */
    protected void bluetoothNotAvailableError(String message) {
        fatalError(message);
    }


    @Override
    public void onStart() {
        super.onStart();

        if (HunterMobileWMS.isRFIDAvailable()) {
            // If no other attempt to connect is ongoing try to connect to last used reader
            // Note: When returning from the Device List activity
            if (mDevice == null) {
                // Attempt to reconnect to the last reader used
                if (BuildConfig.DEBUG)
                    Toast.makeText(this, getText(R.string.msg_reconnect_reader), Toast.LENGTH_SHORT).show();

                getCommander().connect(null);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (HunterMobileWMS.isRFIDAvailable()) {
            getCommander().disconnect();
            mDevice = null;
        }
    }

    /**
     * Connect the current AsciiCommander to the given device
     *
     * @param deviceData the device information received from the DeviceListActivity
     * @param secure     true if a secure connection should be requested
     */
    private void connectToDevice(Intent deviceData, boolean secure) {
        Toast.makeText(this.getApplicationContext(), getText(R.string.msg_reader_reconnecting), Toast.LENGTH_LONG).show();
        // Get the device MAC address
        String address = Objects.requireNonNull(deviceData.getExtras()).getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        if (mDevice != null) {
            getCommander().connect(mDevice);
        } else {
            if (D) Log.e(TAG, getString(R.string.msg_unable_reconnect));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (D)
            Log.d(TAG, "selectDevice() onActivityResult: " + resultCode + " for request: " + requestCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectToDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectToDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    bluetoothNotAvailableError(getString(R.string.bt_not_enabled_leaving));
                }
        }
    }

    /**
     * Launches an activity that allows user to select a device to use
     */
    public void selectDevice() {
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }


    /**
     * Disconnects the currently connected device
     */
    public void disconnectDevice() {
        mDevice = null;
        getCommander().disconnect();
    }

    /**
     * Reconnects to the last successfully connected reader
     */
    public void reconnectDevice() {
        getCommander().connect(null);
    }
}
