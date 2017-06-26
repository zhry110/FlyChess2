package com.zhry.like1.flychess;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhry.like1.flychess.data.Map;
import com.zhry.like1.flychess.data.NetPlayer;
import com.zhry.like1.flychess.sqlite.GameDatabase;
import com.zhry.like1.flychess.view.Tip;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import local.server.LocalServer;

public class MainActivity extends Activity {
    public static String playerName = null;
    private static Context context;
    private static MainActivity instance;
    private ImageView local, localServer, server;
    static LocalServer l;
    private ImageView music;
    private ImageView about;
    private ImageView replay;
    private Tip tip;
    private Tip exitTip;
    private TextView[] views;
    private PopupWindow gameSet;

    private PopupWindow localChooser;
    private ImageView create;
    private ImageView join;

    private EditText editText;
    private ImageView ok;
    private TextView tName;

    private RelativeLayout inputName;

    private LinearLayout main;

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == local) {
                startLocalGame(4);
            } else if (v == localServer) {
                startLocalServerGame();
            } else {
                startServerGame();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        instance = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game_mode);
        replay = (ImageView) findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,ReplayActivity.class);
                startActivity(intent);
            }
        });
        //openOrCreateDatabase(GameDatabase.DATABASENAME,MODE_PRIVATE,null).execSQL("drop table if exists game;");
        //deleteDatabase(GameDatabase.DATABASENAME);
        editText = (EditText) findViewById(R.id.editText);
        editText.requestFocus();
        main = (LinearLayout) findViewById(R.id.main);
        main.setVisibility(View.INVISIBLE);
        tName = (TextView) findViewById(R.id.textView2);
        inputName = (RelativeLayout) findViewById(R.id.input_name);
        ok = (ImageView) findViewById(R.id.imageView4);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString();
                if (name.equals("")||name.equals(" "))
                {
                    Toast.makeText(getContext(),"你并没有输入",0).show();
                }
                else
                {
                    tName.setText(name);
                    inputName.setVisibility(View.INVISIBLE);
                    LinearLayout linearLayout = (LinearLayout) inputName.getParent();
                    linearLayout.removeView(inputName);
                    main.setVisibility(View.VISIBLE);
                    playerName = name;
                }
            }
        });
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        View.OnClickListener unl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tip.dismiss();
            }
        };
        tip = new Tip(this, "code : like1\npicture : Hong\nAI : haha\nnet : FFlover", dm.widthPixels, dm.widthPixels / 2 + 260, unl, unl);
        exitTip = new Tip(this, "退出", dm.widthPixels, dm.widthPixels / 2, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitTip.dismiss();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitTip.dismiss();
                finish();
            }
        });
        gameSet = new PopupWindow(getLayoutInflater().inflate(R.layout.game_setting, null), dm.widthPixels, dm.widthPixels + 100);
        localChooser = new PopupWindow(getLayoutInflater().inflate(R.layout.local_server_join_or_create, null),
                dm.widthPixels, dm.widthPixels / 2);
        create = (ImageView) localChooser.getContentView().findViewById(R.id.create);
        join = (ImageView) localChooser.getContentView().findViewById(R.id.join);
        views = new TextView[9];
        views[0] = (TextView) gameSet.getContentView().findViewById(R.id.bot1);
        final Drawable back = getResources().getDrawable(R.drawable.ok);
        views[1] = (TextView) gameSet.getContentView().findViewById(R.id.open1);
        views[2] = (TextView) gameSet.getContentView().findViewById(R.id.no1);

        views[3] = (TextView) gameSet.getContentView().findViewById(R.id.bot2);
        views[4] = (TextView) gameSet.getContentView().findViewById(R.id.open2);
        views[5] = (TextView) gameSet.getContentView().findViewById(R.id.no2);

        views[6] = (TextView) gameSet.getContentView().findViewById(R.id.bot3);
        views[7] = (TextView) gameSet.getContentView().findViewById(R.id.open3);
        views[8] = (TextView) gameSet.getContentView().findViewById(R.id.no3);

        ImageView start = (ImageView) gameSet.getContentView().findViewById(R.id.start);

        View.OnClickListener setListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                for (int j = i / 3 * 3; j < i / 3 * 3 + 3; j++) {
                    if (views[j] == v) {
                        views[j].setBackground(back);
                    } else {
                        views[j].setBackground(null);
                    }
                }
            }
        };
        for (int i = 0; i < views.length; i++) {
            views[i].setId(i);
            if (i % 3 != 1)
                views[i].setBackground(null);
            views[i].setOnClickListener(setListener);
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameSet.dismiss();
                int bots = 0, players = 0;
                for (int i = 0; i < views.length; i++) {
                    if (i % 3 == 0 && views[i].getBackground() != null)
                        bots++;
                    else if (i % 3 == 1 && views[i].getBackground() != null)
                        players++;
                }
                Intent i = new Intent();
                i.setClass(getContext(), GameActivity.class);
                i.putExtra("mode", 0);
                i.putExtra("players", players + 1);
                i.putExtra("bot", bots);
                startActivity(i);
            }
        });
        music = (ImageView) findViewById(R.id.music);
        about = (ImageView) findViewById(R.id.about);
        local = (ImageView) findViewById(R.id.local);
        localServer = (ImageView) findViewById(R.id.local_server);
        server = (ImageView) findViewById(R.id.server);
        local.setOnClickListener(listener);
        localServer.setOnClickListener(listener);
        server.setOnClickListener(listener);
        music.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (Map.isMusic()) {
                    Map.noMusic();
                    music.setBackground(getResources().getDrawable(R.drawable.nomusic));
                } else {
                    Map.openMusic();
                    music.setBackground(null);
                }
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tip.isShowing()) {
                    tip.dismiss();
                } else {
                    tip.show(about.getRootView());
                }
            }
        });
    }


    private void startLocalGame(int players) {
        Drawable back = getResources().getDrawable(R.drawable.ok);
        for (int i = 0; i < views.length; i++) {
            if (i % 3 == 1)
                views[i].setBackground(back);
            else
                views[i].setBackground(null);
        }
        gameSet.showAtLocation(about.getRootView(), Gravity.CENTER, 0, 0);
    }

    private void startLocalServerGame() {
        localChooser.showAtLocation(about.getRootView(), Gravity.CENTER, 0, 0);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocalServer.getLocalNetAddress() == null) {
                    Toast.makeText(MainActivity.this, "你并没有加入任何WIFI，请和你的好友加入同一个WIFI。移动热点通常是一个好选择", 1).show();
                    return;
                }
                localChooser.dismiss();
                Intent i = new Intent();
                if (v == create) {
                    createLocalServer(i,MainActivity.this);
                }else {
                    i.setClass(MainActivity.this, LocalServerGameActivity.class);
                }
                startActivity(i);
            }
        };
        create.setOnClickListener(listener);
        join.setOnClickListener(listener);
        /*Intent i = new Intent();
        i.setClass(this, LocalServerGameActivity.class);
        startActivity(i);*/
    }

    public void createLocalServer(Intent i,Context c) {
        l = new LocalServer();
        l.start();
        i.setClass(c, RoomActivity.class);
        RoomActivity.setLocalServer(new com.zhry.like1.flychess.server.LocalServer(l.getLocalAddress(),4,1,""));
    }

    private void startServerGame() {
        Intent i = new Intent();
        i.setClass(this,ServerGameActivity.class);
        startActivity(i);
        //
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (localChooser.isShowing()) {
                localChooser.dismiss();
                return true;
            }
            if (tip.isShowing()) {
                tip.dismiss();
                return true;
            }
            if (exitTip.isShowing()) {
                exitTip.dismiss();
                return true;
            }
            if (gameSet.isShowing()) {
                gameSet.dismiss();
                return true;
            }
            exitTip.show(about.getRootView());
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        System.out.println("MainActivity destoryed");
        instance = null;
        super.onDestroy();
    }

    public static Context getContext() {
        return context;
    }

    private boolean openWIFIhotPot() {
        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "like1";
        apConfig.preSharedKey = "123456789";
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        Method method = null;
        try {
            method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enabled = true;
            final Boolean invoke = (Boolean) method.invoke(wifiManager, apConfig, enabled);
            return invoke;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Activity getInstance() {
        return instance;
    }
}
