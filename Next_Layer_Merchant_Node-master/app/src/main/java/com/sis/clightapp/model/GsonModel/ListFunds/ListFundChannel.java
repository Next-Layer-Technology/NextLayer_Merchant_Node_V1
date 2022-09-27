package com.sis.clightapp.model.GsonModel.ListFunds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListFundChannel {

    @SerializedName("peer_id")
    @Expose
    public String peer_id;
    @SerializedName("connected")
    @Expose
    public boolean connected;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("short_channel_id")
    @Expose
    public String short_channel_id;
    @SerializedName("channel_sat")
    @Expose
    public int channel_sat;
    @SerializedName("our_amount_msat")
    @Expose
    public String our_amount_msat;
    @SerializedName("channel_total_sat")
    @Expose
    public int channel_total_sat;
    @SerializedName("amount_msat")
    @Expose
    public String amount_msat;
    @SerializedName("funding_txid")
    @Expose
    public String funding_txid;
    @SerializedName("funding_output")
    @Expose
    public int funding_output;

    public String getPeer_id() {
        return peer_id;
    }

    public void setPeer_id(String peer_id) {
        this.peer_id = peer_id;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getShort_channel_id() {
        return short_channel_id;
    }

    public void setShort_channel_id(String short_channel_id) {
        this.short_channel_id = short_channel_id;
    }

    public int getChannel_sat() {
        return channel_sat;
    }

    public void setChannel_sat(int channel_sat) {
        this.channel_sat = channel_sat;
    }

    public String getOur_amount_msat() {
        return our_amount_msat;
    }

    public void setOur_amount_msat(String our_amount_msat) {
        this.our_amount_msat = our_amount_msat;
    }

    public int getChannel_total_sat() {
        return channel_total_sat;
    }

    public void setChannel_total_sat(int channel_total_sat) {
        this.channel_total_sat = channel_total_sat;
    }

    public String getAmount_msat() {
        return amount_msat;
    }

    public void setAmount_msat(String amount_msat) {
        this.amount_msat = amount_msat;
    }

    public String getFunding_txid() {
        return funding_txid;
    }

    public void setFunding_txid(String funding_txid) {
        this.funding_txid = funding_txid;
    }

    public int getFunding_output() {
        return funding_output;
    }

    public void setFunding_output(int funding_output) {
        this.funding_output = funding_output;
    }


}
