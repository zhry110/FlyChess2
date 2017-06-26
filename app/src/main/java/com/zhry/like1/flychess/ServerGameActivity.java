package com.zhry.like1.flychess;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhry.like1.flychess.data.NetPlayer;
import com.zhry.like1.flychess.listener.JoinRoomLinstener;
import com.zhry.like1.flychess.server.ServerInfo;

/**
 * Created by like1 on 2017/6/17.
 */

public class ServerGameActivity extends Activity {
    private LinearLayout serverWraper;
    private static ServerGameActivity activity;
    private TextView createServer;
    public  Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ServerInfo serverInfo = (ServerInfo) msg.obj;
            if (serverInfo == null)
                return;
            LayoutInflater inflater = activity.getLayoutInflater();
            LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.game_room,null);
            linearLayout.setGravity(Gravity.CENTER);
            activity.serverWraper.addView(linearLayout);
            ((TextView) linearLayout.findViewById(R.id.trasScreenTextView04)).setText(serverInfo.getName());
            ((TextView) linearLayout.findViewById(R.id.more)).setText( "房间ID:"+serverInfo.getId()+"   玩家("
                    + serverInfo.getPlayers() + "/" + serverInfo.getType() + ")");
            linearLayout.setOnClickListener(new JoinRoomLinstener(null, activity,serverInfo.getId()));
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_server_game_view);
        createServer = (TextView) findViewById(R.id.create_home);
        createServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetPlayer.createHome(MainActivity.playerName,ServerGameActivity.this);
            }
        });
        serverWraper = (LinearLayout) findViewById(R.id.server_wraper);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetPlayer.scanRoomsFromServer(handler);
            }
        }).start();
    }
    public int getScreenWidth()
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
