package com.gtp.hunter.wms.fragment.rfid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.rfid.asciiprotocol.responders.IAsciiCommandResponder;
import com.gtp.hunter.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.gtp.hunter.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.gtp.hunter.rfid.asciiprotocol.responders.TransponderData;
import com.gtp.hunter.structure.NoAnimationItemAnimator;
import com.gtp.hunter.structure.WrapContentLinearLayoutManager;
import com.gtp.hunter.structure.adapter.ThingRecyclerViewAdapter;
import com.gtp.hunter.structure.viewmodel.BaseDocumentViewModel;
import com.gtp.hunter.structure.viewmodel.RFIDViewModel;
import com.gtp.hunter.wms.client.ThingClient;
import com.gtp.hunter.wms.fragment.DocumentFragment;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentThing;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.Unit;
import com.gtp.hunter.wms.model.ViewThingStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

public abstract class BaseRFIDFragment extends DocumentFragment implements Observer, ITransponderReceivedDelegate, ICommandResponseLifecycleDelegate, IAsciiCommandResponder {

    protected TextView txtDoc;
    protected RFIDViewModel mViewModel;
    private ThingClient tClient;
    private ProgressDialog progressDialog;

    // Control
    private boolean mAnyTagSeen;
    private final List<String> seenTags = new ArrayList<>();

    private CopyOnWriteArrayList<ViewThingStub> things;

    private ThingRecyclerViewAdapter adapter;

    //----------------------------------------------------------------------------------------------
    // Menu
    //----------------------------------------------------------------------------------------------

    protected MenuItem mReconnectMenuItem;
    protected MenuItem mConnectMenuItem;
    protected MenuItem mDisconnectMenuItem;
    protected MenuItem mResetMenuItem;
    protected MenuItem mSepSaveMenuItem;
    protected MenuItem mSepSendMenuItem;
    protected MenuItem mSepCancelMenuItem;
    protected MenuItem mRmiSendMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        things = new CopyOnWriteArrayList<>();
        adapter = new ThingRecyclerViewAdapter(things, getActionListener());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        setHasOptionsMenu(true);
        mViewModel = new ViewModelProvider(this).get(RFIDViewModel.class);

        // Use the ViewModel
        if (b != null) {
            mViewModel.setDocument((Document) b.getSerializable("DOCUMENT"));
            txtDoc.setText(mViewModel.getDocument().getCode());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.reader, menu);
        mResetMenuItem = menu.findItem(R.id.reset_reader_menu_item);
        mReconnectMenuItem = menu.findItem(R.id.reconnect_reader_menu_item);
        mConnectMenuItem = menu.findItem(R.id.insecure_connect_reader_menu_item);
        mDisconnectMenuItem = menu.findItem(R.id.disconnect_reader_menu_item);
        mSepSendMenuItem = menu.findItem(R.id.sepSend);
        mSepSaveMenuItem = menu.findItem(R.id.sepApply);
        mSepCancelMenuItem = menu.findItem(R.id.sepCancel);
        mRmiSendMenuItem = menu.findItem(R.id.rmiSend);
    }

    protected void basePrepareOptionsMenu(Menu menu) {
        boolean isConnected = getActionListener().isRfidDeviceConnected();
        mResetMenuItem.setEnabled(isConnected);
        mDisconnectMenuItem.setEnabled(isConnected);

        mReconnectMenuItem.setEnabled(HunterMobileWMS.isRFIDAvailable() && !isConnected);
        mConnectMenuItem.setEnabled(HunterMobileWMS.isRFIDAvailable() && !isConnected);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getLayoutId(), container, false);
        RecyclerView thingListView = v.findViewById(R.id.rfidReadList);

        txtDoc = v.findViewById(R.id.txtDocument);
        thingListView.setLayoutManager(new WrapContentLinearLayoutManager(v.getContext(), LinearLayoutManager.VERTICAL, false));
        thingListView.setItemAnimator(new NoAnimationItemAnimator());
        thingListView.setAdapter(adapter);
        return v;
    }

    /**
     * Respond to menu item selections
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reconnect_reader_menu_item) {
            getActionListener().reconnectRfidDevice();
            return true;
        } else if (item.getItemId() == R.id.insecure_connect_reader_menu_item) {
            getActionListener().connectRfidDevice();
            return true;
        } else if (item.getItemId() == R.id.disconnect_reader_menu_item) {
            getActionListener().disconnectRfidDevice();
            return true;
        } else if (item.getItemId() == R.id.reset_reader_menu_item) {
            getActionListener().resetRfidDevice();
            return true;
        } else if (item.getItemId() == R.id.sepCancel) {
            clearViewModel();
//            tClient.asyncFindByTagId("534C52100170000000000229");
            return true;
        } else if (item.getItemId() == R.id.sepApply) {
            completeTask();
            return true;
        } else if (item.getItemId() == R.id.sepSend) {
            for (ViewThingStub t : things) {
                if (!t.getThing().isError() && !t.isSent()) {
                    new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                            .setTitle("Efetuar Consumo" + t.getThing().getProduct().getName())
                            .setMessage("Deseja enviar etiqueta " + t.getTagId().substring(7))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> new AsyncSendThing(t.getThing()).execute());
                                t.setSent(true);
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .create()
                            .show();
                }
            }
            return true;
        } else if (item.getItemId() == R.id.rmiSend) {
            getActionListener().sendDocument(mViewModel.getDocument());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        tClient = new ThingClient(ctx);
        tClient.addObserver(this);
    }

    @Override
    public void clearViewModel() {
        mViewModel.setDocument(null);
        mViewModel.getDiViewList().clear();
        things.clear();
        seenTags.clear();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        getActionListener().returnFromFragment();
    }

    @Override
    public BaseDocumentViewModel getViewModel() {
        return mViewModel;
    }

    //TODO: Check why its not receiving events
    @Override
    public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
        Timber.e("BaseRFIDFragment - TransponderReceived: %s", transponder.getEpc());
        Thing t = tClient.findByTagId(transponder.getEpc());
        if (t != null) {
            ViewThingStub tStub = new ViewThingStub(t);

            tStub.setTagId(transponder.getEpc());
            things.add(tStub);
        }
    }

    @Override
    public boolean isResponseFinished() {
        Timber.e("isResponseFinished? false");
        return false;
    }

    @Override
    public void clearLastResponse() {
        Timber.e("ClearLastResponse");
    }

    @Override
    public boolean processReceivedLine(String fullLine, boolean moreLinesAvailable) {
        if (fullLine.startsWith("EP: ")) {
            String epc = fullLine.replace("EP: ", "");
            Timber.e("%s processReceivedLine: %s - hasMoreLines? %s - seen? %s", getClass().getSimpleName(), fullLine, moreLinesAvailable, seenTags.contains(epc));

            if (!seenTags.contains(epc)) {
                tClient.asyncFindByTagId(epc);

                seenTags.add(epc);
            }
        }
        return true;
    }

    protected void showAlert(String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());

        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.create().show();
    }

    @Override
    public void update(Observable observable, @NonNull Object o) {
        if (o instanceof Thing) {
            Thing t = (Thing) o;

            if (checkThing(t)) {
                ViewThingStub tStub = new ViewThingStub(t);

                if (!t.isError()) {
                    DocumentThing dt = new DocumentThing();

                    for (Unit u : t.getUnits()) {
                        tStub.setTagId(u.getTagId());
                    }
                    dt.setThing(t);
                    dt.setDocument(mViewModel.getDocument());
                    dt.setStatus("RFID");
                    mViewModel.getDocument().getThings().add(dt);
                }
                things.add(tStub);
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        }
    }

    @Override
    public void responseEnded() {
        if (!mAnyTagSeen) {
            getActionListener().sendMessageNotification("No transponders seen", 750);
        }
        Timber.e("responseEnded: %s", mAnyTagSeen);
    }

    @Override
    public void responseBegan() {
        mAnyTagSeen = false;
        Timber.e("responseBegan ");
    }

    public void refresh() {
        this.adapter.notifyDataSetChanged();
    }

    public class AsyncSendThing extends AsyncTask<Integer, Void, Boolean> {

        Thing t;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle("Enviando...");
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(true);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage("Aguarde um instante");
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        public AsyncSendThing(Thing thing) {
            this.t = thing;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return getActionListener().sendThing(this.t);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (progressDialog != null)
                progressDialog.dismiss();
            adapter.notifyDataSetChanged();
        }
    }

    protected abstract boolean checkThing(Thing t);

    protected abstract int getLayoutId();
}
