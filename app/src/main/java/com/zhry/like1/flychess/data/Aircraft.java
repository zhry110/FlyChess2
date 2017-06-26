package com.zhry.like1.flychess.data;


import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Message;
import android.view.animation.TranslateAnimation;

import com.zhry.like1.flychess.R;
import com.zhry.like1.flychess.server.LocalServer;

/**
 * Created by like1 on 2017/4/12.
 */
public class Aircraft {
    public static final int ANTICLOCKWISE = 257;
    public static final int CLOCKWISE = 256;
    private int id;
    private int flyOrder = CLOCKWISE;
    private int uid;
    private boolean canFly = false;
    private PathNode currentPostion;
    private PathProvider provider;
    private int continueFlyTime = 0;
    private boolean atHome = true;
    private boolean isArrive;
    private PathNode last;
    private Handler handler;
    public Aircraft(int uid, int id, PathNode postion, PathProvider provider, Handler handler)
    {
        this.handler = handler;
        if (id<0||id>3)
            throw new IllegalArgumentException();
        else
            this.id = id;
        if (uid<0||uid>3)
            throw new IllegalArgumentException();
        else
            this.uid = uid;
        if (postion == null)
            throw new NullPointerException();
        else
            currentPostion = postion;
        if (provider == null)
            throw new NullPointerException();
        else
            this.provider = provider;
        isArrive = false;
    }
    private boolean setPosition(PathNode position)
    {
        if (position == null) {
            return false;
        }
        currentPostion = position;
        return true;
    }
    public int testFly(int points) //return steps to AI
    {
        int startSteps = currentPostion.startSteps();
        if (points <= 0||points>6) {
            Map.setErrno(Map.BAD_POINTS_FOR_FLY);
            return 0;
        }
        if (atHome())
        {
            if (points == 6)
                return 100;
            else
                return 0;
        }
        PathNode pathNode = currentPostion;
        int steps = startSteps;
        while (points-- > 0)
        {
            pathNode = pathNode.next(this);
            steps++;
            if (flyOrder == ANTICLOCKWISE)
            {
                setFlyOrder(CLOCKWISE);
                return steps - 1;
            }
        }
        steps += pathNode.stepsContinue(this,1);
        System.out.println("testFly steps "+steps);
        return steps ;
    }
    public synchronized boolean fly(int points)
    {
        if (points <= 0||points>6) {
            Map.setErrno(Map.BAD_POINTS_FOR_FLY);
            return false;
        }
        if (!canFly) {
            System.out.println("can not fly");
            return false;
        }
        atHome = false;
        Message msg ;
        continueFlyTime++;
        last = currentPostion;
        currentPostion.removeAircraft(this);
        while (points > 0)
        {
            setPosition(currentPostion.next(this));
            msg = handler.obtainMessage();
            msg.what = 0;
            msg.obj = currentPostion;
            handler.sendMessage(msg);
            try {
                Map.getInstance().playSound(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (points != 1) {
                msg = handler.obtainMessage();
                msg.obj = currentPostion;
                msg.what = 1;
                handler.sendMessage(msg);
            }
            points --;
        }
        currentPostion.layoutAircraft(this);
        return true;
    }
    public boolean respawn()
    {
        //currentPostion.removeAircraft(this);
        System.out.println("respawn");
        PathNode c = currentPostion;
        setPosition(provider.getHome(uid,id));
        if(!currentPostion.layoutAircraft(this))
        {
            System.out.println("setPosition Failed at respawn()");
            return false;
        }
        TranslateAnimation t = new TranslateAnimation(c.view.getX(),currentPostion.view.getX(),c.view.getY(),currentPostion.view.getY());
        t.setDuration(1000);
        Message msg = handler.obtainMessage();
        msg.what = 11;
        msg.obj = new Object[]{t,getUid()};
        handler.sendMessage(msg);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map.getInstance().playSound(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        atHome = true;
        return true;
    }
    public void setCanFly(boolean can)
    {
        canFly = can;
    }
    public int getUid()
    {
        return uid;
    }
    public int getContinueFlyTime()
    {
        return continueFlyTime;
    }

    public void layout() {
        flyOrder = CLOCKWISE;
        continueFlyTime = 0;
        canFly = false;
        Message msg = handler.obtainMessage();
        msg.obj = currentPostion;
        msg.what = 2;
        handler.sendMessage(msg);
    }

    public boolean flyTo(PathNode target) {
        if (target == null)
            return false;
        PathNode c = currentPostion;
        currentPostion.removeAircraft(this);
        TranslateAnimation t = new TranslateAnimation(c.view.getX(),target.view.getX(),c.view.getY(),target.view.getY());
        t.setDuration(800);
        setPosition(target);
        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.obj = currentPostion;
        handler.sendMessage(msg);
        msg = handler.obtainMessage();
        msg.what = 9;
        msg.obj = new Object[]{t,getUid()};
        handler.sendMessage(msg);
        try {
            Map.getInstance().playSound(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return currentPostion.layoutAircraft(this);
    }
    public void setFlyOrder(int order)
    {
        flyOrder = order;
    }
    public int getFlyOrder() {
        return flyOrder;
    }
    public void finish()
    {
        Map.getInstance().getUser(uid).arrive();
    }

    public PathProvider getProvider() {
        return provider;
    }

    public PathNode getCurrentPostion() {
        return currentPostion;
    }

    public boolean atHome() {
        return atHome;
    }
    public void arrive()
    {
        atHome = false;
        isArrive = true;
        Map.getInstance().getCurPlayer().arrive();
        Message msg = handler.obtainMessage();
        msg.obj = this;
        msg.what = 7;
        handler.sendMessage(msg);
    }

    public boolean isArrive() {
        return isArrive;
    }

    public int getId() {
        return id;
    }

    public void rollBack()
    {
        flyTo(last);
    }

    public boolean moreThan(Aircraft aircraft,int points)
    {
        if (aircraft == null)
            return true;
        if (testFly(points) > aircraft.testFly(points))
            return true;
        return false;
    }
}
