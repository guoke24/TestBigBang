package com.topwise.testbigbang;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.topwise.testbigbang.R;

import com.topwise.fenci_ik.IKSegmenterParser;

import com.topwise.fenci_lib.HandlerCallback;
import com.topwise.fenci_lib.NetworkParser;
import com.topwise.fenci_lib.SimpleParser;

//import java.io.IOException;
//import java.io.StringReader;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.Token;
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class FenciActivity extends Activity {

    public static final String EXTRA_TEXT = "extra_text";

    //持有一个Fencilayout
    private FenciLayout fenciLayout;

    //先使用测试的文本
    String[] testWords = {"哈哈","嘿嘿","哈哈","嘿嘿","哈哈","嘿嘿"};

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fenci);
        initView();
        handleIntent(getIntent());
    }

    private void initView(){
        fenciLayout = (FenciLayout) findViewById(R.id.fenci_layout);
    }

    /**
     * 处理传过来的文本
     * @param intent
     */
    private void handleIntent(Intent intent){
        Uri data = intent.getData();
        if (data != null) {
            String text = data.getQueryParameter(EXTRA_TEXT);

            if (TextUtils.isEmpty(text)) {
                finish();
                return;
            }

            //toast(text);
            //目前没有分词功能，只能全部拆分为一个一个字
//            char[] cs = text.toCharArray();
//            for (char c :cs){
//                //Log.d("guohao", String.valueOf(c));
//
//                fenciLayout.addTextItem(String.valueOf(c));
//            }

            //在此添加分词
            //先重置
//            fenciLayout.reset();
//            for (String s:testWords){
//                //每次add之后都会调用invalidate函数来重绘
//                fenciLayout.addTextItem(s);
//            }

            parser = getSegmentParser();
            parser.parse(text, new HandlerCallback<String[]>() {
                @Override
                public void onFinish(String[] result) {
                    fenciLayout.reset();
                    for (String str : result) {
                        fenciLayout.addTextItem(str);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(FenciActivity.this, "分词出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    SimpleParser parser;
    public SimpleParser getSegmentParser() {
        SimpleParser sParser;

            //默认采用网络分词
            //sParser = new NetworkParser();
            //采用第三方分词
            sParser = new IKSegmenterParser((Application) getApplicationContext());

        return sParser;
    }

    /**
     * 测试分词功能
     * @param
     */
//    void fenciTest(String indexStr){
//        if (indexStr.equals("")||indexStr==null) return;
//
//        Analyzer analyzer = new StandardAnalyzer();
//        //String  indexStr = "我的QQ号码是58472399";
//        StringReader reader = new StringReader(indexStr);
//        TokenStream ts = analyzer.tokenStream(indexStr, reader);
//        Token t = null;
//        try {
//            t = ts.next();
//            while (t != null) {
//                Log.d("guohao",t.termText());
//                t = ts.next();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * 弹窗显示文本
     * @param s
     */
    void toast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}
