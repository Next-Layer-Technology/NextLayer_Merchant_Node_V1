package com.sis.clightapp.model.Invoices;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.GsonModel.Invoice;

import java.util.ArrayList;

public class InvoicesResponse {
    @SerializedName("invoices")
    @Expose
    ArrayList<Invoice> invoiceArrayList;

    public ArrayList<Invoice> getInvoiceArrayList() {
        return invoiceArrayList;
    }

    public void setInvoiceArrayList(ArrayList<Invoice> invoiceArrayList) {
        this.invoiceArrayList = invoiceArrayList;
    }
}
