package com.topwise.testbigbang;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.topwise.testbigbang.R;

public class MainActivity extends Activity {

    EditText textEdit;
    Button fenciButton;

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


        //初始化最后执行
        initListener();
    }

    private void initListener(){
        fenciButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToFenci();
                //fenciTest(textEdit.getText().toString());
            }
        });
    }

    /**
     * 跳转到分词界面
     */
    private void jumpToFenci(){
        if (textEdit.getText().toString().equals("")|| TextUtils.isEmpty(textEdit.getText())){
            return;
        }
//        Intent intent = new Intent("android.intent.action.fenci");
//        intent.addCategory("android.intent.category.DEFAULT");
//        intent.putExtra("text",textEdit.getText().toString());
//        //启动分词界面
//        startActivity(intent);

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

    /**
     * 测试分词功能
     * @param
     */
//    void fenciTest(String indexStr){
//        Log.d("guohao","fenciTest");
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
}
