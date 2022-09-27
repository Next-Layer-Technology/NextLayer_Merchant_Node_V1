package com.sis.clightapp.model.GsonModel;

public class Refund {

    /*"id": 1,
         "payment_hash": "b9da402bfcfdb6e2a07d7f463068f94b40d60875d87a81a79ce4ed50e480ae72",
         "destination": "03ce4d3edecdcacba271a18f6287a1acbcbc123ea16dde6ffe77d0ed312b2be568",
         "msatoshi": 500000,
         "amount_msat": "500000msat",
         "msatoshi_sent": 500000,
         "amount_sent_msat": "500000msat",
         "created_at": 1606879136,
         "status": "complete",
         "payment_preimage": "c5091e5efe2f5d82d004168c0636a47eb9a9d84b56bbdfe0ee3304262eb3584e",
         "bolt11": "lnbc5u1p0udemfpp5h8dyq2lulkmw9gra0arrq68efdqdvzr4mpagrfuuunk4peyq4eeqdq9w3e8jxqyjw5qcqpjsp567m87msjl7hfpz3ehf8eq3l43glr9jnxk7dgylj8qr5a0hnkx3ws9qy9qsqlm5zxknk2znadrctz57wn00w205s4zt4fmm4hv4kmmafppt5m7x9jvfar3e9wvgsv222pvkzh4924kalz9pe33t9wprxvk499va5eksq5hfqza"*/
    private  int id;
    private  String payment_hash;
    private  String destination;
    private  double msatoshi;
    private  String amount_msat;
    private  double msatoshi_sent;
    private  String amount_sent_msat;
    private  long created_at;
    private  String status;
    private  String payment_preimage;
    private  String bolt11;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public double getMsatoshi_sent() {
        return msatoshi_sent;
    }

    public void setMsatoshi_sent(double msatoshi_sent) {
        this.msatoshi_sent = msatoshi_sent;
    }

    public String getAmount_sent_msat() {
        return amount_sent_msat;
    }

    public void setAmount_sent_msat(String amount_sent_msat) {
        this.amount_sent_msat = amount_sent_msat;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayment_preimage() {
        return payment_preimage;
    }

    public void setPayment_preimage(String payment_preimage) {
        this.payment_preimage = payment_preimage;
    }

    public String getBolt11() {
        return bolt11;
    }

    public void setBolt11(String bolt11) {
        this.bolt11 = bolt11;
    }



}
