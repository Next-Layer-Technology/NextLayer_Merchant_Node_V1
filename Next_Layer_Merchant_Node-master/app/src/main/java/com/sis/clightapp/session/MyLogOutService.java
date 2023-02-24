package com.sis.clightapp.session;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.sis.clightapp.util.CustomSharedPreferences;
import com.sis.clightapp.util.GlobalState;

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
        GlobalState.getInstance().itemsList.clear();
        GlobalState.getInstance().selectedItems.clear();
        //stop service
        this.stopSelf();
    }
}
