package com.desaysv.autodelete;

import android.util.Log;

import com.desaysv.autodelete.receiver.USBDiskReceiver;
import com.desaysv.autodelete.utils.CommonUtils;

import java.io.File;

public class DeleteThread extends Thread{

    private static final String TAG = "DeleteThread";

    @Override
    public void run() {
        String usbPath = USBDiskReceiver.getUsbPath();
        Log.d(TAG, "usbPath: " + usbPath);
        if (null != usbPath && usbPath != "") {
            if(CommonUtils.checkFileIsExist(usbPath,"delete.flag")){
                CommonUtils.deleteFileBefore(usbPath,1000 * 60 * 60);
            } else {
                Log.d(TAG, "delete.flag not exit" );
            }

        }
    }


    /**
     * java程序调用shell脚本
     */
    public void autoRunShellWithChmod(){
        try{
            Log.i(TAG,"autoRunShellWithChmod start......");
            //要执行的命令
            String command = "./deleteFile_echo.sh";
            //要执行的命令所在目录path
            String path = "/log";

            //1.执行命令，要先切换目录
            ProcessBuilder processBuilder = new ProcessBuilder();
            //先切换目标目录
            processBuilder.directory(new File(path));
            processBuilder.command(command);
            Process ps = processBuilder.start();
            int execStatus  = ps.waitFor(); //阻塞，直到上述命令执行完,返回为0则表示执行成功
            Log.i(TAG,"autoRunShellWithChmod execStatus:"+execStatus+",返回为0表示执行成功");
            Log.i(TAG,"autoRunShellWithChmod stop......");
        }catch (Exception e){
            Log.e(TAG,"autoRunShellWithChmod:error:"+e.getMessage(),e);
        }
    }
}
