package com.zhry.like1.flychess.listener;

import android.view.View;
import android.view.View.OnClickListener;

import com.zhry.like1.flychess.data.Aircraft;
import com.zhry.like1.flychess.data.Map;
import com.zhry.like1.flychess.data.NetPlayer;
import com.zhry.like1.flychess.data.PathNode;
import com.zhry.like1.flychess.data.Player;
import com.zhry.like1.flychess.view.PathNodeView;

/**
 * Created by like1 on 2017/4/29.
 */

public class PathNodeClickListener implements OnClickListener, Runnable {
    private static PathNodeClickListener pathNodeClickListener;
    private Aircraft aircraft;
    private Player player;
    private Thread flyThread;
    private boolean clickOver = true;
    private NetPlayer netPlayer;

    public PathNodeClickListener(NetPlayer netPlayer) {
        this.netPlayer = netPlayer;
    }

    @Override
    public void onClick(View v) {
        System.out.println("click pathNode");
        if (!clickOver) {
            System.out.println("last click is not over");
            return;
        }
        clickOver = false;
        PathNode pathNode = ((PathNodeView) v).getPathNode();
        if (pathNode == null) {
            clickOver = true;
            System.out.println("pathnode is null");
            return;
        }
        player = Map.getInstance().getCurPlayer();
        if (player == null) {
            clickOver = true;
            System.out.println("curPlayer is null");
            return;
        }
        if (pathNode.getAircrafts().size() == 0) {
            clickOver = true;
            System.out.println("there is no aircraft");
            return;
        }
        if (!player.canTouch())
        {
            clickOver = true;
            System.out.println("your can't touch when other's turn");
            return;
        }
        aircraft = pathNode.getAircrafts().get(0);
        if (netPlayer != null)
        {
            if (player.getUid() != netPlayer.getUid())
            {
                clickOver = true;
                System.out.println("netPlayer id:" +netPlayer.getUid() +"\ncurPlayer id:"+player.getUid());
                System.out.println("not netplayer");
                return;
            }
        }
        if (aircraft.getUid() == Map.getInstance().getCurPlayer().getUid() && player.isDiced()) {
            if (netPlayer != null)
            {
                netPlayer.fly(aircraft.getId());
                clickOver = true;
            }
            else {
            flyThread = new Thread(this);
            flyThread.start();
            }
        } else {
            if (!player.isDiced()) {
                System.out.println("player not dice");
            }
            clickOver = true;
        }

    }

    public static PathNodeClickListener getInstance(NetPlayer netPlayer) {
        if (pathNodeClickListener == null) {
            return pathNodeClickListener = new PathNodeClickListener(netPlayer);
        }
        if (pathNodeClickListener.netPlayer != netPlayer)
        {
            return pathNodeClickListener = new PathNodeClickListener(netPlayer);
        }
        return pathNodeClickListener;
    }

    @Override
    public void run() {
        if (aircraft.atHome()) {
            if (player.getDice() == 6) {
                aircraft.setCanFly(true);
                aircraft.fly(1);
            } else {
                clickOver = true;
                return;
            }
        } else {
            aircraft.setCanFly(true);
            aircraft.fly(player.getDice());
        }
        player.finishFly();
        if (!player.isCanDice()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            player.setTurnIsOver();
        }
        clickOver = true;
    }
}
