package com.gtp.hunter.wms.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import com.gtp.hunter.structure.viewmodel.TransportViewModel;
import com.gtp.hunter.util.AlertSeverity;
import com.gtp.hunter.util.AlertType;
import com.gtp.hunter.util.DateUtil;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.client.AlertClient;
import com.gtp.hunter.wms.client.CustomWMSClient;
import com.gtp.hunter.wms.interfaces.RawDataHandler;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLThing;
import com.gtp.hunter.wms.model.AGLTransport;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.Alert;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentThing;
import com.gtp.hunter.wms.model.DocumentTransport;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.LocationPayload;
import com.gtp.hunter.wms.model.Rawdata;
import com.gtp.hunter.wms.model.Thing;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

import static com.gtp.hunter.structure.Step.DESTINATION;
import static com.gtp.hunter.structure.Step.DROP;
import static com.gtp.hunter.structure.Step.LIFT;

public class TransportFragment extends LocationDocumentFragment implements RawDataHandler<LocationPayload> {

    private static final int CORRECT_LOCATION_INTERVAL = 3000;
    private static final int MIN_CORRECT_LOCATION_CNT = 2;
    private static final int WEIGHT_OVERRIDE_TIMEOUT = 2500;
    private static final int BACKGROUND_CHANGER_INTERVAL = 3000;
    private static final int TASK_TIMEOUT_INTERVAL = 5000;

    private Handler timeoutHandler;
    private Drawable drwChanged;
    private Drawable drwRunning;
    private Drawable drwStopped;
    private boolean lost;

    private ObjectAnimator anim;

    private static final DecimalFormat DF = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

    private long lastEventTS = 0L;
    private long lastAddressOkTS;
    private int correctCount;

    private TransportViewModel mViewModel;
    private ProgressBar progress;

    private TextView txtTransportCount;
    private TextView txtTransportOrig;
    private TextView txtTransportPrd;
    private TextView txtTransportDest;
    private TextView txtTranspNearAddress;
//    private TextView txtMacAddress;

    private View currentSeq;
    private View currentPos;
    private MenuItem mniCloseTask;

    private UUID lastNearAddress;

    private CustomWMSClient tClient;
    private AlertClient aClient;

    private boolean finished;
    boolean useWeight;

    public static TransportFragment newInstance() {
        return new TransportFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transport, container, false);
        TextView txtRTLSTag = v.findViewById(R.id.txtRTLSTagId);
        MaterialButton btnRemoveStep = v.findViewById(R.id.btnTranspRemoveStep);
        MaterialButton btnCompleteStep = v.findViewById(R.id.btnTranspCompleteStep);

        //btnCompleteStep.setVisibility(View.GONE);
        if (HunterMobileWMS.getUser().getProperties().containsKey("forklift-tag"))
            txtRTLSTag.setText(getString(R.string.forklift_id, HunterMobileWMS.getUser().getProperties().get("forklift-tag")));
        else if (HunterMobileWMS.getUser().getProperties().containsKey("rtls-tag"))
            txtRTLSTag.setText(getString(R.string.forklift_id, HunterMobileWMS.getUser().getProperties().get("rtls-tag")));
        else
            txtRTLSTag.setText(getString(R.string.no_tag));
        drwChanged = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_transp_ok);
        drwRunning = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_transp_changed);
        drwStopped = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_transp_nok);
        currentSeq = v.findViewById(R.id.transpCurrentSeq);
        currentPos = v.findViewById(R.id.transpCurrentSensor);
        txtTransportCount = v.findViewById(R.id.txtSeqCount);
        txtTransportOrig = v.findViewById(R.id.txtTranspOrig);
        txtTransportPrd = v.findViewById(R.id.txtTranspPrd);
        txtTransportDest = v.findViewById(R.id.txtTranspDest);
        txtTranspNearAddress = v.findViewById(R.id.txtTranspCurAdd);
//        txtMacAddress = v.findViewById(R.id.txtMacAddress);
        mapImg = v.findViewById(R.id.imgMapView);
        progress = v.findViewById(R.id.transpProgress);
        timeoutHandler = new Handler();
        anim = ObjectAnimator.ofInt(currentSeq, "backgroundColor", ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.stopped), ContextCompat.getColor(getContext(), R.color.running), ContextCompat.getColor(getContext(), R.color.paused));
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        btnCompleteStep.setOnClickListener((vw) -> completeStep(getString(R.string.msg_complete_step)));
        btnRemoveStep.setOnClickListener((vw) -> new AlertDialog.Builder(getActivity())
                .setTitle(TransportFragment.this.getString(R.string.question_remove_step))
                .setMessage(TransportFragment.this.getString(R.string.action_notundoable))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> new AsyncRemoveStep(TransportFragment.this).execute())
                .setNegativeButton(android.R.string.no, null).show());

        initMap(v.findViewById(R.id.mapView), savedInstanceState);
        initMarker(R.drawable.red_marker);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        setHasOptionsMenu(true);

        mViewModel = new ViewModelProvider(this).get(TransportViewModel.class);
        // Use the ViewModel
        if (b != null) {
            mViewModel.setDocument((Document) b.getSerializable("DOCUMENT"));
        }
        tClient = new CustomWMSClient(Objects.requireNonNull(getContext()));
        aClient = new AlertClient(Objects.requireNonNull(getContext()));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.transport, menu);
        mniCloseTask = menu.findItem(R.id.transpCancel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.transpStop:
                AlertDialog dia = new AlertDialog.Builder(getActivity())
                        .setTitle(Objects.requireNonNull(getActivity()).getString(R.string.transport_stopped))
                        .setMessage(getString(R.string.question_cancel_transport))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> stop())
                        .setNegativeButton(android.R.string.no, null).create();
                dia.show();
                break;
            case R.id.transpCancel:
                clearViewModel();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void clearViewModel() {
        finished = false;
        mViewModel.setReturn(null);
        mViewModel.setDocument(null);
        mViewModel.setTransport(null);
        mViewModel.setStep(null);
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
        final FragmentActivity act = getActivity();

        if (!finished && mViewModel.isActive() && act != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
            timeoutHandler.postDelayed(() -> act.runOnUiThread(() -> {
                currentSeq.setBackground(drwStopped);
                lost = true;
            }), TASK_TIMEOUT_INTERVAL);
            if (lost) {
                currentSeq.setBackground(drwRunning);
            }
            useWeight = false && (useWeight || rd.getType().equals("SENSOR"));
            UUID nearAddress = rd.getPayload() == null || rd.getPayload().getNearby() == null || rd.getPayload().getNearby().isEmpty() ? null : UUID.fromString(rd.getPayload().getNearby());
            Double weight = rd.getPayload() == null || rd.getPayload().getValue() == null ? null : rd.getPayload().getValue();

            switch (mViewModel.getStep()) {
                case ORIGIN:
                    if (!checkArrival(nearAddress, mViewModel.getTransport().getThing().getAddress().getParent_id(), now, LIFT, getString(R.string.load_item)) && weight != null) {
                        sendAlert(nearAddress, weight);
                    }
                    break;
                case DESTINATION:
                    if (!checkArrival(nearAddress, mViewModel.getTransport().getAddress().getParent_id(), now, DROP, getString(R.string.unload_item)) && weight != null) {
                        sendAlert(nearAddress, weight);
                    }
                    break;
                case LIFT:
                    if ((!useWeight && (now - lastEventTS) > WEIGHT_OVERRIDE_TIMEOUT) || (useWeight && weight != null && weight > 0)) {
                        saveThingWeight(weight);
                        completeStep(getString(R.string.goto_destination));
                    }
                    break;
                case DROP:
                    if ((!useWeight && (now - lastEventTS) > WEIGHT_OVERRIDE_TIMEOUT) || (useWeight && weight != null && weight == 0)) {
                        completeStep("");
                    }
                    break;
            }

            act.runOnUiThread(() -> updateSensorData(rd));
            lastNearAddress = nearAddress;
        }
    }

    private void saveThingWeight(Double weight) {
        if (weight != null && weight > 0) {
            for (DocumentThing dt : mViewModel.getDocument().getThings()) {
                Thing t = dt.getThing();

                if (t.getId().equals(mViewModel.getTransport().getThing().getId())) {
                    findByMetaname(t, "ACTUAL_WEIGHT").setValue(DF.format(weight));
                    for (Thing ts : t.getSiblings()) {
                        try {
                            double qt = Double.parseDouble(findByMetaname(ts, "QUANTITY").getValue());

                            if (BuildConfig.DEBUG) {
                                double singleWeight = weight / qt;
                                double sw = Double.parseDouble(findByMetaname(ts, "STARTING_WEIGHT").getValue());

                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), DF.format(singleWeight) + "<->" + DF.format(sw), Toast.LENGTH_SHORT).show());
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    break;
                }
            }
            for (AGLThing at : mViewModel.getReturn().getThings()) {
                if (at.getId().equalsIgnoreCase(mViewModel.getTransport().getThing().getId().toString())) {
                    at.getProps().put("actual_weight", DF.format(weight));
                    break;
                }
            }
        }
    }

    private void sendAlert(UUID nearAddress, Double weight) {
        TextView txtX = currentPos.findViewById(R.id.txtLocX);
        TextView txtY = currentPos.findViewById(R.id.txtLocY);
        TextView txtZ = currentPos.findViewById(R.id.txtLocZ);
        Alert al = new Alert();
        Address near = HunterMobileWMS.findAddress(nearAddress);
        String descTxt = txtX.getText() + "," + txtY.getText();

        if (near == null) {
            if (lastNearAddress != null) {
                Address lastNear = HunterMobileWMS.findAddress(lastNearAddress);

                if (lastNear != null) {
                    descTxt += " Última posição conhecida: " + lastNear.getName();
                }
            }
        } else
            descTxt = near.getName();
        al.setDescription("Pesagem Incorreta na Posição " + descTxt);
        al.setItem(HunterMobileWMS.getUser().getName());
        al.setMsg("Pesagem " + DF.format(weight) + " fora de Endereço " + mViewModel.getTransport().getThing().getAddress().getName() + " Tarefa: " + mViewModel.getDocument().getCode() + " Seq: " + mViewModel.getTransport().getSeq());
        al.setSeverity(AlertSeverity.ERROR);
        al.setType(AlertType.PROCESS);
        new AsyncSendAlert(this, al).execute();
    }

    private void stop() {
        if (!finished) {
            finished = true;
            if (mViewModel.getCurrentSeq() == 1 && mViewModel.getStep().equals(Step.ORIGIN))
                clearViewModel();
            else {
                mViewModel.getReturn().setStatus("FALHA");
                if (lastNearAddress != null)
                    for (AGLThing t : mViewModel.getReturn().getThings()) {
                        if (t.getId().equalsIgnoreCase(mViewModel.getTransport().getThing().getId().toString())) {
                            t.setAddress_id(lastNearAddress.toString());
                            break;
                        }
                    }
                new AsyncSendAction(this).execute();
            }
            getActionListener().sendMessageNotification("CANCELADO", 500);
        }
    }

    private void completeStep(String message) {
        int current = mViewModel.getCurrentSeq();
        int count = mViewModel.getDocument() != null ? mViewModel.getDocument().getTransports().size() : current;

        if (mViewModel.getStep() != null) {
            switch (mViewModel.getStep()) {
                case ORIGIN:
                    mViewModel.setStep(LIFT);
                    break;
                case LIFT:
                    mViewModel.setStep(DESTINATION);
                    break;
                case DESTINATION:
                    mViewModel.setStep(DROP);
                    break;
                case DROP:
                    if (!finished) {
                        for (AGLThing at : mViewModel.getReturn().getThings())
                            if (at.getId().equalsIgnoreCase(mViewModel.getTransport().getThing().getId().toString())) {
                                at.setAddress_id(mViewModel.getTransport().getAddress().getId().toString());
                                break;
                            }
                        message = checkFinish(current, count);
                    }
                    break;
            }
            getActionListener().sendMessageNotification(message, 1500);
            lastEventTS = SystemClock.elapsedRealtime();
            correctCount = 0;
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                if (!BuildConfig.DEBUG)
                    mniCloseTask.setVisible(false);
                stopAnim();
                updateUI();
            });
        }
    }

    private void stopAnim() {
        if (anim.isRunning())
            anim.pause();
    }

    private boolean checkArrival(UUID nearAddress, UUID finalAddress, long now, Step step, String message) {
        boolean ret = false;
        if (nearAddress != null && nearAddress.equals(finalAddress)) {
            if (++correctCount >= MIN_CORRECT_LOCATION_CNT) {
                if ((now - lastAddressOkTS) < CORRECT_LOCATION_INTERVAL) {
                    ret = true;
                    completeStep(message);
                } else
                    correctCount--;
            }
            lastAddressOkTS = SystemClock.elapsedRealtime();
        }
        return ret;
    }

    private String checkFinish(int current, int count) {
        String ret;

        if (current >= count) {
            finished = true;
            ret = getString(R.string.transport_completed);
            mViewModel.getReturn().setStatus("SUCESSO");
            for (AGLThing at : mViewModel.getReturn().getThings()) {
                for (AGLTransport atr : mViewModel.getReturn().getTransports()) {
                    if (atr.getThing_id().equalsIgnoreCase(at.getId())) {
                        if (atr.getOrigin_id() == null || atr.getOrigin_id().isEmpty())
                            atr.setOrigin_id(at.getAddress_id());
                        at.setAddress_id(atr.getAddress_id());
                    }
                }
            }
            new AsyncSendAction(this).execute();
        } else {
            ret = getString(R.string.next_transport);
            mViewModel.startTransportStep(current);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> txtTransportCount.setText(getString(R.string.transports, mViewModel.getCurrentSeq(), count)));
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
                    txtTranspNearAddress.setText("");
                else
                    txtTranspNearAddress.setText(getString(R.string.current_position_label, rd.getPayload().getNearbyName()));
            }
        } else if (rd.getType().equals("SENSOR")) {
            if (rd.getPayload().getValue() != null)
                txtValue.setText(DF.format(rd.getPayload().getValue()));
        }
    }

    private void updateUI() {
        TextView txtStep = currentSeq.findViewById(R.id.txtTransportStep);
        TextView txtStepAction = currentSeq.findViewById(R.id.txtTransportStepAction);
        MaterialButton btnRemoveStep = ((View) currentSeq.getParent()).findViewById(R.id.btnTranspRemoveStep);
        Thing t = Objects.requireNonNull(mViewModel.getTransport().getThing());
        Address a = Objects.requireNonNull(mViewModel.getTransport().getAddress());
        StringBuilder products = new StringBuilder();
        StringBuilder productsSku = new StringBuilder();
        Handler hnd = new Handler();

        currentSeq.setBackground(drwChanged);
        hnd.postDelayed(() -> currentSeq.setBackground(drwRunning), BACKGROUND_CHANGER_INTERVAL);
        for (Thing ts : t.getSiblings()) {
            if (products.length() > 0) products.append("\r\n");
            if (productsSku.length() > 0) productsSku.append("\r\n");
            BaseField bf = ThingUtil.getExpiryField(ts);
            BaseField bfBu = ProductUtil.getBoxUnit(ts.getProduct());
            products.append(ts.getProduct().getName());
            if (bfBu != null && bfBu.getValue() != null && !bfBu.getValue().isEmpty()) {
                products.append(" - C");
                products.append(bfBu.getValue());
            }
            if (bf != null && bf.getValue() != null && !bf.getValue().isEmpty()) {
                products.append(" - ");
                products.append(new SimpleDateFormat("dd/MM/yy", Locale.US).format(DateUtil.parseDate(bf.getValue())));
            }
            productsSku.append(ts.getProduct().getSku()).append(" - ").append(ts.getProduct().getName());
        }
        txtStep.setText(getString(mViewModel.getStep().getResourceId()));
        txtTransportOrig.setText(getString(R.string.dyn_origin, t.getAddress().getName()));
        txtTransportPrd.setText(productsSku.toString());
        txtTransportDest.setText(getString(R.string.dyn_dest, a.getName()));
        switch (mViewModel.getStep()) {
            case ORIGIN:
                txtStepAction.setText(t.getAddress().getName());
                if (mViewModel.getTransport().getThing().getSiblings().size() == 0)
                    btnRemoveStep.setVisibility(View.VISIBLE);
                else
                    btnRemoveStep.setVisibility(View.GONE);
                txtTransportOrig.setVisibility(View.VISIBLE);
                txtTransportPrd.setVisibility(View.GONE);
                txtTransportDest.setVisibility(View.GONE);
                break;
            case LIFT:
                txtTransportPrd.setVisibility(View.VISIBLE);
                txtStepAction.setText(products.toString());
                break;
            case DESTINATION:
                txtTransportDest.setVisibility(View.VISIBLE);
                txtStepAction.setText(a.getName());
                break;
            case DROP:
                txtStepAction.setText(products.toString());
                break;
        }
        if (mViewModel.isAlert())
            anim.start();
    }

    private BaseField findByMetaname(Thing t, String metaname) {
        for (BaseField b : t.getProperties())
            if (b.getField().getMetaname().equalsIgnoreCase(metaname))
                return b;

        BaseField ret = new BaseField();
        ret.setValue("");
        return ret;
    }

    private static class AsyncCreateViews extends AsyncTask<Integer, Integer, IntegrationReturn> {
        private final WeakReference<TransportFragment> fragRef;
        private final Document d;

        public AsyncCreateViews(TransportFragment frag, Document d) {
            this.fragRef = new WeakReference<>(frag);
            this.d = d;
        }

        @Override
        protected void onPreExecute() {
            TransportFragment frag = fragRef.get();

            Objects.requireNonNull(fragRef.get().getActivity()).runOnUiThread(() -> {
                frag.progress.setVisibility(View.VISIBLE);
                frag.progress.setMax(3);
                frag.progress.setProgress(0);
//                frag.anim.start();
            });
        }

        @Override
        protected IntegrationReturn doInBackground(Integer... integers) {
            TransportFragment frag = fragRef.get();

            if (d.getTransports().isEmpty()) {
                return new IntegrationReturn(false, Objects.requireNonNull(frag.getActivity()).getString(R.string.invalid_document_notransp));
            }
            Collections.sort(d.getTransports());

            frag.mViewModel.setDocument(d);
            publishProgress(1);
            frag.mViewModel.createReturn();
            publishProgress(2);
            frag.mViewModel.startTransportStep(0);
            publishProgress(3);
            frag.stopAnim();

            return IntegrationReturn.OK;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            fragRef.get().progress.setProgress(fragRef.get().progress.getProgress() + values[0]);
        }

        @Override
        protected void onPostExecute(IntegrationReturn ret) {
            TransportFragment frag = fragRef.get();
            if (ret.getResult()) {
                frag.txtTransportCount.setText(frag.getString(R.string.transports, 1, d.getTransports().size()));
                frag.updateUI();
                frag.progress.setVisibility(View.GONE);
            } else {
                frag.getActionListener().sendMessageNotification(ret.getMessage(), 6000);
                if (BuildConfig.DEBUG) {
                    Timber.e("Transport Document status: %s Things: %d", d.getStatus(), d.getThings().size());
                }
                frag.getActionListener().returnFromFragment();
            }
        }
    }

    private static class AsyncSendAction extends AsyncTask<Integer, Void, Boolean> {
        private final WeakReference<TransportFragment> fragRef;
        boolean finish = false;
        private ProgressDialog progressDialog;

        public AsyncSendAction(TransportFragment frag) {
            this.fragRef = new WeakReference<>(frag);
        }

        @Override
        protected void onPreExecute() {
            TransportFragment frag = fragRef.get();

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
        protected Boolean doInBackground(Integer... params) {
            TransportFragment frag = fragRef.get();

            if (frag.getActionListener().sendAGLDocument(frag.mViewModel.getReturn())) {
                finish = true;
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TransportFragment frag = fragRef.get();

            if (!result) {
                frag.showError(frag.getString(R.string.connection_failed), frag.getString(R.string.try_again), true);
                frag.finished = false;
            } else if (finish) {
                frag.clearViewModel();
            }
            progressDialog.dismiss();
        }
    }

    private static class AsyncRemoveStep extends AsyncTask<Integer, Void, Boolean> {
        private final WeakReference<TransportFragment> fragRef;
        private ProgressDialog progressDialog;

        public AsyncRemoveStep(TransportFragment frag) {
            this.fragRef = new WeakReference<>(frag);
        }

        @Override
        protected void onPreExecute() {
            TransportFragment frag = fragRef.get();

            Objects.requireNonNull(frag.getActivity()).runOnUiThread(() -> {
                progressDialog = new ProgressDialog(frag.getActivity());
                // Set horizontal animation_progress bar style.
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                // Set animation_progress dialog icon.
                progressDialog.setIcon(R.drawable.image_logo_gtp);
                // Set animation_progress dialog title.
                progressDialog.setTitle(frag.getString(R.string.deleting));
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
        protected Boolean doInBackground(Integer... params) {
            TransportFragment frag = fragRef.get();
            Document d = frag.mViewModel.getDocument();
            AGLDocument ad = frag.mViewModel.getReturn();

            for (DocumentTransport dtr : d.getTransports()) {
                if (dtr.getSeq() >= frag.mViewModel.getCurrentSeq()) {
                    Thing t = dtr.getThing();
                    Iterator<AGLThing> itAt = ad.getThings().iterator();
                    Iterator<AGLTransport> itAtr = ad.getTransports().iterator();

                    //Where are my streams?
                    while (itAtr.hasNext()) {
                        AGLTransport atr = itAtr.next();

                        if (atr.getSeq() == dtr.getSeq()) {
                            while (itAt.hasNext())
                                if (itAt.next().getId().equals(atr.getThing_id()))
                                    itAt.remove();
                            itAtr.remove();
                        }
                    }
                    try {
                        frag.tClient.removePallet(t).execute();
                    } catch (IOException e) {
                        frag.showError(frag.getString(R.string.error_delete_thing), e.getLocalizedMessage(), true);
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TransportFragment frag = fragRef.get();

            if (!result) {
                frag.showError(frag.getString(R.string.connection_failed), frag.getString(R.string.try_again), true);
            } else {
                Iterator<AGLThing> aglThingIterator = frag.mViewModel.getReturn().getThings().iterator();
                Iterator<DocumentTransport> thingIterator = frag.mViewModel.getDocument().getTransports().iterator();
                Thing t = frag.mViewModel.getTransport().getThing();

                while (aglThingIterator.hasNext()) {
                    AGLThing rt = aglThingIterator.next();

                    if (rt.getId().equals(t.getId().toString())) {
                        aglThingIterator.remove();
                        break;
                    }
                }
                while (thingIterator.hasNext()) {
                    Thing rt = thingIterator.next().getThing();

                    if (rt.getId().equals(t.getId())) {
                        thingIterator.remove();
                        break;
                    }
                }
                int current = frag.mViewModel.getCurrentSeq();
                int count = frag.mViewModel.getDocument().getTransports().size();

                frag.checkFinish(current, count);
            }
            progressDialog.dismiss();
        }
    }

    private static class AsyncSendAlert extends AsyncTask<Integer, Void, Boolean> {
        private final WeakReference<TransportFragment> fragRef;
        private final Alert alert;

        public AsyncSendAlert(TransportFragment frag, Alert al) {
            this.fragRef = new WeakReference<>(frag);
            this.alert = al;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            TransportFragment frag = fragRef.get();

            return frag.aClient.save(alert) != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TransportFragment frag = fragRef.get();

            if (!result) {
                frag.showError(frag.getString(R.string.connection_failed), frag.getString(R.string.try_again), true);
            } else {
                frag.getActionListener().sendMessageNotification("ALERTA GERADO!", 3000);
            }
        }
    }

//    public TextView getNetworkLabel() {
//        return txtMacAddress;
//    }
}
