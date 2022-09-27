package com.sis.clightapp.model.GsonModel.ItemsMerchant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddItemsModel {
    @SerializedName("success")
    @Expose
    boolean success;
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    ItemLIstModel list;

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

    public ItemLIstModel getList() {
        return list;
    }

    public void setList(ItemLIstModel list) {
        this.list = list;
    }
}
