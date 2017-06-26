package game;



/**
 * Created by like1 on 2017/4/13.
 */
public class PrivatePath {
    private PrivatePathNode[] start;
    private int[] vStart;
    public PrivatePath(PublicPath publicPath)
    {
        vStart = new int[4];
        start = new PrivatePathNode[4];
        for (int i = 0;i<4;i++) {
            start[i] = new PrivatePathNode(i);
        }
        for (int i = 0;i<4;i++)
        {
            System.out.println(publicPath.getPrivateEnter(i).setPrivateNext(start[(i+1)%4]));
        }
        createPrivatePath();
    }

    private void createPrivatePath()
    {
        PrivatePathNode pointer ;
        for (int i = 0;i<start.length;i++)
        {

            pointer = start[i];
            PrivatePathNode front = null;
            for (int j = 1;j<=5;j++)
            {
                if (j == 5)
                    pointer.setNext(new PrivatePathNode(i));
                else
                    pointer.setNext(new PrivatePathNode(i));
                pointer.setFront(front);
                front = pointer;
                pointer = (PrivatePathNode)pointer.next;
            }
            pointer.setFront(front);
        }

    }
    public PathNode getBelowSuperFly(int uid)
    {
        return start[uid].next.next;
    }
}
