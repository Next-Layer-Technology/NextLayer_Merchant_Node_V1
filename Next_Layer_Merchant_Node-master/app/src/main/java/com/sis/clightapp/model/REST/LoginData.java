package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginData {

    /*
    *   "message": "successfully done",
    "data": {
        "id": 6,
        "user_type": "Checkout",
        "sign_in_username": "checkoutonoff",
        "sign_in_password": "111111",
        "merchant_data_id": 16
    }*/

    @SerializedName("id")
    @Expose
    int id;
    @SerializedName("user_type")
    @Expose
    String user_type;
    @SerializedName("sign_in_username")
    @Expose
    String sign_in_username;
    @SerializedName("sign_in_password")
    @Expose
    String sign_in_password;
    @SerializedName("merchant_data_id")
    @Expose
    String merchant_data_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getSign_in_username() {
        return sign_in_username;
    }

    public void setSign_in_username(String sign_in_username) {
        this.sign_in_username = sign_in_username;
    }

    public String getSign_in_password() {
        return sign_in_password;
    }

    public void setSign_in_password(String sign_in_password) {
        this.sign_in_password = sign_in_password;
    }

    public String getMerchant_data_id() {
        return merchant_data_id;
    }

    public void setMerchant_data_id(String merchant_data_id) {
        this.merchant_data_id = merchant_data_id;
    }
}
