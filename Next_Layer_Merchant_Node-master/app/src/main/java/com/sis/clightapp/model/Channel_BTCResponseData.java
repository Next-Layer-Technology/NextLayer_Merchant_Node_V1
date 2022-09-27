package com.sis.clightapp.model;

public class Channel_BTCResponseData {
    /*{
  "data": {
    "id": 199351252,
    "timestamp": "1632395233",
    "amount": 0.51,
    "amount_str": "0.51000000",
    "price": 43731.81,
    "price_str": "43731.81",
    "type": 1,
    "microtimestamp": "1632395233926000",
    "buy_order_id": 1407222801702913,
    "sell_order_id": 1407222821965824
  }

}*/

    int id;
    String timestamp;
    double amount;
    String amount_str;
    double price;
    String price_str;
    int type;
    String microtimestamp;
    int buy_order_id;
    int sell_order_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAmount_str() {
        return amount_str;
    }

    public void setAmount_str(String amount_str) {
        this.amount_str = amount_str;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPrice_str() {
        return price_str;
    }

    public void setPrice_str(String price_str) {
        this.price_str = price_str;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMicrotimestamp() {
        return microtimestamp;
    }

    public void setMicrotimestamp(String microtimestamp) {
        this.microtimestamp = microtimestamp;
    }

    public int getBuy_order_id() {
        return buy_order_id;
    }

    public void setBuy_order_id(int buy_order_id) {
        this.buy_order_id = buy_order_id;
    }

    public int getSell_order_id() {
        return sell_order_id;
    }

    public void setSell_order_id(int sell_order_id) {
        this.sell_order_id = sell_order_id;
    }
}
