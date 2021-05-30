package com.gtp.hunter.wms.client;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.DocumentAPI;
import com.gtp.hunter.wms.model.Document;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentClient extends AuthenticatedClient {

    private Context ctx;
    private DocumentAPI documentAPI;

    public DocumentClient(Context context) {
        this.ctx = context;
        createDocumentAPI();
    }

    private void createDocumentAPI() {
        documentAPI = retrofit().create(DocumentAPI.class);
    }

    private Callback<List<Document>> documentListCallback = new Callback<List<Document>>() {
        @Override
        public void onResponse(Call<List<Document>> call, Response<List<Document>> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Log.d("Document Callback", "Code: " + response.code() + " Message: " + response.message());
                Log.e("Document Response", response.raw().toString());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_thing), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<List<Document>> call, Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    private Callback<Document> ordProdCallback = new Callback<Document>() {
        @Override
        public void onResponse(Call<Document> call, Response<Document> response) {
            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    setChanged();
                    notifyObservers(response.body());
                }
            } else {
                Log.d("Document Callback", "Code: " + response.code() + " Message: " + response.message());
                Log.e("Document Response", response.raw().toString());
                Toast.makeText(ctx, ctx.getText(R.string.error_load_thing), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<Document> call, Throwable t) {
            Toast.makeText(ctx, ctx.getText(R.string.connection_failed), Toast.LENGTH_LONG).show();
        }
    };

    public void listByModelAndStatus(String docModel, String docStatus) {
        documentAPI.listByModelAndStatus(docModel, docStatus).enqueue(documentListCallback);
    }

    public Call<Document> getRunningPO(String line) {
        return documentAPI.getRunningPO(line);
    }
}