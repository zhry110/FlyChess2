package com.zhry.like1.flychess.data;

import android.os.Handler;
import android.util.Log;

import com.zhry.like1.flychess.view.PathNodeView;

/**
 * Created by like1 on 2017/4/13.
 */
public class KeyPathNode extends PathNode{
    private PathNode superNext;
    public KeyPathNode(int uid, PathNodeView view, Handler handler)
    {
        super(uid,view,handler);
        next = null;
        superNext = null;
    }
    @Override
    public boolean layoutAircraft(Aircraft aircraft)
    {
        Log.i("keyuid",""+uid);
        if (aircraft.getUid() == getUid())
        {
            aircraft.getProvider().getBelowSuperFly(uid).destoryAircrafts(Player.USER_ALL);
            return aircraft.flyTo(superNext);
        }
        destoryAircrafts(aircraft.getUid());
        aircrafts.add(aircraft);
        aircraft.layout();
        return true;
    }
    public void setSuperNext(PathNode next)
    {
        this.superNext = next;
    }

    @Override
    public int stepsContinue(Aircraft aircraft,int times) {

        if (aircraft.getUid() == getUid())
        {
            if (times == 2)
                return 12;
            else
                return 16;
        }
        return 0;
    }
}
