package com.moko.beaconx.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class IOUtils {
    public static final String CRASH_FILE = "crash_log.txt";

    /**
     * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist) {
            sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return sdpath;

    }

    /**
     * 获取默认的文件路径
     *
     * @return
     */
    public static String getDefaultFilePath() {
        String filepath = "";
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "mokoBeaconX",
                CRASH_FILE);
        try {
            if (file.exists()) {
                filepath = file.getAbsolutePath();
            } else {
                file.createNewFile();
                filepath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath;
    }

    /**
     * 获取指定的文件路径
     *
     * @return
     */
    public static String getFilePath(String fileName) {
        String filepath = "";
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "mokoBeaconX",
                fileName);
        try {
            if (file.exists()) {
                filepath = file.getAbsolutePath();
            } else {
                file.createNewFile();
                filepath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath;
    }

    public static boolean isFileExist(String filePath) {
        if (!isSdCardExist()) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static void drawableToFile(Bitmap bitmap, String filePath) {
        if (bitmap == null)
            return;

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取文件内容
     *
     * @return
     */
    public static String getCrashLog() {
        try {
            File file = new File(getDefaultFilePath());
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream is = new FileInputStream(file);
            byte[] b = new byte[is.available()];
            is.read(b);
            String result = new String(b);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 写文件内容
     *
     * @param info
     */
    public static void setCrashLog(String info) {
        if (!isSdCardExist()) {
            return;
        }
        try {
            File file = new File(getDefaultFilePath());
            if (!file.exists()) {
                file.createNewFile();
            }
            StringBuffer buffer = new StringBuffer();
            // 记录时间
            buffer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    .format(Calendar.getInstance().getTime()));
            buffer.append("\r\n");
            // 记录机型
            buffer.append(android.os.Build.MODEL);
            buffer.append("\r\n");
            // 记录版本号
            buffer.append(android.os.Build.VERSION.RELEASE);
            buffer.append("\r\n");
            buffer.append(info);
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(buffer.toString().getBytes());
            fos.write("\r\n".getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
