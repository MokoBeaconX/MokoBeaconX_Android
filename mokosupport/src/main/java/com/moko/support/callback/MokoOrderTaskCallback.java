package com.moko.support.callback;

import com.moko.support.task.OrderTaskResponse;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 返回数据回调类
 * @ClassPath com.moko.support.callback.OrderCallback
 */
public interface MokoOrderTaskCallback {

    void onOrderResult(OrderTaskResponse response);

    void onOrderTimeout(OrderTaskResponse response);

    void onOrderFinish();
}
