package game;



/**
 * Created by like1 on 2017/4/19.
 */

public class Home {
    private PathNode[] homes;
    public Home(PublicPath common)
    {
        homes = new PathNode[20];
        for (int i = 0;i<20;i++)
        {
            if (i%5 == 0)
                homes[i] = new PathNode(Player.USER_ALL);
            else
                homes[i] = new PathNode(i/5);
            if (i%5 != 0)
            {
                homes[i].setNext(homes[(i/5)*5]);
            }
            else
            {
                homes[i].setNext(common.getStart(i/5));
            }
        }
    }

    public PathNode[] getHome() {
        return homes;
    }
}
