package com.sis.clightapp.model;

public class Refunds {


    private  int refundId;
    private  int saleId;
    private int itemid;
    private String itemName;
    private String refundPrice;
    // Constructor that is used to create an instance of the Refund object

    public Refunds(int rId,int sId,int iId,String iName,String rPrice) {

        this.refundId = rId;
        this.saleId=sId;
        this.itemid=iId;
        this.itemName=iName;
        this.refundPrice=rPrice;
    }


    public int getRefundId() {
        return refundId;
    }

    public void setRefundId(int refundId) {
        this.refundId = refundId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getRefundPrice() {
        return refundPrice;
    }

    public void setRefundPrice(String refundPrice) {
        this.refundPrice = refundPrice;
    }
}