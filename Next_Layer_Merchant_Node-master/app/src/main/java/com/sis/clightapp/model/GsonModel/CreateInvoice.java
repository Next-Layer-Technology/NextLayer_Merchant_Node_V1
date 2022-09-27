package com.sis.clightapp.model.GsonModel;

public class CreateInvoice {

        /*       resp,status,rpc-cmd,cli-node,[ {
                "payment_hash": "87d034804ee622234aee69914df7cac3f5d9eeaf256ea69f0fb45151363dacf4",
                        "expires_at": 1595368227,
                        "bolt11": "lnbc11200u1p0su29rpp5slgrfqzwuc3zxjhwdxg5ma72c06anm40y4h2d8c0k3g4zd3a4n6qdq92368gxqyjw5qcqp2sp5xj3jelx4alya3r4uahfy2839y4hussj30qzkl9lwrlhdg98tttsq9qy9qsq95dsrlq5399ten09juc5tz4cd8qk855fvv7wpsup93zkwtrnn9p4kf6q3hdhhqhfze6r37qdklsy7acd5pue0q883mtuyp9r4dhdvfsquudxqj",
                        "warning_deadends": "No channel with a peer that is not a dead end"
            }
 ]*/

    private String payment_hash;
    private long expires_at;
    private String bolt11;
    private String payment_secret;
    //    private String warning_deadends;
    private String warning_capacity;

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public long getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(long expires_at) {
        this.expires_at = expires_at;
    }

    public String getBolt11() {
        return bolt11;
    }

    public void setBolt11(String bolt11) {
        this.bolt11 = bolt11;
    }

//    public String getWarning_deadends() {
//        return warning_deadends;
//    }
//
//    public void setWarning_deadends(String warning_deadends) {
//        this.warning_deadends = warning_deadends;
//    }

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
