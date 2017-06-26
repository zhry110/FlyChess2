package com.zhry.like1.flychess.view;

import android.content.Context;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * Created by like1 on 2017/4/13.
 */

public class GameView extends View{
    private int childWidth,childHeight;
    private int mWidth,mHeight;
    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childHeight = childWidth = (mWidth = MeasureSpec.getSize(widthMeasureSpec)/18);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.i("w h",childHeight+" "+childWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.alert_dark_frame),0,0,new Paint());
    }
}
