package com.sis.clightapp.model.GsonModel;

public class Pay {

      /*  resp,status,rpc-cmd,cli-node,[ {
                         "destination": "02dc8590dd675b5bf89c6bdf9eeed767290b3d6056465e5b013756f65616d3d372",
                        "payment_hash": "8c8fb25e7a1851944f2f10974549fa0845fbd480dde33569e4382a99a2ccd59d",
                        "created_at": 1596655318.504,
                        "parts": 1,
                        "msatoshi": 965000,
                        "amount_msat": "965000msat",
                        "msatoshi_sent": 965000,
                        "amount_sent_msat": "965000msat",
                        "payment_preimage": "881d6ee425fcb0b670191b140742364d35a4fc51a831197709756886aed8e7d7",
                        "status": "complete"
            }
 ]*/



/*{   "destination": "02dc8590dd675b5bf89c6bdf9eeed767290b3d6056465e5b013756f65616d3d372",
       "payment_hash": "8c8fb25e7a1851944f2f10974549fa0845fbd480dde33569e4382a99a2ccd59d",
          "created_at": 1596664277.619,
            "parts": 1,
           "msatoshi": 965000,
           "amount_msat": "965000msat",
           "msatoshi_sent": 965000,
            "amount_sent_msat": "965000msat",   "
            payment_preimage": "881d6ee425fcb0b670191b140742364d35a4fc51a831197709756886aed8e7d7",
            "status": "complete"} */

      private  String  destination;
      private  String payment_hash;
      private  double created_at;
      private  double parts;
      private  double msatoshi;
      private  String amount_msat;
      private  double msatoshi_sent;
      private  String amount_sent_msat;
      private  String payment_preimage;
      private  String status;
      private  double code;
      private  String message;


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public String getPayment_hash() {
        return payment_hash;
    }

    public double getCode() {
        return code;
    }

    public void setCode(double code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPayment_hash(String payment_hash) {
        this.payment_hash = payment_hash;
    }

    public double getCreated_at() {
        return created_at;
    }

    public void setCreated_at(double created_at) {
        this.created_at = created_at;
    }

    public double getParts() {
        return parts;
    }

    public void setParts(double parts) {
        this.parts = parts;
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
