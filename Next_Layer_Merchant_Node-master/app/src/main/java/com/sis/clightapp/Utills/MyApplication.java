package com.sis.clightapp.Utills;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Iterator;
import java.util.List;

public class MyApplication extends Application {
    private int lastInteractionTime;
    private Boolean isScreenOff = false;
    Context context=this;
    public void onCreate() {
        super.onCreate();
        // ......
        startUserInactivityDetectThread(); // start the thread to detect inactivity
        new ScreenReceiver();  // creating receive SCREEN_OFF and SCREEN_ON broadcast msgs from the device.
    }

    public void startUserInactivityDetectThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(15000); // checks every 15sec for inactivity
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(isScreenOff || getLastInteractionTime()> 120000 ||  !isAppForeground())
                    {
                        //...... means USER has been INACTIVE over a period of
                        // and you do your stuff like log the user out
                    }
                }
            }
        }).start();
    }

    public long getLastInteractionTime() {
        return lastInteractionTime;
    }

    public void setLastInteractionTime(int lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
    }

    private class ScreenReceiver extends BroadcastReceiver {

        protected ScreenReceiver() {
            // register receiver that handles screen on and screen off logic
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isScreenOff = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                isScreenOff = false;
            }
        }
    }
    public boolean isAppForeground() {

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = mActivityManager
                .getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = i.next();

            if (info.uid == context.getApplicationInfo().uid && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                return true;
            }
        }
        return false;
    }
}
