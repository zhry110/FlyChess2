package com.zhry.like1.flychess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhry.like1.flychess.data.NetPlayer;
import com.zhry.like1.flychess.data.Player;
import com.zhry.like1.flychess.data.Replayer;
import com.zhry.like1.flychess.net.FindLocalServerThread;
import com.zhry.like1.flychess.server.LocalServer;

import org.w3c.dom.Text;

import java.io.IOException;

/**
 * Created by like1 on 2017/5/17.
 */

public class RoomActivity extends Activity {
    private LinearLayout players;
    private static LocalServer localServer;
    private LayoutInflater layoutInflater;
    private RoomActivity instance;
    private LinearLayout[] linearLayouts;
    private TextView prepare;
    private String[] names;
    private LinearLayout addBot;
    public static NetPlayer netPlayer;
    private static int roomID = 0;
    public static boolean replay = false;
    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            System.out.println("msg.what="+msg.what);
            if (msg.what == -2)
            {
                instance.netPlayer = (NetPlayer) msg.obj;
                return;
            }
            else {
                if (instance.netPlayer == null)
                {
                    if (msg.what == -3)
                    {
                        instance.finish();
                        Toast.makeText(instance,"房间拒绝了你,请选择一个不易重复的ID或刷新房间状态",0).show();
                        return;
                    }
                    instance.finish();
                    Toast.makeText(instance,"断开连接",0).show();
                    return;
                }
            }
            if (msg.what == 0) {
                Player.Struct playerInfo = (Player.Struct) msg.obj;
                if (playerInfo == null)
                    return;
                if(instance.netPlayer.getUid() != 2)
                {
                    instance.addBot.setVisibility(View.INVISIBLE);
                }
                LinearLayout linearLayout;
                if (instance.linearLayouts[playerInfo.getUid()] == null) {
                    linearLayout = (LinearLayout) instance.layoutInflater.inflate(R.layout.player_in_room, null);
                    instance.linearLayouts[playerInfo.getUid()] = linearLayout;
                    ((TextView) linearLayout.findViewById(R.id.name)).setText(playerInfo.getName());
                    Log.i("name",playerInfo.getName());
                    //ViewGroup.LayoutParams layoutParams = new ViewGroup.MarginLayoutParams(instance.getScreenWidth()-50,instance.getScreenWidth()/6);
                    //linearLayout.setGravity(Gravity.CENTER);
                    //linearLayout.setLayoutParams(layoutParams);
                    //instance.addBot.setLayoutParams(layoutParams);
                    if (playerInfo.getUid() != 2) {
                        ((ImageView) linearLayout.findViewById(R.id.host)).setImageDrawable(null);
                    }
                    else
                    {
                        ((ImageView) linearLayout.findViewById(R.id.host)).setImageDrawable(RoomActivity.this.getResources().getDrawable(R.drawable.host));
                    }
                    instance.players.addView(linearLayout);
                } else {
                    linearLayout = instance.linearLayouts[playerInfo.getUid()];
                    ((TextView) linearLayout.findViewById(R.id.name)).setText(playerInfo.getName());
                    if (playerInfo.getUid() != 2)
                        ((ImageView) linearLayout.findViewById(R.id.host)).setImageDrawable(null);
                }
                instance.names[playerInfo.getUid()] = playerInfo.getName();
            } else if (msg.what == 1) {
                int uid = (Integer) msg.obj;
                if (uid < 0 || uid > 5)
                    return;
                if (instance.linearLayouts[uid] != null) {
                    instance.names[uid] = null;
                    instance.players.removeView(instance.linearLayouts[uid]);
                    instance.linearLayouts[uid] = null;
                }
            } else if (msg.what == -1) {
                Toast.makeText(instance,"离开房间",0).show();
                instance.finish();
            } else if (msg.what == 2) {
                int uid = (Integer) msg.obj;
                instance.freshPrepare(uid, "已准备");
                System.out.println("已准备");
                instance.freshPrepareBuuttom();
            } else if (msg.what == 3) {
                int uid = (Integer) msg.obj;
                instance.freshPrepare(uid, "未准备");
                System.out.println("未准备");
                instance.freshPrepareBuuttom();
            } else if (msg.what == 4) {
                instance.freshPrepareBuuttom();
            } else if (msg.what == 5) {
                instance.startGameActivity((Integer) msg.obj);
            }
            else if (msg.what == -3)
            {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        names = new String[4];
        linearLayouts = new LinearLayout[5];
        layoutInflater = getLayoutInflater();
        setContentView(R.layout.game_room_detail);
        addBot = (LinearLayout) findViewById(R.id.addbot);
        prepare = (TextView) findViewById(R.id.prpare);
        players = (LinearLayout) findViewById(R.id.playersWraper);
        if (MainActivity.playerName == null)
        {
            Toast.makeText(this,"你需要在启动程序时填写一个ID",0).show();
            finish();
        }
        if (replay)
        {
            try {
                new Replayer(handler).JoinRoom(getIntent().getLongExtra("time",0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            if (localServer == null)
            {
                NetPlayer.JoinRoom(roomID,MainActivity.playerName,handler);
            }
            else
            {
                NetPlayer.JoinRoom(localServer.getAddress(), MainActivity.playerName,handler);
            }
        }
        prepare.setOnClickListener((View v) -> {
            if (netPlayer != null) {
                if (netPlayer.isHost()) {
                    netPlayer.start();
                } else if (netPlayer.isPrepare()) {
                    netPlayer.unPrepare();
                } else {
                    netPlayer.prepare();
                }
            }
        });
        addBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("addbot");
                if (netPlayer != null)
                    netPlayer.sendAddbot();
            }
        });
    }

    @Override
    protected void onDestroy() {
        replay = false;
        if (netPlayer != null) {
            new Thread(() ->
            {
                netPlayer.leftRoom();
                netPlayer = null;
            }).start();
        }
        if (MainActivity.l != null)
            MainActivity.l.exit();
        super.onDestroy();
    }

    public static void setLocalServer(LocalServer localServer) {
        RoomActivity.localServer = localServer;
    }
    public static void setRoomID(int id)
    {
        roomID = id;
    }
    private void freshPrepare(int uid, String state) {
        System.out.println(uid);
        if (uid < 0 || uid > 5)
            return;
        if (instance.linearLayouts[uid] != null) {
            ((TextView) (instance.linearLayouts[uid].findViewById(R.id.state))).setText(state);
        } else {
            System.out.println("state is null");
        }
    }

    private void freshPrepareBuuttom() {
        if (netPlayer == null)
            return;
        if (netPlayer.isPrepare()) {
            prepare.setText("取消");
        } else {
            if (netPlayer.getUid() == 2) {
                prepare.setText("开始游戏");
            } else {
                prepare.setText("准备");
            }
        }
    }

    public void startGameActivity(int players) {
        Intent i = new Intent();
        i.putExtra("players", players);
        i.setClass(this, GameActivity.class);
        i.putExtra("names", names);
        i.putExtra("mode", 1);
        startActivity(i);
    }
    public int getScreenWidth()
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public NetPlayer getNetPlayer() {
        return netPlayer;
    }



    public static int getRoomID() {
        return roomID;
    }
}


