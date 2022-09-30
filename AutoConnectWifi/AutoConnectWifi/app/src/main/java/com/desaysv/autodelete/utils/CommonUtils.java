package com.desaysv.autodelete.utils;

import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CommonUtils {

    private static final String TAG = "DeleteThread.CommonUtils";
    private static final boolean DEBUG = true;

    // 脚本收集拷贝日志服务指令
    public static final String SV_BUG_REPORT_SERVICE = "sys.ivi.svbugreport_service";
    // U盘中存放的标志文件的名称
    public static final String FLAG_FILENAME = "svlog.flag";
    // 日志收集结束的标志
    public static final String END_FLAG = "endlog";
    // 日志拷贝是否成功标志
    public static final String FINISH_FLAG = "finishFlag";

    // U盘路径
    private static final String USB0_PATH = "/storage/usb0";
    private static final String USB1_PATH = "/storage/usb1";
    private static final String MNT_USB0_PATH = "/mnt/media_rw/usb0";
    private static final String MNT_USB1_PATH = "/mnt/media_rw/usb1";
	
	public static final String MNT_USB_DIR = "/mnt/media_rw/";
    public static final String INTERNAL_STORAGE_PATH = "/storage/emulated/";

    // U盘剩余最小存储空间大小，1 * 1024 = 1024(1G)
    private static final int MIN_SPACE = 1024;
    // svlog.flag文件是否存在标志
    public static boolean mIsSvLogExist = false;

    /**
     * U盘插入原路径转映射路径
     *
     * @param path 原路径
     * @return 映射路径
     */
    public static String getUDiskPath(String path) {
        if (USB0_PATH.equals(path)) {
            return MNT_USB0_PATH;
        } else if (USB1_PATH.equals(path)) {
            return MNT_USB1_PATH;
        }
        return null;
    }

    /**
     * 检测插入设备是否是U盘
     *
     * @param path 设备路径
     * @return true:U盘   false:其他设备
     */
    public static boolean isUDiskPath(String path) {
        switch (path) {
            case USB0_PATH:
            case USB1_PATH:
                return true;
            default:
                return false;
        }
    }

    /**
     * 检测U盘剩余存储空间大小
     *
     * @param path U盘路径
     */
    public static boolean isEnoughSpace(String path) {
        boolean isEnoughSpace = true;
        try {
            StatFs statFs = new StatFs(path);
            long availableSize = statFs.getAvailableBytes() / 1024 / 1024; //获取U盘可用空间
            Log.d(TAG, "availableSize: " + availableSize + "Mb");
            if (availableSize < MIN_SPACE) {
                Log.d(TAG, "U disk pace not enough!");
                isEnoughSpace = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "isEnoughSpace error : "+e.getMessage());
        }
        return isEnoughSpace;
    }

    /**
     * 检查int类型系统属性，如果大于0则返回false
     *
     * @param prop 需要查询的属性值的名称
     * @return 是否小于等于0
     */
    public static boolean checkProperties(String prop) {
        int a = SystemProperties.getInt(prop, 0);
        if (DEBUG)Log.d(TAG, "checkProperties: prop: " + prop + "; value  = " + a);
        return a <= 0;
    }

    /**
     * 创建flag文件
     *
     * @param path     U盘路径
     * @param fileName 文件名
     */
    public static boolean createFlagFile(String path, String fileName) {
        boolean isSuccess = false;
        File file = new File(path + File.separator + fileName);
        try {
            if (!file.exists()) {
                isSuccess = file.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "createFlagFile error: "+ e.getMessage());
        }
        return isSuccess;
    }

    /**
     * 删除flag文件
     *
     * @param path     U盘路径
     * @param fileName 文件名
     */
    public static boolean deleteFlagFile(String path, String fileName) {
        boolean isSuccess = false;
        File file = new File(path + File.separator + fileName);
        if (file.exists()) {
            isSuccess = file.delete();
        }
        return isSuccess;
    }

    /**
     * 检测UDisk中的flag文件是否存在
     *
     * @param path     U盘路径
     * @param fileName 文件名
     */
    public static boolean checkFileIsExist(String path, String fileName) {
        File file = new File(path + File.separator + fileName);
        if (file.exists() && file.isFile()) {
            if (DEBUG) Log.d(TAG, " checkFileIsExist:USB File Exists:" + path + "/" + fileName);
            return true;
        } else {
            if (DEBUG) Log.d(TAG, " checkFileIsExist:USB File Not Exists:" + path + "/" + fileName);
            return false;
        }
    }


    public static void deleteFileBefore(String path, long time){
        File file = new File(path);
        File[] files = file.listFiles();
        Date date = new Date(System.currentTimeMillis() - time);
        for (File f : files) {
            if (f.isDirectory() && f.getName().startsWith("logs_")){
                Log.d(TAG,new StringBuilder("filename: ").append(f.getName())
                        .append("fileLastModified: ").append(new Date(file.lastModified()).toString())
                        .append(" data: ").append(date.toString()).toString() );
                if (new Date(file.lastModified()).before(date)) {
                    FileUtils.deleteLogDir(f.getPath());
                    Log.d(TAG, "deleteFile: " + f.getName());
                }
            }
        }
    }




}
