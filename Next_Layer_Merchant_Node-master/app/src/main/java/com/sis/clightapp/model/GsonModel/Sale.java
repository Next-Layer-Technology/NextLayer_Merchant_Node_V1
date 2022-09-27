package com.sis.clightapp.model.GsonModel;

public class Sale {
/*
* label": "from3to4",
         "bolt11": "lnbc1u1p0ud67gpp5cyf6h7rhapnl28ca6eydethkygfvke86npwfkjs3y9adha0zkd8qdq9w3e8jxqyjw5qcqpjsp55y5ehcmlga2elnaegp8lr46nfexl9ccq60vvxd6pan84q7gfa0as9qy9qsq05gd2tylj542k9xw0tssq6s85qkp9t6tvw22nn9x029089n747s8hm0g5cgyall5c06xedz3pxk46gej2l85ehsw390gmymcwccjd8gp23mtjs",
         "payment_hash": "c113abf877e867f51f1dd648dcaef62212cb64fa985c9b4a11217adbf5e2b34e",
         "msatoshi": 100000,
         "amount_msat": "100000msat",
         "status": "expired",
         "description": "try",
         "expires_at": 1607476808*/
    private String label;
    private String bolt11;
    private String payment_hash;
    private double msatoshi;
    private String amount_msat;
    private String status;
    private double pay_index;
    private double msatoshi_received;
    private String amount_received_msat;
    private long  paid_at;
    private String payment_preimage;
    private String description;
    private long expires_at;

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

    public double getMsatoshi() {
        return msatoshi;
    }

    public void setMsatoshi(double msatoshi) {
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

    public double getPay_index() {
        return pay_index;
    }

    public void setPay_index(double pay_index) {
        this.pay_index = pay_index;
    }

    public double getMsatoshi_received() {
        return msatoshi_received;
    }

    public void setMsatoshi_received(double msatoshi_received) {
        this.msatoshi_received = msatoshi_received;
    }

    public String getAmount_received_msat() {
        return amount_received_msat;
    }

    public void setAmount_received_msat(String amount_received_msat) {
        this.amount_received_msat = amount_received_msat;
    }

    public long getPaid_at() {
        return paid_at;
    }

    public void setPaid_at(long paid_at) {
        this.paid_at = paid_at;
    }

    public String getPayment_preimage() {
        return payment_preimage;
    }

    public void setPayment_preimage(String payment_preimage) {
        this.payment_preimage = payment_preimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(long expires_at) {
        this.expires_at = expires_at;
    }
}
