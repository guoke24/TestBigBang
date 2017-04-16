package com.topwise.testbigbang;

import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.topwise.testbigbang.R;

/**
 * Created by topwise on 17-3-29.
 */

public class FenciActionBar extends ViewGroup implements View.OnClickListener {

    String TAG = "FenciActionBar";

    ImageView mSearch;
    ImageView mShare;
    ImageView mCopy;
    Drawable mBorder;
    private int mActionGap;
    private int mContentPadding;
    private ActionListener mActionListener;


    public FenciActionBar(Context context) {
        super(context);
        initSubViews();

    }

    public FenciActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubViews();
    }

    public FenciActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSubViews();
    }

    /**
     * 初始化view
     */
    private void initSubViews() {
        Context context = getContext();

        //mBorder = ContextCompat.getDrawable(context, R.drawable.bigbang_action_bar_bg);
        mBorder = context.getResources().getDrawable(R.drawable.bigbang_action_bar_bg);
        mBorder.setCallback(this);

        mSearch = new ImageView(context);
        mSearch.setImageResource(R.mipmap.bigbang_action_search);
        mSearch.setOnClickListener(this);
        mShare = new ImageView(context);
        mShare.setImageResource(R.mipmap.bigbang_action_share);
        mShare.setOnClickListener(this);
        mCopy = new ImageView(context);
        mCopy.setImageResource(R.mipmap.bigbang_action_copy);
        mCopy.setOnClickListener(this);

        addView(mSearch, createLayoutParams());
        addView(mShare, createLayoutParams());
        addView(mCopy, createLayoutParams());

        setWillNotDraw(false);

        mActionGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        mContentPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
    }

    /**
     * 创建参数
     * @return
     */
    private LayoutParams createLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        return params;
    }

    @Override
    public void onClick(View v) {
        if (mActionListener == null) {
            return;
        }
        if (v == mSearch) {
            mActionListener.onSearch();
        } else if (v == mShare) {
            mActionListener.onShare();
        } else if (v == mCopy) {
            mActionListener.onCopy();
        }
    }

    /**
     * 测量的回调函数
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG,"--->FenciActionBar.onMeasure");

        int childCount = getChildCount();
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.measure(measureSpec, measureSpec);
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG,"[onMeasure] width = "+width);
        Log.d(TAG,"[onMeasure] height = "+height);
        Log.d(TAG,"[onMeasure] setMeasuredDimension 的 height = "+height + mContentPadding + mSearch.getMeasuredHeight());

        setMeasuredDimension(width, height + mContentPadding + mSearch.getMeasuredHeight());
    }

    /**
     * 布局的回调函数
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG,"--->FenciActionBar.onLayout");

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        //Log.d(TAG,"[onLayout] width = "+width);
        //Log.d(TAG,"[onLayout] height = "+height);

        layoutSubView(mSearch, mActionGap, 0);
        layoutSubView(mShare, width - mActionGap * 2 - mShare.getMeasuredWidth() - mCopy.getMeasuredWidth(), 0);
        layoutSubView(mCopy, width - mActionGap - mCopy.getMeasuredWidth(), 0);

        Rect oldBounds = mBorder.getBounds();
        Log.d(TAG,"[onLayout] Rect height = "+height);
        Rect tempBounds = new Rect(0, mSearch.getMeasuredHeight() / 2, 0, 0);
        Rect newBounds = new Rect(0, mSearch.getMeasuredHeight() / 2, width, height);

//        if (!oldBounds.equals(newBounds)) {
//            Log.d(TAG,"[onLayout] !oldBounds.equals(newBounds)");
//            //这一步是让 选中的内容的边框出现
//            ObjectAnimator.ofObject(mBorder, "bounds", new RectEvaluator(), oldBounds, newBounds).setDuration(200).start();
//        }

        //ObjectAnimator.ofObject(mBorder, "bounds", new RectEvaluator(), oldBounds, newBounds).setDuration(200).start();
        //这里ok，所以就是上面的问题！！！
        mBorder.setBounds(newBounds);
    }

    /**
     * 给子view布局
     * @param view
     * @param l
     * @param t
     */
    private void layoutSubView(View view, int l, int t) {
        view.layout(l, t, view.getMeasuredWidth() + l, view.getMeasuredHeight() + t);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBorder.draw(canvas);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mBorder;
    }

    public int getContentPadding() {
        return mContentPadding;
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    interface ActionListener {
        void onSearch();
        void onShare();
        void onCopy();
    }

}
