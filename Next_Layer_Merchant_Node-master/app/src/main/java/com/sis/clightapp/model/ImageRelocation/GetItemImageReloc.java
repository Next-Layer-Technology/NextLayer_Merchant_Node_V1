package com.sis.clightapp.model.ImageRelocation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetItemImageReloc {
    /*{
             "id": 13,
            "merchant_id": 16,
            "upc_number": "12345",
            "image": "16item1615892062.jpeg",
            "updated_at": "2021-03-16 10:54:22",
            "created_at": "2021-09-15 02:28:00",
            "name": null,
            "quantity": null,
            "price": null,
            "additional_info": null,
            "select_quatity": null,
            "total_price": null,
            "image_in_hex": null
        }*/
    @SerializedName("id")
    @Expose
    int itemId;
    @SerializedName("merchant_id")
    @Expose
    int merchant_id;
    @SerializedName("upc_number")
    @Expose
    String upc_number;
    @SerializedName("image")
    @Expose
    String image;
    @SerializedName("name")
    @Expose
    String name;
    @SerializedName("quantity")
    @Expose
    String quantity;
    @SerializedName("price")
    @Expose
    String price;
    @SerializedName("additional_info")
    @Expose
    String additional_info;
    @SerializedName("select_quatity")
    @Expose
    String select_quatity;
    @SerializedName("total_price")
    @Expose
    double total_price;
    @SerializedName("image_in_hex")
    @Expose
    String image_in_hex;
    @SerializedName("updated_at")
    @Expose
    String updated_at;
    @SerializedName("created_at")
    @Expose
    String created_at;

    public GetItemImageReloc(int itemId, int merchant_id, String upc_number, String image, String name, String quantity, String price, String additional_info, String select_quatity, double total_price, String image_in_hex, String updated_at, String created_at) {
        this.itemId = itemId;
        this.merchant_id = merchant_id;
        this.upc_number = upc_number;
        this.image = image;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.additional_info = additional_info;
        this.select_quatity = select_quatity;
        this.total_price = total_price;
        this.image_in_hex = image_in_hex;
        this.updated_at = updated_at;
        this.created_at = created_at;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAdditional_info() {
        return additional_info;
    }

    public void setAdditional_info(String additional_info) {
        this.additional_info = additional_info;
    }

    public String getSelect_quatity() {
        return select_quatity;
    }

    public void setSelect_quatity(String select_quatity) {
        this.select_quatity = select_quatity;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    public String getImage_in_hex() {
        return image_in_hex;
    }

    public void setImage_in_hex(String image_in_hex) {
        this.image_in_hex = image_in_hex;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(int merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getUpc_number() {
        return upc_number;
    }

    public void setUpc_number(String upc_number) {
        this.upc_number = upc_number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
