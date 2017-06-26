package com.zhry.like1.flychess.data;

import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhry.like1.flychess.R;
import com.zhry.like1.flychess.listener.PathNodeClickListener;
import com.zhry.like1.flychess.view.PathNodeView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by like1 on 2017/4/12.
 */
public class PathNode {
    protected int uid;
    protected PathNode next;
    protected List<Aircraft> aircrafts;
    protected PathNodeView view;
    protected Handler handler;
    public PathNode(int uid, PathNodeView view, Handler handler) {
        if (uid <-1||uid>4) {
            throw new IllegalArgumentException();
        }
        else {
            this.uid = uid;
        }
        this.handler = handler;
        aircrafts = new ArrayList<>();
        next = null;
        this.view = view;
        if (view!=null&&uid!=-1)
            view.setBackground(Map.pos[uid]);
        if (this.view != null) {
            this.view.setPathNode(this);
            this.view.setOnClickListener(PathNodeClickListener.getInstance(Map.getInstance().getNetPlayer()));
        }
    }
    public int getUid()
    {
        return uid;
    }
    public boolean removeAircraft(Aircraft aircraft)
    {
        boolean ret = aircrafts.remove(aircraft);
        if (view!=null)
        {
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = this;
            handler.sendMessage(msg);
        }
        return ret;
    }
    public boolean layoutAircraft(Aircraft aircraft)
    {
        if (aircraft == null)
            return false;
        if (uid == aircraft.getUid()&&aircraft.getContinueFlyTime()<2) {
            if (aircraft.fly(4)) {
                return true;
            }
            else
            {
                aircrafts.add(aircraft);
                aircraft.layout();
            }
        }
        else {
            destoryAircrafts(aircraft.getUid());
            aircrafts.add(aircraft);
            aircraft.layout();
            Log.i("uid",""+uid);
        }
        return true;
    }
    public void setNext(PathNode next)
    {
        this.next = next;
    }
    public PathNode next(Aircraft aircraft)
    {
        return next;
    }
    protected void destoryAircrafts(int uid)
    {
        if (aircrafts == null)
            return;
        Iterator<Aircraft> iterator = aircrafts.iterator();
        Aircraft aircraft;
        while (iterator.hasNext())
        {
            aircraft = iterator.next();
            if (aircraft != null)
            {
                if (aircraft.getUid() != uid) {
                    aircraft.respawn();
                    iterator.remove();
                }
            }
        }
    }

    public List<Aircraft> getAircrafts() {
        return aircrafts;
    }

    public int stepsContinue(Aircraft aircraft,int times)
    {
        int steps = 0;
        if (times == 2)
            return 0;
        if (aircraft.getUid() == getUid())
        {
            return 4 + next(aircraft).next(aircraft).next(aircraft).next(aircraft).stepsContinue(aircraft,++times);
        }
        return 0;
    }
    public int startSteps()
    {
        return 2;
    }
}
