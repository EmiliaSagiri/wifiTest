package com.desaysv.autoconnectwifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AutoTestMainactivity extends AppCompatActivity {
    private static final String TAG = AutoTestMainactivity.class.getSimpleName();
    private static final String PASSWORD = "1234567890zxcvbnmasdfghjklqwertyuiop";
    private TextView tAutoSearch;
    private TextView tCurrentWiFi;
    private EditText eName;
    private EditText ePassword;
    private EditText eNumber;
    private WifiUtil wifiMangerUtil;
    private Button bStart;
    private Button bStop;
    private String ssid;
    private String password;
    private TextView progress;
    private List<ScanResult> wifiList;
    private ListView listView;
    private int testNum;
    private static volatile int curTestNum = 0;
    private boolean runFlag = false;
    private Context context;
    private HashMap<String, String> wiFiMap = new HashMap<String, String>();
    private HashMap<Integer, String> statusMap = new HashMap<>();
    private List<WifiData> wifiDataList = new ArrayList<>();
    private String Id = "";

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
                case 3:
                    tCurrentWiFi.setText(msg.getData().getString("data"));
                default:
                    return;
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_test_mainactivity);
        initView();
        initListView();
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFlag = true;
                ssid = eName.getText().toString().trim();
                password = ePassword.getEditableText().toString().trim();

                try {
                    testNum = Integer.valueOf(eNumber.getText().toString().trim());
                }catch (Exception e) {
                    new AlertDialog.Builder(context)
                            .setTitle("错误" )
                            .setMessage("测试次数输入框请输入数字" )
                            .setPositiveButton("确定" , null )
                            .show();
                    return;
                }

                ManagerThread managerThread = new ManagerThread(2);
                managerThread.start();
            }
        });
    }

    private void initView(){
        tAutoSearch = findViewById(R.id.auto_search);
        tCurrentWiFi = findViewById(R.id.wifi_current_data);
        progress = findViewById(R.id.wifi_number_current);
        eName = findViewById(R.id.wifi_name);
        ePassword = findViewById(R.id.wifi_password);
        eNumber = findViewById(R.id.wifi_number_total);
        bStart = findViewById(R.id.test_start);
        bStop = findViewById(R.id.btn_stop);
        listView = findViewById(R.id.listview_wifi);

        wifiMangerUtil = WifiUtil.getInstance(this);
        wifiMangerUtil.startScan();
        context = AutoTestMainactivity.this;
        wiFiMap.put("tems", "sv2655888");
//        wiFiMap.put("guest", "");
//        wiFiMap.put("partner", "");
        statusMap.put(0,"已连接");
        statusMap.put(1,"可连接");
        statusMap.put(2,"不可连接");

    }

    private void initListView(){
        wifiList = wifiMangerUtil.searchList(context);
        for(ScanResult scanResult : wifiList){
            int isExist = 0;
            for (WifiConfiguration configuration : wifiMangerUtil.getConfigurations()){
                if(configuration.SSID.equals(scanResult.SSID)){
                    isExist = configuration.status;
                    return;
                }
            }
            WifiData wifiData = new WifiData(scanResult.SSID,statusMap.get(isExist));
            wifiDataList.add(wifiData);
        }

        ListAdapter listAdapter = new ListAdapter(context, R.layout.activity_auto_test_mainactivity, wifiDataList);
        listView.setAdapter(listAdapter);
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

    private void sendIdMsg(){
        Message msg = workHandler.obtainMessage();
        msg.what = 3;
        Bundle bundle = new Bundle();
        bundle.putString("data", Id);
        msg.setData(bundle);
        workHandler.sendMessage(msg);
    }

    private String getCurrentId(){
        String name = "";
        if(wifiMangerUtil != null){
            WifiInfo wifiInfo = wifiMangerUtil.mWifiManager.getConnectionInfo();
            if(wifiInfo != null){
               name = wifiInfo.getSSID();
            }
        }
        return name;
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
                    for(int i = 0; i < workTreadNum; i++){
                        WifiTaskThread taskThread = new WifiTaskThread((testNum-curTestNum)/workTreadNum,countDownLatch);
                        //AutoToggleThread autoToggleThread = new AutoToggleThread((testNum-curTestNum)/workTreadNum,countDownLatch);
                        taskThread.start();
                    }

                    //autoToggleThread.start();

                try {
                    countDownLatch.await();
                    sendSuccessMsg();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*/
    自动搜索，自动连接，自动断开，自动删除
     */
    class WifiTaskThread extends Thread {

        private int testNum;
        private CountDownLatch countDownLatch;

        public WifiTaskThread(int testNum,CountDownLatch countDownLatch) {
            this.testNum = testNum;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            for (int i = 0; i < testNum; i++) {
                if (!runFlag) {
                    return;
                }
                wifiList.clear();
                wifiList = wifiMangerUtil.searchList(context);
                int size = wifiList.size();
                for(int j = 0; j < size; j++){
                    ScanResult pw = wifiList.get(j);
                    if(wiFiMap.containsKey(pw.SSID)){
                        Log.e(TAG,pw.SSID );
                        wifiMangerUtil.addNetWork(wifiMangerUtil.createWifiInfo(pw.SSID, wiFiMap.get(pw),  3));
                        Id = pw.SSID;
                        Log.e(TAG, Id);
                        sendIdMsg();
                    }
                    wifiMangerUtil.disconnectWifi(wifiMangerUtil.getNetworkId());
                    wifiMangerUtil.deleteWifi();
                }




//                for(WifiConfiguration configuration : wifiMangerUtil.getConfigurations()){
//                    wifiMangerUtil.addNetWork(configuration);
//
//                }

                wifiList.clear();
                wifiList = wifiMangerUtil.searchList(context);
                int length = wifiList.size();
                for(int k = 0; k < length; k++){
                    ScanResult pw = wifiList.get(k);
                    wifiMangerUtil.addNetWork(wifiMangerUtil.createWifiInfo(pw.SSID, PASSWORD, PASSWORD.length() == 0 ? 1 : 3));
                    Id = "连接失败 " + pw.SSID;
                    sendIdMsg();
                }

                curTestNum++;
                sendUpdateMsg();
                Log.e(TAG,"thread: " + Thread.currentThread().getName() + " exec : " + i);
            }
            countDownLatch.countDown();

        }
    }

    /*/
     自动切换A,B已连接的WIFI。
     */
    class AutoToggleThread extends Thread{
        private int testNum;
        private CountDownLatch countDownLatch;

        public AutoToggleThread(int testNum,CountDownLatch countDownLatch) {
            this.testNum = testNum;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();
            for (int i = 0; i < testNum; i++) {
                if (!runFlag) {
                    return;
                }
                Id = getCurrentId();
                sendIdMsg();
                curTestNum++;
                Log.e(TAG,"thread: " + Thread.currentThread().getName() + " exec : " + i);
            }
            countDownLatch.countDown();

        }
    }
    /*/
    自动输入超长密码，自动多次反复连接
     */
//    class AutoLongThread extends Thread{
//        private int testNum;
//        private CountDownLatch countDownLatch;
//
//        public AutoLongThread(int testNum,CountDownLatch countDownLatch) {
//            this.testNum = testNum;
//            this.countDownLatch = countDownLatch;
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            for (int i = 0; i < testNum; i++) {
//                if (!runFlag) {
//                    return;
//                }
//                wifiList.clear();
//                wifiList = wifiMangerUtil.searchList(context);
//                int size = wifiList.size();
//                for(int j = 0; j < size; j++){
//                    ScanResult pw = wifiList.get(j);
//                    wifiMangerUtil.addNetWork(wifiMangerUtil.createWifiInfo(pw.SSID, PASSWORD, PASSWORD.length() == 0 ? 1 : 3));
//                    Id = "连接失败";
//                    sendIdMsg();
//                }
//                curTestNum++;
//                sendUpdateMsg();
//                Log.e(TAG,"thread: " + Thread.currentThread().getName() + " exec : " + i);
//            }
//            countDownLatch.countDown();
//
//        }
//        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runFlag = false;
    }
}









