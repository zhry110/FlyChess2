package com.zhry.like1.flychess.data;

import android.os.Handler;

/**
 * Created by like1 on 2017/6/6.
 */

public class StepAIPlayer extends AIPlayer {
    public StepAIPlayer(int uid, PathProvider provider, Handler handler) {
        super(uid, provider,handler);
    }

    @Override
    protected void think() {
        dice();
        if (turnIsOver)
            return;
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Aircraft[] craftsAtHome = new Aircraft[4]; int i = 0;
        Aircraft stepMax = null;
        for (Aircraft aircraft : Map.getInstance().getAircrafts(uid))
        {
            if (aircraft.atHome())
            {
                craftsAtHome[i++] = aircraft;
            }
            else if (!aircraft.isArrive())
            {
                if (aircraft.moreThan(stepMax,dice))
                {
                    stepMax = aircraft;
                }
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
                stepMax.setCanFly(true);
                stepMax.fly(dice);
            }
            finishFly();
            think();
        }
        else
        {
            stepMax.setCanFly(true);
            stepMax.fly(dice);
            finishFly();
            setTurnIsOver();
        }
    }
}
