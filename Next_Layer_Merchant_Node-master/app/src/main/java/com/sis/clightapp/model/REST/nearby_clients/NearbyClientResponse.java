package com.sis.clightapp.model.REST.nearby_clients;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NearbyClientResponse {

    @SerializedName("message")
    @Expose
    String message;

    @SerializedName("data")
    @Expose
    List<NearbyClients> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NearbyClients> getData() {
        return data;
    }

    public void setData(List<NearbyClients> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ClassPojo [data = " + data + ", message = " + message + "]";
    }
}
