package com.gtp.hunter.wms.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.ThingAPI;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThingClient extends AuthenticatedClient {

    private Context ctx;
    private ThingAPI thingAPI;

    public ThingClient(Context context) {
        this.ctx = context;
        createThingAPI();
    }

    private void createThingAPI() {
        thingAPI = retrofit().create(ThingAPI.class);
    }

    private Callback<Thing> thingCallback = new Callback<Thing>() {
        @Override
        public void onResponse(Call<Thing> call, Response<Thing> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    Log.e("Response", response.body() == null ? "NULL" : response.body().getName());
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
        public void onFailure(Call<Thing> call, Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    private Callback<List<Thing>> thingListCallback = new Callback<List<Thing>>() {
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

    public void asyncFindByTagId(String tagId) {
        thingAPI.findByTagId(tagId).enqueue(thingCallback);
    }

    public Thing findByTagId(String tagId) {
        try {
            return thingAPI.findByTagId(tagId).execute().body();
        } catch (IOException ignored) {

        }
        return null;
    }

    public void asyncListByDocument(String docId) {
        thingAPI.listByDocument(docId).enqueue(thingListCallback);
    }

    public List<Thing> listByDocument(String docId) {
        try {
            return thingAPI.listByDocument(docId).execute().body();
        } catch (IOException ignored) {

        }
        return null;
    }

    public void asyncListByProduct(Product p) {
        thingAPI.listByProduct(p.getId().toString()).enqueue(thingListCallback);
    }

    public void asyncQuickListByProduct(Product p) {
        thingAPI.listQuickByProduct(p.getId().toString()).enqueue(thingListCallback);
    }

    public void asyncListByProductNotStatus(Product p, String status) {
        thingAPI.listByProductIdAndNotStatus(p.getId().toString(), status).enqueue(thingListCallback);
    }

    public void asyncListByAddress(Address a) {
        thingAPI.listByAddress(a.getId().toString()).enqueue(thingListCallback);
    }

    public void asyncListByAddressChildren(Address a) {
        thingAPI.listByAddressChildren(a.getId().toString()).enqueue(thingListCallback);
    }

    public Thing remove(Thing t) {
        try {
            thingAPI.removeById(t.getId().toString()).execute();
        } catch (IOException ignored) {

        }
        return t;
    }

    public void findParent(Thing t) {
        thingAPI.findParent(t.getId().toString()).enqueue(thingCallback);
    }
}