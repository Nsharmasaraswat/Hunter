package com.gtp.hunter.wms.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.Step;
import com.gtp.hunter.structure.viewmodel.BaseDocumentViewModel;
import com.gtp.hunter.structure.viewmodel.PickingViewModel;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.interfaces.RawDataHandler;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.DocumentThing;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.LocationPayload;
import com.gtp.hunter.wms.model.Rawdata;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.ThingModel;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

import static com.gtp.hunter.structure.Step.PICK;

public class PickingFragment extends LocationDocumentFragment implements RawDataHandler<LocationPayload> {

    private static final int CORRECT_LOCATION_INTERVAL = 3000;
    private static final int MIN_CORRECT_LOCATION_CNT = 2;
    private static final int BACKGROUND_CHANGER_INTERVAL = 3000;
    private static final int TASK_TIMEOUT_INTERVAL = 5000;
    private static final double WEIGHT_VAR_PERCENT = 0.5;

    private Handler timeoutHandler;
    private Drawable drwChanged;
    private Drawable drwRunning;
    private Drawable drwStopped;
    private boolean lost;

    private static final DecimalFormat DF = new DecimalFormat("#0.0000", DecimalFormatSymbols.getInstance(Locale.US));

    private long lastAddressOkTS;
    private int correctCount;

    private PickingViewModel mViewModel;
    private ProgressBar progress;

    private TextView txtPickCount;
    private TextView txtPickOrig;
    private TextView txtPickPrd;
    private TextView txtPickLayer;
    private TextView txtPickSeparator;
    private TextView txtPickNearAddress;

    private View currentSeq;
    private View currentPos;
    private MenuItem mniCloseTask;

    private boolean finished;
    private boolean useWeight;
    private double currentTara;
    private double requiredWeight;
    private double theoreticalWeight;

    public static PickingFragment newInstance() {
        return new PickingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_picking, container, false);
        TextView txtFL = v.findViewById(R.id.txtTagId);
        MaterialButton btnCompleteStep = v.findViewById(R.id.btnTranspCompleteStep);

        //btnCompleteStep.setVisibility(View.GONE);
        if (HunterMobileWMS.getUser().getProperties().containsKey("rtls-tag"))
            txtFL.setText(getString(R.string.tag_id, HunterMobileWMS.getUser().getProperties().get("rtls-tag")));
        else
            txtFL.setText(getString(R.string.no_tag));
        drwChanged = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_transp_ok);
        drwRunning = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_transp_changed);
        drwStopped = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_transp_nok);
        currentSeq = v.findViewById(R.id.transpCurrentSeq);
        currentPos = v.findViewById(R.id.transpCurrentSensor);
        txtPickCount = v.findViewById(R.id.txtSeqCount);
        txtPickOrig = v.findViewById(R.id.txtTranspOrig);
        txtPickPrd = v.findViewById(R.id.txtTranspPrd);
        txtPickLayer = v.findViewById(R.id.txtPickLayer);
        txtPickSeparator = v.findViewById(R.id.txtPickSeparator);
        txtPickNearAddress = v.findViewById(R.id.txtTranspCurAdd);
        progress = v.findViewById(R.id.transpProgress);
        mapImg = v.findViewById(R.id.imgMapView);
        ImageView flImg = v.findViewById(R.id.imgForklift);
        timeoutHandler = new Handler();
        btnCompleteStep.setOnClickListener((vw) -> {
            if (mViewModel != null && mViewModel.getStep() != null) {
                switch (mViewModel.getStep()) {
                    case PICK:
                        addPick(mViewModel.getProductWeight() * mViewModel.getItem().getQty());
                        break;
                    case PALLET:
                        //ADD WIEGHT TO PALLET
                        for (BaseField bf : mViewModel.getThing().getProperties()) {
                            if (bf.getField().getMetaname().equals("ACTUAL_WEIGHT")) {
                                double oldWeight = Double.parseDouble(bf.getValue());

                                bf.setValue("20.0000");
                                break;
                            }
                        }
                    default:
                        completeStep(getString(R.string.msg_complete_step));
                        break;
                }
            }
        });
        initMap(v.findViewById(R.id.mapView), savedInstanceState);
        initMarker(R.drawable.red_marker);
        if (BuildConfig.DEBUG)
            txtPickNearAddress.setOnClickListener((vw) -> {
                int visibility = mapImg.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;

                mapImg.setVisibility(visibility);
                flImg.setVisibility(visibility);
            });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        setHasOptionsMenu(true);

        mViewModel = new ViewModelProvider(this).get(PickingViewModel.class);
        // Use the ViewModel
        if (b != null) {
            mViewModel.setDocument((Document) b.getSerializable("DOCUMENT"));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.picking, menu);
        mniCloseTask = menu.findItem(R.id.mnuCancel);
        if (!BuildConfig.DEBUG) mniCloseTask.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.mnuResume:
                showLastPick();
                break;
            case R.id.mnuCancel:
                clearViewModel();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLastPick() {
        DocumentItem last = mViewModel.getLastPick();
        if (last != null) {
            Address add = HunterMobileWMS.findAddress(UUID.fromString(last.getProps().get("ADDRESS_ID")));
            int layer = Integer.parseInt(Objects.requireNonNull(last.getProps().get("LAYER")));
            String prdDesc = Objects.requireNonNull(last.getProps().get("PRODUCT_DESCRIPTION_LONG"));

            assert add != null;
            new AlertDialog.Builder(getActivity())
                    .setTitle(Objects.requireNonNull(getActivity()).getString(R.string.resume))
                    .setMessage(getString(R.string.last_pick, last.getQty(), last.getMeasureUnit(), prdDesc, add.getName(), layer))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNeutralButton(android.R.string.ok, null)
                    .create()
                    .show();
        }
    }

    @Override
    public void clearViewModel() {
        finished = false;
        mViewModel.setDocument(null);
        mViewModel.setItem(null);
        mViewModel.setStep(null);
        mViewModel.setAddress(null);
        mViewModel.setLayer(0);
        mViewModel.setThing(null);
        mViewModel.setSeparator(false);
        mViewModel.setProductText("");
        mViewModel.setHighlight(false);
        if (progress != null)
            progress.setVisibility(View.GONE);
        getActionListener().returnFromFragment();
    }

    @Override
    public BaseDocumentViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void interact(String msg) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_LONG));
    }

    @Override
    public void transform(Document d) {
        new AsyncCreateViews(this, d).execute();
    }

    @Override
    public void rawdata(@NotNull Rawdata<LocationPayload> rd) {
        long now = SystemClock.elapsedRealtime();
        FragmentActivity act = getActivity();

        if (!finished && mViewModel.isActive() && act != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
            timeoutHandler.postDelayed(() -> act.runOnUiThread(() -> {
                currentSeq.setBackground(drwStopped);
                lost = true;
            }), TASK_TIMEOUT_INTERVAL);
            if (lost) {
                currentSeq.setBackground(drwRunning);
            }
            useWeight = (useWeight || rd.getType().equals("SENSOR"));
            UUID nearAddress = rd.getPayload() == null || rd.getPayload().getNearby() == null || rd.getPayload().getNearby().isEmpty() ? null : UUID.fromString(rd.getPayload().getNearby());
            Double weight = rd.getPayload() == null || rd.getPayload().getValue() == null ? null : rd.getPayload().getValue();

            switch (mViewModel.getStep()) {
                case ADDRESS:
                    checkArrival(nearAddress, now, getString(R.string.pick_items, mViewModel.getItem().getQty()));
                    break;
                case PICK:
                    //CHECK WEIGHT AND COMPLETE
                    if (!useWeight) {
                        new AlertDialog.Builder(act)
                                .setTitle(act.getString(R.string.bypass_weight))
                                .setMessage(getString(R.string.question_bypass_weight))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> addPick(mViewModel.getProductWeight() * mViewModel.getItem().getQty()))
                                .setNegativeButton(android.R.string.no, null).create();
                    } else if (weight != null && weight > 0) {//TODO: Compare Product Weight
                        double tmpWeight = weight - currentTara;
                        double varWeight = mViewModel.getProductWeight() * WEIGHT_VAR_PERCENT;
                        double maxWeight = requiredWeight + varWeight;
                        double minWeight = requiredWeight - varWeight;

                        if (tmpWeight >= minWeight && tmpWeight <= maxWeight) {
                            addPick(weight);
                            currentTara = weight;
                            theoreticalWeight += requiredWeight;
                        } else if (tmpWeight <= minWeight) {
                            getActionListener().sendMessageNotification(getString(R.string.msg_underpicked_boxes), 1000);
                        } else if (tmpWeight >= maxWeight) {
                            getActionListener().sendMessageNotification(getString(R.string.msg_overpicked_boxes), 1000);
                        }
                        if (BuildConfig.DEBUG)
                            Timber.d("CurrentTara %.4f Theoretical %.4f Product %.4f Count %.0f Required %.4f Weight %.4f Current %.4f Var %.4f Min %.4f Max %.4f", currentTara, theoreticalWeight, mViewModel.getProductWeight(), mViewModel.getItem().getQty(), requiredWeight, weight, tmpWeight, varWeight, minWeight, maxWeight);
                    }
                    break;
                case STAGE:
                    checkArrival(nearAddress, now, "STAGE ALCANCADO");
                    break;
                case PALLET:
                    if (weight != null && weight > 0) {
                        //ADD WIEGHT TO PALLET
                        for (BaseField bf : mViewModel.getThing().getProperties()) {
                            if (bf.getField().getMetaname().equals("ACTUAL_WEIGHT")) {
                                bf.setValue(DF.format(weight));
                            }
                            if (bf.getField().getMetaname().equals("STARTING_WEIGHT")) {
                                bf.setValue(DF.format(weight));
                            }
                        }
                        currentTara = weight;
                        theoreticalWeight = weight;
                        completeStep(getString(R.string.pallet_weight, weight));
                    }
                    break;
            }

            act.runOnUiThread(() -> updateSensorData(rd));
        }
    }

    private void addPick(Double weight) {
        boolean add = true;
        Thing prdThing = new Thing();

        for (Thing ts : mViewModel.getThing().getSiblings()) {
            if (ts.getProduct().getId().equals(mViewModel.getItem().getProduct().getId())) {
                add = false;
                for (BaseField bf : ts.getProperties()) {
                    if (bf.getField().getMetaname().equals("QUANTITY")) {
                        double oldQty = Double.parseDouble(bf.getValue().replace(",", "."));

                        bf.setValue(DF.format(oldQty + mViewModel.getItem().getQty()));
                        continue;
                    }
                    if (bf.getField().getMetaname().equals("ACTUAL_WEIGHT")) {
                        double oldWeight = Double.parseDouble(bf.getValue().replace(",", "."));

                        bf.setValue(DF.format(useWeight ? currentTara : oldWeight + weight));
                    }
                }
                break;
            }
        }

        if (add) {
            ThingModel tm = new ThingModel();

            prdThing.getProperties().add(ThingUtil.createField("ACTUAL_WEIGHT", DF.format(weight)));
            prdThing.getProperties().add(ThingUtil.createField("LOT_EXPIRE", "TEMPORARIO"));
            prdThing.getProperties().add(ThingUtil.createField("LOT_ID", "TEMPORARIO"));
            prdThing.getProperties().add(ThingUtil.createField("MANUFACTURING_BATCH", "TEMPORARIO"));
            prdThing.getProperties().add(ThingUtil.createField("QUANTITY", DF.format(mViewModel.getItem().getQty())));
            prdThing.getProperties().add(ThingUtil.createField("STARTING_WEIGHT", DF.format(weight)));
            prdThing.setModel(tm);
            prdThing.setProduct(HunterMobileWMS.findProduct(mViewModel.getItem().getProduct().getId()));
            prdThing.setAddress(HunterMobileWMS.findAddress(mViewModel.getThing().getAddress().getId()));
            mViewModel.getThing().getSiblings().add(prdThing);
        }
        //ADD WIEGHT TO PALLET
        for (BaseField bf : mViewModel.getThing().getProperties()) {
            if (bf.getField().getMetaname().equals("ACTUAL_WEIGHT")) {
                double oldWeight = Double.parseDouble(bf.getValue());

                bf.setValue(DF.format(oldWeight + weight));
                break;
            }
        }
        completeStep("");
    }

    private void stop() {
        if (!finished) {
            finished = true;
            if (mViewModel.getCurrentSeq() == 1 && mViewModel.getStep().equals(Step.PALLET))
                clearViewModel();
            else {
                getActionListener().sendMessageNotification("NAO IMPLEMENTADO", 1500);
            }
            getActionListener().sendMessageNotification("CANCELADO", 500);
        }
    }

    private void completeStep(String message) {
        int current = mViewModel.getCurrentSeq();
        int count = mViewModel.getDocument() != null ? mViewModel.getDocument().getItems().size() : current;

        switch (mViewModel.getStep()) {
            case PALLET:
                mViewModel.setStep(Step.ADDRESS);
                break;
            case ADDRESS:
                mViewModel.setStep(PICK);
                break;
            case PICK:
                message = checkFinish(current, count);
                break;
            case STAGE:
                mViewModel.setStep(Step.PICK_FINISH);
            case PICK_FINISH:
                if (!finished)
                    message = checkFinish(current, count);
                break;
        }
        getActionListener().sendMessageNotification(message, 1200);
        correctCount = 0;
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (!BuildConfig.DEBUG)
                mniCloseTask.setVisible(false);
            updateUI();
        });
    }

    private void checkArrival(UUID nearAddress, long now, String message) {

        if (nearAddress != null && nearAddress.equals(mViewModel.getAddress().getParent_id())) {
            if (++correctCount >= MIN_CORRECT_LOCATION_CNT) {
                if ((now - lastAddressOkTS) < CORRECT_LOCATION_INTERVAL) {
                    completeStep(message);
                } else
                    correctCount--;
            }
            lastAddressOkTS = SystemClock.elapsedRealtime();
        }
    }

    private String checkFinish(int current, int count) {
        String ret;

        if (BuildConfig.DEBUG)
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getContext(), String.format(Locale.US, "Check Finish: Current: %d Count: %d Step: %s", current, count, mViewModel.getStep()), Toast.LENGTH_LONG));

        if (current >= count) {
            if (mViewModel.getStep() == PICK) {
                mViewModel.setStep(Step.STAGE);
                ret = mViewModel.getStep().toString() + " " + mViewModel.getStage().getName();
                mViewModel.setAddress(mViewModel.getStage());
            } else {
                finished = true;
                ret = getString(R.string.picking_completed);
                mViewModel.getDocument().setStatus("SUCESSO");
                new AsyncSendAction(this).execute();
            }
        } else {
            ret = getString(R.string.next_pick_item);
            requiredWeight = mViewModel.startPickingStep(current);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> txtPickCount.setText(getString(R.string.picks, mViewModel.getCurrentSeq(), count)));
        }
        return ret;
    }

    private void updateSensorData(Rawdata<LocationPayload> rd) {
        TextView txtX = currentPos.findViewById(R.id.txtLocX);
        TextView txtY = currentPos.findViewById(R.id.txtLocY);
        TextView txtZ = currentPos.findViewById(R.id.txtLocZ);
        TextView txtValue = currentPos.findViewById(R.id.txtWgtValue);

        if (rd.getType().equals("LOCATION")) {
            if (rd.getPayload().getX() != null && rd.getPayload().getY() != null)
                updateMap(rd.getPayload().getX().intValue(), rd.getPayload().getY().intValue());
            if (rd.getPayload().getX() != null)
                txtX.setText(DF.format(rd.getPayload().getX() / 100));
            if (rd.getPayload().getY() != null)
                txtY.setText(DF.format(rd.getPayload().getY() / 100));
            if (rd.getPayload().getZ() != null)
                txtZ.setText(DF.format(rd.getPayload().getZ() / 100));
            if (rd.getPayload().getNearbyName() != null) {
                if (rd.getPayload().getNearbyName().isEmpty())
                    txtPickNearAddress.setText("");
                else if (txtPickNearAddress.getText().length() > 0 && !txtPickNearAddress.getText().toString().contains(rd.getPayload().getNearbyName()))
                    txtPickNearAddress.setText(new StringBuilder(txtPickNearAddress.getText()).append(", ").append(rd.getPayload().getNearbyName()));
                else
                    txtPickNearAddress.setText(getString(R.string.current_position_label, rd.getPayload().getNearbyName()));
            }
        } else if (rd.getType().equals("SENSOR")) {
            if (rd.getPayload().getValue() != null)
                txtValue.setText(DF.format(rd.getPayload().getValue()));
        }
    }

    private void updateUI() {
        TextView txtStep = currentSeq.findViewById(R.id.txtTransportStep);
        TextView txtStepAction = currentSeq.findViewById(R.id.txtTransportStepAction);
        Address a = Objects.requireNonNull(mViewModel.getAddress());
        Handler hnd = new Handler();

        currentSeq.setBackground(drwChanged);
        hnd.postDelayed(() -> currentSeq.setBackground(drwRunning), BACKGROUND_CHANGER_INTERVAL);
        txtStep.setText(getString(mViewModel.getStep().getResourceId()));
        txtPickPrd.setText(mViewModel.getProductText());
        txtPickOrig.setText(getString(R.string.dyn_address, a.getName()));
        txtPickOrig.setVisibility(a.getName().isEmpty() ? View.GONE : View.VISIBLE);
        txtPickLayer.setText(getString(R.string.dyn_layer, mViewModel.getLayer()));
        txtPickLayer.setVisibility(mViewModel.getLayer() > 0 ? View.VISIBLE : View.GONE);
        txtPickSeparator.setText(mViewModel.isSeparator() ? getString(R.string.separator) : "");
        txtPickSeparator.setVisibility(mViewModel.isSeparator() ? View.VISIBLE : View.GONE);
        switch (mViewModel.getStep()) {
            case PALLET:
                txtStepAction.setText("");
                txtPickOrig.setVisibility(View.GONE);
                txtPickLayer.setVisibility(View.GONE);
                txtPickSeparator.setVisibility(View.GONE);
                txtPickPrd.setVisibility(View.GONE);
                break;
            case ADDRESS:
                txtStepAction.setText(a.getName());
                txtPickPrd.setVisibility(View.GONE);
                break;
            case PICK:
                txtPickPrd.setVisibility(View.VISIBLE);
                txtStepAction.setText(getString(R.string.prd_pick_count, mViewModel.getItem().getQty(), mViewModel.getItem().getMeasureUnit(), mViewModel.getProductText()));
                break;
            case STAGE:
            case PICK_FINISH:
                txtPickOrig.setVisibility(View.GONE);
                txtPickLayer.setVisibility(View.GONE);
                txtPickSeparator.setVisibility(View.GONE);
                txtPickPrd.setVisibility(View.GONE);
                txtStepAction.setText(a.getName());
                break;
        }
    }

    private static class AsyncCreateViews extends AsyncTask<Integer, Integer, IntegrationReturn> {
        private final WeakReference<PickingFragment> fragRef;
        private final Document d;

        public AsyncCreateViews(PickingFragment frag, Document d) {
            this.fragRef = new WeakReference<>(frag);
            this.d = d;
        }

        @Override
        protected void onPreExecute() {
            PickingFragment frag = fragRef.get();

            Objects.requireNonNull(fragRef.get().getActivity()).runOnUiThread(() -> {
                frag.progress.setVisibility(View.VISIBLE);
                frag.progress.setMax(d.getItems().size());
                frag.progress.setProgress(0);
            });
        }

        @Override
        protected IntegrationReturn doInBackground(Integer... integers) {
            PickingFragment frag = fragRef.get();

            if (d.getItems().isEmpty()) {
                return new IntegrationReturn(false, Objects.requireNonNull(frag.getActivity()).getString(R.string.invalid_document_noitem));
            }

            frag.mViewModel.setDocument(d);
            frag.mViewModel.init();
            frag.requiredWeight = frag.mViewModel.startPickingStep(0);
            frag.mViewModel.setStep(Step.PALLET);
            publishProgress(1);
            return IntegrationReturn.OK;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            fragRef.get().progress.setProgress(fragRef.get().progress.getProgress() + values[0]);
        }

        @Override
        protected void onPostExecute(IntegrationReturn ret) {
            PickingFragment frag = fragRef.get();

            if (ret.getResult()) {
                frag.txtPickCount.setText(frag.getString(R.string.transports, 1, d.getItems().size()));
                frag.updateUI();
                frag.progress.setVisibility(View.GONE);
            } else {
                frag.getActionListener().sendMessageNotification(ret.getMessage(), 6000);
                if (BuildConfig.DEBUG) {
                    Timber.e("Document status: %s Things: %d", d.getStatus(), d.getThings().size());
                }
                frag.getActionListener().returnFromFragment();
            }
        }
    }

    private static class AsyncSendAction extends AsyncTask<Integer, Void, IntegrationReturn> {
        private final WeakReference<PickingFragment> fragRef;
        private ProgressDialog progressDialog;

        public AsyncSendAction(PickingFragment frag) {
            this.fragRef = new WeakReference<>(frag);
        }

        @Override
        protected void onPreExecute() {
            PickingFragment frag = fragRef.get();

            Objects.requireNonNull(frag.getActivity()).runOnUiThread(() -> {
                progressDialog = new ProgressDialog(frag.getActivity());
                // Set horizontal animation_progress bar style.
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                // Set animation_progress dialog icon.
                progressDialog.setIcon(R.drawable.image_logo_gtp);
                // Set animation_progress dialog title.
                progressDialog.setTitle(frag.getString(R.string.info_sending));
                // Whether animation_progress dialog can be canceled or not.
                progressDialog.setCancelable(true);
                // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
                progressDialog.setCanceledOnTouchOutside(false);
                // Set animation_progress dialog message.
                progressDialog.setMessage(frag.getString(R.string.waitamoment));
                // Popup the animation_progress dialog.
                progressDialog.show();
            });
        }

        @Override
        protected IntegrationReturn doInBackground(Integer... params) {
            try {
                PickingFragment frag = fragRef.get();
                DocumentThing dt = new DocumentThing();

                dt.setThing(frag.mViewModel.getThing());
                frag.mViewModel.getDocument().getThings().add(dt);
                return frag.getActionListener().sendDocument(frag.mViewModel.getDocument());
            } catch (Exception ex) {
                return new IntegrationReturn(false, ex.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(IntegrationReturn ret) {
            PickingFragment frag = fragRef.get();

            if (!ret.getResult()) {
                frag.showError(frag.getString(R.string.connection_failed), ret.getMessage(), true);
                frag.finished = false;
            } else {
                frag.clearViewModel();
            }
            progressDialog.dismiss();
        }
    }
}
