package game;



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
    public PathNode(int uid) {
        if (uid <-1||uid>4) {
            throw new IllegalArgumentException();
        }
        else {
            this.uid = uid;
        }
        aircrafts = new ArrayList<>();
        next = null;
    }
    public int getUid()
    {
        return uid;
    }
    public boolean removeAircraft(Aircraft aircraft)
    {
        return aircrafts.remove(aircraft);
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
                if (aircraft.getUid()!=uid) {
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
