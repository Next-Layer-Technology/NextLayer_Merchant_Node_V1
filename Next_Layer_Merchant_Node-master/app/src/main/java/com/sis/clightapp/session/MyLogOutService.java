package com.sis.clightapp.session;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sis.clightapp.Utills.CustomSharedPreferences;

import java.util.Date;

public class MyLogOutService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        //do something you want before app closes.
        Date date = new Date(System.currentTimeMillis()); //or simply new Date();
        System.out.println("date"+date.toString());
        CustomSharedPreferences customSharedPreferences=new CustomSharedPreferences();
        customSharedPreferences.setsession(date,"lastdate",getApplicationContext());
        //stop service
        this.stopSelf();
    }
}
