package com.sis.clightapp.model.GsonModel.Merchant;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;

public class MerchantData {
    @SerializedName("id")
    @Expose
    int id;

    @SerializedName("merchant_data_id")
    public String merchant_data_id;

    @SerializedName("merchant_id")
    public String merchant_id;

    @SerializedName("merchant_name")
    @Expose
    String merchant_name;
    @SerializedName("email")
    @Expose
    String email;
    @SerializedName("tax_rate")
    @Expose
    String tax_rate;

    @SerializedName("admin_administrator_password")
    @Expose
    String admin_administrator_password;


    @SerializedName("token_expiry_time")
    @Expose
    int token_expiry_time;

    @SerializedName("ssh_ip_port")
    @Expose
    String ssh_ip_port;
    @SerializedName("ssh_username")
    @Expose
    String ssh_username;
    @SerializedName("ssh_password")
    @Expose
    String ssh_password;
    @SerializedName("is_own_bitcoin")
    @Expose
    int is_own_bitcoin;
    @SerializedName("rpc_username")
    @Expose
    String rpc_username;
    @SerializedName("rpc_password")
    @Expose
    String rpc_password;
    @SerializedName("store_name")
    @Expose
    String store_name;
    @SerializedName("latitude")
    @Expose
    String latitude;
    @SerializedName("longitude")
    @Expose
    String longitude;
    @SerializedName("password")
    @Expose
    String password;
    //maxboost _limit => per_boost_limit
    @SerializedName("per_boost_limit")
    @Expose
    String maxboost_limit;
    //merchant_maxboost => max_daily_boost
    @SerializedName("max_daily_boost")
    @Expose
    String merchant_maxboost;
    @SerializedName("created_at")
    @Expose
    String created_at;
    @SerializedName("updated_at")
    @Expose
    String updated_at;
    @SerializedName("container_address")
    @Expose
    String container_address;
    @SerializedName("lightning_port")
    @Expose
    String lightning_port;
    @SerializedName("pws_port")
    @Expose
    String pws_port;
    @SerializedName("mws_port")
    @Expose
    String mws_port;
    @SerializedName("accessToken")
    @Expose
    String accessToken;
    @SerializedName("refreshToken")
    @Expose
    String refreshToken;

    public String getMerchant_data_id() {
        if(merchant_data_id != null && !merchant_data_id.equalsIgnoreCase(""))
        return merchant_data_id;
        return merchant_id;
    }

    public void setMerchant_data_id(String merchant_data_id) {
        this.merchant_data_id = merchant_data_id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getContainer_address() {
        return container_address;
    }

    public void setContainer_address(String container_address) {
        this.container_address = container_address;
    }

    public String getLightning_port() {
        return lightning_port;
    }

    public void setLightning_port(String lightning_port) {
        this.lightning_port = lightning_port;
    }

    public String getPws_port() {
        return pws_port;
    }

    public void setPws_port(String pws_port) {
        this.pws_port = pws_port;
    }

    public String getMws_port() {
        return mws_port;
    }

    public void setMws_port(String mws_port) {
        this.mws_port = mws_port;
    }

    public String getAdmin_administrator_password() {
        return admin_administrator_password;
    }

    public void setAdmin_administrator_password(String admin_administrator_password) {
        this.admin_administrator_password = admin_administrator_password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMaxboost_limit() {
        return maxboost_limit;
    }

    public void setMaxboost_limit(String maxboost_limit) {
        this.maxboost_limit = maxboost_limit;
    }

    public String getMerchant_maxboost() {
        return merchant_maxboost;
    }

    public void setMerchant_maxboost(String merchant_maxboost) {
        this.merchant_maxboost = merchant_maxboost;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }


    public String decodeFromBase64(String base64) {
        // Receiving side
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        String text = new String(data, StandardCharsets.UTF_8);
        return text;
    }

    public String getSsh_ip_port() {
        return ssh_ip_port;
    }

    public void setSsh_ip_port(String ssh_ip_port) {
        this.ssh_ip_port = ssh_ip_port;
    }

    public String getSsh_username() {
        return ssh_username;
    }

    public void setSsh_username(String ssh_username) {
        this.ssh_username = ssh_username;
    }

    public String getSsh_password() {
//        if(ssh_password!=null){
//            return decodeFromBase64(ssh_password);
//        }else {
//            return  ssh_password;
//        }
        return ssh_password;
    }

    public void setSsh_password(String ssh_password) {
        this.ssh_password = ssh_password;
    }

    public boolean isIs_own_bitcoin() {

        if (is_own_bitcoin == 0) {
            return false;
        } else if (is_own_bitcoin == 1) {
            return true;
        } else {
            return false;
        }

    }

    public int getToken_expiry_time() {
        return token_expiry_time;
    }

    public void setToken_expiry_time(int token_expiry_time) {
        this.token_expiry_time = token_expiry_time;
    }

    public void setIs_own_bitcoin(int is_own_bitcoin) {
        this.is_own_bitcoin = is_own_bitcoin;
    }

    public String getRpc_username() {
        return rpc_username;
    }

    public void setRpc_username(String rpc_username) {
        this.rpc_username = rpc_username;
    }

    public String getRpc_password() {
        return rpc_password;
    }

    public void setRpc_password(String rpc_password) {
        this.rpc_password = rpc_password;
    }

    public String getTax_rate() {
        return tax_rate;
    }

    public void setTax_rate(String tax_rate) {
        this.tax_rate = tax_rate;
    }
}
