package com.gtp.hunter.wms.client;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.UserAPI;
import com.gtp.hunter.wms.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class UserClient extends AuthenticatedClient {
    private final Context ctx;
    private UserAPI userAPI;

    public UserClient(Context context) {
        this.ctx = context;
        createUserAPI();
    }

    private void createUserAPI() {
        userAPI = retrofit().create(UserAPI.class);
    }

    private final Callback<User> userCallback = new Callback<User>() {
        @Override
        public void onResponse(@NonNull Call<User> call, Response<User> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    Timber.e("Response %s", response.body() == null ? "NULL" : response.body().getName());
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Timber.e("User Callback: %s", "Code: " + response.code() + " Message: " + response.message());
                Timber.e("User Response: %s", response.raw().toString());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_user), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    private final Callback<Void> voidCallback = new Callback<Void>() {
        @Override
        public void onResponse(@NonNull Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    Timber.d("Void Callback %s", "SUCCESS");
                }
            } else if (response.code() == 303) {
                List<String> path = call.request().url().pathSegments();

                putProperty(path.get(4), path.get(5));
            } else {
                Timber.d("Void Callback: %s", "Code: " + response.code() + " Message: " + response.message());
                Timber.e("Void Response %s", response.raw().toString());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_user), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    public void getLogged() {
        userAPI.getLogged().enqueue(userCallback);
    }

    public void postProperty(String key, String value) {
        userAPI.postProperty(key, value).enqueue(voidCallback);
    }

    public void putProperty(String key, String value) {
        userAPI.putProperty(key, value).enqueue(voidCallback);
    }
}