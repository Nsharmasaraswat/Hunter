package com.gtp.hunter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import com.gtp.hunter.R;

public class WiFiRSSIChangeReceiver extends BroadcastReceiver {
    private TextView label;

    public WiFiRSSIChangeReceiver(TextView label) {
        this.label = label;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String bssid = info.getBSSID();
        int rssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -127);
        int level = WifiManager.calculateSignalLevel(rssi, 5);
        int linkSpeed = info.getLinkSpeed();

        if (label != null)
            label.setText(context.getString(R.string.wifiRSSI, rssi, level).concat(" - ").concat(context.getString(R.string.wifiBSSID, bssid != null ? bssid.toUpperCase() : context.getApplicationContext().getString(R.string.wifi_disconnected))).concat("LS: " + linkSpeed).concat(WifiInfo.LINK_SPEED_UNITS));
    }

    public void setLabel(TextView tv) {
        this.label = tv;
    }
}
