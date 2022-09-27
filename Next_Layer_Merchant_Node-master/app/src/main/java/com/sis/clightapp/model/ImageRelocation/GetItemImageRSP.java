package com.sis.clightapp.model.ImageRelocation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetItemImageRSP {
    @SerializedName("status")
    @Expose
    String status;
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    ArrayList<GetItemImageReloc> itemImageRelocArrayList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<GetItemImageReloc> getItemImageRelocArrayList() {
        return itemImageRelocArrayList;
    }

    public void setItemImageRelocArrayList(ArrayList<GetItemImageReloc> itemImageRelocArrayList) {
        this.itemImageRelocArrayList = itemImageRelocArrayList;
    }
}
