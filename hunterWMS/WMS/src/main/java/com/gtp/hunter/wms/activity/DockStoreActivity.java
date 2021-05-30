package com.gtp.hunter.wms.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.gson.stream.JsonReader;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.util.AddressUtil;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.client.CustomWMSClient;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.BaseModelField;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.Thing;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DockStoreActivity extends AppCompatActivity implements Observer {

    private View mContentView;
    private LinearLayout llPallets;
    private MaterialButton btnSend;
    private TextView txtQty;
    private ProgressBar undProgressBar;

    private Address destination;

    private CustomWMSClient wmsClient;
    private List<Thing> originList;
    private final List<Thing> transportList = new ArrayList<>();

    private boolean moveBlocked;
    private boolean loadOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dock_store);

        final SearchableSpinner cbxDestination = findViewById(R.id.cbxStoreAddressDestination);
        final ActionBar actionBar = getSupportActionBar();
        final AddressAdapter destinationAdapter = new AddressAdapter(this, R.layout.item_address_big, HunterMobileWMS.filterAddressParent());

        txtQty = findViewById(R.id.txtStoreQty);
        btnSend = findViewById(R.id.btnStoreSend);
        undProgressBar = findViewById(R.id.undProgressBar);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        txtQty.setText(getString(R.string.dyn_qty_int, 0));
        llPallets = findViewById(R.id.llPallets);
        mContentView = findViewById(R.id.storeLayout);
        cbxDestination.setTitle(getString(R.string.select_address));
        cbxDestination.setAdapter(destinationAdapter.init());
        cbxDestination.setTag(0);
        cbxDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxDestination.getTag() != null && (int) cbxDestination.getTag() != pos) {
                    if (cbxDestination.getSelectedItemPosition() > 0) {
                        moveBlocked = false;
                        destination = (Address) cbxDestination.getSelectedItem();
                        try {
                            if (destination.getFields().size() == 0)
                                Executors.newSingleThreadExecutor().submit(() -> {
                                    List<AddressField> afList = HunterMobileWMS.getDB().afDao().listByAddressId(destination.getId());
                                    Address staticInstance = HunterMobileWMS.findAddress(destination.getId());

                                    destination.getFields().addAll(afList);
                                    if (staticInstance != null && staticInstance.getFields() != null && staticInstance.getFields().size() == 0)
                                        staticInstance.getFields().addAll(afList);
                                }).get();
                        } catch (Exception ignored) {
                        }
                        btnSend.setEnabled(false);
                        undProgressBar.setVisibility(View.VISIBLE);
                        wmsClient.loadAllocation(destination);
                        showFullScreen();
                    } else {
                        onNothingSelected(parent);
                    }
                }
                cbxDestination.setTag(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Timber.e("No Address Selected");
                destination = null;
                checkEnableSend();
                showFullScreen();
            }
        });
        btnSend.setOnClickListener((View v) -> {
            AlertDialog dlg;

            if (moveBlocked) {
                dlg = new AlertDialog.Builder(DockStoreActivity.this)
                        .setTitle(getString(R.string.msg_invalid_operation))
                        .setMessage(getString(R.string.error_pending_transport) + "\n" + getString(R.string.msg_change_destination))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.no, null)
                        .create();
            } else {
                dlg = new AlertDialog.Builder(DockStoreActivity.this)
                        .setTitle(getString(R.string.complete))
                        .setMessage(DockStoreActivity.this.getString(R.string.question_send_transport, transportList.size(), "DOCAS", destination.getName()))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> new AsyncSendAction(DockStoreActivity.this).execute())
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            }
            dlg.show();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        wmsClient = new CustomWMSClient(this);
        wmsClient.addObserver(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("Load Dock Allocation");
        loadOrigin = true;
        undProgressBar.setVisibility(View.VISIBLE);
        wmsClient.listDockAllocation();
        showFullScreen();
    }

    private void showFullScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mContentView.setVisibility(View.VISIBLE);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof List) {
            List<Thing> thList = (List<Thing>) arg;
            Thing[] ar = new Thing[thList.size()];
            Comparator<Thing> compareByAddress = (Thing o1, Thing o2) -> {
                if (o1 == null && o2 == null) return 0;
                if (o2 == null) return -1;
                if (o1 == null) return 1;
                if (o1.getAddress() == null && o2.getAddress() == null) return 0;
                if (o2.getAddress() == null) return -1;
                if (o1.getAddress() == null) return 1;
                Address a = o1.getAddress();
                Address b = o2.getAddress();
                UUID bmfSeqId = null;
                int oa = 0;
                int ob = 0;

                for (BaseModelField bmf : a.getModel().getFields()) {
                    if (bmf.getMetaname().equals("ROAD_SEQ")) {
                        bmfSeqId = bmf.getId();
                        break;
                    }
                }
                if (bmfSeqId != null) {
                    for (AddressField af : a.getFields()) {
                        if (af.getModelId().equals(bmfSeqId)) {
                            oa = Integer.parseInt(af.getValue());
                            break;
                        }
                    }
                    for (AddressField af : b.getFields()) {
                        if (af.getModelId().equals(bmfSeqId)) {
                            ob = Integer.parseInt(af.getValue());
                            break;
                        }
                    }
                    return ob - oa;
                }
                return 0;
            };

            ar = thList.toArray(ar);
            Arrays.sort(ar, compareByAddress);
            if (loadOrigin) {
                Timber.d("Load Dock Allocation Loaded %d", ar.length);
                originList = new ArrayList<>(Arrays.asList(ar));
                for (Thing th : originList) {
                    CheckBox cbxPallet = new CheckBox(this);

                    cbxPallet.setBackground(ContextCompat.getDrawable(this, R.drawable.background_primary_border));
                    cbxPallet.setOnCheckedChangeListener((vw, chk) -> {
                        if (chk)
                            transportList.add(th);
                        else
                            transportList.remove(th);
                        runOnUiThread(() -> txtQty.setText(getString(R.string.dyn_qty_int, transportList.size())));
                    });
                    for (Thing ts : th.getSiblings()) {
                        BaseField bfExp = ThingUtil.getExpiryField(ts);
                        BaseField bfUb = ProductUtil.getBoxUnit(ts.getProduct());
                        StringBuilder plName = new StringBuilder(ts.getProduct().getName());

                        if (bfUb != null)
                            plName.append(" (C").append(bfUb.getValue()).append(")");
                        if (bfExp != null)
                            plName.append(" - ").append(bfExp.getValue());

                        plName.append(" (").append(th.getAddress().getMetaname()).append(")");
                        cbxPallet.setText(plName);
                        break;
                    }
                    llPallets.addView(cbxPallet);
                }
                if (originList.size() == 0)
                    runOnUiThread(() -> Toast.makeText(DockStoreActivity.this, "Docas Vazias!", Toast.LENGTH_SHORT).show());
                loadOrigin = false;
            } else {
                List<Thing> destinationList = (List<Thing>) arg;
                AddressField cap = AddressUtil.getCapacity(destination);
                int capacity = cap == null || cap.getValue().isEmpty() ? 0 : Integer.parseInt(cap.getValue());
                boolean canMove = true;

                for (Thing t : destinationList) {
                    if (t.getPayload() != null) {
                        try (JsonReader jsonReader = new JsonReader(new StringReader(t.getPayload()))) {
                            if (jsonReader.hasNext()) {
                                jsonReader.beginObject();
                                String key = jsonReader.nextName();

                                if (key.equals("allocation")) {
                                    switch (jsonReader.nextInt()) {
                                        case 1:
                                            canMove = false;
                                            if (BuildConfig.DEBUG)
                                                Timber.d("Há paletes entrando no destino %s", t.getAddress().getName());
                                            runOnUiThread(() -> Toast.makeText(this, R.string.error_pending_income, Toast.LENGTH_LONG).show());
                                            break;
                                        case 2:
                                            canMove = false;
                                            if (BuildConfig.DEBUG)
                                                Timber.d("Há paletes saindo do destino %s", t.getAddress().getName());
                                            runOnUiThread(() -> Toast.makeText(this, R.string.pendingExit, Toast.LENGTH_LONG).show());
                                            break;
                                    }
                                }
                            }
                        } catch (IOException ioe) {
                            Timber.e("AsyncThings %s", ioe.getLocalizedMessage());
                        }
                    }
                }
                if (canMove) {
                    if (transportList.size() > capacity)
                        runOnUiThread(() -> Toast.makeText(DockStoreActivity.this, "Endereço de Destino Cheio!", Toast.LENGTH_SHORT).show());
                } else {
                    blockMove();
                }
            }
        }
        undProgressBar.setVisibility(View.GONE);
        checkEnableSend();
    }

    private void blockMove() {
        moveBlocked = true;
        transportList.clear();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_invalid_operation))
                .setMessage(getString(R.string.error_pending_transport))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton(android.R.string.no, null)
                .create()
                .show();
    }

    private void checkEnableSend() {
        boolean destinationSelected = destination != null;
        boolean quantitySelected = transportList.size() > 0;

        btnSend.setEnabled(destinationSelected && quantitySelected);
    }

    private static class AsyncSendAction extends AsyncTask<Integer, Void, IntegrationReturn> {
        private final WeakReference<DockStoreActivity> actRef;
        private ProgressDialog progressDialog;

        public AsyncSendAction(DockStoreActivity f) {
            this.actRef = new WeakReference<>(f);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(actRef.get());
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle(actRef.get().getString(R.string.info_sending));
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(false);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage(actRef.get().getString(R.string.question_send_to_hunter));
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        @Override
        protected IntegrationReturn doInBackground(Integer... params) {
            try {
                return actRef.get().wmsClient.createTransport(actRef.get().transportList, actRef.get().destination).execute().body();
            } catch (IOException ioe) {
                return new IntegrationReturn(false, ioe.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(IntegrationReturn result) {
            DockStoreActivity act = actRef.get();

            if (result.getResult()) {
                Intent mIntent = new Intent(act, TasksActivity.class);

                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(mIntent);
                act.finish();
            } else {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act);

                alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                alertBuilder.setCancelable(false);
                alertBuilder.setTitle(act.getString(R.string.try_again));
                alertBuilder.setMessage(result.getMessage());
                alertBuilder.setNeutralButton(android.R.string.ok, (DialogInterface dialog, int which) -> dialog.dismiss());
                alertBuilder.create().show();
            }
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }
}
