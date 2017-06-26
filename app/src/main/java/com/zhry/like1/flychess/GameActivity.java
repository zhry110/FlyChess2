package com.zhry.like1.flychess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhry.like1.flychess.brodcastreciver.WifiStateReciver;
import com.zhry.like1.flychess.data.Cmd;
import com.zhry.like1.flychess.data.LocalServerMap;
import com.zhry.like1.flychess.data.Map;
import com.zhry.like1.flychess.data.NetPlayer;
import com.zhry.like1.flychess.listener.DiceClickListener;
import com.zhry.like1.flychess.view.PathNodeView;
import com.zhry.like1.flychess.view.Tip;

import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.io.IOException;

/**
 * Created by like1 on 2017/5/1.
 */

public class GameActivity extends Activity {
    private RelativeLayout relativeLayout;
    private Map map = null;
    private ImageView dice;
    public static ImageView[] flags;
    public static GameThread gameThread;
    private TextView[] names;
    private Tip tip;
    private Tip deputeTip,winTip;
    private TextView depute;
    private ImageView[] bot;
    private RelativeLayout first;
    private NetPlayer netPlayer;
    private TextView roomID;
    private TextView term;
    private DisplayMetrics dm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        netPlayer = RoomActivity.netPlayer;
        names = new TextView[4];
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        bot = new ImageView[4];

        depute = (TextView) findViewById(R.id.depute);
        bot[0] = (ImageView) findViewById(R.id.bot0);
        bot[1] = (ImageView) findViewById(R.id.bot1);
        bot[2] = (ImageView) findViewById(R.id.bot2);
        bot[3] = (ImageView) findViewById(R.id.bot3);
        flags = new ImageView[4];
        roomID = (TextView) findViewById(R.id.roomID);
        flags[0] = (ImageView)findViewById(R.id.pointer_0);
        flags[1] = (ImageView)findViewById(R.id.pointer_1);
        flags[2] = (ImageView)findViewById(R.id.pointer_2);
        flags[3] = (ImageView)findViewById(R.id.pointer_3);
        dm = new DisplayMetrics();
        ((WindowManager)getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels/15-2;
        tip = new Tip(this, "退出游戏", dm.widthPixels,dm.widthPixels/2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tip.dismiss();
                map.setPause(false);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tip.dismiss();
                map.setPause(false);
                finish();
            }
        });
        deputeTip = new Tip(this, "托管后将由Bot代替你。你将无法赶走Bot哦", dm.widthPixels, dm.widthPixels / 2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deputeTip.dismiss();
                depute.setVisibility(View.VISIBLE);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deputeTip.dismiss();
                netPlayer.depute();
                findViewById(R.id.deputeOn).setVisibility(View.VISIBLE);
            }
        });
        relativeLayout = (RelativeLayout)findViewById(R.id.activity_main);
        first = (RelativeLayout) findViewById(R.id.tip);
        first.setVisibility(View.INVISIBLE);
        //Toast.makeText(GameActivity.getInstance(), "玩家" + getNextUser().getUid() + "获得先手", 0).show();
        ViewGroup.LayoutParams layoutParams = null;
        dice = (ImageView)findViewById(R.id.dice);
        PathNodeView[] comViews = new PathNodeView[52];
        PathNodeView[] priViews = new PathNodeView[21];
        PathNodeView[] homeViews = new PathNodeView[20];
        names[0] = (TextView) findViewById(R.id.user0);
        names[1] = (TextView) findViewById(R.id.user1);
        names[2] = (TextView) findViewById(R.id.user2);
        names[3] = (TextView) findViewById(R.id.user3);
        for (int i = 0 ;i< 52;i++)
        {
            layoutParams = ((PathNodeView)relativeLayout.getChildAt(i)).getLayoutParams();
            layoutParams.height = width;
            layoutParams.width = width;
            PathNodeView child = (PathNodeView)relativeLayout.getChildAt(i);
            child.setLayoutParams(layoutParams);
            if (i<4) {
                layoutParams = flags[i].getLayoutParams();
                layoutParams.height = width;
                layoutParams.width = width;
                flags[i].setLayoutParams(layoutParams);
                flags[i].setImageDrawable(null);
            }
            int j = i%4;
            child.setImageDrawable(null);
            switch (j)
            {
                case 0:
                    child.setBackground(getResources().getDrawable(R.drawable.blackpos));
                    break;
                case 1:
                    child.setBackground(getResources().getDrawable(R.drawable.redpos));
                    break;
                case 2:
                    child.setBackground(getResources().getDrawable(R.drawable.rangepos));
                    break;
                case 3:
                    child.setBackground(getResources().getDrawable(R.drawable.greenpos));
                    break;
            }
            comViews[i] = child;
        }
        for (int i = 52;i<73;i++)
        {
            PathNodeView child = (PathNodeView)relativeLayout.getChildAt(i);
            priViews[i-52] = child;
            if (i == 72)
                break;
            layoutParams = ((PathNodeView)relativeLayout.getChildAt(i)).getLayoutParams();
            layoutParams.height = width;
            layoutParams.width = width;
            child.setLayoutParams(layoutParams);
            child.setImageDrawable(null);
        }
        for (int i = 73;i<93;i++)
        {
            PathNodeView child = (PathNodeView)relativeLayout.getChildAt(i);
            homeViews[i-73] = child;
            layoutParams = ((PathNodeView)relativeLayout.getChildAt(i)).getLayoutParams();
            layoutParams.height = width;
            layoutParams.width = width;
            child.setLayoutParams(layoutParams);
            child.setImageDrawable(null);
        }
        Intent i = getIntent();
        //map = new LocalServerMap(0,4,comViews,priViews,homeViews,getResources());
        int players = i.getIntExtra("players",0);
        String[] snames = i.getStringArrayExtra("names");
        int mode = i.getIntExtra("mode",0);
        int bots = i.getIntExtra("bot",0);
        if (mode == 0)
        {
            map = new Map(this,null,players,bots,comViews,priViews,homeViews,getResources(),names);
            map.startGame();
            roomID.setText("");
        }
        else if (mode == 1)
        {
            depute.setVisibility(View.VISIBLE);
            map = new LocalServerMap(this,netPlayer,players,bots,comViews,priViews,homeViews,getResources(),names,snames);
            if (netPlayer == null)
                roomID.setText("");
            else
                roomID.setText("房间ID:"+RoomActivity.getRoomID());
            depute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (netPlayer != null) {
                        deputeTip.show(v.getRootView());
                        v.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
        dice.setOnClickListener(new DiceClickListener(netPlayer));

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.exit();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (tip.isShowing())
            {
                tip.dismiss();
                map.setPause(false);
            }
            else
            {
                tip.show(relativeLayout);
                map.setPause(true);
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.setPause(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (firstLauch){
            first.show(relativeLayout);
            firstLauch = false;
        }*/
        map.setPause(false);
    }

    public ImageView getDice() {
        return dice;
    }

    public void showTip(String text)
    {
        System.out.println("show tip");
        first = (RelativeLayout) findViewById(R.id.tip);
        first.setVisibility(View.VISIBLE);
        TextView t = (TextView) first.findViewById(R.id.textView);
        t.setText(text);
        ImageView x = (ImageView) first.findViewById(R.id.imageView4);
        ImageView ok = (ImageView) first.findViewById(R.id.linearLayout);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first.setVisibility(View.INVISIBLE);
                map.schedule();
            }
        };
        x.setOnClickListener(listener);
        ok.setOnClickListener(listener);
    }
    public void showBotView(int uid)
    {
        bot[uid].setVisibility(View.VISIBLE);
    }
    public void replay()
    {
        depute.setVisibility(View.INVISIBLE);
        findViewById(R.id.deputeOn).setVisibility(View.VISIBLE);
    }
    public void showGameOver(String s)
    {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                winTip.dismiss();
            }
        };
        winTip = new Tip(this,s,dm.widthPixels,dm.widthPixels/2+50,listener,listener ,0);
        winTip.show(depute.getRootView());
    }
}
