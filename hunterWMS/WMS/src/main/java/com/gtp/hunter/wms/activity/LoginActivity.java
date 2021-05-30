package com.gtp.hunter.wms.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.barcode.BarcodeActivity;
import com.gtp.hunter.biometric.BiometricCallback;
import com.gtp.hunter.biometric.BiometricManager;
import com.gtp.hunter.util.BiometricUtils;
import com.gtp.hunter.util.CryptoUtil;
import com.gtp.hunter.vision.ChooserActivity;
import com.gtp.hunter.wms.api.AuthAPI;
import com.gtp.hunter.wms.api.HunterURL;
import com.gtp.hunter.wms.client.UserClient;
import com.gtp.hunter.wms.fragment.SettingsDialogFragment;
import com.gtp.hunter.wms.model.Auth;
import com.gtp.hunter.wms.model.PreAuth;
import com.gtp.hunter.wms.model.User;
import com.gtp.hunter.wms.model.Validate;

import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static retrofit2.Retrofit.Builder;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity implements SettingsDialogFragment.SettingsDialogListener, Observer, BiometricCallback {
    private static final String SETTINGS_TAG = "settings_fragment";
    private static final int REQUEST_PERMISSIONS_TOKEN = 129;

    private ConstraintLayout mControlsView;
    private TextInputEditText txtLogin;
    private TextInputEditText txtPassword;
    private CheckBox chbSavePassword;
    private MaterialButton btnLogin;
    private LinearLayout greyOverlay;
    private ProgressBar loginProgress;

    private String userName;

    private BiometricManager mBiometricManager;
//    private LocationManager mLocationManager;

    private AuthAPI authAPI;
    private final Callback<PreAuth> preAuthCallback = new Callback<PreAuth>() {

        @Override
        public void onResponse(@NonNull Call<PreAuth> call, @NonNull Response<PreAuth> response) {
            if (response.isSuccessful()) {
                PreAuth resp = response.body();

                if (resp != null) {
                    String pwd = Objects.requireNonNull(txtPassword.getText()).toString();
                    Auth auth = new Auth(resp.getUserId(), resp.getType().equals("CredentialNTDS") ? pwd : hashPassword(pwd, resp.getSalt(), resp.getSession()));

                    authAPI.validate(userName, auth).enqueue(validateCallback);
                } else {
                    useTypedAuthentication();
                    Toast.makeText(LoginActivity.this, getText(R.string.invalid_user_pass), Toast.LENGTH_LONG).show();
                    unlockUI();
                }
            } else {
                useTypedAuthentication();
                Timber.d("Code: %d Message: %s", response.code(), response.message());
                Timber.e(response.raw().toString());
                Toast.makeText(LoginActivity.this, getText(R.string.invalid_user_pass), Toast.LENGTH_LONG).show();
                unlockUI();
            }
        }

        @Override
        public void onFailure(@NonNull Call<PreAuth> call, @NonNull Throwable t) {
            useTypedAuthentication();
            Timber.e(t, "onFailure: ");
            if (t instanceof SocketTimeoutException)
                Toast.makeText(LoginActivity.this, getString(R.string.server_unavailable), Toast.LENGTH_LONG).show();
            else if (t instanceof EOFException)
                Toast.makeText(LoginActivity.this, getString(R.string.invalid_user_pass), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(LoginActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            unlockUI();
        }
    };

    private String hashPassword(String pwd, String salt, String session) {
        String step1 = CryptoUtil.getPbkdf2(pwd, CryptoUtil.byteFromHex(salt));

        return CryptoUtil.getPbkdf2(step1, CryptoUtil.byteFromHex(session));
    }

    private final Callback<Validate> validateCallback = new Callback<Validate>() {

        @Override
        public void onResponse(@NonNull Call<Validate> call, Response<Validate> response) {
            if (response.isSuccessful()) {
                Validate resp = response.body();

                if (resp != null) {
                    HunterMobileWMS.setToken(resp.getToken());
                    loadUser();
                } else {
                    Toast.makeText(LoginActivity.this, getText(R.string.invalid_user_pass), Toast.LENGTH_LONG).show();
                }
            } else {
                Timber.d("Code: %d Message: %s", response.code(), response.message());
                Timber.e(response.raw().toString());
                Toast.makeText(LoginActivity.this, getText(R.string.invalid_user_pass), Toast.LENGTH_LONG).show();
                unlockUI();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Validate> call, @NonNull Throwable t) {
            Timber.e(t, "OnFailure");
            Toast.makeText(LoginActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            unlockUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if (!preferences.contains(getString(com.gtp.hunter.vision.R.string.pref_key_camera_live_viewport))) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(getString(com.gtp.hunter.vision.R.string.pref_key_camera_live_viewport), true);
        editor.putString(getString(com.gtp.hunter.vision.R.string.pref_key_camerax_target_analysis_size), "1200x1600");
        editor.apply();
//        }
        HunterURL.changeURL(preferences.getString(getString(R.string.pref_key_server_ip), "10.62.132.45"), preferences.getString(getString(R.string.pref_key_server_port), "8080"), preferences.getBoolean(getString(R.string.pref_key_use_ssl), false));
        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        setContentView(R.layout.activity_login);
        loginProgress = findViewById(R.id.loginProgressBar);
        greyOverlay = findViewById(R.id.greyoutOverlay);
        mControlsView = findViewById(R.id.frameLayout);
        mControlsView.setOnClickListener((View v) -> showFullScreen());
        if (BuildConfig.DEBUG) {
            findViewById(R.id.imgLogo).setOnTouchListener(new View.OnTouchListener() {
                private boolean firstTap;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (firstTap) {
                                unlockUI();
                                Intent intent = new Intent(getBaseContext(), ChooserActivity.class);
                                startActivity(intent);
                            }
                            firstTap = !firstTap;
                            break;
                        case MotionEvent.ACTION_UP:
                            Executors.newSingleThreadScheduledExecutor().schedule(() -> firstTap = false, 200, TimeUnit.MILLISECONDS);
                            break;
                    }
                    v.performClick();
                    return true;
                }
            });
        }
//        findViewById(R.id.imgSettings).setOnClickListener((view) -> demo());
        findViewById(R.id.imgSettings).setOnClickListener((view) -> openSettings());
        txtLogin = findViewById(R.id.etUsername);
        txtPassword = findViewById(R.id.etPassword);
        chbSavePassword = findViewById(R.id.chbSavePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener((View v) -> {
            btnLogin.setEnabled(false);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            loginProgress.setVisibility(View.VISIBLE);
            greyOverlay.setVisibility(View.VISIBLE);
            userName = Objects.requireNonNull(txtLogin.getText()).toString().trim().toLowerCase();
            authAPI.preAuth(userName).enqueue(preAuthCallback);
        });
        btnLogin.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (hasFocus)
                hideKeyboard();//hide keyboard when focus gets to button (mostly for development in emulator
        });
        mBiometricManager = new BiometricManager.BiometricBuilder(LoginActivity.this)
                .setTitle(getString(R.string.biometric_title))
                .setSubtitle(getString(R.string.biometric_subtitle))
                .setDescription(getString(R.string.biometric_description))
                .setNegativeButtonText(getString(R.string.biometric_negative_button_text))
                .build();
//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocChangeListener());
        updateAppInfo();
    }

    /*---------- Listener class to get coordinates ------------- */
//    private class LocChangeListener implements LocationListener {
//
//        @Override
//        public void onLocationChanged(Location loc) {
//            /*------- To get city name from coordinates -------- */
//            String cityName = null;
//            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
//            List<Address> addresses;
//
//            try {
//                addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
//                if (addresses.size() > 0) {
//                    cityName = addresses.get(0).getLocality();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            if (BuildConfig.DEBUG) {
//                Toast.makeText(getBaseContext(), "Location changed: Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude() + " City: " + cityName, Toast.LENGTH_SHORT).show();
//                Timber.d("Longitude: %f", loc.getLongitude());
//                Timber.d("Latitude: %f", loc.getLatitude());
//            }
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

        checkPermissions();
        HunterMobileWMS.setUser(null);
        HunterMobileWMS.setProductList(null);
        HunterMobileWMS.setAddressList(null);
        if (!"".equals(preferences.getString(getString(R.string.pref_key_saved_user), ""))) {
            chbSavePassword.setChecked(true);
            mBiometricManager.authenticate(this);
        } else if (BiometricUtils.isBiometricAvailable(this)) {
            chbSavePassword.setVisibility(View.VISIBLE);
            useTypedAuthentication();
        } else
            useTypedAuthentication();
        showFullScreen();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        createAuthAPI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Toast.makeText(LoginActivity.this, data.toString(), Toast.LENGTH_LONG).show();
                    break;
                case 346:
                    assert data != null;
                    String displayValue = data.getStringExtra("DISPLAY_VALUE");
                    String rawValue = data.getStringExtra("RAW_VALUE");

                    if (BuildConfig.DEBUG)
                        Timber.d(Objects.toString(displayValue));
                    if (BuildConfig.DEBUG)
                        Timber.d(Objects.toString(rawValue));
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip;

                    assert rawValue != null;
                    if (rawValue.contains("sender_id")) {
                        MLDemoPackage pkg = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(rawValue, MLDemoPackage.class);

                        clip = ClipData.newPlainText("PACKAGE", pkg.getId());
                    } else if (rawValue.contains("container_id")) {
                        MLDemoCage cage = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(rawValue, MLDemoCage.class);

                        clip = ClipData.newPlainText("PACKAGE", cage.getContainer_id());
                    } else
                        clip = ClipData.newPlainText("NONE", "");


                    clipboard.setPrimaryClip(clip);
                    break;
            }
        }
    }

    private static class MLDemoCage {
        @Expose
        private String container_id;

        @Expose
        private String facility_id;

        public String getContainer_id() {
            return container_id;
        }

        public void setContainer_id(String container_id) {
            this.container_id = container_id;
        }

        public String getFacility_id() {
            return facility_id;
        }

        public void setFacility_id(String facility_id) {
            this.facility_id = facility_id;
        }
    }

    private static class MLDemoPackage {
        @Expose
        private String id;

        @Expose
        private String sender_id;

        @Expose
        private String hash_code;

        @Expose
        private String security_digit;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSender_id() {
            return sender_id;
        }

        public void setSender_id(String sender_id) {
            this.sender_id = sender_id;
        }

        public String getHash_code() {
            return hash_code;
        }

        public void setHash_code(String hash_code) {
            this.hash_code = hash_code;
        }

        public String getSecurity_digit() {
            return security_digit;
        }

        public void setSecurity_digit(String security_digit) {
            this.security_digit = security_digit;
        }
    }

    private void demo() {
        Intent intent = new Intent(LoginActivity.this, BarcodeActivity.class);

        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 346);
    }

    private void openSettings() {
        FragmentManager manager = getSupportFragmentManager();
        SettingsDialogFragment dynamicFormDialog = SettingsDialogFragment.newInstance();

        // close existing dialog fragments
        Fragment frag = manager.findFragmentByTag(SETTINGS_TAG);

        if (frag != null)
            manager.beginTransaction().remove(frag).commit();
        dynamicFormDialog.show(manager, SETTINGS_TAG);
    }

    private void showFullScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.VISIBLE);
        mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void createAuthAPI() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Builder()
                .baseUrl(HunterURL.BASE)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        authAPI = retrofit.create(AuthAPI.class);
    }

    private void loadUser() {
        UserClient uClient = new UserClient(getBaseContext());

        uClient.addObserver(this);
        uClient.getLogged();
        if (chbSavePassword.isChecked()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(getString(R.string.pref_key_saved_user), userName);
            editor.putString(getString(R.string.pref_key_saved_pass), Objects.requireNonNull(txtPassword.getText()).toString());
            editor.apply();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof User) {
            HunterMobileWMS.setUser((User) arg);
            home();
        }
    }

    private void home() {
        unlockUI();
        Intent intent = new Intent(getBaseContext(), HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void settingsChanged() {
        updateAppInfo();
        createAuthAPI();
    }

    private void updateAppInfo() {
        runOnUiThread(() ->
                ((TextView) findViewById(R.id.txtVersionIp)).setText(getString(R.string.app_info, getResources().getString(R.string.app_version), HunterURL.BASE))
        );
    }

    private void useTypedAuthentication() {
        txtLogin.setVisibility(View.VISIBLE);
        txtPassword.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
    }

    /*BIOMETRIC CALLBACK*/
    @Override
    public void onSdkVersionNotSupported() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_sdk_not_supported), Toast.LENGTH_LONG).show();
        useTypedAuthentication();
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_hardware_not_supported), Toast.LENGTH_LONG).show();
        useTypedAuthentication();
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_fingerprint_not_available), Toast.LENGTH_LONG).show();
        useTypedAuthentication();
    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_permission_not_granted), Toast.LENGTH_LONG).show();
        useTypedAuthentication();
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
//        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
        useTypedAuthentication();
    }

    @Override
    public void onAuthenticationFailed() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_failure), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationCancelled() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_cancelled), Toast.LENGTH_LONG).show();
        mBiometricManager.cancelAuthentication();
        useTypedAuthentication();
    }

    @Override
    public void onAuthenticationSuccessful() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_success), Toast.LENGTH_LONG).show();
        showFullScreen();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        userName = preferences.getString(getString(R.string.pref_key_saved_user), "");
        txtLogin.setText(userName);
        txtPassword.setText(preferences.getString(getString(R.string.pref_key_saved_pass), ""));
        authAPI.preAuth(userName).enqueue(preAuthCallback);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
//        Toast.makeText(getApplicationContext(), helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
//        Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_LONG).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void unlockUI() {
        runOnUiThread(() -> {
            btnLogin.setEnabled(true);
            loginProgress.setVisibility(View.GONE);
            greyOverlay.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        });
    }

    private void checkPermissions() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissions = info.requestedPermissions;//This array contains the requested permissions.

            for (String perm : permissions) {
                if (perm.equals(Manifest.permission.USE_BIOMETRIC)) continue;
//                if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
//                    // You can use the API that requires the permission.
//                    // performAction(...);
//                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
//                    // In an educational UI, explain to the user why your app requires this
//                    // permission for a specific feature to behave as expected. In this UI,
//                    // include a "cancel" or "no thanks" button that allows the user to
//                    // continue using your app without granting the permission.
//                    // performAction(...);
//                } else {
//                    // You can directly ask for the permission.
//                    // The registered ActivityResultCallback gets the result of this request.
//                    ActivityCompat.requestPermissions(this, new String[]{perm}, REQUEST_PERMISSIONS_TOKEN);
//                }

                if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_DENIED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    ActivityCompat.requestPermissions(this, new String[]{perm}, REQUEST_PERMISSIONS_TOKEN);
                }
            }
        } catch (PackageManager.NameNotFoundException ignored) {

        }
    }
}
