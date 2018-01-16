package com.moko.support.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class BaseMessageHandler<T> extends Handler {
    private WeakReference<T> reference;

    public BaseMessageHandler(T t) {
        super(Looper.getMainLooper());
        reference = new WeakReference<>(t);
    }

    @Override
    public void handleMessage(Message msg) {
        if (reference.get() == null) {
            return;
        }
        handleMessage(reference.get(), msg);
    }

    protected abstract void handleMessage(T t, Message msg);
}
