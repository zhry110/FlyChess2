package com.zhry.like1.flychess.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhry.like1.flychess.R;

/**
 * Created by like1 on 2017/6/9.
 */

public class Tip extends PopupWindow {
    private ImageView vx,vok;
    public Tip(Activity activity, String text, int width ,int height , View.OnClickListener x, View.OnClickListener ok)
    {
        super(activity.getLayoutInflater().inflate(R.layout.tip,null),width-10,height);
        vok = (ImageView) getContentView().findViewById(R.id.imageView4);
        vx = (ImageView) getContentView().findViewById(R.id.linearLayout);
        vok.setOnClickListener(ok);
        vx.setOnClickListener(x);
        ((TextView)getContentView().findViewById(R.id.textView)).setText(text);
    }
    public Tip(Activity activity, String text, int width ,int height , View.OnClickListener x, View.OnClickListener ok,int more)
    {
        super(activity.getLayoutInflater().inflate(R.layout.winner,null),width-10,height);
        vok = (ImageView) getContentView().findViewById(R.id.imageView4);
        vx = (ImageView) getContentView().findViewById(R.id.linearLayout);
        vok.setOnClickListener(ok);
        vx.setOnClickListener(x);
        ((TextView)getContentView().findViewById(R.id.winners)).setText(text);
    }
    public void show(View root)
    {
        showAtLocation(root, Gravity.CENTER,0,0);
    }
}
