package com.zhry.like1.flychess.data;

import android.content.res.Resources;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.zhry.like1.flychess.GameActivity;
import com.zhry.like1.flychess.view.PathNodeView;

import java.io.IOException;

/**
 * Created by like1 on 2017/5/7.
 */

public class LocalServerMap extends Map {
    private int meUid;
    private int currentDice = 1;
    private boolean initFinish = false;
    public LocalServerMap(GameActivity gameActivity,NetPlayer netPlayer,int users,int bots, PathNodeView[] comViews,
                          PathNodeView[] priViews, PathNodeView[] homeViews, Resources res, TextView[] tnames,String[] names)
    {
        super(gameActivity,netPlayer,users,bots, comViews, priViews, homeViews, res,tnames);
        if (netPlayer == null)
        {
            System.out.println("NetPlayer is null");
            return;
        }
        instance = this;
        this.netPlayer = netPlayer;
        meUid = netPlayer.getUid();
        netPlayer.sendReadyMsg();
        netPlayer.setProvider(this);
        setNetPlayer(netPlayer);
        netPlayer.handler = handler;
        setNames(names);
        initFinish = true;
        netPlayer.goOn();
        netPlayer.doSomeThing(gameActivity);
        //NetPlayer.setServerSocketAddress("192.168.2.1",10006);
    }
    public boolean startGame(int uid) {
        while (!initFinish);
        curPlayer = players[uid];
        Message msg = handler.obtainMessage();
        msg.what = 8;
        msg.obj = new String ("玩家"+names[getNextUser().getUid()].getText().toString()+"获得先手");
        handler.sendMessage(msg);
        return true;//NetPlayer.getInstance().beReady();
    }

    @Override
    public boolean startGame() {
        return true;
    }

    @Override
    public void schedule() {
        System.out.println("not permit schedule");
    }
    public synchronized void schedule(int net,int nets) {
        int i = 0;
        while (!curPlayer.turnIsOver)
        {
            try {
                Thread.sleep(200);
                i++;
                if (i == 50) {
                    System.out.println("turn cann't over");
                    System.out.println("游戏数据出错");
                    gameActivity.finish();
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("client:schedule");
        Message msg = handler.obtainMessage();
        msg.what = 4;
        msg.obj = curPlayer;
        handler.sendMessage(msg);
        curPlayer = getNextUser();
        System.out.println("client:it's " + curPlayer.getUid() + " turn");
        curPlayer.play();
    }

    @Override
    public int dicing() {
        return currentDice;
    }

    public void setCurrentDice(int currentDice) {
        this.currentDice = currentDice;
        System.out.println("setDice "+currentDice);
    }

    @Override
    public int getMeUid() {
        return meUid;
    }

    @Override
    public void exit() {
        if (netPlayer != null)
            netPlayer.leftMap();
        System.out.println("exit");
        super.exit();
    }
    public static LocalServerMap getInstance()
    {
        return (LocalServerMap)instance;
    }
}
