package com.zhry.like1.flychess.server;

import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;

import com.zhry.like1.flychess.RoomActivity;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Created by like1 on 2017/5/15.
 */

public class LocalServer {
    private InetAddress address;
    private int gameType;
    private int curPlayers;
    private String name;
    private static Map<InetAddress,LocalServer> serversMap = new HashMap<>();
    private LinearLayout linearLayout;
    private boolean freshed ;
    public LocalServer(InetAddress address, int gameType, int curPlayers, String name)
    {
        this.address = address;
        this.gameType = gameType;
        this.curPlayers = curPlayers;
        this.name = name;
        freshed = true;
    }

    public void setLinearLayout(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }

    public int getGameType() {
        return gameType;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getCurPlayers() {
        return curPlayers;
    }

    public String getName() {
        return name;
    }

    public static Map<InetAddress,LocalServer> getLocalServers() {
        return serversMap;
    }

    public static void updateLocalServers(LocalServer server, Handler handler)
    {
        if (server == null) {
            System.out.println("null");
            serversMap.clear();
            handler.sendEmptyMessage(2);
            return;
        }
        Message msg;
        if (serversMap.get(server.getAddress()) != null)
        {
            //System.out.println("add top");
            LocalServer localServer = serversMap.get(server.getAddress());
            if (!server.equals(localServer))
            {
                localServer.ObjectCopy(server);
            }
            msg = handler.obtainMessage();
            msg.what = 0;
            msg.obj = serversMap.get(server.getAddress());
            handler.sendMessage(msg);
            localServer.setFreshed(true);
        }
        else
        {
            System.out.println("add");
            server.setFreshed(true);
            serversMap.put(server.getAddress(),server);
            msg = handler.obtainMessage();
            msg.what = 0;
            msg.obj = server;
            handler.sendMessage(msg);

        }
    }

    public boolean hasView() {
        return linearLayout != null;
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    @Override
    public boolean equals(Object obj) {
        if (!((LocalServer)obj).name.equals(name))
            return false;
        if (((LocalServer)obj).gameType != gameType)
            return false;
        if (((LocalServer)obj).curPlayers != curPlayers)
            return false;
        return true;
    }
    private void ObjectCopy(LocalServer l2)
    {
        name = l2.name;
        gameType = l2.gameType;
        curPlayers = l2.curPlayers;
        address = l2.address;
    }

    public void setFreshed(boolean freshed) {
        this.freshed = freshed;
    }

    public boolean isFreshed() {
        return freshed;
    }

    public static void clearServersMap() {
        LocalServer.serversMap.clear();
    }
}
