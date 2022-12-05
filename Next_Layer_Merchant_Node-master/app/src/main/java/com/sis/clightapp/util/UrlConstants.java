package com.sis.clightapp.util;

public class UrlConstants {
    public static String getInvoiceSendCommand(String token, String rMSatoshi, String label, String description) {
        return "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli invoice" + " " + rMSatoshi + " " + label + " " + description + " " + 300 +
                "\"]" +
                " }";
    }
}
