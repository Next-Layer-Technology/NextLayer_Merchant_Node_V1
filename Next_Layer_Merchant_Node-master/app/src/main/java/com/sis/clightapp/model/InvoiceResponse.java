package com.sis.clightapp.model;

public class InvoiceResponse {

    /*{
   "payment_hash": "b00332f97053e81d11a20724d954a931f8f0504e8c060cf6f67d8b2adc350f19",
   "expires_at": 1633599428,
   "bolt11": "lnbc231697300p1ps4tp2ypp5kqpn97ts205p6ydzqujdj49fx8u0q5zw3srqeahk0k9j4hp4puvsdq9v3hkxxqyjw5qcqpjsp58lc4j8az9czse9v8qaxcehm4wszjtynq455hdaq99lqntxnrdzvsrzjq08y60k7eh9vhgn35x8k9pap4j7tcy3759kauml7wlgw6vft90jksz009gqqf6sqqqqqqqlgqqqqqzsqyg9qyyssqgnep9nwhafpgp8cnlydz4cnxk4fefwxkuprjq33crts33uvgq5fnf95c5a2xfgngcv28velrncy29zsx8axv2kkkwcu50efa8mkzfzqpeu0ysk",
   "payment_secret": "3ff1591fa22e050c9587074d8cdf757405259260ad2976f4052fc1359a636899",
   "warning_capacity": "Insufficient incoming channel capacity to pay invoice"
}*/

    String payment_hash;
    int expires_at;
    String bolt11;
    String payment_secret;
    String warning_capacity;

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public int getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(int expires_at) {
        this.expires_at = expires_at;
    }

    public String getBolt11() {
        return bolt11;
    }

    public void setBolt11(String bolt11) {
        this.bolt11 = bolt11;
    }

    public String getPayment_secret() {
        return payment_secret;
    }

    public void setPayment_secret(String payment_secret) {
        this.payment_secret = payment_secret;
    }

    public String getWarning_capacity() {
        return warning_capacity;
    }

    public void setWarning_capacity(String warning_capacity) {
        this.warning_capacity = warning_capacity;
    }
}
