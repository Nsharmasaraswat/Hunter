package com.gtp.hunter.wms.client;

import android.content.Context;

import com.gtp.hunter.wms.api.CustomPrintAPI;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.PrintPayload;

import java.io.IOException;

import retrofit2.Callback;
import retrofit2.Response;

public class PrintClient extends AuthenticatedClient {

    private Context ctx;
    private CustomPrintAPI printAPI;

    public PrintClient(Context context) {
        this.ctx = context;
        createProductAPI();
    }

    private void createProductAPI() {
        printAPI = retrofit().create(CustomPrintAPI.class);
    }

    public IntegrationReturn print(PrintPayload pl) {
        try {
            Response<IntegrationReturn> resp = printAPI.printSugar(pl).execute();

            if (resp.code() == 500) {
                return new IntegrationReturn(false, "Erro interno");
            } else
                return resp.body();
        } catch (IOException e) {
            return new IntegrationReturn(false, e.getLocalizedMessage());
        }
    }

    public void print(PrintPayload pl, Callback<IntegrationReturn> calFunc) {
        printAPI.printSugar(pl).enqueue(calFunc);
    }
}