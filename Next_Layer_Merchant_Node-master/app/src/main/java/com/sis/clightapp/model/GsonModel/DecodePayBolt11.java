package com.sis.clightapp.model.GsonModel;

public class DecodePayBolt11 {


    /*
    resp,status,rpc-cmd,cli-node,[ {
   "currency": "bc",
   "created_at": 1596653872,
   "expiry": 604800,
   "payee": "02dc8590dd675b5bf89c6bdf9eeed767290b3d6056465e5b013756f65616d3d372",
   "msatoshi": 965000,
   "amount_msat": "965000msat",
   "description": "firstrefundtest",
   "min_final_cltv_expiry": 10,
   "payment_secret": "65b2f135147ee24e3c28d03fb733c406b3ab84ef6fb554beae1dc75d78e506a1",
   "features": "028200",
   "payment_hash": "8c8fb25e7a1851944f2f10974549fa0845fbd480dde33569e4382a99a2ccd59d",
   "signature": "30440220049f8ddd183ae01fb1f242459b1a0504eea05e1bcb9eab180b481ab1d61943f20220229d14b37c52443063a98e69e58e55750a5d2d3130be106913ff75558b0a6818"
}
 ]*/

    private  String currency;
    private  long created_at;
    private  long expiry;
    private  String payee;
    private  double msatoshi;
    private  String amount_msat;
    private  String description;
    private  double min_final_cltv_expiry;
    private  String payment_secret;
    private  String features;
    private  String payment_hash;
    private  String signature;


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMin_final_cltv_expiry() {
        return min_final_cltv_expiry;
    }

    public void setMin_final_cltv_expiry(double min_final_cltv_expiry) {
        this.min_final_cltv_expiry = min_final_cltv_expiry;
    }

    public String getPayment_secret() {
        return payment_secret;
    }

    public void setPayment_secret(String payment_secret) {
        this.payment_secret = payment_secret;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getPayment_hash() {
        return payment_hash;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }



}
