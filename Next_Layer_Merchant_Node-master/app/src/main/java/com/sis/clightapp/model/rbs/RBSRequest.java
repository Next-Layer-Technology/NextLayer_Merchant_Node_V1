package com.sis.clightapp.model.rbs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.REST.nearby_clients.NearbyClients;

import java.util.List;

public class RBSRequest {

    @SerializedName("to")
    @Expose
    String to;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("payload")
    @Expose
    Payload payload;

    public RBSRequest(String to, String type, Payload payload) {
        this.to = to;
        this.type = type;
        this.payload = payload;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
