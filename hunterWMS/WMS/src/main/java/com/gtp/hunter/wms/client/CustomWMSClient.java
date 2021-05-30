package com.gtp.hunter.wms.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.CustomWMSAPI;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.Thing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomWMSClient extends AuthenticatedClient {

    private final Context ctx;
    private CustomWMSAPI wmsAPI;

    public CustomWMSClient(@NonNull Context context) {
        this.ctx = context;
        createCustomWMSAPI();
    }

    private void createCustomWMSAPI() {
        wmsAPI = retrofit().create(CustomWMSAPI.class);
    }

    private final Callback<List<Thing>> thingListCallback = new Callback<List<Thing>>() {
        @Override
        public void onResponse(Call<List<Thing>> call, Response<List<Thing>> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Log.d("Thing Callback", "Code: " + response.code() + " Message: " + response.message());
                Log.e("Thing Response", response.raw().toString());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_thing), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<List<Thing>> call, Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    public Call<IntegrationReturn> removePallet(Thing t) {
        return wmsAPI.removeThing(t.getId());
    }

    public Call<IntegrationReturn> createTransport(Address origin, Address destination, int quantity) {
        return wmsAPI.createTransport(origin.getId(), destination.getId(), quantity);
    }

    public Call<IntegrationReturn> createTransport(List<Thing> thList, Address destination) {
        List<UUID> thIdList = new ArrayList<>();

        for (Thing th : thList) {
            thIdList.add(th.getId());
        }
        return createTransport(thIdList, destination.getId());
    }

    public Call<IntegrationReturn> createTransport(List<UUID> thIdList, UUID addId) {
        return wmsAPI.createTransport(thIdList, addId);
    }

    public void loadAllocation(Address address) {
        wmsAPI.loadAllocation(address.getId()).enqueue(thingListCallback);
    }

    public void listDockAllocation() {
        wmsAPI.listDockAllocation().enqueue(thingListCallback);
    }
}