package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionInfo {


    @SerializedName("transaction_label")
    @Expose
    String transaction_label;
    @SerializedName("payment_hash")
    @Expose
    String payment_hash;
    @SerializedName("transaction_amountBTC")
    @Expose
    String transaction_amountBTC;
    @SerializedName("transaction_amountUSD")
    @Expose
    String transaction_amountUSD;
    @SerializedName("payment_preimage")
    @Expose
    String payment_preimage;
    @SerializedName("status")
    @Expose
    String status;
    @SerializedName("destination")
    @Expose
    String destination;
    @SerializedName("transaction_timestamp")
    @Expose
    String transaction_timestamp;
    @SerializedName("msatoshi")
    @Expose
    String msatoshi;
    @SerializedName("conversion_rate")
    @Expose
    String conversion_rate;
    @SerializedName("updated_at")
    @Expose
    String updated_at;
    @SerializedName("created_at")
    @Expose
    String created_at;
    @SerializedName("id")
    @Expose
    int id;

    public String getTransaction_label() {
        return transaction_label;
    }

    public void setTransaction_label(String transaction_label) {
        this.transaction_label = transaction_label;
    }

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public String getTransaction_amountBTC() {
        return transaction_amountBTC;
    }

    public void setTransaction_amountBTC(String transaction_amountBTC) {
        this.transaction_amountBTC = transaction_amountBTC;
    }

    public String getTransaction_amountUSD() {
        return transaction_amountUSD;
    }

    public void setTransaction_amountUSD(String transaction_amountUSD) {
        this.transaction_amountUSD = transaction_amountUSD;
    }

    public String getPayment_preimage() {
        return payment_preimage;
    }

    public void setPayment_preimage(String payment_preimage) {
        this.payment_preimage = payment_preimage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTransaction_timestamp() {
        return transaction_timestamp;
    }

    public void setTransaction_timestamp(String transaction_timestamp) {
        this.transaction_timestamp = transaction_timestamp;
    }

    public String getMsatoshi() {
        return msatoshi;
    }

    public void setMsatoshi(String msatoshi) {
        this.msatoshi = msatoshi;
    }

    public String getConversion_rate() {
        return conversion_rate;
    }

    public void setConversion_rate(String conversion_rate) {
        this.conversion_rate = conversion_rate;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
