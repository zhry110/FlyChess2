package com.zhry.like1.flychess.server;

/**
 * Created by like1 on 2017/6/17.
 */

public class ServerInfo {
    private String name;
    private int id;
    private int type;
    private int players;
    public ServerInfo(String name,int id,int type,int players)
    {
        this.name = name;
        this.id = id;
        this.type = type;
        this.players = players;
    }

    public int getId() {
        return id;
    }

    public int getPlayers() {
        return players;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
