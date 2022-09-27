package com.sis.clightapp.model.WebsocketResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebSocketResponse {

    @SerializedName("code")
    @Expose
    int code;
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("OTP")
    @Expose
    String token;

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
    //    @SerializedName("code")
//    @Expose
//    int code;
//    @SerializedName("message")
//    @Expose
//    String message;
//    @SerializedName("OTP")
//    @Expose
//    String token;
//
//    public int getCode() {
//        return code;
//    }
//
//    public void setCode(int code) {
//        this.code = code;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
}

