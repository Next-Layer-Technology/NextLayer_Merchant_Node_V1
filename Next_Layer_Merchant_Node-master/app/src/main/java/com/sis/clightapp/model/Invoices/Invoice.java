package com.sis.clightapp.model.Invoices;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Invoice {
    /* "label": "sale1632994458",
         "bolt11": "lnbc231697300p1ps4tp2ypp5kqpn97ts205p6ydzqujdj49fx8u0q5zw3srqeahk0k9j4hp4puvsdq9v3hkxxqyjw5qcqpjsp58lc4j8az9czse9v8qaxcehm4wszjtynq455hdaq99lqntxnrdzvsrzjq08y60k7eh9vhgn35x8k9pap4j7tcy3759kauml7wlgw6vft90jksz009gqqf6sqqqqqqqlgqqqqqzsqyg9qyyssqgnep9nwhafpgp8cnlydz4cnxk4fefwxkuprjq33crts33uvgq5fnf95c5a2xfgngcv28velrncy29zsx8axv2kkkwcu50efa8mkzfzqpeu0ysk",
         "payment_hash": "b00332f97053e81d11a20724d954a931f8f0504e8c060cf6f67d8b2adc350f19",
         "msatoshi": 23169730,
         "amount_msat": "23169730msat",
         "status": "unpaid",
         "description": "doc",
         "expires_at": 1633599428*/
    @SerializedName("label")
    @Expose
    String label;
    @SerializedName("bolt11")
    @Expose
    String bolt11;
    @SerializedName("payment_hash")
    @Expose
    String payment_hash;
    @SerializedName("msatoshi")
    @Expose
    String msatoshi;
    @SerializedName("amount_msat")
    @Expose
    String amount_msat;
    @SerializedName("status")
    @Expose
    String status;
    @SerializedName("description")
    @Expose
    String description;
    @SerializedName("expires_at")
    @Expose
    String expires_at;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBolt11() {
        return bolt11;
    }

    public void setBolt11(String bolt11) {
        this.bolt11 = bolt11;
    }

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public String getMsatoshi() {
        return msatoshi;
    }

    public void setMsatoshi(String msatoshi) {
        this.msatoshi = msatoshi;
    }

    public String getAmount_msat() {
        return amount_msat;
    }

    public void setAmount_msat(String amount_msat) {
        this.amount_msat = amount_msat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }
}
