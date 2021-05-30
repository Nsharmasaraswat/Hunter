package com.gtp.hunter.wms.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.structure.viewmodel.ProcessViewModel;
import com.gtp.hunter.structure.websocket.WebSocketWrapper;
import com.gtp.hunter.wms.api.HunterURL;
import com.gtp.hunter.wms.model.ActionMessage;
import com.gtp.hunter.wms.model.Rawdata;
import com.gtp.hunter.wms.model.SpinnerDisplayName;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import timber.log.Timber;

import static com.google.gson.reflect.TypeToken.getParameterized;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ProcessActivity extends AppCompatActivity {
    private static final int MAX_ACTION_SEND_RETRIES = 2;
    private static final int ACTION_TIMEOUT = 30000;
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 20000;
    private static final int WRITE_TIMEOUT = 20000;

    private static final int PING_INTERVAL = 35000;

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final int GOING_AWAY = 1001;
    private static final int CANNOT_ACCEPT = 1003;
    private static final int NO_STATUS_CODE = 1005;
    private static final int VIOLATED_POLICY = 1008;
    private static final int ERROR_CLOSURE_STATUS = 1011;

    private static final boolean RETRY_OK_HTTP = true;

    private static final Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

    private ProgressDialog progressDialog;
    private ProcessViewModel mViewModel;

    private Handler uiHandler;

    private AlertDialog alert;
    private ConstraintLayout baseLayout;
    private TextView txtBarcode;
    private TextView txtDestination;

    private WebSocketWrapper processWs;
    private UUID lastProcessId;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        JsonObject params = gson.fromJson(Objects.requireNonNull(getIntent().getExtras()).getString("params"), JsonObject.class);
        String procId = params.get("process_id").getAsString();
        if (HunterURL.BASE == null) {
            onNavigateUp();
        }

        mViewModel = new ViewModelProvider(this).get(ProcessViewModel.class);
        uiHandler = new Handler();
        setContentView(R.layout.activity_process);

        if (HunterMobileWMS.getUser().getProperties().containsKey("uses_forklift") && HunterMobileWMS.getUser().getProperties().get("uses_forklift").equalsIgnoreCase("TRUE")) {
            SearchableSpinner cbxForklifts = findViewById(R.id.cbxForklifts);
            String[] stringArray = getResources().getStringArray(R.array.forklifts);
            List<SpinnerDisplayName> forklifts = new ArrayList<>();
            //TODO: List Forklifts, filter device

            for (String s : stringArray) {
                String[] splitResult = s.split("\\|");

                if (splitResult[2].equals(procId))
                    forklifts.add(new SpinnerDisplayName(splitResult[0], getString(R.string.forklift) + " - " + splitResult[1]));
            }
            cbxForklifts.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, forklifts));
            cbxForklifts.setVisibility(View.VISIBLE);
            cbxForklifts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Timber.d("Selected: %d - %s", pos, parent.getItemAtPosition(pos));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        baseLayout = findViewById(R.id.processLayout);
        txtBarcode = findViewById(R.id.txtBarcode);
        txtDestination = findViewById(R.id.txtDestination);
        baseLayout.setOnClickListener((v) -> showFullScreen());
        openWebsocket(UUID.fromString(procId));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFullScreen();
    }

    @Override
    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
    }

    public void openWebsocket(UUID processId) {
        uiHandler.post(() -> {
            if (processWs != null)
                processWs.close(NORMAL_CLOSURE_STATUS, "Opening Another Action");
            processWs = openProcessWebSocket(processId.toString());
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            lastProcessId = processId;
        });
    }

    private WebSocketWrapper openProcessWebSocket(String processId) {
        // WebSocket
        Request request = new Request.Builder().url(HunterURL.PROCESS_WS + "process/" + processId).build();
        ProcessWebSocketListener listener = new ProcessWebSocketListener();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .pingInterval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(RETRY_OK_HTTP)
                .build();
        final WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        okHttpClient.dispatcher().executorService().shutdown();
        Timber.i("Process Opened. ProcessId %s", processId);
        return listener.initialize(webSocket, uiHandler, "ACTION", 20000, PING_INTERVAL);
    }

    // WebSocket
    private final class ProcessWebSocketListener extends WebSocketWrapper {

        public ProcessWebSocketListener() {
        }

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            if (BuildConfig.DEBUG)
                uiHandler.post(() -> Toast.makeText(ProcessActivity.this, "Connected to Process Service", Toast.LENGTH_LONG).show());
            start();
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, String text) {
            if ((text.equalsIgnoreCase("\"PONG\"") || text.equalsIgnoreCase("\"PING\"") || text.equalsIgnoreCase("PONG") || text.equalsIgnoreCase("PING"))) {
                keepAlive(PING_INTERVAL);
                return;
            }
            keepAlive((text.length() < Byte.MAX_VALUE * 100) ? PING_INTERVAL : text.length() * 2);
            if (BuildConfig.DEBUG)
                Timber.d("Process WS Message %s", text);
            if (text.equals("null")) {
                Timber.e("Mensagem inválida");
            } else if (text.contains("\"command\"") && text.contains("\"data\"")) {
                ActionMessage msg = gson.fromJson(text, ActionMessage.class);

                Timber.d("Message Received!");
                switch (msg.getCommand()) {
                    case "TRANSPORT_PALLET":
                        sendMessageNotification("ENDEREÇO DESTINO: " + msg.getData(), 2000);
                        mViewModel.setDestination(msg.getData());
                        uiHandler.post(() -> txtDestination.setText(msg.getData()));
                        mViewModel.setTransporting(true);
                        break;
                    case "SUCCESS":
                        mViewModel.setTransporting(false);
                        uiHandler.post(() -> {
                            mViewModel.setPallet(null);
                            mViewModel.setDestination(null);
                            txtBarcode.setText("");
                            txtDestination.setText("");
                            baseLayout.setBackground(null);
                        });
                        break;
                    case "CANCEL":
                        Timber.d("Cancel Transport");
                        uiHandler.post(() -> {
                            txtBarcode.setText("");
                            txtDestination.setText(msg.getData());
                            baseLayout.setBackground(null);
                        });
                        mViewModel.setTransporting(false);
                        break;
                }
            } else {
                TypeToken<?> tt = getParameterized(Rawdata.class, String.class);
                Rawdata<String> rd = gson.fromJson(text, tt.getType());

                switch (rd.getType()) {
                    case "IDENT":
                        if (!mViewModel.isTransporting()) { //Leitura de palete pre-transporte
                            mViewModel.setTransporting(true);
                            uiHandler.post(() -> {
                                sendMessageNotification("NOVO PALETE DETECTADO: " + rd.getTagId(), 2000);
                                mViewModel.setPallet(rd.getTagId());
                                mViewModel.setDestination(null);
                                txtBarcode.setText(rd.getTagId());
                                txtDestination.setText("");
                                processWs.sendString("{\"command\":\"PALLET_CHECK\",\"data\":\"" + rd.getTagId() + "\"}");
                            });
                        } else {//Leitura de endereço
                            if (txtDestination.getText().equals(rd.getTagId())) {
                                transport(rd.getTagId());
                            } else {
                                new AlertDialog.Builder(ProcessActivity.this)
                                        .setTitle(getString(R.string.title_wrong_destination, rd.getTagId()))
                                        .setMessage(getString(R.string.question_confirm_transport, rd.getTagId()))
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) ->
                                                transport(rd.getTagId())
                                        )
                                        .setNegativeButton(android.R.string.no, (dialog, whichButton) ->
                                                uiHandler.post(() -> {
                                                    baseLayout.setBackground(ContextCompat.getDrawable(ProcessActivity.this, R.drawable.background_transp_nok));
                                                    mViewModel.setTransporting(true);
                                                })
                                        ).show();
                            }
                        }
                        break;
                    case "STATUS":
                        runOnUiThread(() -> Toast.makeText(ProcessActivity.this, rd.getPayload(), Toast.LENGTH_LONG));
                    case "SENSOR":
                    case "LOCATION":
                    default:
                        break;
                }
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, ByteString bytes) {
            sendMessageNotification("ByteMessage: " + bytes.toAsciiUppercase(), 20000);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
            if (BuildConfig.DEBUG) {
                uiHandler.post(() -> Toast.makeText(ProcessActivity.this, "Process Failure: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                Timber.e(t, t.getLocalizedMessage());
            }
            if (t instanceof SocketTimeoutException || t instanceof SocketException)
                processWs.close(ERROR_CLOSURE_STATUS, t.getLocalizedMessage());
            stop(t.getLocalizedMessage());
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            switch (code) {
                case NORMAL_CLOSURE_STATUS:
                    break;
                case CANNOT_ACCEPT:
                    backToLogin("CANNOT_ACCEPT");
                    break;
                case VIOLATED_POLICY:
                case ERROR_CLOSURE_STATUS:
                    sendMessageNotification(getString(R.string.reconnecting_ws, getString(R.string.actions)), 1500);
                    uiHandler.postDelayed(() -> processWs = openProcessWebSocket(lastProcessId.toString()), CONNECT_TIMEOUT);
                    break;
                case GOING_AWAY:
                    uiHandler.postDelayed(() -> processWs = openProcessWebSocket(lastProcessId.toString()), CONNECT_TIMEOUT);
                    break;
                default:
                    sendMessageNotification("FECHOU PQ? " + reason + " (" + code + ")", 50000);
                    break;
            }
            if (BuildConfig.DEBUG)
                uiHandler.post(() -> Toast.makeText(ProcessActivity.this, "Comunicação com o serviço de ações perdida: (" + code + ": " + reason + ")", Toast.LENGTH_LONG).show());
            Timber.i("Action Closed. Code %d Reason %s", code, reason);
            stop(reason);
        }
    }

    public void transport(String tagId) {
        mViewModel.setTransporting(false);
        uiHandler.post(() -> {
            String dataStr = "{\\\"tagid\\\":\\\"" + mViewModel.getPallet() + "\\\",\\\"user\\\":\\\"" + HunterMobileWMS.getUser().getName() + "\\\",\\\"address\\\":\\\"" + tagId + "\\\"}";

            sendMessageNotification("ENDEREÇO DESTINO: " + tagId + " ALCANÇADO", 2000);
            baseLayout.setBackground(ContextCompat.getDrawable(ProcessActivity.this, R.drawable.background_transp_ok));
            processWs.sendString("{\"command\":\"PALLET_STORE\",\"data\":\"" + dataStr + "\"}");
        });
    }

    public void sendMessageNotification(String message, long duration) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final TextView txtView = new TextView(this);

        if (processWs != null)
            processWs.reset(duration);
        txtView.setText(message);
        txtView.setGravity(Gravity.CENTER);
        txtView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtView.setTextSize(25);
        txtView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.GTPNavy));
        builder.setView(txtView);
        if (Looper.myLooper() != null) {
            showMessage(builder, duration);
        } else {
            runOnUiThread(() -> showMessage(builder, duration));
        }
    }

    private void showMessage(AlertDialog.Builder builder, long duration) {
        try {
            alert = builder.create();
            final Runnable runnable = () -> {
                if (alert != null && alert.isShowing()) {
                    alert.dismiss();
                    alert = null;
                }
            };

            alert.show();
            alert.setOnDismissListener(dialog12 -> uiHandler.removeCallbacks(runnable));
            uiHandler.postDelayed(runnable, duration);
        } catch (WindowManager.BadTokenException ignored) {
        }
    }

    private void backToLogin(String from) {
        Intent intent = new Intent(this, LoginActivity.class);

        if (processWs != null)
            processWs.close(NORMAL_CLOSURE_STATUS, "Back to Login: " + from);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
}
