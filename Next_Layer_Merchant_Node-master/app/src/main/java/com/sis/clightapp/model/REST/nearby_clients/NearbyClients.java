package com.sis.clightapp.model.REST.nearby_clients;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NearbyClients {

    @SerializedName("id")
    @Expose
    Long id;

    @SerializedName("client_id")
    @Expose
    String client_id;

    @SerializedName("client_name")
    @Expose
    String client_name;

    @SerializedName("client_image_url")
    @Expose
    String client_image_url;

    public NearbyClients(Long id, String client_id, String client_name, String client_image_url) {
        this.id = id;
        this.client_id = client_id;
        this.client_name = client_name;
        this.client_image_url = client_image_url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getClient_image_url() {
        return client_image_url;
    }

    public void setClient_image_url(String client_image_url) {
        this.client_image_url = client_image_url;
    }
}
