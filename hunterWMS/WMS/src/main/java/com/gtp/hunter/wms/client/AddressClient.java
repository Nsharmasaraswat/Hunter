package com.gtp.hunter.wms.client;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.AddressAPI;
import com.gtp.hunter.wms.model.Address;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class AddressClient extends AuthenticatedClient {

    private long init;
    private final Context ctx;
    private AddressAPI addressAPI;

    private final Callback<List<Address>> addressCallback = new Callback<List<Address>>() {
        @Override
        public void onResponse(@NonNull Call<List<Address>> call, @NonNull Response<List<Address>> response) {
            Timber.d("Load Addresses %d s", (SystemClock.elapsedRealtime() - init) / 1000);
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Timber.e("Address Response Code: %d Message: %s", response.code(), response.message());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_address), Toast.LENGTH_LONG).show();
                setChanged();
                notifyObservers(null);
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Address>> call, Throwable t) {
            Timber.d("Address Callback %s", t.getLocalizedMessage() == null ? ctx.getString(R.string.connection_failed) : t.getLocalizedMessage());

            if (t instanceof JsonSyntaxException || t instanceof MalformedJsonException) {
                setChanged();
                notifyObservers(new ArrayList<Address>());
            } else {
                Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
                setChanged();
                notifyObservers(null);
            }
        }
    };

    public AddressClient(Context context) {
        this.ctx = context;
        createAddressAPI();
    }

    private void createAddressAPI() {
        addressAPI = retrofit().create(AddressAPI.class);
    }

    public void listFrom(Date updatedAt) {
        if (updatedAt == null) updatedAt = new Date(0L);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(updatedAt);

        init = SystemClock.elapsedRealtime();
        Timber.d("Address load Init %d", init);
        addressAPI.listByLocationFrom("4cc18967-92f3-11e9-815b-005056a19775", date).enqueue(addressCallback);
    }

    public void listAll() {
        addressAPI.listAll().enqueue(addressCallback);
    }

    public void listAllNoField() {
        addressAPI.listAllNoField().enqueue(addressCallback);
    }
}
