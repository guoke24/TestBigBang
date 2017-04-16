package com.topwise.testbigbang;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import com.topwise.testbigbang.R;
/**
 * Created by topwise on 17-3-29.
 */

public class FenciLayout extends ViewGroup implements FenciActionBar.ActionListener {

    private int mLineSpace;
    private int mItemSpace;
    private int mItemTextSize;
    private Item mTargetItem;
    private List<Line> mLines;
    private int mActionBarTopHeight;
    private int mActionBarBottomHeight;
    private FenciActionBar mActionBar;
    private AnimatorListenerAdapter mActionBarAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mActionBar.setVisibility(View.GONE);
        }
    };
    private ActionListener mActionListener;
    private int mScaledTouchSlop;
    private float mDownX;
    private boolean mDisallowedParentIntercept;

    public FenciLayout(Context context) {
        super(context);
        readAttribute(null);
    }

    public FenciLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttribute(attrs);
    }

    public FenciLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttribute(attrs);
    }

    public FenciLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        readAttribute(attrs);
    }

    /**
     * 读取设置属性
     * @param attrs
     */
    private void readAttribute(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BigBangLayout);
            mItemSpace = typedArray.getDimensionPixelSize(R.styleable.BigBangLayout_itemSpace, getResources().getDimensionPixelSize(R.dimen.big_bang_default_item_space));
            mLineSpace = typedArray.getDimensionPixelSize(R.styleable.BigBangLayout_lineSpace, getResources().getDimensionPixelSize(R.dimen.big_bang_default_line_space));
            mItemTextSize = typedArray.getDimensionPixelSize(R.styleable.BigBangLayout_textSize, getResources().getDimensionPixelSize(R.dimen.big_bang_default_text_size));
            typedArray.recycle();
            mActionBarBottomHeight = mLineSpace;
            mActionBarTopHeight = getResources().getDimensionPixelSize(R.dimen.big_bang_action_bar_height);
        }

        // TODO 暂时放到这里
        mActionBar = new FenciActionBar(getContext());
        mActionBar.setVisibility(View.GONE);
        mActionBar.setActionListener(this);

        addView(mActionBar, 0);

        setClipChildren(false);

        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if (isInEditMode()) {
            // 测试预览数据
            addTextItem("BigBang");
            addTextItem("是");
            addTextItem("一个");
            addTextItem("非常");
            addTextItem("实用");
            addTextItem("的");
            addTextItem("功能");
            addTextItem("！");
        }
    }

    /**
     * 清除所有的行，每一行是一个child的View
     */
    public void reset() {

        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (mActionBar == child) {
                mActionBar.setVisibility(View.GONE);
                continue;
            }
            removeView(child);
        }
    }

    /**
     *测量自己的大小和子视图的大小
     * @param widthMeasureSpec 父视图的宽度
     * @param heightMeasureSpec 父视图的高度
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("guohao","--->onMeasure");
        //这个宽度是mActionBar的宽度，=父视图的宽减去左右内间距
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        //这个宽度是文字内容区域的宽度
        int contentWidthSize = widthSize - mActionBar.getContentPadding();
        //int contentWidthSize = widthSize ;
        int heightSize = 0;
        //分词块的数量
        int childCount = getChildCount();

        //这个方法的主要作用就是根据你提供的大小和模式，返回你想要的大小值，这个里面根据传入模式的不同来做相应的处理。
        //简单的说，就是得到一个和合成值
        //但是现在这个值意味着什么，还不知道？
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        mLines = new ArrayList<>();
        Line currentLine = null;
        int currentLineWidth = contentWidthSize;
        //--start-- 遍历子视图
        for (int i = 0; i < childCount; i++) {
            //每次取出一个子view
            View child = getChildAt(i);
            //mActionBar也是子视图之一
            if (mActionBar == child) {
                //如果取到的子view 是 mActionBar，不做处理，跳到下一次循环
                continue;
            }
            //测量每一个子视图的大小，由其他地方可知，child是一个TextView类，文字长度不固定，所以给不了准确值
            child.measure(measureSpec, measureSpec);
            //增加当前行的宽度，增量为子视图的间隔
            if (currentLineWidth > 0) {
                currentLineWidth += mItemSpace;
            }
            //增加当前行的宽度，增量为子视图的宽
            currentLineWidth += child.getMeasuredWidth();

            //--start-- 判断是否需要增加一行
            //若行数为0.或者当前行的宽度超过contentWidthSize，就增加一行
            if (mLines.size() == 0 || currentLineWidth > contentWidthSize) {
                //增加高度,child.getMeasuredHeight()得到的是0吗？
                //Log.d("guohao","onMeasure -->child.getMeasuredHeight()="+child.getMeasuredHeight());
                heightSize += child.getMeasuredHeight();
                //更新当前行的宽度，由于是新的一行，宽度就等于当前的子视图的宽度
                currentLineWidth = child.getMeasuredWidth();
                //创建新的行，分配一个下标
                currentLine = new Line(mLines.size());
                //添加行对象到集合
                mLines.add(currentLine);
            }
            //--end-- 判断是否需要增加一行

            //创建新的分词块
            Item item = new Item(currentLine);
            item.view = child;
            item.index = i;
            item.width = child.getMeasuredWidth();
            item.height = child.getMeasuredHeight();
            currentLine.addItem(item);
        }
        //--end-- 遍历子视图

        Line firstSelectedLine = findFirstSelectedLine();
        Line lastSelectedLine = findLastSelectedLine();
        if (firstSelectedLine != null && lastSelectedLine != null) {

            int selectedLineHeight = (lastSelectedLine.index - firstSelectedLine.index + 1) *
                    (firstSelectedLine.getHeight() + mLineSpace);
            Log.d("guohao","计算出mActionBar的高度 selectedLineHeight = "+selectedLineHeight);

            //测量mActionBar的大小
            mActionBar.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(selectedLineHeight, MeasureSpec.UNSPECIFIED));
        }
        //精确计算出文字区域的值
        int size = heightSize + getPaddingTop() + getPaddingBottom() +
                (mLines.size() - 1) * mLineSpace + mActionBarTopHeight + mActionBarBottomHeight;
        //宽跟父视图一致，高是精确值
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
    }

    /**
     * 给子视图设置布局，但是没给自己设置布局。。。
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("guohao","--->onLayout");
        int top;
        int left;
        int offsetTop;

        //选中的最下面一行
        Line lastSelectedLine = findLastSelectedLine();

        //选中的最上面一行
        Line firstSelectedLine = findFirstSelectedLine();

        //--start-- 遍历每一个行，让他们给自己设置布局
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            List<Item> items = line.getItems();
            left = getPaddingLeft() + mActionBar.getContentPadding();

            //--start-- 计算top的偏移量
            if (firstSelectedLine != null && firstSelectedLine.index > line.index) {
                //如果当前行在选中的第一行的上面，则向上偏移
                offsetTop = -mActionBarTopHeight;
            } else if (lastSelectedLine != null && lastSelectedLine.index < line.index) {
                //如果当前行在选中的最下面一行的下面面，则向下偏移
                offsetTop = mActionBarBottomHeight;
            } else {
                offsetTop = 0;
            }
            //--end-- 计算top的偏移量

            //--start-- 遍历每一个分词块，让他们给自己设置布局和动画
            for (int j = 0; j < items.size(); j++) {
                Item item = items.get(j);
                top = getPaddingTop() + i * (item.height + mLineSpace) + offsetTop + mActionBarTopHeight;
                View child = item.view;
                int oldTop = child.getTop();
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                if (oldTop != top) {
                    //如果就的top边距跟旧的不一样，就产生动画
                    int translationY = oldTop - top;
                    child.setTranslationY(translationY);
                    child.animate().translationYBy(-translationY).setDuration(200).start();
                }
                left += child.getMeasuredWidth() + mItemSpace;
            }
            //--end-- 遍历每一个分词块，让他们给自己设置布局和动画
        }
        //--end-- 遍历每一个行，让他们给自己设置布局

        //设置mActionBar的布局和动画
        if (firstSelectedLine != null && lastSelectedLine != null) {

            mActionBar.setVisibility(View.VISIBLE);
            mActionBar.setAlpha(1);

            //oldTop 似乎没获得正确的值
            //int oldTop = mActionBar.getTop();
            int oldTop = oldActionBarTop;
            Log.d("guohao","[onLayout] oldTop = "+oldTop);

            //计算出actionBar新的top的值，选中的第一行行号 *（行高度+行间距）+顶部的间距
            int actionBarTop = firstSelectedLine.index * (firstSelectedLine.getHeight() + mLineSpace) + getPaddingTop();
            Log.d("guohao","[onLayout] actionBarTop = "+actionBarTop);
            Log.d("guohao","[onLayout] getPaddingTop() = "+getPaddingTop());
            Log.d("guohao","[onLayout] getPaddingLeft() = "+getPaddingLeft());
            Log.d("guohao","[onLayout] mActionBar.getMeasuredWidth() = "+mActionBar.getMeasuredWidth());
            Log.d("guohao","[onLayout] mActionBar.getMeasuredHeight() = "+mActionBar.getMeasuredHeight());

            //这个是指选中的那部分的布局吗?是的
            // 参数：左边，顶部，右边，底部
            // 第一 L 控件左边离容器左边的距离
            // 第二 T 控件顶部离容器顶部的距离
            // 第三 R 控件右边离容器左边的距离
            // 第四 B 控件底部离容器顶部的距离
            mActionBar.layout(getPaddingLeft(),
                    actionBarTop,
                    getPaddingLeft() + mActionBar.getMeasuredWidth(),
                    actionBarTop + mActionBar.getMeasuredHeight());

            //如果新旧的高度不一样，就发生动画
            if (oldTop != actionBarTop) {
                int translationY = oldTop - actionBarTop;
                mActionBar.setTranslationY(translationY);
                Log.d("guohao-1","animate translationYBy = "+(-translationY));
                //让mActionBar上下移动的动画
                mActionBar.animate().translationYBy(-translationY).setDuration(200).start();
            }

            //记录旧的top值
            oldActionBarTop = actionBarTop;

        } else {
            if (mActionBar.getVisibility() == View.VISIBLE) {
                Log.d("guohao","消失的动画");
                mActionBar.animate().alpha(0).setDuration(200).setListener(mActionBarAnimationListener).start();
            }
        }
    }

    int oldActionBarTop = 0;

    /**
     * 找到选中的最后一行
     * @return
     */
    private Line findLastSelectedLine() {
        Line result = null;
        for (Line line : mLines) {
            if (line.hasSelected()) {
                result = line;
            }
        }
        if (result != null){
            //Log.d("guohao","选中的最后一行行号 = "+result.index);
        }else{
            //Log.d("guohao","选中的最后一行行号 无");
        }
        return result;
    }

    /**
     * 找到选中的第一行
     * @return
     */
    private Line findFirstSelectedLine() {
        for (Line line : mLines) {
            if (line.hasSelected()) {
                //Log.d("guohao","选中的第一行行号 = "+line.index);
                return line;
            }
        }
        return null;
    }

    /**
     * 找到分词快
     * @param x
     * @param y
     * @return
     */
    private Item findItemByPoint(int x, int y) {
        for (Line line : mLines) {
            List<Item> items = line.getItems();
            for (Item item : items) {
                if (item.getRect().contains(x, y)) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * 添加一个词
     * @param text
     */
    public void addTextItem(String text) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setBackgroundResource(R.drawable.item_background);
        //view.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.bigbang_item_text));
        view.setGravity(Gravity.CENTER);
        if (mItemTextSize > 0) view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mItemTextSize);
        //调用了ViewGroup的函数
        addView(view);
    }

    /**
     * 重写onTouchEvent，定义了选中分词块的逻辑
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDisallowedParentIntercept = false;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                if (!mDisallowedParentIntercept && Math.abs(x - mDownX) > mScaledTouchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mDisallowedParentIntercept = true;
                }
                //根据x,y轴找到对应的分词块
                Item item = findItemByPoint(x, (int) event.getY());

                if (mTargetItem != item) {
                    mTargetItem = item;
                    if (item != null) {
                        //状态反选
                        item.setSelected(!item.isSelected());
                        ItemState state = new ItemState();
                        state.item = item;
                        state.isSelected = item.isSelected();
                        //这是干嘛的还不懂
                        if (mItemState == null) {
                            mItemState = state;
                        } else {
                            state.next = mItemState;
                            mItemState = state;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mItemState != null) {
                    ItemState state = mItemState;
                    while (state != null) {
                        state.item.setSelected(!state.isSelected);
                        state = state.next;
                    }
                }
            case MotionEvent.ACTION_UP:
                requestLayout();
                invalidate();
                mTargetItem = null;
                if (mDisallowedParentIntercept) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                mItemState = null;
                break;
        }
        return true;
    }



    ItemState mItemState;

    @Override
    public void onSearch() {

    }

    @Override
    public void onShare() {

    }

    @Override
    public void onCopy() {

    }

    /**
     * 每一个分词块的状态
     */
    class ItemState {
        Item item;
        boolean isSelected;
        ItemState next;
    }

    /**
     * 每一行
     */
    static class Line {
        int index;
        List<Item> items;

        public Line(int index) {
            this.index = index;
        }

        void addItem(Item item) {
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(item);
        }

        List<Item> getItems() {
            return items;
        }

        boolean hasSelected() {
            for (Item item : items) {
                if (item.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        int getHeight() {
            List<Item> items = getItems();
            if (items != null && items.size() > 0) {
                return items.get(0).view.getMeasuredHeight();
            }
            return 0;
        }

        String getSelectedText() {
            StringBuilder builder = new StringBuilder();
            List<Item> items = getItems();
            if (items != null && items.size() > 0) {
                for (Item item : items) {
                    if (item.isSelected()) {
                        builder.append(item.getText());
                    }
                }
            }
            return builder.toString();
        }

    }

    /**
     * 分词块
     */
    static class Item {
        Line line;
        int index;
        int height;
        int width;
        View view;

        public Item(Line line) {
            this.line = line;
        }

        Rect getRect() {
            Rect rect = new Rect();
            view.getHitRect(rect);
            return rect;
        }
        boolean isSelected() {
            return view.isSelected();
        }

        void setSelected(boolean selected) {
            view.setSelected(selected);
        }

        CharSequence getText() {
            return ((TextView) view).getText();
        }
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    /**
     * Action Listener
     */
    public interface ActionListener {
        void onSearch(String text);

        void onShare(String text);

        void onCopy(String text);
    }
}
