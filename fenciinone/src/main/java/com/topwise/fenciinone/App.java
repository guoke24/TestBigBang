package com.topwise.fenciinone;

import android.app.Application;
import android.util.Log;

import com.topwise.fenciinone.action.BaiduSearchAction;
import com.topwise.fenciinone.action.CopyAction;
import com.topwise.fenciinone.action.ShareAction;
import com.topwise.fenciinone.service.ListenClipboardService;

/**
 * Created by topwise on 17-4-2.
 */

public class App extends Application {

    /**
     * 这里才是应用程序的真正入口
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("guohao","[app.onCreate]");
        //在此处配置百度查找Action
        Fenci.registerAction(Fenci.ACTION_SEARCH, BaiduSearchAction.create());
        //在此处配置复制Action
        Fenci.registerAction(Fenci.ACTION_COPY, CopyAction.create());
        //在此处配置分享Action
        Fenci.registerAction(Fenci.ACTION_SHARE, ShareAction.create());
        //启动监听剪贴板服务
        //ListenClipboardService.start(this);

    }
}
