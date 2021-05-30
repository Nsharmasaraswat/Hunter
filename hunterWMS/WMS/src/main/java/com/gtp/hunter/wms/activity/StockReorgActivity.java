package com.gtp.hunter.wms.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.button.MaterialButton;
import com.google.gson.stream.JsonReader;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.util.AddressUtil;
import com.gtp.hunter.util.DateUtil;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.client.CustomWMSClient;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.BaseModelField;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StockReorgActivity extends AppCompatActivity implements Observer {

    private View mContentView;
    private SeekBar skbQty;
    private TextView txtProducts;
    private MaterialButton btnSend;
    private ProgressBar undProgressBar;

    private Address origin;
    private Address destination;

    private CustomWMSClient wmsClient;
    private List<Thing> originList;
    private List<Thing> destinationList;

    private boolean loadOrigin;
    private boolean moveBlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_reorg);

        final TextView txtQty = findViewById(R.id.txtReorgQty);
        final SearchableSpinner cbxOrigin = findViewById(R.id.cbxReorgAddressOrigin);
        final SearchableSpinner cbxDestination = findViewById(R.id.cbxReorgAddressDestination);

        ActionBar actionBar = getSupportActionBar();
        AddressAdapter originAdapter = new AddressAdapter(this, R.layout.item_address_big, HunterMobileWMS.filterAddressParent());
        AddressAdapter destinationAdapter = new AddressAdapter(this, R.layout.item_address_big, HunterMobileWMS.filterAddressParent());

        btnSend = findViewById(R.id.btnReorgSend);
        undProgressBar = findViewById(R.id.undProgressBar);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        txtQty.setText(getString(R.string.dyn_qty_int, 0));
        skbQty = findViewById(R.id.skbQuantity);
        txtProducts = findViewById(R.id.txtReorgProduct);
        mContentView = findViewById(R.id.reorgLayout);
        skbQty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                txtQty.setText(getString(R.string.dyn_qty_int, progress));
                checkEnableSend();
                if (originList != null && originList.size() > 0) {
                    Map<String, Integer> prdCountMap = new LinkedHashMap<>();

                    for (int i = 0; i < Math.min(originList.size(), progress); i++) {
                        Thing t = originList.get(i);

                        for (Thing ts : t.getSiblings()) {
                            Product p = ts.getProduct();
                            BaseField bf = ThingUtil.getExpiryField(ts);
                            String key = p.getName() + (bf == null ? "" : " " + new SimpleDateFormat("dd/MM/yy", Locale.US).format(DateUtil.parseDate(bf.getValue())));
                            Integer prdCount = prdCountMap.containsKey(key) ? prdCountMap.get(key) : 0;

                            if (prdCount == null) prdCount = Integer.valueOf("0");

                            prdCountMap.put(key, ++prdCount);
                        }
                    }

                    if (prdCountMap.size() > 0) {
                        StringBuilder lbl = new StringBuilder();

                        for (Map.Entry<String, Integer> e : prdCountMap.entrySet())
                            lbl.append(e.getValue()).append(" - ").append(e.getKey()).append("\n");
                        txtProducts.setText(lbl.toString().trim());
                    } else
                        txtProducts.setText("");
                } else
                    txtProducts.setText("");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        cbxOrigin.setTitle(getString(R.string.select_address));
        cbxOrigin.setAdapter(originAdapter.init());
        cbxOrigin.setTag(0);
        cbxOrigin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxOrigin.getTag() != null && (int) cbxOrigin.getTag() != pos) {
                    if (cbxOrigin.getSelectedItemPosition() > 0) {
                        if (moveBlocked) {
                            new AlertDialog.Builder(StockReorgActivity.this)
                                    .setTitle(getString(R.string.msg_invalid_operation))
                                    .setMessage(getString(R.string.error_pending_transport) + "\n" + getString(R.string.msg_change_destination))
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setNeutralButton(android.R.string.no, null)
                                    .create()
                                    .show();
                        } else {
                            loadOrigin = true;
                            origin = (Address) cbxOrigin.getSelectedItem();
                            try {
                                if (origin.getFields().size() == 0)
                                    Executors.newSingleThreadExecutor().submit(() -> {
                                        List<AddressField> afList = HunterMobileWMS.getDB().afDao().listByAddressId(origin.getId());
                                        Address staticInstance = HunterMobileWMS.findAddress(origin.getId());

                                        origin.getFields().addAll(afList);
                                        if (staticInstance != null && staticInstance.getFields() != null && staticInstance.getFields().size() == 0)
                                            staticInstance.getFields().addAll(afList);
                                    }).get();
                            } catch (Exception ignored) {
                            }
                            btnSend.setEnabled(false);
                            undProgressBar.setVisibility(View.VISIBLE);
                            wmsClient.loadAllocation(origin);
                        }
                        showFullScreen();
                    } else {
                        onNothingSelected(parent);
                    }
                }
                cbxOrigin.setTag(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Timber.e("No Address Selected");
                origin = null;
                checkEnableSend();
                showFullScreen();
            }
        });
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
        skbQty.setProgress(0);
        btnSend.setOnClickListener((View v) -> new AlertDialog.Builder(StockReorgActivity.this)
                .setTitle(getString(R.string.complete))
                .setMessage(StockReorgActivity.this.getString(R.string.question_send_transport, skbQty.getProgress(), origin.getName(), destination.getName()))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> new AsyncSendAction(StockReorgActivity.this).execute())
                .setNegativeButton(android.R.string.no, null).show());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        wmsClient = new CustomWMSClient(this);
        wmsClient.addObserver(this);
        showFullScreen();
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
            if (loadOrigin) {
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
                originList = new ArrayList<>(Arrays.asList(ar));
                int available = originList.size();
                Iterator<Thing> iter = originList.iterator();
                while (iter.hasNext()) {
                    Thing t = iter.next();

                    if (t.getSiblings().isEmpty()) {
                        available--;
                        iter.remove();
                        continue;
                    }
                    if (t.getPayload() != null) {
                        try (JsonReader jsonReader = new JsonReader(new StringReader(t.getPayload()))) {

                            if (jsonReader.hasNext()) {
                                jsonReader.beginObject();
                                String key = jsonReader.nextName();

                                if (key.equals("allocation"))
                                    switch (jsonReader.nextInt()) {
                                        case 1:
                                            if (BuildConfig.DEBUG)
                                                Timber.d("Há paletes chegando na origem %s", t.getAddress().getName());
                                            available--;
                                            runOnUiThread(() -> Toast.makeText(this, R.string.error_pending_income, Toast.LENGTH_LONG).show());
                                            iter.remove();
                                            break;
                                        case 2:
                                            if (BuildConfig.DEBUG)
                                                Timber.d("Há paletes saindo da origem %s", t.getAddress().getName());
                                            available--;
                                            runOnUiThread(() -> Toast.makeText(this, R.string.pendingExit, Toast.LENGTH_LONG).show());
                                            iter.remove();
                                            break;
                                    }
                            }
                        } catch (IOException ioe) {
                            Timber.e("AsyncThings %s", ioe.getLocalizedMessage());
                        }
                    }
                }
                skbQty.setProgress(available);
                skbQty.setMax(available);
                skbQty.setVisibility(available > 0 ? View.VISIBLE : View.INVISIBLE);
                if (BuildConfig.DEBUG)
                    Timber.d("OriginList: %d DestinationList: %d Available: %d Tracker: %d Max: %d", originList == null ? -1 : originList.size(), destinationList == null ? -1 : destinationList.size(), available, skbQty.getProgress(), skbQty.getMax());
                if (originList.size() == 0)
                    runOnUiThread(() -> Toast.makeText(StockReorgActivity.this, "Endereço de Origem Vazio!", Toast.LENGTH_SHORT).show());
            } else {
                destinationList = (List<Thing>) arg;
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
                    if (skbQty.getProgress() > 0)
                        skbQty.setMax(Math.min(skbQty.getProgress(), capacity - destinationList.size()));
                    else
                        skbQty.setMax(capacity - destinationList.size());
                    if (skbQty.getVisibility() == View.VISIBLE)
                        skbQty.setProgress(skbQty.getMax());
                    if (skbQty.getMax() == 0)
                        runOnUiThread(() -> Toast.makeText(StockReorgActivity.this, "Endereço de Destino Cheio!", Toast.LENGTH_SHORT).show());
                    if (BuildConfig.DEBUG)
                        Timber.d("OriginList: %d DestinationList: %d Capacity: %d Tracker: %d Max: %d", originList.size(), destinationList.size(), capacity, skbQty.getProgress(), skbQty.getMax());
                } else {
                    blockMove();
                }
            }
        }
        undProgressBar.setVisibility(View.GONE);
        loadOrigin = false;
        checkEnableSend();
    }

    private void blockMove() {
        moveBlocked = true;
        skbQty.setMax(0);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_invalid_operation))
                .setMessage(getString(R.string.error_pending_transport))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton(android.R.string.no, null)
                .create()
                .show();
    }

    private void checkEnableSend() {
        boolean originSelected = origin != null;
        boolean destinationSelected = destination != null;
        boolean quantitySelected = skbQty.getProgress() > 0;

        btnSend.setEnabled(originSelected && destinationSelected && quantitySelected);
    }

    private static class AsyncSendAction extends AsyncTask<Integer, Void, IntegrationReturn> {
        private final WeakReference<StockReorgActivity> actRef;
        private ProgressDialog progressDialog;

        public AsyncSendAction(StockReorgActivity f) {
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
            StockReorgActivity act = actRef.get();
            IntegrationReturn iRet;

            try {
                iRet = act.wmsClient.createTransport(act.origin, act.destination, act.skbQty.getProgress()).execute().body();
            } catch (IOException ioe) {
                iRet = new IntegrationReturn(false, ioe.getLocalizedMessage());
            }
            return iRet;
        }

        @Override
        protected void onPostExecute(IntegrationReturn result) {
            StockReorgActivity act = actRef.get();

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
