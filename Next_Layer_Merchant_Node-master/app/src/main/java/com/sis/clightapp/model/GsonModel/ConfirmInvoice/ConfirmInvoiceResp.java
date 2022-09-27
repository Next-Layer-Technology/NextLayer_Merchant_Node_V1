package com.sis.clightapp.model.GsonModel.ConfirmInvoice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.GsonModel.Invoice;

import java.util.ArrayList;

public class ConfirmInvoiceResp {

    @SerializedName("invoices")
    @Expose
    ArrayList<Invoice> invoiceArrayList=null;

    public ArrayList<Invoice> getInvoiceArrayList() {
        return invoiceArrayList;
    }

    public void setInvoiceArrayList(ArrayList<Invoice> invoiceArrayList) {
        this.invoiceArrayList = invoiceArrayList;
    }
}
