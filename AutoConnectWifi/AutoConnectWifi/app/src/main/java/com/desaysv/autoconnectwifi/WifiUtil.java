package com.desaysv.autoconnectwifi;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.android.server.wifi.WifiContext;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/8/28.
 */
public class WifiUtil {

    public WifiManager mWifiManager;

    private WifiInfo mWifiInfo;

    private WifiMetrics wifiMetrics;

    private List<ScanResult> mWifiList;

    private List<WifiConfiguration> mWificonfiguration;

    private WifiManager.WifiLock mWifiLock;

    private Context mContext;

    private WifiContext mWifiContext;

    private WifiInjector mWifiInjector;

    private static WifiUtil util;


    /**
     * 单例方法
     *
     * @param context
     * @return
     */
    public static WifiUtil getInstance(Context context) {
        if (util == null) {
            synchronized (WifiUtil.class) {
                util = new WifiUtil(context);
            }
        }
        return util;
    }

    private WifiUtil(Context context) {
        mContext = context;
        mWifiContext = new WifiContext(mContext);
        mWifiInjector = new WifiInjector(mWifiContext);
        wifiMetrics = mWifiInjector.getWifiMetrics();
        mWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public int checkState() {
        return mWifiManager.getWifiState();
    }

    public void acquireWifiLoc() {
        mWifiLock.acquire();
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    public void createWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("test");
    }

    public List<WifiConfiguration> getConfigurations() {
        return mWificonfiguration;
    }

    public Boolean connectConfiguration(int index) {
        mWifiManager.enableNetwork(index, true);
        mWifiManager.saveConfiguration();
        mWifiManager.reconnect();
        return true;
    }

    public void startScan() {
        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mWificonfiguration = mWifiManager.getConfiguredNetworks();
    }

    public List<ScanResult> getmWifiList() {
        return mWifiList;
    }

    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + String.valueOf(i + 1) + ":");
            stringBuilder.append(mWifiList.get(i).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }

    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();

    }

    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    public boolean addNetWork(WifiConfiguration wifiConfiguration) {
        int wcgID = mWifiManager.addNetwork(wifiConfiguration);
        Log.i("wcgID", wcgID + "true");
        wifiMetrics.logStaEvent(15,wifiConfiguration);
        mWifiManager.enableNetwork(wcgID, true);
        mWifiManager.saveConfiguration();
        mWifiManager.reconnect();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Network network = mWifiManager.getCurrentNetwork();
//        if (network != null) {
//            Log.i("WifiUtil" , "network: " + network.toString());
//        }
        return true;

    }

    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public void deleteWifi(){
        mWifiManager.removeNetwork( WifiUtil.getInstance(mContext).getNetworkId());
    }

    public ArrayList<ScanResult> searchList(Context context) {
        ArrayList<ScanResult> mItems = new ArrayList<>();
        List<ScanResult> results = WifiUtil.getInstance(context).getmWifiList();
        int size = results.size();
        HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
        try {
            for (int i = 0; i < size; i++) {
                ScanResult result = results.get(i);
                if (!result.SSID.isEmpty()) {
                    String key = result.SSID + " "
                            + result.capabilities;
                    if (!signalStrength.containsKey(key)) {
                        signalStrength.put(key, i);
                        mItems.add(result);
                    } else {
                        int position = signalStrength.get(key);
                        if ( result.level >
                                result.level) {
                            mItems.set(position, result);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mItems;
    }

    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.isExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        switch (Type) {
            case 1:
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case 2:
                configuration.hiddenSSID = false;
                configuration.wepKeys[0] = "\"" + Password + "\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            case 3:
                configuration.preSharedKey = "\"" + Password + "\"";
                configuration.hiddenSSID = false;
                // configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return configuration;
    }

    private WifiConfiguration isExsits(String SSID) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig:
             existingConfigs) {
            if (existingConfig.SSID.equals("\"" +SSID+"\"")) {
                return  existingConfig;
            }
            
        }
        return null;
    }




}
