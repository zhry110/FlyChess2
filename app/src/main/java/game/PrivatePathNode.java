package game;




/**
 * Created by like1 on 2017/4/13.
 */
public class PrivatePathNode extends PathNode {
    private PathNode front;
    public PrivatePathNode(int uid) {
        super(uid);
        front = null;
    }
    public boolean setFront(PathNode front)
    {
        if (front == null)
            return false;
        this.front = front;
        return true;
    }

    @Override
    public boolean layoutAircraft(Aircraft aircraft) {
        if (next == null)
        {
            System.out.println("arrive");
            aircraft.setCanFly(false);
            aircraft.arrive();
            return true;
        }
        aircrafts.add(aircraft);
        aircraft.layout();
        return true;
    }

    @Override
    public PathNode next(Aircraft aircraft) {
        if (aircraft.getFlyOrder() == Aircraft.CLOCKWISE)
        {
            if (next == null)
            {
                aircraft.setFlyOrder(Aircraft.ANTICLOCKWISE);
            }
            else {
                return next;
            }
        }
        return front;
    }

    @Override
    public int stepsContinue(Aircraft aircraft,int times) {
        if (next == null)
            return 1;
        return 0;
    }

    @Override
    protected void destoryAircrafts(int uid) {
        super.destoryAircrafts(uid);
    }

    @Override
    public int startSteps() {
        if (Map.getInstance().getBelowSuperFly((uid+2) % 4) == this)
        {
            return super.startSteps();
        }
        return 1;
    }
}
