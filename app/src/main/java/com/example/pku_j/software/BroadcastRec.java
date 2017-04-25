package com.example.pku_j.software;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import java.util.List;

public class BroadcastRec extends BroadcastReceiver {
      
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    static final String ACTION2 = "android.intent.action.ACTION_TIME_TICK";
    private Intent intent = null;
    private int flag = 0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        boolean isServiceRunning = false;
        if (intent.getAction().equals(ACTION)) {
            this.intent = new Intent(context, MsgService.class);
            context.startService(this.intent);
            Log.v("trace~","restart");
        }
        else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
            Log.v("trace~","Tick!");
            isServiceRunning = false;
            ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
            if(serviceList != null && serviceList.size() != 0) {
                for (ActivityManager.RunningServiceInfo info : serviceList) {
                    if (info.service.getClassName().equals("com.example.pku_j.software.MsgService")) {
                        isServiceRunning = true;
                        break;
                    }
                }
            }
            if (! isServiceRunning) {
                this.intent = new Intent(context, MsgService.class);
                context.startService(this.intent);
                Log.v("trace~", "hhhhh restart");
            }
        }
    }


    public Intent getIntent(){
        return intent;

    }
    public int getFlag(){
        return flag;
    }
}  