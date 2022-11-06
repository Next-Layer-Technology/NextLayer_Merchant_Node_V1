package com.sis.clightapp.model.WebsocketResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MWSWebSocketResponse {

    int code;
    String message;
    String token;
    boolean error = false;
    private String payment_hash;
    private long expires_at;
    private String bolt11;
    private String payment_secret;
    private String warning_capacity;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public long getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(long expires_at) {
        this.expires_at = expires_at;
    }

    public String getBolt11() {
        return bolt11;
    }

    public void setBolt11(String bolt11) {
        this.bolt11 = bolt11;
    }

    public String getPayment_secret() {
        return payment_secret;
    }

    public void setPayment_secret(String payment_secret) {
        this.payment_secret = payment_secret;
    }

    public String getWarning_capacity() {
        return warning_capacity;
    }

    public void setWarning_capacity(String warning_capacity) {
        this.warning_capacity = warning_capacity;
    }
}

