package com.gtp.hunter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.gtp.hunter.BuildConfig;


public class SupplicantConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);

        if (BuildConfig.DEBUG)
            Toast.makeText(context, "Supplicant" + (connected ? "CONNECTED" : "DISCONNECTED"), Toast.LENGTH_LONG).show();
    }
}
