package com.desaysv.autodelete.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.desaysv.autodelete.DeleteThread;
import com.desaysv.autodelete.TimeToDelActivity;

public class DataDeleteReceiver extends BroadcastReceiver {

    private static final String TAG = "DataDeleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TimeToDelActivity.ALARM_ACTION_CODE.equals(action)) {
            Log.d(TAG,"-----定时清理数据-----");
            new DeleteThread().start();
        }
    }

}
