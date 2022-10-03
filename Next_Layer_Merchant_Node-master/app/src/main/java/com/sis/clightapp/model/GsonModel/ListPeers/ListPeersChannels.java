package com.sis.clightapp.model.GsonModel.ListPeers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListPeersChannels {
    @SerializedName("channel_id")
    @Expose
    public String channel_id;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("short_channel_id")
    @Expose
    public String short_channel_id;
    @SerializedName("spendable_msat")
    @Expose
    public String spendable_msat;
    @SerializedName("max_to_us_msat")
    @Expose
    public String max_to_us_msat;
    @SerializedName("funding_txid")
    @Expose
    public String funding_txid;

    @SerializedName("receivable_msatoshi")
    @Expose
    public long receivable_msatoshi;


    @SerializedName("spendable_msatoshi")
    public long spendable_msatoshi;

    public long getReceivable_msatoshi() {
        return receivable_msatoshi;
    }

    public void setReceivable_msatoshi(long receivable_msatoshi) {
        this.receivable_msatoshi = receivable_msatoshi;
    }

    public long getSpendable_msatoshi() {
        return spendable_msatoshi;
    }

    public void setSpendable_msatoshi(long spendable_msatoshi) {
        this.spendable_msatoshi = spendable_msatoshi;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
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

    public String getSpendable_msat() {
        return spendable_msat;
    }

    public void setSpendable_msat(String spendable_msat) {
        this.spendable_msat = spendable_msat;
    }

    public String getMax_to_us_msat() {
        return max_to_us_msat;
    }

    public void setMax_to_us_msat(String max_to_us_msat) {
        this.max_to_us_msat = max_to_us_msat;
    }

    public String getFunding_txid() {
        return funding_txid;
    }

    public void setFunding_txid(String funding_txid) {
        this.funding_txid = funding_txid;
    }
}
