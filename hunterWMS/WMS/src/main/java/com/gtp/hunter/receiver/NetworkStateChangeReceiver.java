package com.gtp.hunter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;

import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.R;

public class NetworkStateChangeReceiver extends BroadcastReceiver {
    private TextView label;

    public NetworkStateChangeReceiver(TextView label) {
        this.label = label;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isAvailable()) {
            if (BuildConfig.DEBUG)
                Log.d("Network Available ", "Flag No 1");
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager.isWifiEnabled()) {
                WifiInfo info = wifiManager.getConnectionInfo();
                String bssid = info.getBSSID();

                if (label != null)
                    label.setText(context.getString(R.string.wifiBSSID, bssid != null ? bssid.toUpperCase() : context.getApplicationContext().getString(R.string.wifi_disconnected)));
            }
        }
    }

    public void setLabel(TextView tv) {
        this.label = tv;
    }
}
