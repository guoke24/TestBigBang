/*
 * The MIT License (MIT)
 * Copyright (c) 2016 baoyongzhang <baoyz94@gmail.com>
 */
package com.topwise.fenciinone.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.topwise.fenciinone.widget.FloatingView;


/**
 * 这个服务需要启动
 */
public final class ListenClipboardService extends Service {

    private ClipboardManager mClipboardManager;

    private FloatingView mFloatingView;

    /**
     *
     * @param context
     */
    public static void start(Context context,ClipboardManager.OnPrimaryClipChangedListener l) {

        //mOnPrimaryClipChangedListener = l;
        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        context.startService(serviceIntent);
    }

    /**
     *
     * @param context
     */
    public static void stop(Context context) {
        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        context.stopService(serviceIntent);
    }


    //private static ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener ;
    /**
     * 实现监听剪贴板的回调接口，一有改变就显示浮动按钮
     */
    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            showAction();
        }
    };
    /**
     * 此处是显示浮动的button
     */
    private void showAction() {
        ClipData primaryClip = mClipboardManager.getPrimaryClip();
        if (primaryClip != null
                && primaryClip.getItemCount() > 0
                // 跟这里呼应？ service.setPrimaryClip(ClipData.newPlainText("Fenci", text));
                && !"Fenci".equals(primaryClip.getDescription().getLabel())
                ) {
            //获取复制的文字
            CharSequence text = primaryClip.getItemAt(0).coerceToText(this);
            if (text != null) {
                mFloatingView.setText(text.toString());
                mFloatingView.show();
            }
        }
    }

    /**
     * 通过调用start函数来启动服务
     * 启动服务的时候就添加剪贴板监听函数
     * 同时创建浮动按钮
     */
    @Override
    public void onCreate() {
        mFloatingView = new FloatingView(this);
        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (mOnPrimaryClipChangedListener!=null){
            //添加监听函数前先移除
            //每次onCreate，监听函数对象都是新的了。移除就得无效
            //mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
            mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        }

        toast_static("已开启监听剪贴板服务",this);
    }



    /**
     * 被销毁的时候，就移除剪贴板监听函数
     * 所以如果不是系统服务，该功能不是常驻功能
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        toast_static("已停止监听剪贴板服务",this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 弹窗一下文字
     * @param s
     */
    public void toast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    public static void toast_static(String s,Context context){
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
}