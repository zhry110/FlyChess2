package game;


/**
 * Created by like1 on 2017/4/13.
 */
public class KeyPathNode extends PathNode{
    private PathNode superNext;
    public KeyPathNode(int uid)
    {
        super(uid);
        next = null;
        superNext = null;
    }
    @Override
    public boolean layoutAircraft(Aircraft aircraft)
    {
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
