package com.sis.clightapp.model;

public class Tax {
    private  double taxInUSD;
    private  double taxInBTC;
    private double taxpercent;

    public double getTaxpercent() {
        return taxpercent;
    }

    public void setTaxpercent(double taxpercent) {
        this.taxpercent = taxpercent;
    }

    public double getTaxInUSD() {
        return taxInUSD;
    }

    public void setTaxInUSD(double taxInUSD) {
        this.taxInUSD = taxInUSD;
    }

    public double getTaxInBTC() {
        return taxInBTC;
    }

    public void setTaxInBTC(double taxInBTC) {
        this.taxInBTC = taxInBTC;
    }
}
