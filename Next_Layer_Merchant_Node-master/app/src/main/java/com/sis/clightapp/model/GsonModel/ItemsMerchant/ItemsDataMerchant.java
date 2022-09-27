package com.sis.clightapp.model.GsonModel.ItemsMerchant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;

import java.util.List;

public class ItemsDataMerchant {
    @SerializedName("success")
    @Expose
    boolean success;
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    List<ItemLIstModel> list;

    public ItemsDataMerchant(boolean success, String message, List<ItemLIstModel> list) {
        this.success = success;
        this.message = message;
        this.list = list;
    }

    public boolean getSuccess() {
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

    public List<ItemLIstModel> getList() {
        return list;
    }

    public void setList(List<ItemLIstModel> list) {
        this.list = list;
    }
}
