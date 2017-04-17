package com.topwise.fenciinone.ui;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.topwise.fenciinone.Fenci;
import com.topwise.fenciinone.R;
import com.topwise.fenciinone.fencibase.HandlerCallback;
import com.topwise.fenciinone.fencibase.SimpleParser;

import com.topwise.fenciinone.parserlocal_ik.parser.IKSegmenterParser;
import com.topwise.fenciinone.parsernetwork.NetworkParser;



public class FenciActivity extends AppCompatActivity implements FenciLayout.ActionListener{

    public static final String EXTRA_TEXT = "extra_text";

    //持有一个Fencilayout
    private FenciLayout fenciLayout;

    //切换切分算法的按钮
    Button switchBt;

    //控制标点符号是否出现的按钮
    Button pointBt;

    //网络或本地分词
    Button localAndNetBt;

    //控制空格是否出现的按钮
    Button spaceBt;

    /**
     * 网络智能切分的结果
     */
    String[] netSmartResult;
    /**
     * 本地智能切分的结果
     */
    String[] localSmartResult;

    /**
     * 智能切分的结果
     */
    String[] smartResult;

    /**
     * 未处理的文本
     */
    String rawContent;

    /**
     * 一些控制变量
     */
    boolean isSmart = true;
    boolean isShowPoint = true;
    boolean isNet = false;
    boolean isSpace = false;

    /**
     * 是否第一次加载的flag
     */
    boolean isFisteLaunch = true;

    ActionBar actionBar;
    MenuItem cutMenu;
    MenuItem pointMenu;
    MenuItem blankMenu;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fenci);
        //得到actionBar，注意我的是V7包，使用getSupportActionBar()
        actionBar = getSupportActionBar();
        //让actionBar显示自定义的logo
        //actionBar.setLogo(R.mipmap.ic_launcher_plus);
        //actionBar.setTitle("");
        //actionBar.setDisplayUseLogoEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(true);
        initView();
        handleIntent(getIntent());
    }


    /**
     * 加载menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 实例化 menuItem
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        cutMenu = menu.findItem(R.id.action_cut);
        pointMenu = menu.findItem(R.id.action_point);
        blankMenu = menu.findItem(R.id.action_blank);
        return true;
    }

    /**
     * 点击menuItem的响应函数
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cut:
                onCutMenuItem();
                return true;
            case R.id.action_point:
                onPointMenuItemClick();
                return true;
            case R.id.action_blank:
                onBlankMenuItemClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //清理掉分词结果
        netSmartResult = null;
        localSmartResult = null;
        smartResult = null;
        rawContent = null;
    }

    /**
     * 初始化组件和监听函数
     */
    private void initView(){
        fenciLayout = (FenciLayout) findViewById(R.id.fenci_layout);
        fenciLayout.setActionListener(this);
        switchBt = (Button) findViewById(R.id.switch_bt);
        //add by guohao 20170417 先取消按钮显示
        switchBt.setVisibility(View.GONE);
        switchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSmart){
                    isSmart = false;
                    switchBt.setText(getString(R.string.single_cut));
                }else{
                    isSmart = true;
                    switchBt.setText(getString(R.string.smart_cut));
                }
                reFreshLayout();
            }
        });

        pointBt = (Button) findViewById(R.id.point_bt);
        //add by guohao 20170417 先取消按钮显示
        pointBt.setVisibility(View.GONE);
        pointBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowPoint){
                    isShowPoint = false;
                    pointBt.setText(getString(R.string.hide_punctuation));
                }else{
                    isShowPoint = true;
                    pointBt.setText(getString(R.string.show_punctuation));
                }
                reFreshLayout();
            }
        });

        localAndNetBt = (Button) findViewById(R.id.local_net_bt);
        //add by guohao 20170417 先取消网络分词的选项
        localAndNetBt.setVisibility(View.GONE);
        localAndNetBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNet){
                    isNet = false;
                    localAndNetBt.setText(getString(R.string.local_dict));
                }else{
                    isNet = true;
                    localAndNetBt.setText(getString(R.string.net_dict));

                }
                onLocalAndNetChanged();
            }
        });

        spaceBt = (Button) findViewById(R.id.space_bt);
        //add by guohao 20170417 先取消按钮显示
        spaceBt.setVisibility(View.GONE);
        spaceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpace){
                    isSpace = false;
                    spaceBt.setText(getString(R.string.hide_blank));
                }else{
                    isSpace = true;
                    spaceBt.setText(getString(R.string.show_punctuation));
                }
                reFreshLayout();
            }
        });
    }

    /**
     * 点击了 smart/single msnuitem
     */
    void onCutMenuItem(){
        if (isSmart){
            isSmart = false;
            cutMenu.setTitle(R.string.single_cut);
            cutMenu.setIcon(R.drawable.single);
            switchBt.setText(getString(R.string.single_cut));
        }else{
            isSmart = true;
            cutMenu.setTitle(R.string.smart_cut);
            cutMenu.setIcon(R.drawable.smart);
            switchBt.setText(getString(R.string.smart_cut));
        }
        reFreshLayout();
    }

    /**
     * 点击了切换标点显示的item
     */
    void onPointMenuItemClick(){
        if (isShowPoint){
            isShowPoint = false;
            pointMenu.setIcon(R.drawable.point_hide);
            pointBt.setText(getString(R.string.hide_punctuation));
        }else{
            isShowPoint = true;
            pointMenu.setIcon(R.drawable.point_show);
            pointBt.setText(getString(R.string.show_punctuation));
        }
        reFreshLayout();
    }

    /**
     * 点击切换空格显示的item
     */
    void onBlankMenuItemClick(){
        if (isSpace){
            isSpace = false;
            blankMenu.setTitle(getString(R.string.show_blank));
            spaceBt.setText(getString(R.string.hide_blank));
        }else{
            isSpace = true;
            blankMenu.setTitle(getString(R.string.hide_blank));
            spaceBt.setText(getString(R.string.show_punctuation));
        }
        reFreshLayout();
    }

    /**
     * 注意！这是异步操作，所以更新ui只能放在回调函数里
     */
    void onLocalAndNetChanged(){
        if (isNet){
            //采用网络分词
            if (netSmartResult==null){
                //还没有缓存网络分词结果
                parser = getNetSegmentParser();
                if (rawContent == null) return;
                parser.parse(rawContent, new HandlerCallback<String[]>() {
                    @Override
                    public void onFinish(String[] result) {
                        netSmartResult = result;
                        smartResult = netSmartResult;
                        reFreshLayout();
                    }

                    @Override
                    public void onError(Exception e) {
                        closeWaitingDialog();
                        Toast.makeText(FenciActivity.this, "网络分词出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //已经缓存分词结果
                smartResult = netSmartResult;
                reFreshLayout();
            }
        }else{
            //采用本地分词
            smartResult = localSmartResult;
            reFreshLayout();
        }
    }

    /**
     * 更新分词的显示
     * 该函数依赖的分词结果有两个变量：smartResult，rawContent
     * 依赖其他开关的变量：isSmart，isShowPoint，isSpace
     *
     */
    private void reFreshLayout(){
        if (isSmart){
            //采用智能分词策略
            if (smartResult != null){
                fenciLayout.reset();
                for (String str : smartResult) {
                    if (!isShowPoint){
                        //如果不显示标点，才进入这
                        if(isPointChar(str)&&!str.equals(" ")){
                            //如果是标点且不是空格,跳过本次循环
                            continue;
                        }
                    }
                    if (!isSpace){
                        //如果不显示空格，才进入这里
                        if (str.equals(" ")){
                            continue;
                        }
                    }
                    fenciLayout.addTextItem(str);
                }

                switchBt.setText(getString(R.string.smart_cut));
            }
        }else{
            //采用最细粒度分词策略
            if (rawContent != null){
                fenciLayout.reset();
                char[] cs = rawContent.toCharArray();
                for (char c :cs){
                    if (!isShowPoint){
                        //如果不显示标点，才进入这
                        if(isPointChar(String.valueOf(c))){
                            //如果是标点,跳过本次循环
                            continue;
                        }
                    }
                    if (!isSpace){
                        //如果不显示空格，才进入这里
                        if (Character.isSpace(c)){
                            continue;
                        }
                    }
                    fenciLayout.addTextItem(String.valueOf(c));
                }
                switchBt.setText(getString(R.string.single_cut));
            }
        }
    }

    /**
     * 判断一个字符是否是标点
     * @param s
     * @return
     */
    private boolean isPointChar(String s){
        if (s.length()!=1){
            return false;
        }
        int b = CharacterUtil.identifyCharType(s.charAt(0));
        Log.d("guohao-414",s.charAt(0)+" 是否字符 = "+b);
        return (b==0);//标点和空格为0
    }

    /**
     * 处理传过来的文本
     * @param intent
     */
    private void handleIntent(Intent intent){

        showWaitingDialog();
        Uri data = intent.getData();
        if (data != null) {
            String text = data.getQueryParameter(EXTRA_TEXT);
            rawContent = text;
            if (TextUtils.isEmpty(text)) {
                finish();
                return;
            }
            
            parser = getSegmentParser();
            parser.parse(text, new HandlerCallback<String[]>() {
                @Override
                public void onFinish(String[] result) {
                    localSmartResult = result;
                    smartResult = localSmartResult;
                    closeWaitingDialog();
//                    fenciLayout.reset();
//                    for (String str : result) {
//                        fenciLayout.addTextItem(str);
//                    }
                    reFreshLayout();
                }

                @Override
                public void onError(Exception e) {
                    closeWaitingDialog();
                    Toast.makeText(FenciActivity.this, "分词出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    SimpleParser parser;

    /**
     * 获取本地IK分词接口
     * @return
     */
    public SimpleParser getSegmentParser() {
        SimpleParser sParser;

            //默认采用网络分词
            //如果有网络！采用网络分词解析器
            //if ()
            //sParser = new NetworkParser();
            //采用第三方分词:TK的分词
            sParser = new IKSegmenterParser((Application) getApplicationContext());

        return sParser;
    }

    /**
     * 获取网络分词接口
     * @return
     */
    public SimpleParser getNetSegmentParser(){
        SimpleParser sParser;
        sParser = new NetworkParser();
        return sParser;
    }

    ProgressDialog waitingDialog;

    private void showWaitingDialog() {
        if (!isFisteLaunch) return;

    /* 等待Dialog具有屏蔽其他控件的交互能力
     * @setCancelable 为使屏幕不可点击，设置为不可取消(false)
     * 下载等事件完成后，主动调用函数关闭该Dialog
     */
        if (waitingDialog == null){
            waitingDialog= new ProgressDialog(FenciActivity.this);
        }

        waitingDialog.setTitle("分词ing");
        waitingDialog.setMessage("等待中...");
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);
        waitingDialog.show();
    }

    private void closeWaitingDialog(){
        if (waitingDialog != null){
            waitingDialog.cancel();
        }
    }

    /**
     * 弹窗显示文本
     * @param s
     */
    void toast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearch(String text) {
        Fenci.startAction(this, Fenci.ACTION_SEARCH, text);
    }

    @Override
    public void onShare(String text) {
        Fenci.startAction(this, Fenci.ACTION_SHARE, text);
    }

    @Override
    public void onCopy(String text) {
        Fenci.startAction(this, Fenci.ACTION_COPY, text);
    }
}
