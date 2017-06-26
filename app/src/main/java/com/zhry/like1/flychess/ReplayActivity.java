package com.zhry.like1.flychess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhry.like1.flychess.listener.JoinRoomLinstener;
import com.zhry.like1.flychess.sqlite.GameDatabase;
import com.zhry.like1.flychess.view.Tip;

/**
 * Created by like1 on 2017/6/19.
 */

public class ReplayActivity extends Activity {
    private LinearLayout replaysWraper;
    private Tip tip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replays);
        replaysWraper = (LinearLayout) findViewById(R.id.replay_wraper);
        LinearLayout[] times = GameDatabase.getAllGames(this);
        if (times != null)
            for (LinearLayout l : times) {
                l.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long time = (long) v.getTag();
                        RoomActivity.replay = true;
                        Intent i = new Intent();
                        i.putExtra("time",time);
                        System.out.println("src time:"+time);
                        RoomActivity.setLocalServer(null);
                        i.setClass(ReplayActivity.this, RoomActivity.class);
                        ReplayActivity.this.startActivity(i);
                    }
                });
                l.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        tip = new Tip(ReplayActivity.this, "删除", getScreenWidth(), getScreenWidth() / 2, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tip.dismiss();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tip.dismiss();
                                long time = (long) view.getTag();
                                GameDatabase.deleteAllData(time,ReplayActivity.this);
                                ReplayActivity.this.replaysWraper.removeView(view);
                            }
                        });
                        tip.show(view.getRootView());
                        return true;
                    }
                });
                replaysWraper.addView(l);
            }
    }
    public int getScreenWidth()
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
