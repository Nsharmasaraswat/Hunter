package com.gtp.hunter.wms.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.CustomDocumentAPI;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.IntegrationReturn;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomDocumentClient extends AuthenticatedClient {

    private Context ctx;
    private CustomDocumentAPI documentAPI;

    public CustomDocumentClient(Context context) {
        this.ctx = context;
        createCustomDocumentAPI();
    }

    private void createCustomDocumentAPI() {
        documentAPI = retrofit().create(CustomDocumentAPI.class);
    }

    private Callback<IntegrationReturn> documentListCallback = new Callback<IntegrationReturn>() {
        @Override
        public void onResponse(Call<IntegrationReturn> call, Response<IntegrationReturn> response) {
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
        public void onFailure(Call<IntegrationReturn> call, Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    public Call<IntegrationReturn> postDocument(AGLDocument doc) {
        return documentAPI.postDocument(doc);
    }
}