package com.desaysv.autodelete;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.desaysv.autoconnectwifi.R;
import com.desaysv.autodelete.receiver.DataDeleteReceiver;

import java.util.Calendar;


public class TimeToDelActivity extends AppCompatActivity {

    private static final String TAG = "TimeToDelActivity";
    //注：ALARM_ACTION_CODE这个是action，后面需要匹配判断
    public static final String ALARM_ACTION_CODE = "com.desaysv.autodelete.receiver.DataDeleteReceiver";

    private EditText interval;
    private int intervalTime;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;



    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_to_delete);
        initView();
    }

    private void initView(){
        interval = (EditText) findViewById(R.id.interval);
    }

    public void startClick(View view) {
        //构造一个PendingIntent对象（用于发送广播）
        Intent intent = new Intent(ALARM_ACTION_CODE);
        //适配8.0以上（不然没法发出广播）
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            intent.setComponent(new ComponentName(this, DataDeleteReceiver.class));
        }
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        intervalTime = Integer.valueOf(interval.getText().toString().trim());
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 给当前时间加上若干秒
        calendar.add(Calendar.SECOND, 5);
        // 开始设定闹钟，延迟若干秒后，携带延迟意图发送闹钟广播
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),intervalTime,pendingIntent);
        Log.d(TAG, "任务已启动，触发时间：" + calendar.getTimeInMillis());
    }

    public void stopClick(View view) {
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(TimeToDelActivity.this, "任务已取消！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(TimeToDelActivity.this, "任务不存在！", Toast.LENGTH_SHORT).show();
        }

    }
}
