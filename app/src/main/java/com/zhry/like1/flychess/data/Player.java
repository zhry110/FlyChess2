package com.zhry.like1.flychess.data;

import android.os.Handler;
import android.os.Message;

import com.zhry.like1.flychess.GameActivity;
import com.zhry.like1.flychess.view.DiceThread;

import java.util.Queue;

/**
 * Created by like1 on 2017/4/13.
 */
public class Player {
    public static final int USER_ALL = -1;
    public static final int USER1 = 0;
    public static final int USER2 = 1;
    public static final int USER3 = 2;
    public static final int USER4 = 3;

    protected int uid;
    protected int arrives = 0;
    protected PathProvider provider;
    protected boolean turnIsOver;
    protected int dice;
    protected boolean canDice;
    protected boolean flyed;
    protected boolean diced;
    protected boolean animOver = false;
    public SoundEndCallback notSix;
    public SoundEndCallback isSix;
    protected Handler handler;
    public Player(int uid, final PathProvider provider, Handler handler) {
        resignUid(uid);
        this.handler = handler;
        this.provider = provider;
        dice = 0;
        notSix = new SoundEndCallback() {
            @Override
            public void callBack() {
                notSixCallBack();
            }
        };
        isSix = new SoundEndCallback() {
            @Override
            public void callBack() {
                diced = true;
                flyed = false;
                canDice = true;
                Message msg = handler.obtainMessage();
                msg.obj = new Integer(dice);
                msg.what = 5;
                handler.sendMessage(msg);
                System.out.println("isSix called");
            }
        };
        turnIsOver = true;
    }

    private void notSixCallBack() {
        //dice = provider.dicing();
        //System.out.println("dice "+dice);
        flyed = false;
        diced = true;
        Message msg = handler.obtainMessage();
        msg.obj = new Integer(dice);
        msg.what = 5;
        handler.sendMessage(msg);
        canDice = false;
    }

    public synchronized void dice() {
        if (canDice) {
            diced = true;
            flyed = false;
            System.out.println("dice " + dice);
            canDice = false;
            dice = provider.dicing();
            DiceThread dt = new DiceThread(handler);
            dt.start();
            try {
                Map.getInstance().playSound(3);
                dt.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = handler.obtainMessage();
            msg.obj = new Integer(dice);
            msg.what = 5;
            handler.sendMessage(msg);

            if (dice == 6) {
                canDice = true;
                try {
                    Map.getInstance().playSound(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Aircraft[] as = Map.getInstance().getAircrafts(uid);
                int i;
                for (i = 0; i < as.length; i++) {
                    if (!as[i].atHome() && !as[i].isArrive()) {
                        break;
                    }
                }
                if (i == as.length) {
                    setTurnIsOver();
                }
            }
        }

    }

    public void arrive() {
        arrives++;
        if (arrives == 4) {
            Map.getInstance().win(this);
            System.out.println("win");
        }
    }

    public int getUid() {
        return uid;
    }

    public void play() {
        turnIsOver = false;
        canDice = true;
        flyed = true;
        diced = false;
        Message msg = handler.obtainMessage();
        msg.what = 3;
        msg.obj = this;
        handler.sendMessage(msg);
        System.out.println(uid + " play");
        if (arrives == 4) {
            setTurnIsOver();
        }
        //waitPlayerOperation();
    }

    public void setTurnIsOver() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        turnIsOver = true;
        Map.getInstance().schedule();
    }

    public int getDice() {
        return dice;
    }

    public boolean isCanDice() {
        return canDice;
    }

    public void finishFly() {
        flyed = true;
        diced = false;
    }

    public boolean isFlyed() {
        return flyed;
    }

    public boolean isDiced() {
        return diced;
    }

    public boolean isWin() {
        return arrives == 4;
    }

    public boolean resignUid(int uid) {
        this.uid = uid;
        return true;
    }

    public class Struct {
        private String name;
        private int uid;

        public Struct(String name, int uid) {
            this.name = name;
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public int getUid() {
            return uid;
        }
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setProvider(PathProvider provider) {
        this.provider = provider;
    }

    public void animaOver() {
        animOver = true;
    }

    private void waitAnimaOver() {
        while (!animOver) ;
    }

    public boolean canTouch()
    {
        return true;
    }
}
