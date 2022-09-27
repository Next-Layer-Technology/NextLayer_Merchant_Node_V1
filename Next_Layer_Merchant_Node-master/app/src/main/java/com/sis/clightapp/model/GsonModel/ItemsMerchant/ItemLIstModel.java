package com.sis.clightapp.model.GsonModel.ItemsMerchant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ItemLIstModel {
    @SerializedName("id")
    @Expose
    String id;
    @SerializedName("upc")
    @Expose
    String upc_code;
    @SerializedName("name")
    @Expose
    String name;
    @SerializedName("price")
    @Expose
    String unit_price;
    @SerializedName("quantity")
    @Expose
    String quantity_left;
    @SerializedName("img_path")
    @Expose
    String image_path;

    public ItemLIstModel(String id, String upc_code, String name, String unit_price, String quantity_left, String image_path) {
        this.id = id;
        this.upc_code = upc_code;
        this.name = name;
        this.unit_price = unit_price;
        this.quantity_left = quantity_left;
        this.image_path = image_path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpc_code() {
        return upc_code;
    }

    public void setUpc_code(String upc_code) {
        this.upc_code = upc_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(String unit_price) {
        this.unit_price = unit_price;
    }

    public String getQuantity_left() {
        return quantity_left;
    }

    public void setQuantity_left(String quantity_left) {
        this.quantity_left = quantity_left;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
}
