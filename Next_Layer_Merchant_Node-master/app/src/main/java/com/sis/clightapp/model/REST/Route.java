package com.sis.clightapp.model.REST;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Route implements Serializable {
    @SerializedName("id")
    public String id;

    @SerializedName("msatoshi")
    public long msatoshi;
}
