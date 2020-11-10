package com.moko.support.log;

import android.content.Context;
import android.os.Environment;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator;

import java.io.File;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.log.LogModule
 */
public class LogModule {
    private static final String TAG = "mokoBeaconX";
    private static final String LOG_FOLDER = "mokoBeaconX";
    private static final String LOG_FILE = "mokoBeaconX.txt";
    private static String PATH_LOGCAT;

    public static void init(Context context) {
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LOG_FOLDER;
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + LOG_FOLDER;
        }
        Printer filePrinter = new FilePrinter.Builder(PATH_LOGCAT)
                .fileNameGenerator(new ChangelessFileNameGenerator(LOG_FILE))
                .backupStrategy(new ClearLogBackStrategy())
                .logFlattener(new PatternFlattener("{d yyyy-MM-dd HH:mm:ss} {l}/{t}: {m}"))
                .build();
        LogConfiguration config = new LogConfiguration.Builder()
                .tag(TAG)
                .logLevel(LogLevel.ALL)
                .build();
        XLog.init(config, new AndroidPrinter(), filePrinter);
    }

    public static void v(String msg) {
        XLog.v(msg);
    }

    public static void d(String msg) {
        XLog.d(msg);
    }

    public static void i(String msg) {
        XLog.i(msg);
    }

    public static void w(String msg) {
        XLog.w(msg);
    }

    public static void e(String msg) {
        XLog.e(msg);
    }
}
