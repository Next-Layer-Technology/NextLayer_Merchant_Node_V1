package com.sis.clightapp.model.GsonModel;

public class InvoiceForPrint {
    private  double msatoshi;
    private  long  paid_at;
    private  String purchasedItems;
    private  String tax;
    private  String taxInBtc;
    private  String taxInUsd;
    private  String payment_preimage;
    private String desscription;
    private String mode;
    private  double  created_at;
    private  String  destination;
    private  String payment_hash;

    public String getTaxInBtc() {
        return taxInBtc;
    }

    public void setTaxInBtc(String taxInBtc) {
        this.taxInBtc = taxInBtc;
    }

    public String getTaxInUsd() {
        return taxInUsd;
    }

    public void setTaxInUsd(String taxInUsd) {
        this.taxInUsd = taxInUsd;
    }

    public double getCreated_at() {
        return created_at;
    }

    public void setCreated_at(double created_at) {
        this.created_at = created_at;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDesscription() {
        return desscription;
    }

    public void setDesscription(String desscription) {
        this.desscription = desscription;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }






    public double getMsatoshi() {
        return msatoshi;
    }

    public void setMsatoshi(double msatoshi) {
        this.msatoshi = msatoshi;
    }

    public String getPayment_preimage() {
        return payment_preimage;
    }

    public void setPayment_preimage(String payment_preimage) {
        this.payment_preimage = payment_preimage;
    }

    public long getPaid_at() {
        return paid_at;
    }

    public void setPaid_at(long paid_at) {
        this.paid_at = paid_at;
    }

    public String getPurchasedItems() {
        return purchasedItems;
    }

    public void setPurchasedItems(String purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }
}
