package com.sis.clightapp.model.GsonModel.ListFunds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ListFunds {

    @SerializedName("outputs")
    @Expose
    public ArrayList<ListFundOutput> outputs;

    @SerializedName("channels")
    @Expose
    public ArrayList<ListFundChannel> channels;

    public ArrayList<ListFundOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<ListFundOutput> outputs) {
        this.outputs = outputs;
    }

    public ArrayList<ListFundChannel> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<ListFundChannel> channels) {
        this.channels = channels;
    }
}
