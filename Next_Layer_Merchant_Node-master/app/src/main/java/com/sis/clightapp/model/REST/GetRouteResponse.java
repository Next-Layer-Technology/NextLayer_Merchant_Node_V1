package com.sis.clightapp.model.REST;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GetRouteResponse implements Serializable {
    @SerializedName("route")
    public List<Route> routes;
}
