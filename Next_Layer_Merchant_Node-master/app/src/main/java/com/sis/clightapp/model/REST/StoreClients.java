package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StoreClients {
    /*  "client_id": "NC1619783170551219",
        "client_name": "KhuwajaHassan",
        "client_image_id": "khuwaja Hassan.png"*/
    @SerializedName("client_id")
    @Expose
    String client_id;
    @SerializedName("client_name")
    @Expose
    String client_name;
    @SerializedName("client_image_id")
    @Expose
    String client_image_id;

    public StoreClients(String client_id, String client_name, String client_image_id) {
        this.client_id = client_id;
        this.client_name = client_name;
        this.client_image_id = client_image_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_image_id() {
        return client_image_id;
    }

    public void setClient_image_id(String client_image_id) {
        this.client_image_id = client_image_id;
    }
}
