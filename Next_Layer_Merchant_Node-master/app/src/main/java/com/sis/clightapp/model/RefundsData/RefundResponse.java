package com.sis.clightapp.model.RefundsData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sis.clightapp.model.GsonModel.Refund;

import java.util.ArrayList;

public class RefundResponse {
    @SerializedName("payments")
    @Expose
    ArrayList<Refund> refundArrayList;

    public ArrayList<Refund> getRefundArrayList() {
        return refundArrayList;
    }

    public void setRefundArrayList(ArrayList<Refund> invoiceArrayList) {
        this.refundArrayList = invoiceArrayList;
    }
}
