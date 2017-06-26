package com.zhry.like1.flychess.data;

import android.os.Handler;
import android.os.Message;

/**
 * Created by like1 on 2017/6/3.
 */

public class AIPlayer extends Player {
    private SoundEndCallback notSix;
    public AIPlayer(int uid, PathProvider provider, Handler handler) {
        super(uid, provider,handler);
    }

    @Override
    public void play() {
        super.play();
        if (turnIsOver)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                think();
                System.out.println("AI play over");
            }
        }).start();
    }
    protected void think()
    {
        dice();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (turnIsOver)
            return;
        Aircraft[] craftsAtHome = new Aircraft[4]; int i = 0;
        Aircraft[] craftsAtPath = new Aircraft[4]; int j = 0;
        for (Aircraft aircraft : Map.getInstance().getAircrafts(uid))
        {
            if (aircraft.atHome())
            {
                craftsAtHome[i++] = aircraft;
                aircraft.testFly(dice);
            }
            else if (!aircraft.isArrive())
            {
                craftsAtPath[j++] = aircraft;
                aircraft.testFly(dice);
            }
        }
        if (dice == 6)
        {
            if (craftsAtHome[0] != null)
            {
                craftsAtHome[0].setCanFly(true);
                craftsAtHome[0].fly(1);
            }
            else
            {
                craftsAtPath[0].setCanFly(true);
                craftsAtPath[0].fly(6);
            }
            finishFly();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            think();
        }
        else
        {
            craftsAtPath[0].setCanFly(true);
            craftsAtPath[0].fly(dice);
            finishFly();
            setTurnIsOver();
        }
    }

    @Override
    public  void setTurnIsOver() {
        System.out.println("new turnover");
        turnIsOver = true;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(6);
    }

    @Override
    public boolean canTouch() {
        return false;
    }
}
