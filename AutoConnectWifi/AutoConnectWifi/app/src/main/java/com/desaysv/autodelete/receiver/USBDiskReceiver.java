package com.desaysv.autodelete.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.desaysv.autodelete.DeleteThread;
import com.desaysv.autodelete.utils.CommonUtils;

import java.io.File;
import java.util.Objects;

public class USBDiskReceiver extends BroadcastReceiver {

    private static final String TAG = "DeleteThread.USBDiskReceiver";

    private static String usbPath = "";

    public static String getUsbPath() {
        return usbPath;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String path = Objects.requireNonNull(intent.getData()).getPath();
        Log.i(TAG, "onReceive: action: " + action + "; path :" + path);
		
		File f = new File(path);
        String fileName = f.getName();
        Log.i(TAG, fileName);
        File mnt = new File(CommonUtils.MNT_USB_DIR + fileName);
		//经过测试发现：内置sdcard卡挂载：/mnt/media_rw/internal_sdcard，但是广播发送过来的路径：/storage/emulated/0，名字根本不一样。
		//而外置U盘挂载/mnt/media_rw/中的节点文件名字和广播发送过来/storage/节点文件名字是一样的。
		//判断/mnt/media_rw/目录下是否存在和广播发送过来/storage/节点文件名字一模一样的文件既可。
        if (!mnt.exists() || !mnt.isDirectory()) {
			//判断广播发送过来的路径是否是以/storage/emulated/开头
            if (path.startsWith(CommonUtils.INTERNAL_STORAGE_PATH)) {
                Log.w(TAG, mnt.getPath() + " exist=" + mnt.exists() + " dir=" + mnt.isDirectory());
                Log.w(TAG, path + " exist=" + f.exists() + " dir=" + f.isDirectory());
                return;
            }
        }
        if (!TextUtils.isEmpty(path)) {
            if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                Log.i(TAG, "onReceive: MEDIA_UNMOUNTED");
                usbPath = "";
            }else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                Log.i(TAG, "onReceive: MEDIA_MOUNTED");
                usbPath = path;
                new DeleteThread().start();
            }
        } else {
            Log.e(TAG, "onReceive:path is null or is not a USB disk path!");
        }
    }
}
