package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FundingNode {


    @SerializedName("id")
    @Expose
    int id;
    @SerializedName("node_id")
    @Expose
    String node_id;
    @SerializedName("ip")
    @Expose
    String ip;
    @SerializedName("port")
    @Expose
    String port;
    @SerializedName("username")
    @Expose
    String username;
    @SerializedName("password")
    @Expose
    String password;
    @SerializedName("merchant_boost_fee")
    @Expose
    String merchant_boost_fee;
    @SerializedName("lightning_boost_fee")
    @Expose
    String lightning_boost_fee;
    @SerializedName("created_at")
    @Expose
    String created_at;
    @SerializedName("updated_at")
    @Expose
    String updated_at;
    @SerializedName("registration_fees")
    @Expose
    String registration_fees;

    public String getCompany_email() {
        return company_email;
    }

    public void setCompany_email(String company_email) {
        this.company_email = company_email;
    }

    @SerializedName("company_email")
    @Expose
    String company_email;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMerchant_boost_fee() {
        return merchant_boost_fee;
    }

    public void setMerchant_boost_fee(String merchant_boost_fee) {
        this.merchant_boost_fee = merchant_boost_fee;
    }

    public String getLightning_boost_fee() {
        return lightning_boost_fee;
    }

    public void setLightning_boost_fee(String lightning_boost_fee) {
        this.lightning_boost_fee = lightning_boost_fee;
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
    public double getRegistration_fees() {

        return Double.parseDouble(registration_fees);
    }

    public void setRegistration_fees(String registration_fees) {
        this.registration_fees = registration_fees;
    }
    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

}
