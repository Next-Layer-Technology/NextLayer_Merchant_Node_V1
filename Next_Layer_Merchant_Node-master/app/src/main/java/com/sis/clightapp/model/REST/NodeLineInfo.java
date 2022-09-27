package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class NodeLineInfo {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("connected")
    @Expose
    private boolean connected;
    @SerializedName("netaddr")
    @Expose
    private List<String> netaddr = null;
    @SerializedName("features")
    @Expose
    private String features;

    @SerializedName("channels")
    @Expose
    ArrayList< Channels > channels = new ArrayList< Channels >();

    public List<String> getNetaddr() {
        return netaddr;
    }

    public void setNetaddr(List<String> netaddr) {
        this.netaddr = netaddr;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }


    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    private boolean isOn;

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

    public ArrayList<Channels> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Channels> channels) {
        this.channels = channels;
    }
}
