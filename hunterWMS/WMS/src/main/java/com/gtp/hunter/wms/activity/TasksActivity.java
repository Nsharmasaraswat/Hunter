package com.gtp.hunter.wms.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.barcode.BarcodeActivity;
import com.gtp.hunter.rfid.BluetoothDeviceActivity;
import com.gtp.hunter.rfid.asciiprotocol.AsciiCommander;
import com.gtp.hunter.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.gtp.hunter.rfid.asciiprotocol.commands.InventoryCommand;
import com.gtp.hunter.rfid.asciiprotocol.enumerations.TriState;
import com.gtp.hunter.structure.websocket.WebSocketWrapper;
import com.gtp.hunter.util.DocumentUtil;
import com.gtp.hunter.wms.api.HunterURL;
import com.gtp.hunter.wms.client.PrintClient;
import com.gtp.hunter.wms.fragment.BaseFragment;
import com.gtp.hunter.wms.fragment.CheckingFragment;
import com.gtp.hunter.wms.fragment.DocumentFragment;
import com.gtp.hunter.wms.fragment.FPInventoryFragment;
import com.gtp.hunter.wms.fragment.FPPalletCheckingFragment;
import com.gtp.hunter.wms.fragment.FormDialogFragment;
import com.gtp.hunter.wms.fragment.PickingFragment;
import com.gtp.hunter.wms.fragment.TaskListFragment;
import com.gtp.hunter.wms.fragment.TransportFragment;
import com.gtp.hunter.wms.fragment.rfid.BaseRFIDFragment;
import com.gtp.hunter.wms.fragment.rfid.RFIDConsumptionFragment;
import com.gtp.hunter.wms.fragment.rfid.RFIDRMInventoryFragment;
import com.gtp.hunter.wms.fragment.rfid.RFIDTransferFragment;
import com.gtp.hunter.wms.interfaces.ActionFragmentListener;
import com.gtp.hunter.wms.interfaces.RawDataHandler;
import com.gtp.hunter.wms.interfaces.TaskFragmentListener;
import com.gtp.hunter.wms.model.AGLAddress;
import com.gtp.hunter.wms.model.AGLBaseDoc;
import com.gtp.hunter.wms.model.AGLDocItem;
import com.gtp.hunter.wms.model.AGLDocModel;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLDocumentProps;
import com.gtp.hunter.wms.model.AGLThing;
import com.gtp.hunter.wms.model.AGLTransport;
import com.gtp.hunter.wms.model.ActionMessage;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.DocumentModel;
import com.gtp.hunter.wms.model.DocumentThing;
import com.gtp.hunter.wms.model.DocumentTransport;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.LocationPayload;
import com.gtp.hunter.wms.model.PrintPayload;
import com.gtp.hunter.wms.model.Rawdata;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.ViewTaskStub;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TasksActivity extends BluetoothDeviceActivity implements ActionFragmentListener, TaskFragmentListener {
    private static final int MAX_ACTION_SEND_RETRIES = 2;
    private static final int ACTION_TIMEOUT = 25000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 20000;
    private static final int WRITE_TIMEOUT = 20000;

    private static final int PING_INTERVAL = 5000;

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final int GOING_AWAY = 1001;
    private static final int PROTOCOL_ERROR = 1002;
    private static final int CANNOT_ACCEPT = 1003;
    private static final int CLOSED_ABNORMALLY = 1006;
    private static final int VIOLATED_POLICY = 1008;
    private static final int UNEXPECTED_CONDITION = 1011;

    private static final boolean RETRY_OK_HTTP = true;

    private static final int QR_CODE = 346;
    private static final String DISPLAY_VALUE = "DISPLAY_VALUE";
    private static final String RAW_VALUE = "RAW_VALUE";
    private static final String DYNAMIC_FORM_TAG = "form_dynamic";

    private final Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

    private final FragmentManager manager = getSupportFragmentManager();

    private static WebSocketWrapper taskWs;
    private static WebSocketWrapper actionWs;

    private TaskListFragment taskFragment;

    private TextView lblTaskTitle;
    private TextView lblTaskCount;
    private ObjectAnimator anim;

    private AlertDialog alert;
    private LinearLayout greyOverlay;
    private ProgressBar taskProgress;

    private int sendCount = 0;
    private String lastActionId;
    private String lastParam;//reconnect on failure

    private boolean actionSuccess;
    private boolean backToMenu;//When user connects on another device and fragment is running
    private boolean readingBarcode;//ActiityResult

    private Vibrator vibrator;
    private MediaPlayer mp;

    private Handler uiHandler;

    private boolean displayingFragment;
    private BaseFragment activeFragment;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    //-------------------------------- RFID ------------------------------------//
    private boolean rfidEnabled;
    private boolean connectingDevice;

    // The command used to issue commands
    private final InventoryCommand mInventoryCommand = new InventoryCommand();

    private AsciiCommander commander;

    //------------------------------- PRINTER ---------------------------------//
    private PrintClient printer;

    private final Map<String, DocumentFragment> docFragentMap = new ConcurrentHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHandler = new Handler();
        setContentView(R.layout.activity_tasks);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        taskProgress = findViewById(R.id.taskProgressBar);
        greyOverlay = findViewById(R.id.greyoutOverlay);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        ConstraintLayout baseLayout = findViewById(R.id.tasksLayout);
        TextInputEditText edtSearch = findViewById(R.id.etSearchTask);

        baseLayout.setOnClickListener((View v) -> {
            stopAnimAndSound();
            showFullScreen();
        });

        if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_ALARM) == 0) {
            sendMessageNotification(getString(R.string.warn_increase_volume), 1000);
        }
        findViewById(R.id.imgSearch).setOnClickListener(v -> showHideSearch());
        findViewById(R.id.imgCamera).setOnClickListener(v -> openCamera());
        findViewById(R.id.imgRefresh).setOnClickListener(v -> uiHandler.post(this::refreshTasklist));
        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    taskFragment.scrollToTask(s.toString());
            }
        });

        lblTaskTitle = findViewById(R.id.lblTasks);
        lblTaskCount = findViewById(R.id.lblTaskCountTasks);
        anim = ObjectAnimator.ofInt(baseLayout, "backgroundColor", ContextCompat.getColor(this, R.color.colorPrimaryDark), ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.GTPNavy));
        taskFragment = (TaskListFragment) manager.findFragmentById(R.id.taskFragment);
        docFragentMap.put("ORDCONF", (CheckingFragment) Objects.requireNonNull(manager.findFragmentById(R.id.confFragment)));
        docFragentMap.put("OSG", (PickingFragment) Objects.requireNonNull(manager.findFragmentById(R.id.pickingFragment)));
        docFragentMap.put("ORDMOV", (TransportFragment) Objects.requireNonNull(manager.findFragmentById(R.id.transpFragment)));
        docFragentMap.put("ORDTRANSF", (RFIDTransferFragment) Objects.requireNonNull(manager.findFragmentById(R.id.rfidFragment)));
        docFragentMap.put("ORDPROD", (RFIDConsumptionFragment) Objects.requireNonNull(manager.findFragmentById(R.id.rfidConsFragment)));
        docFragentMap.put("PMINVENTORY", (RFIDRMInventoryFragment) Objects.requireNonNull(manager.findFragmentById(R.id.rfidInvFragment)));
        docFragentMap.put("FPINVENTORY", (FPInventoryFragment) Objects.requireNonNull(manager.findFragmentById(R.id.fpInventoryFragment)));
        docFragentMap.put("ORDCONFPALLET", (FPPalletCheckingFragment) Objects.requireNonNull(manager.findFragmentById(R.id.fpPalletCheckingFragment)));

        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        for (Fragment f : docFragentMap.values())
            fragmentTransaction.hide(f);

        fragmentTransaction.commit();
        if (HunterMobileWMS.isRFIDAvailable()) {
            //
            // An AsciiCommander has been created by the base class
            //
            commander = getCommander();

            // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
            // and passes the line onto the next responder
            // This is added first so that no other responder can consume received lines before they are logged.
            // commander.addResponder(new LoggerResponder());

            // Add a synchronous responder to handle synchronous commands
            commander.addSynchronousResponder();
        }
        anim.setDuration(150);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setRepeatCount(5);
        printer = new PrintClient(getBaseContext());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(readingBarcode || connectingDevice)) {
            if (HunterMobileWMS.getUser() != null) {
                showFullScreen();
            } else
                backToLogin("OnResume");
            uiHandler.post(() -> taskFragment.clearTasks());
            openTaskWebSocket();
            if (displayingFragment) {
                uiHandler.postDelayed(TasksActivity.this::reconnectActionWS, CONNECT_TIMEOUT);
            }
            mp = new MediaPlayer();
            connectingDevice = false;
            readingBarcode = false;
            updateTaskCounter();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG)
            Timber.d("Pausing Activity Reading Barcode %s Conencting Reader: %s", readingBarcode, connectingDevice);
        if (!(readingBarcode || connectingDevice)) {
            if (taskWs != null)
                taskWs.close(NORMAL_CLOSURE_STATUS, "Pausing Activity");
            if (actionWs != null)
                actionWs.close(NORMAL_CLOSURE_STATUS, "Pausing Activity");
            if (alert != null && alert.isShowing())
                alert.dismiss();
            mp.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG)
            Timber.d("Destroying Activity Reading Barcode %s Conencting Reader: %s", readingBarcode, connectingDevice);
        if (taskWs != null)
            taskWs.destroy();
        if (actionWs != null)
            actionWs.destroy();
        if (alert != null && alert.isShowing())
            alert.dismiss();

        taskWs = null;
        actionWs = null;
    }

    private void showFullScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            if (Looper.myLooper() != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                if (displayingFragment) {
//                actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.show();
                } else {
                    actionBar.hide();
                }
            }
        }
    }

    @Override
    public void reconnectActionWS() {
        if (BuildConfig.DEBUG)
            Timber.d("Reconnecting to action %s, doc %s, displayingFragment %s wsOnline %s", lastActionId, lastParam, displayingFragment, actionWs.isOnline());
        if (displayingFragment && lastActionId != null && lastParam != null && actionWs.isOnline()) {
            actionWs.close(GOING_AWAY, "Reconnecting");
        } else if (lastActionId != null && lastParam != null) {
            openActionWebSocket(lastActionId, lastParam);
        } else if (lastActionId == null && lastParam == null) {
            sendMessageNotification(getString(R.string.task_completed), 1500);
            if (displayingFragment)
                returnFromFragment();
        }
    }

    @Override
    public void openAction(ViewTaskStub task) {
        uiHandler.post(() -> {
            if (BuildConfig.DEBUG)
                Timber.d("Clicked Action %s DocName: %s", task.getActions()[0].getName(), task.getDocname() == null ? "NULL NAME" : task.getDocname());
            lockUI();
            actionSuccess = false;
            openActionWebSocket(task.getActions()[0].getId(), task.getActions()[0].getParams());
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        });
        runOnUiThread(this::stopAnimAndSound);
    }

    @Override
    public void onListFragmentInteraction(ViewTaskStub item) {
        stopAnimAndSound();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tasks, menu);
        return true;
    }

    /**
     * Prepare the menu options
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

//    public AsciiCommander actCommander() {
//        return this.commander;
//    }

    private void stopAnimAndSound() {
        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        }
        if (anim.isRunning())
            anim.end();
    }

    private void updateTaskCounter() {
        lblTaskCount.setText(getString(R.string.available_tasks_suffix, taskFragment.getTaskCount()));
        if (taskFragment.getTaskCount() > 0 && !anim.isRunning() && !displayingFragment)
            anim.start();
    }

    //----------------------------------------------------------------------------------------------
    // UI state and display update
    //----------------------------------------------------------------------------------------------

    private void displayReaderState() {
        String connectionMsg = "Reader: " + (getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Disconnected");
        setTitle(connectionMsg);
    }

    //
    // Set the state for the UI controls
    //
    private void UpdateUI() {
        //boolean isConnected = getCommander().isConnected();
        //TODO: configure UI control state
    }

    @Override
    public void cancelTask() {
        if (actionWs != null)
            actionWs.close(NORMAL_CLOSURE_STATUS, "Action Canceled");
        checkTaskWebsocket();
        System.gc();
    }

    @Override
    public boolean isRfidDeviceConnected() {
        return HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected();
    }

    @Override
    public void connectRfidDevice() {
        connectingDevice = true;
        // Choose a device and connect to it
        uiHandler.post(this::selectDevice);
    }

    @Override
    public void reconnectRfidDevice() {
        uiHandler.post(() -> {
            uiHandler.post(() -> Toast.makeText(TasksActivity.this, getString(R.string.msg_reader_reconnecting), Toast.LENGTH_SHORT).show());
            reconnectDevice();
            UpdateUI();
        });
    }

    @Override
    public void disconnectRfidDevice() {
        uiHandler.post(() -> {
            uiHandler.post(() -> Toast.makeText(TasksActivity.this, getString(R.string.msg_reader_disconnecting), Toast.LENGTH_SHORT).show());
            disconnectDevice();
            displayReaderState();
        });
    }

    @Override
    public void resetRfidDevice() {
        uiHandler.post(() -> {
            resetReader();
            UpdateUI();
        });
    }

    @Override
    public void sendMessageNotification(String message, long duration) {
        if (!this.isFinishing()) {//TODO: Fcking isFinishing
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final TextView txtView = new TextView(this);

            if (taskWs != null)
                taskWs.reset(duration);
            if (actionWs != null)
                actionWs.reset(duration);
            txtView.setText(message);
            txtView.setGravity(Gravity.CENTER);
            txtView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            txtView.setTextSize(24);
            txtView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.GTPNavy));
            builder.setView(txtView);
            if (Looper.myLooper() != null) {
                showMessage(builder, duration);
            } else if (!isFinishing()) {
                runOnUiThread(() -> showMessage(builder, duration));
            }
        }
    }

    private void showMessage(AlertDialog.Builder builder, long duration) {
        try {
            alert = builder.create();
            final Runnable runnable = () -> {
                if (!TasksActivity.this.isFinishing()) {//TODO: Fcking isFinishing
                    if (alert != null && alert.isShowing()) {
                        alert.dismiss();
                        alert = null;
                    }
                }
            };

            if (!this.isFinishing()) {//TODO: Fcking isFinishing
                alert.show();
                alert.setOnDismissListener(dialog12 -> uiHandler.removeCallbacks(runnable));
                uiHandler.postDelayed(runnable, duration);
            }
        } catch (WindowManager.BadTokenException bte) {
            if (BuildConfig.DEBUG) {
                Timber.d("BTL(Commented) - WindowManager.BadTokenException");
                runOnUiThread(() -> Toast.makeText(TasksActivity.this, "BTL(Commented) - WindowManager.BadTokenException", Toast.LENGTH_SHORT).show());
            }
            //backToLogin();
        }
    }

    @Override
    public IntegrationReturn sendDocument(Document filledDocument) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
        boolean sent = sendAction(gson.toJson(filledDocument));

        return new IntegrationReturn(sent, sent ? "" : "Falha ao enviar documento");
    }

    @Override
    public boolean sendThing(Thing t) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

        return sendAction(gson.toJson(t));
    }

    @Override
    public boolean sendAGLDocument(AGLDocument filledDocument) {
        return sendAction(gson.toJson(filledDocument));
    }

    @Override
    public boolean sendAGLDocumentProps(AGLDocumentProps filledDocument) {
        if (sendAction(gson.toJson(filledDocument))) {
            removeDynamicForm();
            return true;
        }
        return false;
    }

    private static final Object CON_MON = new Object();

    private boolean sendAction(String json) {
        try {
            if (!actionWs.isOnline())
                blockingReconnect();
            return executor.submit(callableAction(json)).get();
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Timber.e(e, e.getLocalizedMessage());
        }
        return false;
    }

    private void blockingReconnect() {
        while (!actionWs.isOnline()) {
            synchronized (CON_MON) {
                runOnUiThread(() -> Toast.makeText(this, "Reconectando Tarefa", Toast.LENGTH_SHORT).show());
                reconnectActionWS();
                try {
                    CON_MON.wait(CONNECT_TIMEOUT * 2);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private Callable<Boolean> callableAction(String json) {
        return () -> {
            boolean sent = false;

            synchronized (TasksActivity.this) {
                try {
                    do {
                        actionSuccess = false;
                        actionWs.pause();
                        actionWs.sendString(json);
                        long timeBefore = SystemClock.elapsedRealtime();
                        TasksActivity.this.wait(ACTION_TIMEOUT);
                        long runTime = SystemClock.elapsedRealtime() - timeBefore;
                        if (!actionSuccess && runTime > (ACTION_TIMEOUT - 1000) && sendCount < MAX_ACTION_SEND_RETRIES) {//avoid waiting some ms less than timeout
                            sendCount++;
                            sent = false;
                            while (!actionWs.isOnline()) {
                                runOnUiThread(() -> Toast.makeText(this, "Action Timeout (Not Connected) entering Reconnect (" + sendCount + ")", Toast.LENGTH_SHORT).show());
                                blockingReconnect();
                                runOnUiThread(() -> Toast.makeText(this, "Reconnected? " + actionWs.isOnline(), Toast.LENGTH_SHORT).show());
                            }
                        } else if (actionSuccess || runTime <= (ACTION_TIMEOUT - 1000))
                            sent = true;
                    } while (!sent);
                } catch (InterruptedException ie) {
                    sendMessageNotification(getString(R.string.task_expired), 5000);
                    actionSuccess = false;
                }
            }

            return actionSuccess;
        };
    }

    private final Callback<IntegrationReturn> printCallback = new Callback<IntegrationReturn>() {
        @Override
        public void onResponse(@NotNull Call<IntegrationReturn> call, retrofit2.Response<IntegrationReturn> response) {
            synchronized (TasksActivity.this) {
                if (response.isSuccessful()) {
                    IntegrationReturn ret = response.body();

                    if (ret == null)
                        ret = new IntegrationReturn(false, "Ret Null");
                    if (ret.getResult()) {
                        sendMessageNotification(getString(R.string.success), 1000);
                        actionSuccess = true;
                    } else {
                        Toast.makeText(getBaseContext(), ret.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Timber.d("Code: %03d  Message: %s", response.code(), response.message());
                    Timber.e("Print Response %s", response.raw().toString());
                    sendMessageNotification(getString(R.string.error_print), 5000);
                }
                TasksActivity.this.notifyAll();
            }
        }

        @Override
        public void onFailure(@NotNull Call<IntegrationReturn> call, @NotNull Throwable t) {
            sendMessageNotification(getString(R.string.connection_failed), 5000);
        }
    };

    @Override
    public boolean sendPrintFromConference(PrintPayload payload) {
        actionSuccess = false;
        synchronized (TasksActivity.this) {
            printer.print(payload, printCallback);
            try {
                TasksActivity.this.wait(300000);
            } catch (InterruptedException ie) {
                sendMessageNotification(getString(R.string.task_expired), 5000);
                actionSuccess = false;
            }
        }
        return actionSuccess;
    }

    public boolean enabled() {
        return rfidEnabled;
    }

    void setRFIDEnabled(boolean state) {
        if (HunterMobileWMS.isRFIDAvailable() && activeFragment instanceof BaseRFIDFragment) {
            boolean oldState = rfidEnabled;
            rfidEnabled = state;

            // Update the commander for state changes
            if (oldState != state) {
                if (rfidEnabled) {
                    // Listen for transponders
                    getCommander().addResponder((BaseRFIDFragment) activeFragment);
                    // Listen for barcodes
                    // getCommander().addResponder(mBarcodeResponder);
                } else {
                    // Stop listening for transponders
                    getCommander().removeResponder((BaseRFIDFragment) activeFragment);
                    // Stop listening for barcodes
                    // getCommander().removeResponder(mBarcodeResponder);
                }
            }
        } else {
            rfidEnabled = false;
        }
    }

    @Override
    public void updateRFID() {
        if (activeFragment instanceof BaseRFIDFragment) {
            // Configure the type of inventory
            mInventoryCommand.setIncludeTransponderRssi(TriState.YES);
            mInventoryCommand.setCaptureNonLibraryResponses(true);
            mInventoryCommand.setIncludeChecksum(TriState.YES);
            mInventoryCommand.setIncludePC(TriState.YES);
            mInventoryCommand.setTransponderReceivedDelegate((BaseRFIDFragment) activeFragment);
            mInventoryCommand.setResponseLifecycleDelegate((BaseRFIDFragment) activeFragment);
            updateConfiguration();
        }
    }

    //
    // Update the reader configuration from the command
    // Call this after each change to the model's command
    //

    void updateConfiguration() {
        if (HunterMobileWMS.isRFIDAvailable() && commander.isConnected()) {
            mInventoryCommand.setTakeNoAction(TriState.YES);
            commander.executeCommand(mInventoryCommand);
        }
    }

    //
    // Handle reset controls
    //
    private void resetReader() {
        try {
            // Reset the reader
            FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
            getCommander().executeCommand(fdCommand);
            String msg = "Reset " + (fdCommand.isSuccessful() ? "succeeded" : "failed");
            sendMessageNotification(msg, 1000);
            UpdateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareForFragment() {
        uiHandler.post(() -> {
            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.show();
            }
            lblTaskTitle.setVisibility(View.GONE);
            lblTaskCount.setVisibility(View.GONE);
        });
    }

    @Override
    public void returnFromFragment() {
        cancelTask();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        for (Fragment f : docFragentMap.values())
            fragmentTransaction.hide(f);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        fragmentTransaction.show(taskFragment);

        fragmentTransaction.addToBackStack(null);
        // Commit the transaction
        fragmentTransaction.commit();
        displayingFragment = false;
        uiHandler.post(() -> {
            lblTaskTitle.setVisibility(View.VISIBLE);
            lblTaskCount.setVisibility(View.VISIBLE);
            if (HunterMobileWMS.isRFIDAvailable() && enabled())
                setRFIDEnabled(false);
            updateTaskCounter();
        });
        removeDynamicForm();
        showFullScreen();
        System.gc();
    }

    @Override
    public void onBackPressed() {
        if (!displayingFragment) {
            stopAnimAndSound();
            NavUtils.navigateUpFromSameTask(this);
        }
        unlockUI();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (BuildConfig.DEBUG) Timber.d("OrientationChange %s", "landscape");
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (BuildConfig.DEBUG) Timber.d("OrientationChange %s", "portrait");
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
        taskFragment.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == QR_CODE) {
            assert data != null;
            String displayValue = data.getStringExtra(DISPLAY_VALUE);
            String rawValue = data.getStringExtra(RAW_VALUE);
            UUID taskId = UUID.fromString(rawValue);

            if (BuildConfig.DEBUG)
                Timber.d("Display Value %s", Objects.toString(displayValue));
            ViewTaskStub task = taskFragment.findTaskById(taskId);
            if (task != null) {
                openAction(task);
            } else
                sendMessageNotification(getString(R.string.warning_invalid_task), 1000);
        }

    }

    private void openTaskWebSocket() {
        runOnUiThread(this::updateTaskCounter);
        // WebSocket
        Request request = new Request.Builder().url(HunterURL.PROCESS_WS + "tasks/" + HunterMobileWMS.getToken()).build();
        taskWs = new TaskWebSocketListener();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .pingInterval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(RETRY_OK_HTTP)
                .build();
        final WebSocket webSocket = okHttpClient.newWebSocket(request, taskWs);
        okHttpClient.dispatcher().executorService().shutdown();
        taskWs.initialize(webSocket, uiHandler, "TASKS", 20000, PING_INTERVAL);
    }

    private void refreshTasklist() {
        taskFragment.clearTasks();
        if (taskWs.isOnline())
            taskWs.close(GOING_AWAY, "Refreshing");
        else
            openTaskWebSocket();
        sendMessageNotification(getString(R.string.refreshing), 1500);
    }

    // WebSocket
    private final class TaskWebSocketListener extends WebSocketWrapper {

        public TaskWebSocketListener() {
        }

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            if (BuildConfig.DEBUG)
                uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Connected to Tasks Service", Toast.LENGTH_LONG).show());
            start();
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, String text) {
            if ((text.equalsIgnoreCase("\"PONG\"") || text.equalsIgnoreCase("\"PING\"") || text.equalsIgnoreCase("PONG") || text.equalsIgnoreCase("PING"))) {
                keepAlive(PING_INTERVAL);
                return;
            }
            keepAlive(text.length() * 100);
            if (BuildConfig.DEBUG)
                Timber.d("Task WS Message: %s", text);

            final List<ViewTaskStub> receivedTasks = new CopyOnWriteArrayList<>();
            Type type = new TypeToken<List<ViewTaskStub>>() {
            }.getType();
            if (text.startsWith("["))
                receivedTasks.addAll(gson.fromJson(text, type));
            else
                receivedTasks.add(gson.fromJson(text, ViewTaskStub.class));
            filterTasks(receivedTasks);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
            if (BuildConfig.DEBUG) {
                uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Task Failure: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                Timber.d(t, "TaskWebSocket: %s", t.getLocalizedMessage());
            }
            stop(t.getLocalizedMessage());
            if (t instanceof SocketTimeoutException || t instanceof SocketException) {
                if (t.getLocalizedMessage() != null && t.getLocalizedMessage().contains("Connection reset by peer")) {
                    returnFromFragment();
                    taskWs.close(VIOLATED_POLICY, t.getLocalizedMessage());
                    backToLogin("SERVIDOR REINICIALIZADO");
                } else {
                    taskWs.close(GOING_AWAY, t.getLocalizedMessage());
                }
            }
            unlockUI();
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            switch (code) {
                case NORMAL_CLOSURE_STATUS:
                    break;
                case CANNOT_ACCEPT:
                    sendMessageNotification(getString(R.string.error_executing_task, reason), 4000);
                    break;
                case VIOLATED_POLICY:
                    backToLogin("Tasks VIOLATED POLICY");
                    break;
                case UNEXPECTED_CONDITION:
                    if (BuildConfig.DEBUG) {
                        Timber.d("Tasks Closed - Code %d Reason: %s", code, reason);
                        uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Comunicação com o serviço de tarefas perdida: (" + code + ": " + reason + ")", Toast.LENGTH_LONG).show());
                    }
                    sendMessageNotification(getString(R.string.reconnecting_ws, getString(R.string.menu_MOBILETASKS, "")), 1000);
                    uiHandler.postDelayed(TasksActivity.this::openTaskWebSocket, 5000);
                    break;
                case PROTOCOL_ERROR:
                    if (!displayingFragment) {
                        sendMessageNotification(getString(R.string.user_connected_another_device), 5000);
                        uiHandler.postDelayed(TasksActivity.this::onNavigateUp, 500);
                    } else backToMenu = true;
                    break;
                case CLOSED_ABNORMALLY:
                    sendMessageNotification(getString(R.string.msg_websocket_timeout), 3000);
                case GOING_AWAY:
                    uiHandler.postDelayed(TasksActivity.this::openTaskWebSocket, 100);
                    break;
                default:
                    if (BuildConfig.DEBUG)
                        sendMessageNotification("FECHOU PQ? " + reason + " (" + code + ")", 50000);
                    Timber.d("FECHOU PQ? %s (%d)", reason, code);
                    break;
            }
            if (BuildConfig.DEBUG)
                uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Comunicação com o serviço de tarefas perdida: (" + code + ": " + reason + ")", Toast.LENGTH_LONG).show());
        }

        private void filterTasks(List<ViewTaskStub> receivedTasks) {
            uiHandler.post(() -> {
                for (ViewTaskStub task : receivedTasks) {
                    if (task.isCancel() || task.isCancel_task()) {
                        taskFragment.removeTaskById(task.getId());
                        receivedTasks.remove(task);
                        continue;
                    }
                    if (taskFragment.containsTask(task)) {
                        receivedTasks.remove(task);
                    }
                }
                taskFragment.addTasks(receivedTasks);
                int taskCount = receivedTasks.size();
                if (taskCount > 0 && !displayingFragment) {
                    if (mp != null && !mp.isPlaying()) {
                        try {
                            vibrator.vibrate(new long[]{0L, 300L, 200L, 300L, 100L, 300L}, -1);
                            mp.setDataSource(TasksActivity.this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
                            mp.setLooping(true);
                            mp.prepare();
                            mp.start();
                        } catch (IOException ioe) {
                            Timber.e("Error Playing Sound: %s", ioe.getLocalizedMessage());
                        }
                    }
                    //sendMessageNotification(taskCount > 1 ? taskCount + " Novas Tarefas Recebidas" : "1 Nova Tarefa Recebida", 1000);
                }
                updateTaskCounter();
            });
        }
    }

    private void openActionWebSocket(String actionId, String param) {
        //openFakeWebSocket();
        // WebSocket
        Request request = new Request.Builder().url(HunterURL.PROCESS_WS + "action/" + HunterMobileWMS.getToken() + "/" + actionId + "/" + param).build();
        lastActionId = actionId;
        lastParam = param;
        if (actionWs == null)
            actionWs = new ActionWebSocketListener();
        else if (actionWs.isOnline()) {
            actionWs.close(GOING_AWAY, "Opening Another Action");
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .pingInterval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(RETRY_OK_HTTP)
                .build();
        final WebSocket webSocket = okHttpClient.newWebSocket(request, actionWs);
        okHttpClient.dispatcher().executorService().shutdown();
        sendCount = 0;
        taskFragment.removeTaskByActionParam(actionId, param);
        Timber.i("Action Opened. ActionId %s DocId %s", actionId, param);
        actionWs.initialize(webSocket, uiHandler, "ACTION", 20000, PING_INTERVAL);
    }

    // WebSocket
    private final class ActionWebSocketListener extends WebSocketWrapper {

        public ActionWebSocketListener() {
        }

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            if (BuildConfig.DEBUG)
                uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Connected to Actions Service", Toast.LENGTH_LONG).show());
            start();
            synchronized (CON_MON) {
                Timber.d("NotifyConnect");
                CON_MON.notifyAll();
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, String text) {
            if ((text.equalsIgnoreCase("\"PONG\"") || text.equalsIgnoreCase("\"PING\"") || text.equalsIgnoreCase("PONG") || text.equalsIgnoreCase("PING"))) {
                keepAlive(PING_INTERVAL);
                return;
            }
            keepAlive((text.length() < Byte.MAX_VALUE * 100) ? PING_INTERVAL : text.length() * 2);
            if (BuildConfig.DEBUG)
                Timber.d("Action WS Message %s", text);
            if (text.equals("null")) {
                if (BuildConfig.DEBUG)
                    Timber.d("Task is not available %s", lastParam);
                uiHandler.post(() -> taskFragment.removeTaskById(UUID.fromString(lastParam)));
            } else if (text.contains("\"result\"")) {
                synchronized (TasksActivity.this) {
                    IntegrationReturn ret = gson.fromJson(text, IntegrationReturn.class);

                    if (!ret.getResult()) {
                        sendMessageNotification(ret.getMessage(), 10000);
                        actionSuccess = false;
                    } else {
                        sendMessageNotification(getString(R.string.success), 1000);
                        actionSuccess = true;
                        actionWs.close(NORMAL_CLOSURE_STATUS, "Action Completed!");
                        lastActionId = null;
                        lastParam = null;
                        checkTaskWebsocket();
                    }
                    sendCount = 0;
                    TasksActivity.this.notifyAll();
                }
            } else if (!displayingFragment && !(text.contains("\"tagId\"") && text.contains("\"type\"") && text.contains("\"source\"") && text.contains("\"device\""))) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                AGLBaseDoc doc = gson.fromJson(text, AGLBaseDoc.class);
                Document d = doc.getMetaname() != null && (doc.getMetaname().toUpperCase().equals("ORDCONF") || doc.getMetaname().toUpperCase().equals("ORDMOV")) ? gson.fromJson(text.replace("\"model\"", "\"modelAGL\""), Document.class) : gson.fromJson(text, Document.class);

                if (d.getModel() == null) {
                    DocumentModel dm = new DocumentModel();

                    dm.setMetaname(doc.getMetaname().toUpperCase());
                    d.setModel(dm);
                }
                switch (d.getModel().getMetaname().toUpperCase()) {
                    case "ORDTRANSF":
//                        rfidTransfFrag(d);
                        break;
                    case "ORDPROD":
//                        rfidConsFrag(d);
                        break;
                    case "OSG":
//                        pickingFrag(d);
                        break;
                    case "APOCONTINV":
                        DocumentField contTypeField = DocumentUtil.getField(d, "CONTTYPE");
                        String contType = contTypeField == null ? "NA" : contTypeField.getValue();

                        switch (contType) {
                            case "CONT_PA":
                                d.getModel().setMetaname("FPINVENTORY");
                                break;
                            case "CONT_MP":
                                d.getModel().setMetaname("PMINVENTORY");
                                break;
                            default:
                                break;
                        }
                        break;
                    case "ORDCONF":
                        DocumentField confTypeField = DocumentUtil.getField(d, "CONF_TYPE");
                        String confType = confTypeField == null ? "NA" : confTypeField.getValue();

                        if (confType.equals("SPA") || confType.equals("SPAPD") || confType.equals("RPAPD")) {
                            d.getModel().setMetaname("ORDCONFPALLET");
                        } else {
                            try {
                                AGLDocument tmp = gson.fromJson(text, AGLDocument.class);

                                //Ajusta alguns itens divergentes do JSON - Armengo do Inferno
                                for (DocumentItem di : d.getItems()) {
                                    if (di.getProduct_id() != null) {
                                        di.setProduct(HunterMobileWMS.findProduct(UUID.fromString(di.getProduct_id())));
                                        if (di.getMeasureUnit() == null || di.getMeasureUnit().isEmpty()) {
                                            for (AGLDocItem aglDi : tmp.getItems()) {
                                                if (aglDi.getProduct_id().equals(di.getProduct_id())) {
                                                    di.setMeasureUnit(aglDi.getMeasureUnit());
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                for (DocumentThing dt : d.getThings()) {
                                    for (AGLThing tmpT : tmp.getThings()) {
                                        if (dt.getId().toString().equals(tmpT.getId())) {
                                            dt.setThing(tmpT.getThing());
                                            break;
                                        }
                                    }
                                }

                                for (AGLDocModel adm : tmp.getModel()) {
                                    DocumentField dField = new DocumentField();

                                    dField.setValue(adm.getValue());
                                    d.getFields().add(dField);
                                }
                                d.setCreatedAt(sdf.parse(doc.getCreatedAtSQL()));
                                d.setUpdatedAt(sdf.parse(doc.getUpdatedAtSQL()));
//                            confFrag(d);
                            } catch (ParseException pe) {
                                Timber.e("ORDCONF %s", Objects.requireNonNull(pe.getLocalizedMessage()));
                            }
                        }
                        break;
                    case "ORDMOV":
                        try {
                            AGLDocument tmp = gson.fromJson(text, AGLDocument.class);

                            d.setCreatedAt(sdf.parse(doc.getCreatedAtSQL()));
                            d.setUpdatedAt(sdf.parse(doc.getUpdatedAtSQL()));
                            d.getThings().clear();
                            d.getTransports().clear();
                            for (AGLThing at : tmp.getThings()) {
                                for (AGLAddress ad : tmp.getAddresses()) {
                                    if (ad.getId().equalsIgnoreCase(at.getAddress_id())) {
                                        DocumentThing dt = new DocumentThing();
                                        Address a = new Address();
                                        Thing t = at.getThing();

                                        a.setId(UUID.fromString(ad.getId()));
                                        a.setName(ad.getName());
                                        a.setParent_id(UUID.fromString(ad.getParent_id()));
                                        t.setAddress(a);
                                        for (AGLThing ats : at.getSiblings()) {
                                            Thing ts = ats.getThing();

                                            ts.setAddress(a);
                                            t.getSiblings().add(ts);
                                        }
                                        for (AGLTransport atr : tmp.getTransports()) {
                                            if (atr.getThing_id().equalsIgnoreCase(at.getId())) {
                                                DocumentTransport dtr = new DocumentTransport();

                                                dtr.setSeq(atr.getSeq());
                                                dtr.setThing(t);
                                                dtr.setOrigin(t.getAddress());
                                                for (AGLAddress add : tmp.getAddresses()) {
                                                    if (add.getId().equalsIgnoreCase(atr.getAddress_id())) {
                                                        Address dest = new Address();

                                                        dest.setId(UUID.fromString(add.getId()));
                                                        dest.setName(add.getName());
                                                        dest.setParent_id(UUID.fromString(add.getParent_id()));
                                                        dtr.setAddress(dest);
                                                    }
                                                }
                                                d.getTransports().add(dtr);
                                                break;
                                            }
                                        }
                                        dt.setThing(t);
                                        dt.setDocument(d);
                                        d.getThings().add(dt);
                                        break;
                                    }
                                }
                            }
//                            transpFrag(d);
                        } catch (ParseException pe) {
                            Timber.e("ORDMOV %s", Objects.requireNonNull(pe.getLocalizedMessage()));
                        }
                        break;
                    case "APOCHECKLIST":
                    case "APOCHECKSAIDA":
                        dynamicForm(gson.fromJson(text, AGLDocumentProps.class));
                        return;
                    case "APOLACRE":
                    case "CHECKINPORTARIA":
                    case "CHECKOUTPORTARIA":
                    default:
                        dynamicForm(d);
                        return;
                }
                displayDocumentFragment(d);
            } else if (text.contains("\"command\"") && text.contains("\"data\"")) {
                ActionMessage msg = gson.fromJson(text, ActionMessage.class);

                if (activeFragment instanceof DocumentFragment) {
                    ((DocumentFragment) activeFragment).interact(msg.getData());
                }
            } else {
                TypeToken<?> tt = TypeToken.getParameterized(Rawdata.class, LocationPayload.class);
                Rawdata<LocationPayload> rd = gson.fromJson(text, tt.getType());

                if (rd != null && rd.getType() != null && rd.getPayload() != null && activeFragment instanceof RawDataHandler)
                    executor.submit(() -> ((RawDataHandler<LocationPayload>) activeFragment).rawdata(rd));
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, ByteString bytes) {
            sendMessageNotification("ByteMessage: " + bytes.toAsciiUppercase(), 20000);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
            if (BuildConfig.DEBUG) {
                uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Action Failure: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                Timber.e(t, t.getLocalizedMessage());
            }
            stop(t.getLocalizedMessage());
            if (t instanceof SocketTimeoutException || t instanceof SocketException) {
                actionWs.close(GOING_AWAY, t.getLocalizedMessage());
            }
            unlockUI();
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            switch (code) {
                case NORMAL_CLOSURE_STATUS:
                    break;
                case PROTOCOL_ERROR:
                    taskFragment.removeTaskById(UUID.fromString(lastParam));
                    sendMessageNotification(getString(R.string.taskInUse), 4000);
                    updateTaskCounter();
                    break;
                case CANNOT_ACCEPT:
                    if (lastParam != null)
                        taskFragment.removeTaskById(UUID.fromString(lastParam));
                    sendMessageNotification(getString(R.string.error_executing_task, reason), 4000);
                    updateTaskCounter();
                    if (BuildConfig.DEBUG)
                        Timber.e("Token %s Last Id %s Last Param: %s", HunterMobileWMS.getToken(), lastActionId, lastParam);
                    break;
                case VIOLATED_POLICY:
                    uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Token: " + HunterMobileWMS.getToken(), Toast.LENGTH_LONG));
                    backToLogin("Action VIOLATED POLICY");
                    break;
                case UNEXPECTED_CONDITION:
                    String message;

                    if (lastActionId != null && lastParam != null) {
                        message = getString(R.string.reconnecting_ws, getString(R.string.actions));
                        uiHandler.postDelayed(TasksActivity.this::reconnectActionWS, CONNECT_TIMEOUT);
                    } else {
                        message = getString(R.string.warning_invalid_task) + " " + getString(R.string.refreshing);
                        refreshTasklist();
                    }
                    sendMessageNotification(message, 1500);
                    break;
                case CLOSED_ABNORMALLY:
                    sendMessageNotification(getString(R.string.msg_websocket_timeout), 3000);
                case GOING_AWAY:
                    uiHandler.postDelayed(TasksActivity.this::reconnectActionWS, 100);
                    break;
                default:
                    sendMessageNotification("FECHOU PQ? " + reason + " (" + code + ")", 50000);
                    break;
            }
            if (BuildConfig.DEBUG)
                uiHandler.post(() -> Toast.makeText(TasksActivity.this, "Comunicação com o serviço de ações perdida: (" + code + ": " + reason + ")", Toast.LENGTH_LONG).show());
            Timber.i("Action Closed. Code %d Reason %s", code, reason);
            stop(reason);
            unlockUI();
        }
    }


    //----------------------------GENERALIZAR---------------------------//
    private void dynamicForm(Document doc) {
        if (!displayingFragment) {
            startDynamicForm(FormDialogFragment.newInstance(doc));
        }
    }

    private void dynamicForm(AGLDocumentProps doc) {
        if (!displayingFragment) {
            startDynamicForm(FormDialogFragment.newInstance(doc));
        }
    }

    private void startDynamicForm(FormDialogFragment dynamicFormDialog) {
        displayingFragment = true;
        if (mp.isPlaying())
            runOnUiThread(this::stopAnimAndSound);
        dynamicFormDialog.show(manager, DYNAMIC_FORM_TAG);
    }

    private void removeDynamicForm() {
        // close existing dialog fragments
        Fragment frag = manager.findFragmentByTag(DYNAMIC_FORM_TAG);

        if (frag != null)
            manager.beginTransaction().remove(frag).commit();
        displayingFragment = false;
        activeFragment = null;
        unlockUI();
    }

    private void displayDocumentFragment(Document doc) {
        DocumentFragment frag = docFragentMap.get(doc.getModel().getMetaname());

        if (frag != null && !displayingFragment) {
            prepareForFragment();
            FragmentTransaction fragmentTransaction = manager.beginTransaction();

            frag.transform(doc);
            activeFragment = frag;
            updateRFID();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.hide(taskFragment);
            for (Fragment f : docFragentMap.values()) {
                if (f != frag)
                    fragmentTransaction.hide(f);
                else if (BuildConfig.DEBUG)
                    Timber.d("Frag Equal: %s", Objects.requireNonNull(frag.getClass().getCanonicalName()));
            }
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            fragmentTransaction.show(frag);
            fragmentTransaction.addToBackStack(null);
            displayingFragment = true;
            uiHandler.post(() -> {
                setTitle(doc.getCode());
                stopAnimAndSound();
            });
            unlockUI();
            // Commit the transaction
            fragmentTransaction.commit();
            if (!enabled())
                setRFIDEnabled(true);
        }
    }

    @Override
    public void lockUI() {
        taskProgress.setVisibility(View.VISIBLE);
        greyOverlay.setVisibility(View.VISIBLE);
    }

    private void unlockUI() {
        uiHandler.post(() -> {
            taskProgress.setVisibility(View.GONE);
            greyOverlay.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        });
    }

    private void backToLogin(String reason) {
        Intent intent = new Intent(this, LoginActivity.class);

        Timber.d("BTL - %s", reason);
        if (BuildConfig.DEBUG)
            runOnUiThread(() -> Toast.makeText(TasksActivity.this, reason, Toast.LENGTH_SHORT).show());
        if (taskWs != null)
            taskWs.close(NORMAL_CLOSURE_STATUS, "BTL - " + reason);
        if (actionWs != null)
            actionWs.close(NORMAL_CLOSURE_STATUS, "BTL - " + reason);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void checkTaskWebsocket() {
        if (backToMenu) {
            sendMessageNotification(getString(R.string.user_connected_another_device), 5000);
            uiHandler.postDelayed(TasksActivity.this::onNavigateUp, 1000);
            backToMenu = false;
        }
    }

    public void openCamera() {
        Intent intent = new Intent(TasksActivity.this, BarcodeActivity.class);

        readingBarcode = true;
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, QR_CODE);
    }

    public void showHideSearch() {
        TextInputLayout tiLayout = findViewById(R.id.tfSearchTask);

        tiLayout.setVisibility(tiLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
}