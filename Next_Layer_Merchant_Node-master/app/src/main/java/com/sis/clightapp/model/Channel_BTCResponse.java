package com.sis.clightapp.model;

public class Channel_BTCResponse {
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
  },
  "channel": "live_trades_btcusd",
  "event": "trade"
}*/

    String channel;
    String event;
    Channel_BTCResponseData data;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Channel_BTCResponseData getData() {
        return data;
    }

    public void setData(Channel_BTCResponseData data) {
        this.data = data;
    }
}
