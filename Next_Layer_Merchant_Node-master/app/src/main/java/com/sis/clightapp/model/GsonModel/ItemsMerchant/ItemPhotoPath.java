package com.sis.clightapp.model.GsonModel.ItemsMerchant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ItemPhotoPath {
    @SerializedName("success")
    @Expose
    boolean success;
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    String data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
