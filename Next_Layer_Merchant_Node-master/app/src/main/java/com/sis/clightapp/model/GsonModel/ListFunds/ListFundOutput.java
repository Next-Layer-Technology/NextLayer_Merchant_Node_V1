package com.sis.clightapp.model.GsonModel.ListFunds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ListFundOutput {
    @SerializedName("txid")
    @Expose
    public String txid;
    @SerializedName("output")
    @Expose
    public int output;
    @SerializedName("value")
    @Expose
    public int value;
    @SerializedName("amount_msat")
    @Expose
    public String amount_msat;
    @SerializedName("scriptpubkey")
    @Expose
    public String scriptpubkey;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("blockheight")
    @Expose
    public int blockheight;
    @SerializedName("reserved")
    @Expose
    public boolean reserved;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getAmount_msat() {
        return amount_msat;
    }

    public void setAmount_msat(String amount_msat) {
        this.amount_msat = amount_msat;
    }

    public String getScriptpubkey() {
        return scriptpubkey;
    }

    public void setScriptpubkey(String scriptpubkey) {
        this.scriptpubkey = scriptpubkey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBlockheight() {
        return blockheight;
    }

    public void setBlockheight(int blockheight) {
        this.blockheight = blockheight;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }


}
