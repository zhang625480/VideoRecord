package com.zhang625480.video.record.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.VideoView;

/**
 * 自定义VideoView，解决全屏显示不全的问题
 *
 */
public class MyVideoView extends VideoView
{
    public MyVideoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    public MyVideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public MyVideoView(Context context)
    {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    @Override
    public void onHoverChanged(boolean hovered) {
        super.onHoverChanged(hovered);
    }
}
