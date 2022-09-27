package com.sis.clightapp.model.GsonModel.ListPeers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFundChannel;

import java.util.ArrayList;

public class ListPeers {
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("connected")
    @Expose
    public boolean connected;

    @SerializedName("channels")
    @Expose
    public ArrayList<ListPeersChannels> channels;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public ArrayList<ListPeersChannels> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<ListPeersChannels> channels) {
        this.channels = channels;
    }
}
