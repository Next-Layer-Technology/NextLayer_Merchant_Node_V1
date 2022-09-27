package com.sis.clightapp.model.GsonModel;

public class Sendreceiveableresponse {
    /* "destination": "03ce4d3edecdcacba271a18f6287a1acbcbc123ea16dde6ffe77d0ed312b2be568",
   "payment_hash": "a09763af361082e1f5e9b08d27499ece8aacf1d79cb8aba5ee4b94bc3672cc4e",
   "created_at": 1634055943.329,
   "parts": 1,
   "msatoshi": 73530,
   "amount_msat": "73530msat",
   "msatoshi_sent": 73530,
   "amount_sent_msat": "73530msat",
   "payment_preimage": "b87249b4afcebed9c8324a4025c9737e0aaf4580e3ce16ce023878e46ae0951c",
   "status": "complete"*/

    String destination;
    String payment_hash;
    Double created_at;
    int parts;
    Double msatoshi;
    String amount_msat;
    Double msatoshi_sent;
    String amount_sent_msat;
    String payment_preimage;
    String status;

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

    public Double getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Double created_at) {
        this.created_at = created_at;
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public Double getMsatoshi() {
        return msatoshi;
    }

    public void setMsatoshi(Double msatoshi) {
        this.msatoshi = msatoshi;
    }

    public String getAmount_msat() {
        return amount_msat;
    }

    public void setAmount_msat(String amount_msat) {
        this.amount_msat = amount_msat;
    }

    public Double getMsatoshi_sent() {
        return msatoshi_sent;
    }

    public void setMsatoshi_sent(Double msatoshi_sent) {
        this.msatoshi_sent = msatoshi_sent;
    }

    public String getAmount_sent_msat() {
        return amount_sent_msat;
    }

    public void setAmount_sent_msat(String amount_sent_msat) {
        this.amount_sent_msat = amount_sent_msat;
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
}
