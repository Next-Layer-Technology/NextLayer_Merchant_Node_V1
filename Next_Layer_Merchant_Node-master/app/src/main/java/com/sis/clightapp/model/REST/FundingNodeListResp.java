package com.sis.clightapp.model.REST;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FundingNodeListResp {
    @SerializedName("message")
    @Expose
    String message;
    @SerializedName("data")
    @Expose
    ArrayList<FundingNode> fundingNodesList;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<FundingNode> getFundingNodesList() {
        return fundingNodesList;
    }

    public void setFundingNodesList(ArrayList<FundingNode> fundingNodesList) {
        this.fundingNodesList = fundingNodesList;
    }
}
