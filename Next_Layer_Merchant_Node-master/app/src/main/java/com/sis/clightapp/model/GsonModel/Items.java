package com.sis.clightapp.model.GsonModel;

public class Items {

    // Store the name of the item
    private String ID;
    // Store the uc code of item
    private String UPC;

    private  String Name;
    //Store the quatity of item
    private String Quantity;
    // Store the Price of item
    private String Price;
    //Store the Extra info of items
    private String AdditionalInfo;

    private  int selectQuatity;

    private double totalPrice;
    private  String imageInHex;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageInHex() {
        return imageInHex;
    }

    public void setImageInHex(String imageInHex) {
        this.imageInHex = imageInHex;
    }



    public String getIsManual() {
        return isManual;
    }

    public void setIsManual(String isManual) {
        this.isManual = isManual;
    }

    private String isManual;


    public String getUPC() {
        return UPC;
    }

    public void setUPC(String UPC) {
        this.UPC = UPC;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }



    public int getSelectQuatity() {
        return selectQuatity;
    }

    public void setSelectQuatity(int selectQuatity) {
        this.selectQuatity = selectQuatity;
    }



    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getAdditionalInfo() {
        return AdditionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        AdditionalInfo = additionalInfo;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if(obj instanceof Items)
        {
            Items temp = (Items) obj;
            if(this.getUPC() == temp.getUPC())
                return true;
        }
        return false;

    }
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub

        return (this.getUPC().hashCode());
    }
}
