package com.sis.clightapp.Utills;

import io.socket.client.Ack;

public class Acknowledgement implements Ack {


    public Acknowledgement(Object... args) {
        call(args);
    }

    @Override
    public void call(Object... args) {
    }


}