package com.gtp.hunter.wms.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.barcode.BarcodeActivity;
import com.gtp.hunter.structure.viewmodel.HomeViewModel;
import com.gtp.hunter.util.FontManager;
import com.gtp.hunter.vision.CameraXLivePreviewActivity;
import com.gtp.hunter.wms.api.HunterURL;
import com.gtp.hunter.wms.api.PermissionAPI;
import com.gtp.hunter.wms.client.AddressClient;
import com.gtp.hunter.wms.client.ProductClient;
import com.gtp.hunter.wms.client.UserClient;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.AddressModel;
import com.gtp.hunter.wms.model.Permission;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.ProductModel;
import com.gtp.hunter.wms.model.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HomeActivity extends AppCompatActivity implements Observer {
    private static final String DISPLAY_VALUE = "DISPLAY_VALUE";
    private static final String RAW_VALUE = "RAW_VALUE";
    private static final String QR_CODE_READ = "qr_code_read_ok";
    private static final int US_QR_CODE = 346;
    private static final int TR_QR_CODE = 356;

    private ConstraintLayout baseLayout;
    private ProgressDialog progressDialog;

    private boolean firstsync;

    private TextView lblUserName;
    private MaterialButton btnMLVision;
    private PermissionAPI permissionAPI;

    //hunter Thing Rest Client
    private ProductClient pClient;
    private AddressClient aClient;
    private UserClient usClient;
    private HomeViewModel mViewModel;

    private final Callback<List<Permission>> permissionCallback = new Callback<List<Permission>>() {

        @Override
        public void onResponse(@NonNull Call<List<Permission>> call, Response<List<Permission>> response) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    ArrayList<Permission> resp = new ArrayList<>(response.body());
                    Iterator<Permission> it = resp.iterator();

                    while (it.hasNext()) {
                        Permission p = it.next();

                        if (!p.getApp().equals("HunterMobile")) {
                            it.remove();
                        }
                    }
                    HunterMobileWMS.getUser().setPermissions(resp);
                    createPermissionMenu();
                } else {
                    runOnUiThread(() -> {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(HomeActivity.this);

                        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        alertBuilder.setTitle(getString(R.string.access_denied));
                        alertBuilder.setMessage(getString(R.string.user_with_no_permissions));
                        alertBuilder.setNeutralButton(android.R.string.ok, (dialog, id) -> {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                            onBackPressed();
                        });
                        alertBuilder.create().show();
                    });
                }
            } else {
                Timber.d("Permission Callback Code: %d  Message: %s", response.code(), response.message());
                Timber.e("Permission Response: %s", response.raw().toString());
                if (progressDialog != null)
                    progressDialog.dismiss();
                onBackPressed();
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Permission>> call, @NonNull Throwable t) {
            Timber.d("Permission Callback Error %s", t.getLocalizedMessage() == null ? getString(R.string.connection_failed) : t.getLocalizedMessage());
            Toast.makeText(HomeActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (HunterURL.BASE == null) {
            onNavigateUp();
        }
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        createPermissionAPI();
        setContentView(R.layout.activity_home);
        baseLayout = findViewById(R.id.homeLayout);
        lblUserName = findViewById(R.id.lblUserName);
        btnMLVision = findViewById(R.id.btnRfid);
        btnMLVision.setTypeface(FontManager.getTypeface(this, FontManager.FA_SOLID));
        btnMLVision.setOnClickListener((View v) -> mlVision());
        if (bundle != null)
            HunterMobileWMS.setQrcodeRead(bundle.getBoolean(QR_CODE_READ, false));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(QR_CODE_READ, !HunterMobileWMS.shouldScanForTag());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        User us = HunterMobileWMS.getUser();
        if (us != null) {
            showFullScreen();
            if (us.getPermissions() == null)
                permissionAPI.listPermissions().enqueue(permissionCallback);
            else if (HunterMobileWMS.getUser().getPermissions().isEmpty()) {
                runOnUiThread(() -> {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

                    alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    alertBuilder.setTitle(getString(R.string.access_denied));
                    alertBuilder.setMessage(getString(R.string.user_with_no_permissions));
                    alertBuilder.setNeutralButton(android.R.string.ok, (dialog, id) -> onNavigateUp());
                    alertBuilder.create().show();
                });
            } else {
                createPermissionMenu();
            }
        } else {
            backToLogin();
        }
    }

    @Override
    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
        User us = HunterMobileWMS.getUser();

        if (us == null)
            onNavigateUp();
        else {
            lblUserName.setText(us.getName());
            btnMLVision.setVisibility(View.GONE);
            pClient = new ProductClient(getBaseContext());
            pClient.addObserver(this);
            aClient = new AddressClient(getBaseContext());
            aClient.addObserver(this);
            usClient = new UserClient(getBaseContext());
            if (!HunterMobileWMS.isSynchronized())
                new AsyncFetcherTask(this).execute();
        }
    }

    private void showFullScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        baseLayout.setVisibility(View.VISIBLE);
        baseLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void createPermissionAPI() {
        if (HunterURL.BASE == null)
            backToLogin();
        else {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + HunterMobileWMS.getToken())
                                .build();
                        return chain.proceed(newRequest);
                    }).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(HunterURL.BASE)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            permissionAPI = retrofit.create(PermissionAPI.class);
        }
    }

    private void backToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void rfid() {
        Intent intent = new Intent(getBaseContext(), InventoryActivity.class);
        startActivity(intent);
    }

    private void mlVision() {
        Intent intent = new Intent(getBaseContext(), CameraXLivePreviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o != null) {
            if (observable instanceof ProductClient) {
                List<Product> pList = (List<Product>) o;

                Timber.i("PRODUCT SYNC %d products", pList.size());
                new AsyncProductUpdaterTask(this, pList).execute();
            }
            if (observable instanceof AddressClient) {
                List<Address> aList = (List<Address>) o;

                Timber.i("ADDRESS SYNC %d addresses", aList.size());
                new AsyncAddressUpdaterTask(this, aList).execute();
            }
        } else {
            if (progressDialog != null)
                progressDialog.dismiss();
            //TODO: Mensagem de Erro
            backToLogin();
        }
    }

    private void createPermissionMenu() {
        LinearLayout permissionLayout = baseLayout.findViewById(R.id.permissionsLayout);
        Set<String> pSet = new HashSet<>();
        List<Permission> perms = HunterMobileWMS.getUser().getPermissions();
        Permission[] a = new Permission[perms.size()];
        Comparator<Permission> compareByName = (Permission o1, Permission o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o2 == null) return -1;
            if (o1 == null) return 1;
            if (o1.getName() == null && o2.getName() == null) return 0;
            if (o2.getName() == null) return -1;
            if (o1.getName() == null) return 1;
            return o1.getName().compareTo(o2.getName());
        };

        permissionLayout.removeAllViews();
        a = perms.toArray(a);
        Arrays.sort(a, compareByName);
        perms = Arrays.asList(a);
        for (Permission p : perms) {
            if (!pSet.contains(p.getId())) {
                //String label = getString(R.string.permission);
                String label = p.getName();
                //set the properties for button
                MaterialButton btnPerm = new MaterialButton(HomeActivity.this);
                int resourceId = getResources().getIdentifier("menu_" + p.getMetaname(), "string", getPackageName());

                if (resourceId != 0) {
                    int iconResourceId = getResources().getIdentifier("fa_icon_" + p.getIcon(), "string", getPackageName());

                    if (iconResourceId == 0) {
                        iconResourceId = R.string.fa_icon_unavailable;
                    }
                    label = getString(resourceId, getString(iconResourceId));
                }
                btnPerm.setId(new Random().nextInt());
                btnPerm.setPadding(0, 10, 0, 10);
                btnPerm.setTypeface(FontManager.getTypeface(this, FontManager.FA_SOLID));
                btnPerm.setText(label);
                btnPerm.setEnabled(false);
                btnPerm.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                btnPerm.setPadding(8, 8, 8, 8);
                btnPerm.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
                btnPerm.setOnClickListener((View v) -> {
                    try {
                        Intent intent = new Intent(getBaseContext(), Class.forName(p.getRoute()));

                        if (p.getParams() != null) {
                            Bundle bParams = new Bundle();

                            bParams.putString("params", p.getParams());
                            intent.putExtras(bParams);
                        }
                        startActivity(intent);
                    } catch (ClassNotFoundException cne) {
                        runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Permissão Inválida: " + p.getMetaname(), Toast.LENGTH_LONG).show());
                    }
                });

                //add button to the layout
                permissionLayout.addView(btnPerm);
                pSet.add(p.getId());
            }
        }
        if (HunterMobileWMS.isSynchronized())
            restorePermissionMenu();
    }

    private void restorePermissionMenu() {
        LinearLayout permissionLayout = baseLayout.findViewById(R.id.permissionsLayout);
        for (int i = 0; i < permissionLayout.getChildCount(); i++) {
            View v = permissionLayout.getChildAt(i);

            if (v instanceof MaterialButton) {
                mViewModel.getPermissionButtons().add((MaterialButton) v);
                v.setEnabled(true);
            }
        }
        if (BuildConfig.DEBUG) {
            btnMLVision.setVisibility(View.VISIBLE);
            btnMLVision.setEnabled(true);
        }

        if (progressDialog != null)
            progressDialog.dismiss();
        if (HunterMobileWMS.shouldScanForTag()) {
            scanTag(US_QR_CODE);
        }
    }

    private void scanTag(int readType) {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

            if (cameraManager.getCameraIdList().length > 0) {
                Intent intent = new Intent(HomeActivity.this, BarcodeActivity.class);

                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, readType);
            } else if (HunterMobileWMS.getUser().getProperties().containsKey("rtls_tag")) {
                runOnUiThread(() -> {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

                    alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    alertBuilder.setTitle(getString(R.string.access_denied));
                    alertBuilder.setMessage(getString(R.string.user_with_no_permissions));
                    alertBuilder.setNeutralButton(android.R.string.ok, (dialog, id) -> dialog.dismiss());
                    alertBuilder.create().show();
                });
            }
        } catch (CameraAccessException ignored) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == US_QR_CODE) {
            assert data != null;
            String displayValue = data.getStringExtra(DISPLAY_VALUE);
            String rawValue = data.getStringExtra(RAW_VALUE);

            if (BuildConfig.DEBUG)
                Timber.d("Display Value %s", Objects.toString(displayValue));
            HunterMobileWMS.getUser().getProperties().put("rtls-tag", rawValue);
            usClient.postProperty("rtls-tag", rawValue);
            if (HunterMobileWMS.shouldScanTransporter())
                scanTag(TR_QR_CODE);
            else HunterMobileWMS.setQrcodeRead(true);
        } else if (resultCode == RESULT_OK && requestCode == TR_QR_CODE) {
            assert data != null;
            String displayValue = data.getStringExtra(DISPLAY_VALUE);
            String rawValue = data.getStringExtra(RAW_VALUE);

            if (BuildConfig.DEBUG)
                Timber.d("Display Value %s", Objects.toString(displayValue));
            HunterMobileWMS.getUser().getProperties().put("transporter-tag", rawValue);
            usClient.postProperty("transporter-tag", rawValue);
            HunterMobileWMS.setQrcodeRead(true);
        } else
            baseLayout.findViewById(R.id.permissionsLayout).setEnabled(false);
    }

    private static class AsyncFetcherTask extends AsyncTask<Integer, Void, String> {
        private final WeakReference<HomeActivity> activityReference;

        public AsyncFetcherTask(HomeActivity ctx) {
            ctx.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            this.activityReference = new WeakReference<>(ctx);
        }

        @Override
        protected void onPreExecute() {
            HomeActivity act = activityReference.get();

            act.progressDialog = new ProgressDialog(act);
            // Set horizontal animation_progress bar style.
            act.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            act.progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            act.progressDialog.setTitle(act.getString(R.string.synchronizing));
            // Whether animation_progress dialog can be canceled or not.
            act.progressDialog.setCancelable(false);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            act.progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            act.progressDialog.setMessage(act.getString(R.string.wait_sync));
            // Popup the animation_progress dialog.
            act.progressDialog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
            HomeActivity act = activityReference.get();
            Product newest = HunterMobileWMS.getDB().prdDao().findLatest();
            Address newestAddr = HunterMobileWMS.getDB().addrDao().findLatest();
            Date newestProductUpdate = newest == null ? new Date(0L) : newest.getUpdatedAt();

            act.firstsync = newest == null || newestAddr == null;
            act.pClient.listFrom(newestProductUpdate);
            if (!act.firstsync) {
                Date newestAddressUpdate = newestAddr == null ? new Date(0L) : newestAddr.getUpdatedAt();

                act.aClient.listFrom(newestAddressUpdate);
            }
            return "OK";
        }
    }

    private static class AsyncProductUpdaterTask extends AsyncTask<Integer, Void, String> {
        private final WeakReference<HomeActivity> activityReference;
        private final List<Product> pList;

        public AsyncProductUpdaterTask(HomeActivity ctx, List<Product> pList) {
            this.activityReference = new WeakReference<>(ctx);
            this.pList = pList;
        }

        @Override
        protected String doInBackground(Integer... params) {
            HomeActivity act = activityReference.get();
            if (!pList.isEmpty()) {
                HunterMobileWMS.getDB().prdDao().insertAll(pList);
                Set<ProductModel> prdModelSet = new HashSet<>();
                Set<ProductField> prdFieldSet = new HashSet<>();

                for (Product p : pList) {
                    prdFieldSet.addAll(p.getFields());
                    prdModelSet.add(p.getModel());
                }
                HunterMobileWMS.getDB().pfDao().insertAll(prdFieldSet);
                HunterMobileWMS.getDB().pmDao().insertAll(prdModelSet);
            }
            HunterMobileWMS.setProductList(HunterMobileWMS.getDB().prdDao().listAll());
            if (act.firstsync)
                act.aClient.listFrom(new Date(0));
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            if (HunterMobileWMS.isSynchronized()) {
                Timber.d("FINISH Product");
                activityReference.get().restorePermissionMenu();
                activityReference.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else
                Timber.d("FIRST Product");
        }
    }

    private static class AsyncAddressUpdaterTask extends AsyncTask<Integer, Void, String> {
        private final WeakReference<HomeActivity> activityReference;
        private final List<Address> aList;

        public AsyncAddressUpdaterTask(HomeActivity ctx, List<Address> pList) {
            this.activityReference = new WeakReference<>(ctx);
            this.aList = pList;
        }

        @Override
        protected String doInBackground(Integer... params) {
            final int TASK_BLOCK_SIZE = 1000;

            if (!aList.isEmpty()) {
                final int inteira = aList.size() / TASK_BLOCK_SIZE;
                final int frac = aList.size() % TASK_BLOCK_SIZE;
                final int qtd = inteira + (frac == 0 ? 0 : 1);

                for (int i = 0; i < qtd; i++) {
                    int fromIndex = TASK_BLOCK_SIZE * i;
                    int toIndex = Math.min(aList.size(), TASK_BLOCK_SIZE * (i + 1));
                    final List<Address> subList = aList.subList(fromIndex, toIndex);

                    HunterMobileWMS.getDB().addrDao().insertAll(subList);
                    final Set<AddressModel> addrModelSet = new HashSet<>();
                    final Set<AddressField> addrFieldSet = new HashSet<>();

                    for (Address a : subList) {
                        addrFieldSet.addAll(a.getFields());
                        addrModelSet.add(a.getModel());
                    }
                    HunterMobileWMS.getDB().afDao().insertAll(addrFieldSet);
                    HunterMobileWMS.getDB().amDao().insertAll(addrModelSet);
                    Timber.i("ADDRESS SYNC From %d to %d", fromIndex, toIndex);
                }
            }
            HunterMobileWMS.setAddressList(HunterMobileWMS.getDB().addrDao().listAll());
            Timber.i("ADDRESS SYNC Persisted %d Addresses", aList.size());
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            if (HunterMobileWMS.isSynchronized()) {
                Timber.d("FINISH Address");
                activityReference.get().restorePermissionMenu();
                activityReference.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else
                Timber.d("FIRST Address");
        }
    }
}
