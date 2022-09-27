package com.sis.clightapp.model.Invoices;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.GsonModel.Sale;
import com.sis.clightapp.model.Sales;

import java.util.ArrayList;

public class InvoicesResponse {
    @SerializedName("invoices")
    @Expose
    ArrayList<Sale> invoiceArrayList;

    public ArrayList<Sale> getInvoiceArrayList() {
        return invoiceArrayList;
    }

    public void setInvoiceArrayList(ArrayList<Sale> invoiceArrayList) {
        this.invoiceArrayList = invoiceArrayList;
    }
}
