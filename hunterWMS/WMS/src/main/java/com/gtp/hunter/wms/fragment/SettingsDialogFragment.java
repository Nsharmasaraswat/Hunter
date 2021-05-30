package com.gtp.hunter.wms.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.HunterURL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsDialogFragment extends DialogFragment {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");

    SettingsDialogListener mSettingsListener;

    SwitchMaterial swcSSL;
    EditText txtIp;
    EditText txtPort;
    MaterialButton btnSave;


    public static SettingsDialogFragment newInstance() {
        return new SettingsDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_settings, container, false);

        swcSSL = v.findViewById(R.id.swcSSL);
        txtIp = v.findViewById(R.id.txtIP);
        txtPort = v.findViewById(R.id.txtPort);
        btnSave = v.findViewById(R.id.btnSaveSettings);
        swcSSL.setChecked(HunterURL.useSSL);
        txtIp.setText(HunterURL.IP);
        txtPort.setText(HunterURL.PORT);
        btnSave.setOnClickListener((View vw) -> saveSettings());
        return v;
    }

    private void saveSettings() {
        String ip = txtIp.getText().toString().trim();
        String port = txtPort.getText().toString();
        Matcher m = IPV4_PATTERN.matcher(ip);

        if (m.matches()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();

            HunterURL.changeURL(ip, port, swcSSL.isChecked());
            editor.putString(getString(R.string.pref_key_server_ip), txtIp.getText().toString());
            editor.putString(getString(R.string.pref_key_server_port), txtPort.getText().toString());
            editor.putBoolean(getString(R.string.pref_key_use_ssl), swcSSL.isChecked());
            editor.putBoolean(getString(com.gtp.hunter.vision.R.string.pref_key_camera_live_viewport), true);
            editor.putString(getString(com.gtp.hunter.vision.R.string.pref_key_camerax_target_analysis_size), "1200x1600");
            editor.apply();
            mSettingsListener.settingsChanged();
            dismiss();
        } else
            Toast.makeText(getContext(), getString(R.string.msg_invalid_ip), Toast.LENGTH_LONG).show();
    }

    public interface SettingsDialogListener {
        void settingsChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mSettingsListener = (SettingsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement NewItemDialogFragment");
        }
    }
}
