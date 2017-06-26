package game;


/**
 * Created by like1 on 2017/4/13.
 */
public class ConnectNode extends PathNode {
    private PathNode privateNext;
    public ConnectNode(int uid)
    {
        super(uid);
        privateNext = null;
    }

    @Override
    public PathNode next(Aircraft aircraft) {
        if (aircraft == null)
            return next;
        if (aircraft.getUid() == uid)
        {
            System.out.println("server:"+privateNext.getUid());
            return privateNext;
        }
        return next;
    }
    public boolean setPrivateNext(PathNode n)
    {
        if (n == null)
            return false;
        privateNext = n;
        return true;
    }

    @Override
    public boolean layoutAircraft(Aircraft aircraft) {
        if (aircraft == null)
            return false;
            destoryAircrafts(aircraft.getUid());
            aircrafts.add(aircraft);
            aircraft.layout();

        return true;
    }

    public PathNode getPrivateNext() {
        return privateNext;
    }
    @Override
    public int stepsContinue(Aircraft aircraft,int times) {
        return 0;
    }
}
