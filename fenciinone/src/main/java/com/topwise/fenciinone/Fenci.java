package com.topwise.fenciinone;

import android.content.Context;
import android.support.annotation.StringDef;

import com.topwise.fenciinone.action.Action;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by topwise on 17-4-2.
 */

public class Fenci {

    public static final String ACTION_SEARCH = "search";
    public static final String ACTION_SHARE = "share";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_BACK = "back";

    @StringDef({ACTION_SEARCH, ACTION_SHARE, ACTION_COPY, ACTION_BACK})
    @Retention(SOURCE)
    public @interface ActionType {

    }

    private static Map<String, Action> mActionMap = new HashMap<>();

    /**
     * 注册Action
     * @param type
     * @param action
     */
    public static void registerAction(@ActionType String type, Action action) {
        mActionMap.put(type, action);
    }

    public static void unregisterAction(@ActionType String type) {
        mActionMap.remove(type);
    }

    public static Action getAction(@ActionType String type) {
        return mActionMap.get(type);
    }

    /**
     * 启动action的统一入口
     * @param context
     * @param type
     * @param text
     */
    public static void startAction(Context context, @ActionType String type, String text) {
        Action action = Fenci.getAction(type);
        if (action != null) {
            action.start(context, text);
        }
    }



}
