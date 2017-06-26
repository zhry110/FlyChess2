package com.zhry.like1.flychess.data;

import android.os.Handler;
import android.util.Log;

import com.zhry.like1.flychess.view.PathNodeView;

/**
 * Created by like1 on 2017/4/13.
 */
public class PrivatePath {
    private PrivatePathNode[] start;
    private PathNodeView[] views;
    private int[] vStart;
    public PrivatePath(PathNodeView[] views, PublicPath publicPath, Handler handler)
    {
        this.views = views;
        vStart = new int[4];
        start = new PrivatePathNode[4];
        for (int i = 0;i<4;i++) {
            start[i] = new PrivatePathNode(i, views[(vStart[i] = (3 - i) * 5)],handler);
        }
        for (int i = 0;i<4;i++)
        {
            Log.i("d",""+publicPath.getPrivateEnter(i).setPrivateNext(start[(i+1)%4]));
        }
        createPrivatePath(handler);
    }

    private void createPrivatePath(Handler handler)
    {
        PrivatePathNode pointer ;
        for (int i = 0;i<start.length;i++)
        {

            pointer = start[i];
            PrivatePathNode front = null;
            for (int j = 1;j<=5;j++)
            {
                if (j == 5)
                    pointer.setNext(new PrivatePathNode(i,null,handler));
                else
                    pointer.setNext(new PrivatePathNode(i,views[(vStart[i]+j)],handler));
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
