package com.desaysv.autodelete.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import com.desaysv.autodelete.utils.CommonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author uidq2602
 * time  : 2020/01/04
 * desc  : utils about file
 */
public final class FileUtils {

    private static final String TAG = "LogService.FileUtils";
    private static final boolean DEBUG = false;

    /**
     * 将文件按名字升序排列
     */
    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            return file1.getName().compareTo(file2.getName());
        }
    }

    /**
     * 判断当前目录下是否有文件
     *
     * @param path 当前文件目录
     * @return false FolderDir is not Exist File
     */
    public static boolean isFolderDirFileExist(String path) {
        boolean ret = false;
        File file = new File(path);
        if (file.isDirectory()) {
            if (file.list() != null && file.list().length != 0) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 创建 log文件
     */
    public static File createFile(final String strLogRootPath) {
        // 格式化系统时间
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        String strFolderFileName = dateFormat.format(new Date());
        String strFolderAllPath = strLogRootPath + strFolderFileName + "/";
        File curLogFolder = new File(strFolderAllPath);
        File curFile = null;
        if (!curLogFolder.exists()) {
            if (curLogFolder.mkdirs()) {
                curFile = new File(strFolderAllPath + "log_" + strFolderFileName + ".txt");
                try {
                    if (curFile.createNewFile()) {
                        Log.d(TAG, "createLogfile succeed:" + curLogFolder.getPath());
                        return curFile;
                    } else {
                        Log.e(TAG, "createLogfile failed:" + curFile.getPath());
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "createFile error: "+ e.getMessage());
                }
            } else {
                Log.e(TAG, "createFile: " + "curLogFolder.mkdirs() is failed:" + curLogFolder.getPath());
            }
        }
        return curFile;
    }

    /**
     * 获取文件创建时间
     *
     * @param file 文件
     */
    public static Long getFileCreateTime(File file) {
        try {
            Path path = Paths.get(file.getPath());
            BasicFileAttributeView basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            BasicFileAttributes attr = basicView.readAttributes();
            return attr.creationTime().toMillis();
        } catch (Exception e) {
            Log.e(TAG, "getFileCreateTime error:"+e.getMessage());
            return file.lastModified();
        }
    }

    /**
     * 遍历指定路径下的所有文件大小
     *
     * @param filePath 文件路径
     * @return 文件大小
     */
    public static long getDirFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            Log.e(TAG, "getDirFilesSize error ", e);
        }
        return blockSize / 1024 / 1024;
    }

    /**
     * 获取指定文件夹中的文件大小
     *
     * @param f 文件夹
     * @return 文件大小
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File[] fList = f.listFiles();
        if (fList != null && fList.length != 0) {
            for (File file : fList) {
                if (file.isDirectory()) {
                    size = size + getFileSizes(file);
                } else {
                    size = size + getFileSize(file);
                }
            }
        } else {
            Log.e(TAG, "getFileSizes:fList is null");
        }
        return size;
    }

    /**
     * 获取指定文件大小
     *
     * @param file 文件
     * @return 文件大小
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis;
            fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        } else {
            file.createNewFile();
        }
        return size;
    }

    /**
     * 复制文件夹及其中的文件
     *
     * @param oldFile File 原文件
     * @param newPath String 复制后的路径
     * @return true if and only if the directory and files were copied;
     * false otherwise
     */
    public static boolean copyFolder(File oldFile, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e(TAG, "copyFolder: cannot create directory:" + newPath);
                    return false;
                }
            }
            FileInputStream fileInputStream = new FileInputStream(oldFile);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + oldFile.getName());
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "copyFolder error ", e);
            return false;
        }
    }

    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径
     * @param newPath String 复制后的路径
     * @return true if and only if the directory and files were copied;
     * false otherwise
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e(TAG, "copyFolder: cannot create directory:" + newPath);
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e(TAG, "copyFolder: oldFile does not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e(TAG, "copyFolder: oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e(TAG, "copyFolder: oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "copyFolder error ", e);
            return false;
        }
    }

    /**
     * 压缩文件,文件夹
     *
     * @param srcFilePath 要压缩的文件/文件夹名字
     * @param zipFilePath 指定压缩的目的和名字
     */
    public static boolean zipFolder(String srcFilePath, String zipFilePath) {
        boolean isCompressSuccess = false;
        ZipOutputStream outZip = null;
        try {
            //创建Zip包
            outZip = new ZipOutputStream(new FileOutputStream(zipFilePath));
            //打开要输出的文件
            File file = new File(srcFilePath);
            //压缩
            isCompressSuccess = zipFiles(file.getParent() + File.separator, file.getName(), outZip);
            if (DEBUG) Log.d(TAG, "isCompressSuccess = " + isCompressSuccess);
        } catch (Exception e) {
            Log.e(TAG, "zipFolder error ", e);
        } finally {
            //完成,关闭
            if (outZip != null) {
                try {
                    outZip.finish();
                    outZip.close();
                } catch (IOException e) {
                    Log.e(TAG, "zipFolder error ", e);
                }
            }
        }
        return isCompressSuccess;
    }

    /**
     * 压缩文件
     *
     * @param folderPath 文件夹路径
     * @param filePath 文件路径
     * @param zipOut 压缩包
     */
    private static boolean zipFiles(String folderPath, String filePath, ZipOutputStream zipOut) {
        if (DEBUG) Log.d(TAG, "file = " + folderPath + filePath);
        if (zipOut == null || folderPath == null || filePath == null) {
            Log.e(TAG, "zipOut is null!!!" + "; folderPath:" + folderPath + "; filePath:" + filePath);
            return false;
        } else if (!CommonUtils.mIsSvLogExist) {
            Log.w(TAG, "Stop compression!");
            return false;
        }
        File file = new File(folderPath + filePath);
        FileInputStream inputStream = null;
        try {
            //判断是不是文件
            if (file.isFile()) {
                ZipEntry zipEntry = new ZipEntry(filePath);
                inputStream = new FileInputStream(file);
                zipOut.putNextEntry(zipEntry);
                int len;
                byte[] buffer = new byte[4096];
                while ((len = inputStream.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, len);
                }
                if (DEBUG) Log.d(TAG, "File compression complete = " + file.getPath());
                zipOut.closeEntry();
            } else {
                //文件夹的方式,获取文件夹下的子文件
                String[] fileList = file.list();
                if (DEBUG) Log.d(TAG, "fileList = " + Arrays.toString(fileList));
                //如果没有子文件, 则添加进去即可
                if (fileList.length <= 0) {
                    ZipEntry zipEntry =
                            new ZipEntry(filePath + File.separator);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.closeEntry();
                }
                //如果有子文件, 遍历子文件
                for (String s : fileList) {
                    if (!zipFiles(folderPath, filePath + File.separator + s, zipOut)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "zipFiles error ", e);
        } finally {
            if (inputStream != null) {
                try {
                    if (DEBUG) Log.d(TAG, "inputStream is close");
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "zipFiles error ", e);
                }
            }
        }
        return true;
    }


    /**
     * 删除log文件夹和文件夹里面的文件
     */
    public static void deleteLogDir(final String strPath) {
        File dir = new File(strPath);
        deleteDirWithLogFile(dir);
    }

    /**
     * 删除log文件
     */
    private static void deleteDirWithLogFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            Log.e(TAG, "deleteDirWithLogFile:dir does not exist:" + dir);
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                // 删除所有文件
                file.delete();
            } else if (file.isDirectory()) {
                // 递规的方式删除文件夹
                deleteDirWithLogFile(file);
            }
        }
        // 删除目录本身
        dir.delete();
    }
}
