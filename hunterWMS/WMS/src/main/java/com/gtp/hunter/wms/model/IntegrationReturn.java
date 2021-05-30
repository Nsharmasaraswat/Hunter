package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class IntegrationReturn extends BaseModel implements Serializable {

    public static transient final IntegrationReturn OK = new IntegrationReturn(true, "");

    @Expose
    @SerializedName("result")
    private Boolean result;

    @Expose
    @SerializedName("message")
    private String message;

    public IntegrationReturn(boolean res, String msg){
        setResult(res);
        setMessage(msg);
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
