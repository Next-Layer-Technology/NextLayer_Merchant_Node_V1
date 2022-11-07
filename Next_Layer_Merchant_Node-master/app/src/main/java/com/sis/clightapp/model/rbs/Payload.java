package com.sis.clightapp.model.rbs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Payload {
    @SerializedName("invoice")
    @Expose
    String invoice;

    public Payload(String invoice) {
        this.invoice = invoice;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }
}
