package com.gtp.hunter.wms.client;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.AlertAPI;
import com.gtp.hunter.wms.model.Alert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AlertClient extends AuthenticatedClient {

    private final Context ctx;
    private AlertAPI alertAPI;

    private final Callback<List<Alert>> alertCallback = new Callback<List<Alert>>() {
        @Override
        public void onResponse(@NonNull Call<List<Alert>> call, @NonNull Response<List<Alert>> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Timber.e("Alert Response Code: %d Message: %s", response.code(), response.message());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_alert), Toast.LENGTH_LONG).show();
                setChanged();
                notifyObservers(null);
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Alert>> call, Throwable t) {
            if (t instanceof JsonSyntaxException || t instanceof MalformedJsonException) {
                setChanged();
                notifyObservers(new ArrayList<Alert>());
            } else {
                Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
                setChanged();
                notifyObservers(null);
            }
        }
    };

    public AlertClient(Context context) {
        this.ctx = context;
        createAlertAPI();
    }

    private void createAlertAPI() {
        alertAPI = retrofit().create(AlertAPI.class);
    }

    public void listAll() {
        alertAPI.listAll().enqueue(alertCallback);
    }

    public Alert save(Alert alert) {
        try {
            Response<Alert> resp = alertAPI.save(alert).execute();

            if (resp.code() == 200)
                return resp.body();

        } catch (IOException ioe) {
            Timber.e(ioe);
        }
        return null;
    }
}
