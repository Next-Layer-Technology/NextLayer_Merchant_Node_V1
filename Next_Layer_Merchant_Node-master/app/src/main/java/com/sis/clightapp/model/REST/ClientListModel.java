package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClientListModel {
    @SerializedName("clients")
    @Expose
    List<StoreClients> clients;

    public List<StoreClients> getStoreClientsList() {
        return clients;
    }

    public void setStoreClientsList(List<StoreClients> storeClientsList) {
        this.clients = storeClientsList;
    }
}
