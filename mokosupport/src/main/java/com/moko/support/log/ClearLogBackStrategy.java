package com.moko.support.log;


import com.elvishew.xlog.printer.file.backup.BackupStrategy;

import java.io.File;
import java.util.Calendar;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.log.ClearLogBackStrategy
 */
public class ClearLogBackStrategy implements BackupStrategy {
    /**
     * @Date 2017/3/30
     * @Author wenzheng.liu
     * @Description 比较时间，如果当前时间大于日志最后操作时间，则备份当前日志，重新创建新的日志
     */
    @Override
    public boolean shouldBackup(File file) {
        long time = file.lastModified();
        Calendar modifiedTime = Calendar.getInstance();
        modifiedTime.setTimeInMillis(time);
        return getIntervalDays(modifiedTime, Calendar.getInstance()) > 0;
    }

    /**
     * @Date 2017/5/9
     * @Author wenzheng.liu
     * @Description 计算间隔时间
     */
    public static int getIntervalDays(Calendar from, Calendar to) {
        long dayMillis = 24 * 60 * 60 * 1000;
        int betweenDays;
        Calendar c1 = Calendar.getInstance();
        c1.set(from.get(Calendar.YEAR), from.get(Calendar.MONTH),
                from.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 0);
        Calendar c2 = Calendar.getInstance();
        c2.set(to.get(Calendar.YEAR), to.get(Calendar.MONTH),
                to.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c2.set(Calendar.MILLISECOND, 0);
        long a = c1.getTimeInMillis();
        long b = c2.getTimeInMillis();
        long bt = Math.abs(a - b);
        betweenDays = (int) (bt / dayMillis);
        return betweenDays;
    }
}