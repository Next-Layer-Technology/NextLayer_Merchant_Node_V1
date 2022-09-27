package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Loginresponse {
    /*
   *   "message": "successfully done",
   "data": {
       "id": 6,
       "user_type": "Checkout",
       "sign_in_username": "checkoutonoff",
       "sign_in_password": "111111",
       "merchant_data_id": 16
   }*/
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    LoginData loginData;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }
}
