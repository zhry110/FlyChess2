package com.zhry.like1.flychess.data;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Handler;
import android.os.Message;

import com.zhry.like1.flychess.GameActivity;
import com.zhry.like1.flychess.MainActivity;
import com.zhry.like1.flychess.net.Protocol;
import com.zhry.like1.flychess.server.Server;
import com.zhry.like1.flychess.sqlite.GameDatabase;

import java.io.IOException;


/**
 * Created by like1 on 2017/6/19.
 */

public class Replayer extends NetPlayer {
    private boolean over = false;
    private Thread replayThread;
    public Replayer(Handler handler) throws IOException {
        super(null,false,handler);
    }
    @Override
    public void sendAddbot() {
        return;
    }

    @Override
    public boolean canTouch() {
        return !super.canTouch();
    }

    @Override
    public void prepare() {
        return;
    }

    @Override
    public void fly(int aid) {
        return;
    }

    @Override
    protected void loseConnect() {
        over = true;
        inRoom = false;
    }
    public void JoinRoom(long time) {
        replayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Activity activity = MainActivity.getInstance();
                if (activity == null)
                    return;
                Protocol[] protocols = GameDatabase.getProtocols(time,activity);
                if (protocols == null) {
                    System.out.println("select protocol is null");
                    return;
                }
                if (protocols.length == 0) {
                    System.out.println("select protocol is 0");
                    return;
                }
                System.out.println("joined");
                Replayer.this.resignUid(protocols[0].getData()[1]);
                if (protocols[0].getData()[1] == 2)
                    Replayer.this.host = true;
                Message msg = roomHandler.obtainMessage();
                msg.obj = Replayer.this;
                msg.what = -2;
                roomHandler.sendMessage(msg);
                roomHandler.sendEmptyMessage(4);
                for (int i = 1;i < protocols.length && !over;i++)
                {
                    System.out.println("protocol" + i);
                    if (protocols[i].getOpt() == Server.START) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Replayer.this.handleAck(protocols[i]);
                        try {
                            Thread.sleep(5000);
                            roomHandler.sendEmptyMessage(-1);
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    Replayer.this.handleAck(protocols[i]);
                }
                roomHandler.sendEmptyMessage(-3);
            }
        });
        replayThread.start();
    }

    @Override
    public void sendReadyMsg() {
        return;
    }

    @Override
    public void leftRoom() {
    }

    @Override
    public void leftMap() {
        over = true;
    }

    @Override
    public void goOn() {
        if (replayThread != null)
            replayThread.interrupt();
        System.out.println("go on repaly");
    }

    @Override
    public void doSomeThing(GameActivity gameActivity) {
        super.doSomeThing(gameActivity);
        gameActivity.replay();
    }
}
