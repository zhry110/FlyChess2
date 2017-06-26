package com.zhry.like1.flychess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhry.like1.flychess.listener.JoinRoomLinstener;
import com.zhry.like1.flychess.net.FindLocalServerThread;
import com.zhry.like1.flychess.server.LocalServer;
import com.zhry.like1.flychess.server.Server;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by like1 on 2017/4/27.
 */

public class LocalServerGameActivity extends Activity {
    private LinearLayout serversWraper;
    private Button button;
    private LayoutInflater inflater;
    private Activity instance;
    private FindLocalServerThread findLocalServerThread;
    private TextView createServer;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LocalServer localServer = (LocalServer) msg.obj;
            if (msg.what == 0) {
                if (localServer == null)
                    return;
                LinearLayout view = localServer.getLinearLayout();
                if (!localServer.hasView()) {
                    view = (LinearLayout) inflater.inflate(R.layout.game_room, null);
                    view.setGravity(Gravity.CENTER);
                    serversWraper.addView(view);
                    localServer.setLinearLayout(view);
                }
                else
                {
                    if (serversWraper.indexOfChild(view) == -1)
                    {
                        LinearLayout parent = (LinearLayout) view.getParent();
                        if (parent != null)
                            parent.removeView(view);
                        serversWraper.addView(view);
                    }
                }
                ((TextView) view.findViewById(R.id.trasScreenTextView04)).setText(localServer.getName());
                ((TextView) view.findViewById(R.id.more)).setText(localServer.getAddress().toString().substring(1) + "   ("
                        + localServer.getCurPlayers() + "/" + localServer.getGameType() + ")");
                view.setOnClickListener(new JoinRoomLinstener(localServer, instance,0));
            } else if (msg.what == 1)
            {
                if (localServer.hasView())
                    serversWraper.removeView(localServer.getLinearLayout());
            }
            else {
                for (int i = 1;i<serversWraper.getChildCount();i++)
                    serversWraper.removeViewAt(i);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.local_server_game_view);
        createServer = (TextView) findViewById(R.id.create_home);
        serversWraper = (LinearLayout) findViewById(R.id.server_wraper);
        inflater = getLayoutInflater();
        try {
            findLocalServerThread = new FindLocalServerThread(handler);
            findLocalServerThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String host = Server.getLocalNetAddress();
        if (host != null)
            System.out.println(host);
        createServer.setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        FindLocalServerThread.getInstance().pause();
        System.out.println("FIND pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        System.out.println("resume");
        FindLocalServerThread.getInstance().wake();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (findLocalServerThread != null)
        {
            findLocalServerThread.exit();
        }
        LocalServer.clearServersMap();
        System.out.println("destory");

        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            serversWraper.removeAllViews();
        }
        return super.onKeyUp(keyCode, event);
    }
    public int getScreenWidth()
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
