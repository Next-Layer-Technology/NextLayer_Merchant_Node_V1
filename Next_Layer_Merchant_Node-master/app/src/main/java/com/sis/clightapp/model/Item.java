package com.sis.clightapp.model;

public class Item {




    // Store the name of the item
    private String mName;
    // Store the release date of the movie
    private String mDescription;
    //Store the quatity of item
    private String mQuantity;
    // Store the Price of item
    private String mPrice;
    //Store the Extra info of items
    private String mExtra;


    // Constructor that is used to create an instance of the Movie object
    public Item(String mName1, String mExtra1,String mPrice1) {

        this.mName = mName1;
        this.mExtra = mExtra1;
        this.mPrice=mPrice1;
    }


    public String getmQuantity() {
        return mQuantity;
    }

    public void setmQuantity(String mQuantity) {
        this.mQuantity = mQuantity;
    }

    public String getmExtra() {
        return mExtra;
    }

    public void setmExtra(String mExtra) {
        this.mExtra = mExtra;
    }

    public String getmPrice() {
        return mPrice;
    }

    public void setmPrice(String mPrice) {
        this.mPrice = mPrice;
    }



    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
