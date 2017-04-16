package com.topwise.fenciinone.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.topwise.fenciinone.R;
import com.topwise.fenciinone.service.ListenClipboardService;
import com.topwise.fenciinone.widget.FloatingView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    EditText textEdit;
    Button fenciButton;
    Button listenClip;
    Button stopListenClip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView(){
        textEdit = (EditText) findViewById(R.id.text_edit);
        textEdit.setSelection(textEdit.getText().length());

        fenciButton = (Button) findViewById(R.id.fenci_bt);

        listenClip = (Button) findViewById(R.id.listenClip);
        stopListenClip = (Button) findViewById(R.id.stop_listenClip);

        //初始化最后执行
        initListener();
    }

    private void initListener(){
        fenciButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToFenci();
            }
        });
        listenClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListenClipService();
            }
        });
        stopListenClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopListenClip();
            }
        });
    }

    /**
     * 手动开启监听服务
     */
    private void startListenClipService(){
        ListenClipboardService.start(this,null);
    }

    public void toast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }


    private void stopListenClip(){
        ListenClipboardService.stop(this);
    }

    /**
     * 跳转到分词界面
     */
    private void jumpToFenci(){
        if (textEdit.getText().toString().equals("")|| TextUtils.isEmpty(textEdit.getText())){
            return;
        }

        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("fenci://?extra_text=" +
                                    /*对文字进行utf-8编码*/
                            URLEncoder.encode(textEdit.getText().toString(), "utf-8"))));

            /**
             * AndroidManifest.xml中BigBangActivity的filter设置了：
             * <action android:name="android.intent.action.VIEW"/>
             * <category android:name="android.intent.category.DEFAULT"/>
             * <category android:name="android.intent.category.BROWSABLE"/>
             * <data android:scheme="fenci"/>
             *
             * 给intent传入uri就是为了匹配后三者
             * 另一边的intent通过getQueryParameter(EXTRA_TEXT)来取得文本
             */
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}