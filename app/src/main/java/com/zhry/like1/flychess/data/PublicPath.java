package com.zhry.like1.flychess.data;

import android.os.Handler;
import android.util.Log;

import com.zhry.like1.flychess.view.PathNodeView;

/**
 * Created by like1 on 2017/4/12.
 */
public class PublicPath {
    public static final int PATH_SIZE = 52;
    private PathNode[] path;
    private ConnectNode[] privateEnter;
    private KeyPathNode[] keys;
    private PathNodeView[] views;
    private int[] vStart;
    public PublicPath(PathNodeView[] views, Handler handler)
    {
        path = new PathNode[4];
        privateEnter = new ConnectNode[4];
        this.views = views;
        vStart = new int[4];
        path[0] = new PathNode(3,views[(vStart[0]=0)],handler);
        path[1] = new PathNode(0,views[(vStart[1]=13)],handler);
        path[2] = new PathNode(1,views[(vStart[2]=26)],handler);
        path[3] = new PathNode(2,views[(vStart[3]=39)],handler);
        keys = new KeyPathNode[4];
        createPath(handler);
    }
    public PathNode getStart(int uid)
    {
        return path[uid];
    }
    private void createPath(Handler handler)
    {
        int base;
        for (int i = 0;i<4;i++)
        {
            PathNode pointer = path[i];
            for (int j = 1;j<=12;j++)
            {
                if (j == 4)
                {
                    KeyPathNode k = new KeyPathNode((pointer.getUid()+1)%4,views[(vStart[i]+j)],handler);
                    keys[i] = k;
                    pointer.setNext(k);
                }
                else if (j == 10) {
                    ConnectNode c = new ConnectNode((pointer.getUid()+1)%4,views[(vStart[i]+j)],handler);
                    pointer.setNext(c);
                    privateEnter[i] = c;
                    Log.i("ddd",""+privateEnter[i].getUid()+" "+i);
                }
                else
                    pointer.setNext(new PathNode((pointer.getUid()+1)%4,views[(vStart[i]+j)],handler));
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
