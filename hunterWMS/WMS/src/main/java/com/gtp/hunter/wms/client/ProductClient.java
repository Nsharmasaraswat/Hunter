package com.gtp.hunter.wms.client;

import android.content.Context;
import android.widget.Toast;

import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.ProductAPI;
import com.gtp.hunter.wms.model.Product;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ProductClient extends AuthenticatedClient {

    private final Context ctx;
    private ProductAPI productAPI;

    private final Callback<List<Product>> productCallback = new Callback<List<Product>>() {
        @Override
        public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
            long sent = response.raw().sentRequestAtMillis();
            long recv = response.raw().receivedResponseAtMillis();
            Timber.d("Load Products %d", System.currentTimeMillis());
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Timber.d("Product Callback Code: %d Message: %s", response.code(), response.message());
                Timber.e("Product Response %s", response.raw().toString());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_product), Toast.LENGTH_LONG).show();
                setChanged();
                notifyObservers(null);
            }
        }

        @Override
        public void onFailure(Call<List<Product>> call, Throwable t) {
            Timber.d("Product Callback Error %s", t.getLocalizedMessage() == null ? ctx.getString(R.string.connection_failed) : t.getLocalizedMessage());
            if (BuildConfig.DEBUG)
                t.printStackTrace();
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
            setChanged();
            notifyObservers(null);
        }
    };

    public ProductClient(Context context) {
        this.ctx = context;
        createProductAPI();
    }

    private void createProductAPI() {
        productAPI = retrofit().create(ProductAPI.class);
    }

    public void listFrom(Date updatedAt) {
        if (updatedAt == null) updatedAt = new Date(0L);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(updatedAt);

        Timber.d("Product Load Init %d", System.currentTimeMillis());
        productAPI.listFrom(date).enqueue(productCallback);
    }

    public void listAll() {
        productAPI.listAll().enqueue(productCallback);
    }

    public void listAllNoField() {
        productAPI.listAllNoField().enqueue(productCallback);
    }
}