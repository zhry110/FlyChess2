package game;



/**
 * Created by like1 on 2017/4/12.
 */
public class PublicPath {
    public static final int PATH_SIZE = 52;
    private PathNode[] path;
    private ConnectNode[] privateEnter;
    private KeyPathNode[] keys;
    private int[] vStart;
    public PublicPath()
    {
        path = new PathNode[4];
        privateEnter = new ConnectNode[4];
        vStart = new int[4];
        path[0] = new PathNode(3);
        path[1] = new PathNode(0);
        path[2] = new PathNode(1);
        path[3] = new PathNode(2);
        keys = new KeyPathNode[4];
        createPath();
    }
    public PathNode getStart(int uid)
    {
        return path[uid];
    }
    private void createPath()
    {
        int base;
        for (int i = 0;i<4;i++)
        {
            PathNode pointer = path[i];
            for (int j = 1;j<=12;j++)
            {
                if (j == 4)
                {
                    KeyPathNode k = new KeyPathNode((pointer.getUid()+1)%4);
                    keys[i] = k;
                    pointer.setNext(k);
                }
                else if (j == 10) {
                    ConnectNode c = new ConnectNode((pointer.getUid()+1)%4);
                    pointer.setNext(c);
                    privateEnter[i] = c;
                }
                else
                    pointer.setNext(new PathNode((pointer.getUid()+1)%4));
                System.out.println(j);
                pointer = pointer.next;
            }
            pointer.setNext(path[(i+1)%4]);
        }
        for (int i = 0;i<4;i++)
           keys[i].setSuperNext(path[(i+1)%4].next.next.next);
    }
    public ConnectNode getPrivateEnter(int uid) {
        return  privateEnter[uid];
    }

    public void showPath()
    {
        PathNode pointer = path[0];
        int i = 0;
        while (pointer!=null) {
            i++;
            System.out.print(pointer.getUid() + " ");
            pointer = pointer.next(null);
            if (pointer == path[0])
                break;
        }
        System.out.println();
        System.out.println(i);
    }
}
