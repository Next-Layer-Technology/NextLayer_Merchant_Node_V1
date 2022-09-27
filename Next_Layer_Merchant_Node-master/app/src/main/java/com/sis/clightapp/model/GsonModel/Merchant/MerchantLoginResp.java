package com.sis.clightapp.model.GsonModel.Merchant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MerchantLoginResp {
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    MerchantData merchantData;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MerchantData getMerchantData() {
        return merchantData;
    }

    public void setMerchantData(MerchantData merchantData) {
        this.merchantData = merchantData;
    }
}
