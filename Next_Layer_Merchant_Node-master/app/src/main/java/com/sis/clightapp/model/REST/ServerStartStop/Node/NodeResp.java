package com.sis.clightapp.model.REST.ServerStartStop.Node;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NodeResp {
    @SerializedName("code")
    @Expose
    int code;

    @SerializedName("message")
    @Expose
    String message;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
