package com.sis.clightapp.model;

public class Sales  {

    private  int saleId;
    private  int itemId;
    private String itemName;
    private String itemPrice;




    // Constructor that is used to create an instance of the Sale object
    public Sales(int sId,int iId,String iName,String iPrice) {


        this.saleId = sId;
        this.itemId=iId;

        this.itemName = iName;
        this.itemPrice = iPrice;

    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }




}