package com.desaysv.autoconnectwifi;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.desaysv.autodelete.TimeToDelActivity;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText ssidEditText;
    private EditText passwordEditText;
    private EditText testNumEditText;
    private Button saveBtn;
    private Button stopBtn;
    private WifiUtil wifiUtil;
    private TextView progress;
    private String ssid;
    private String password;
    private int testNum;
    private static volatile int curTestNum = 0;
    private boolean runFlag = false;

//    Runnable updateThread = new Runnable(){
//        @Override
//        public void run() {
//            Message msg = workHandler.obtainMessage();
//            msg.arg1 = curTestNum;
//            msg.what = 1;
//            if (curTestNum <= testNum) {
//                workHandler.sendMessage(msg);
//            } else {
//                workHandler.removeCallbacks(updateThread);
//            }
//        }
//    };

    private Handler workHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            System.out.println("----------start handle");
            switch(msg.what) {
                case 1 :
                    progress.setText(String.valueOf(msg.arg1));
                    break;
                case 2:
                    successCallback();
                    break;
                default:
                    return;
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        wifiUtil = WifiUtil.getInstance(this);
        wifiUtil.startScan();
        for (WifiConfiguration configuration :
                wifiUtil.getConfigurations()) {

            Log.i("configuration", "ssid:"+configuration.SSID +"--id:"+ configuration.networkId +
                    "--priority" + configuration.priority + "--allowedAuthAlgorithms:"+configuration.allowedAuthAlgorithms +
            "--allowedGroupCiphers:"+configuration.allowedGroupCiphers +"--allowedKeyManagement:" +configuration.allowedKeyManagement +
                    "--allowedAuthAlgorithms:"+configuration.allowedAuthAlgorithms
            + "--allowedPairwiseCiphers:"+configuration.allowedPairwiseCiphers
            +"--hiddenSSID:"+configuration.hiddenSSID
            +"--wepTxKeyIndex:"+configuration.wepTxKeyIndex
            +"--wepKeys:"+configuration.wepKeys[0]
            +"--preSharedKey"+ configuration.preSharedKey
            +"--status:"+configuration.status);
        }


    }

    private void initView() {
        ssidEditText = (EditText) findViewById(R.id.edit_ssid);
        passwordEditText = (EditText) findViewById(R.id.eidt_password);
        testNumEditText = (EditText) findViewById(R.id.eidt_testNum);
        saveBtn = (Button) findViewById(R.id.btn_save);
        stopBtn = (Button) findViewById(R.id.btn_stop);
        progress = (TextView) findViewById(R.id.processBar);

        saveBtn.setOnClickListener(view -> {
            runFlag = true;
            ssid = ssidEditText.getText().toString().trim();
            password = passwordEditText.getEditableText().toString().trim();

            try {
                testNum = Integer.valueOf(testNumEditText.getText().toString().trim());
            }catch (Exception e) {
                new AlertDialog.Builder(this)
                        .setTitle("错误" )
                        .setMessage("测试次数输入框请输入数字" )
                        .setPositiveButton("确定" , null )
                        .show();
                return;
            }

            ManagerThread managerThread = new ManagerThread(1);
            managerThread.start();
        });
    }

    public void startNext(View view){
        Intent intent = new Intent(MainActivity.this,AutoTestMainactivity.class);
        startActivity(intent);
    }


    public void stopClick(View view) {
        runFlag = false;
    }

    private void successCallback(){
        new AlertDialog.Builder(this)
                .setTitle("成功" )
                .setMessage("测试完成" )
                .setPositiveButton("确定" , null )
                .show();
    }

    private void sendUpdateMsg(){
        Message msg = workHandler.obtainMessage();
        msg.arg1 = curTestNum;
        msg.what = 1;
        workHandler.sendMessage(msg);
    }

    private void sendSuccessMsg(){
        Message message = workHandler.obtainMessage();
        message.what = 2;
        workHandler.sendMessage(message);
    }

    public void jumpClick(View view) {
        Intent intent=new Intent(MainActivity.this, TimeToDelActivity.class);
        startActivity(intent);
    }

    class ManagerThread extends Thread {

        private int workTreadNum;

        public ManagerThread(int workTreadNum) {
            this.workTreadNum = workTreadNum;
        }

        @Override
        public void run() {
            if (runFlag) {
                CountDownLatch countDownLatch = new CountDownLatch(workTreadNum);
                for (int i = 0; i < workTreadNum; i++) {
                    WifiTaskThread taskThread = new WifiTaskThread(ssid,password,(testNum-curTestNum)/workTreadNum,countDownLatch);
                    taskThread.start();
                }
                try {
                    countDownLatch.await();
                    sendSuccessMsg();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    class WifiTaskThread extends Thread {

        private String ssid;
        private String password;
        private int testNum;
        private CountDownLatch countDownLatch;

        public WifiTaskThread(String ssid,String password,int testNum,CountDownLatch countDownLatch) {
            this.ssid = ssid;
            this.password = password;
            this.testNum = testNum;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            for (int i = 0; i < testNum; i++) {
                if (!runFlag) {
                    return;
                }
                wifiUtil.addNetWork(wifiUtil.createWifiInfo(ssid, password, password.length() == 0 ? 1 : 3));
                wifiUtil.disconnectWifi(wifiUtil.getNetworkId());
                wifiUtil.deleteWifi();
                curTestNum++;
                sendUpdateMsg();
                Log.e(TAG,"thread: " + Thread.currentThread().getName() + " exec : " + i);
            }
            countDownLatch.countDown();

        }
    }



}
