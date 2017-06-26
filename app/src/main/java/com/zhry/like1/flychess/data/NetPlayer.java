package com.zhry.like1.flychess.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zhry.like1.flychess.GameActivity;
import com.zhry.like1.flychess.MainActivity;
import com.zhry.like1.flychess.RoomActivity;
import com.zhry.like1.flychess.Schedule;
import com.zhry.like1.flychess.ServerGameActivity;
import com.zhry.like1.flychess.listener.PathNodeClickListener;
import com.zhry.like1.flychess.net.Protocol;
import com.zhry.like1.flychess.net.SocketPrint;
import com.zhry.like1.flychess.server.LocalServer;
import com.zhry.like1.flychess.server.Server;
import com.zhry.like1.flychess.server.ServerInfo;
import com.zhry.like1.flychess.sqlite.GameDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import game.*;

/**
 * Created by like1 on 2017/5/3.
 */

public class NetPlayer extends Player implements Serializable {
    private boolean depute = false;
    private Socket socket;
    private boolean isServer ;
    private InputStream in;
    private OutputStream out;
    private boolean prepare;
    protected boolean host;
    private static String serverAddress;
    private static int port;
    protected boolean inRoom = false;
    private LinkedBlockingQueue<Protocol> protocols;
    private Thread operatThread = null;
    protected GameDatabase gameDatabase;
    protected Handler roomHandler;
    private boolean isOver = false;
    protected NetPlayer(Socket socket,boolean isServer,Handler handler) throws IOException {
        super(0, null,null);
        prepare = false;
        host = false;
        this.roomHandler = handler;
        this.socket = socket;
        if (socket != null)
        {
            socket.setSoTimeout(0);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }
        protocols = new LinkedBlockingQueue<>();
        this.isServer = isServer;
        Activity activity = MainActivity.getInstance();
        if (socket != null)
        {
            if (activity != null )
                gameDatabase = new GameDatabase(activity,System.currentTimeMillis(),isServer);
            else {
                handler.sendEmptyMessage(-4);
                System.out.println("MainActivity destoryed");
                return;
            }
        }
    }
    public void prepare() {
        send(Protocol.createPacket((byte) 1, Server.PREPARE, (byte) 1, null));
    }

    public void unPrepare() {
        send(Protocol.createPacket((byte) 1, Server.UNPREPARE, (byte) 1, null));
    }

    public void start() {
        prepare();
        send(Protocol.createPacket((byte) 1, Server.START, (byte) 1, null));
        roomHandler.sendEmptyMessage(4);
    }

    public void leftRoom() {
        byte[] data = new byte[]{(byte) 0xff};//break remote read()
        send(data);
        System.out.println("left room");
        inRoom = false;
    }
    public void  leftMap()
    {
        isOver = true;
        if (operatThread != null) {
            operatThread.interrupt();
            System.out.println("interput opt thread");
        }
    }

    public void fly(int aid) {
        byte[] aids = new byte[]{2, (byte) aid};
        System.out.println("fly");
        send(Protocol.createPacket((byte) 1, Server.FLY, (byte) 1, aids));
    }
    public static void JoinRoom(final InetAddress address, String name,Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(address, Server.port);
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    byte[] data = new byte[100];
                    byte[] names = new byte[name.getBytes().length+1];
                    names[0] = (byte) names.length;
                    for (int i = 0; i < names.length-1;i++)
                    {
                        names[i+1] = name.getBytes()[i];
                    }
                    if (!send(Protocol.createPacket((byte) 1, Server.JOIN, (byte) 1, names), out))
                        return;
                    data = new byte[100];
                    if (!read(data, in, 2))
                        return;
                    Protocol protocol = new Protocol(data);
                    if (protocol.getOpt() != Server.JOIN || !protocol.isPermit()) {
                        handler.sendEmptyMessage(-3);
                        return;
                    }

                    System.out.println("joined");

                    NetPlayer netPlayer = getInstance(socket,false,handler);
                    netPlayer.inRoom = true;
                    if (netPlayer == null)
                    {
                        handler.sendEmptyMessage(-3);
                        return;
                    }
                    netPlayer.gameDatabase.addData(data);
                    netPlayer.resignUid(protocol.getData()[1]);
                    System.out.println("netplayer's uid:"+netPlayer.getUid());
                    System.out.println("uid " + (int) protocol.getData()[1]);
                    if (protocol.getData()[1] == 2)
                        netPlayer.host = true;
                    Message msg =handler.obtainMessage();
                    msg.obj = netPlayer;
                    msg.what = -2;
                    handler.sendMessage(msg);
                    handler.sendEmptyMessage(4);
                    netPlayer.startService();

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    private void send(byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!send(data, out))
                    System.out.println("server dead\n");
            }
        }).start();
    }

    private boolean read(byte[] buf) {
        return read(buf, in, 2);
    }

    private static boolean send(byte[] data, OutputStream out) {
        if (out == null)
            return false;
        try {
            out.write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean read(byte[] buf, InputStream in, int v2) {
        int len;
        if (in == null)
            return false;
        try {
            len = in.read();
            if (len <= 0)
                return false;
            byte[] data = new byte[len];
            data[0] = (byte) len;
            if (in.read(data, 1, len - 1) <= 0) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                buf[i] = data[i];
            }
            return true;
        } catch (IOException e) {
            return false;
        }

    }
    public static NetPlayer getInstance(Socket socket,boolean server,Handler handler) {

        try {
            return new NetPlayer(socket,server,handler);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    protected  void startService() {
        byte[] data = new byte[100];
        Protocol protocol = null;
        operatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isOver)
                {
                    try {
                        Protocol p = protocols.take();
                        handleAck(p);
                        if (protocols.size() == 0&&!inRoom)
                        {
                            break;
                        }
                        System.out.println("take a packet");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        operatThread.start();
        while (inRoom) {
            if (in != null) {
                if (read(data)) {
                    protocol = new Protocol(data);
                    if (!protocol.isRequest())
                    {
                        if (protocol.isPermit())
                        {
                            gameDatabase.addData(data);
                        }
                        protocols.offer(protocol);
                        System.out.println("recv a packet opt:"+protocol.getOpt());
                        if (protocol.getOpt() == 5)
                            System.out.println(protocol.getData()[1]);
                    }
                    else
                    {
                        handleRequest(protocol);
                    }
                } else {
                    inRoom = false; //not connected
                }
            } else {
                inRoom = false;
            }
        }
        loseConnect();
    }

    protected void loseConnect() {
        System.out.println("lose host");
        if (roomHandler != null)
        {
            roomHandler.sendEmptyMessage(-1);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        gameDatabase.destory();
    }

    protected void handleAck(Protocol protocol) {
        switch (protocol.getOpt()) {
            case Server.PREPARE:
                if (protocol.isPermit()) {
                    System.out.println("prepare succeed");
                    prepare = true;
                } else
                    System.out.println("prepare failed");
                break;
            case Server.UNPREPARE:
                if (protocol.isPermit()) {
                    prepare = false;
                    System.out.println("unprepare succeed");
                } else
                    System.out.println("unprepare failed");
                break;
            case Server.PLAYERCHANGEDADD:
                byte[] data = protocol.getData();
                Player.Struct playerInfo = null;
                if (protocol.getData() != null) {
                    if (protocol.getData()[0] > 2) {
                        playerInfo = new Player.Struct(new String(protocol.getData(), 2, protocol.getData()[0] - 2),
                                protocol.getData()[1]);
                    }
                }
                Message msg = roomHandler.obtainMessage();
                msg.obj = playerInfo;
                msg.what = 0;
                roomHandler.sendMessage(msg);
                System.out.println("player add");
                break;
            case Server.PLAYERCHANGEDLEFT:
                if (protocol.getData() != null) {
                    if (protocol.getData()[0] == 2) {
                        msg = roomHandler.obtainMessage();
                        msg.obj = new Integer(protocol.getData()[1]);
                        msg.what = 1;
                        roomHandler.sendMessage(msg);
                    }
                }
                break;
            case Server.PLAYERSTATECHANGE:
                if (protocol.getData() != null) {
                    if (protocol.getData()[0] == 3) {
                        msg = roomHandler.obtainMessage();
                        msg.obj = new Integer(protocol.getData()[2]);
                        if (protocol.getData()[1] == 1)
                            msg.what = 2;
                        else
                            msg.what = 3;
                        roomHandler.sendMessage(msg);
                    }
                }
                break;
            case Server.START:
                if (protocol.isPermit()) {
                    System.out.println("start succed");
                    msg = roomHandler.obtainMessage();
                    msg.what = 5;
                    msg.obj = new Integer(protocol.getData()[1]);
                    setUid(protocol.getData()[2]);
                    roomHandler.sendMessage(msg);
                } else {
                    System.out.println("start failed");
                    unPrepare();
                }
                break;
            case Server.TURN:
                if (protocol.isPermit()) {
                    System.out.println("recv turn");
                    //new Schedule().start();
                    LocalServerMap.getInstance().schedule(0,0);
                }
                break;
            case Server.STARTGAME:
                if (protocol.isPermit()) {
                    LocalServerMap.getInstance().startGame(protocol.getData()[1]);
                }
                break;
            case Server.DICE:
                System.out.println("recv dice from server");
                if (protocol.isPermit()) {
                    LocalServerMap.getInstance().setCurrentDice(protocol.getData()[2]);
                    if (protocol.getData()[1] == getUid()) {
                        System.out.println("recv server dice");
                        super.dice();
                    } else {
                        Map.getInstance().getCurPlayer().dice();
                    }
                }
                break;
            case Server.FLY:
                System.out.println("fly");
                if (protocol.isPermit()) {
                    int auid = protocol.getData()[1];
                    int aid = protocol.getData()[2];
                    /*while (auid != Map.getCurPlayer().getUid()) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }*/
                    Map.getInstance().getAircrafts(auid)[aid].setCanFly(true);
                    Map.getInstance().getAircrafts(auid)[aid].fly(protocol.getData()[3]);
                    Map.getInstance().getCurPlayer().finishFly();
                    if (!Map.getInstance().getCurPlayer().isCanDice())
                        Map.getInstance().getCurPlayer().setTurnIsOver();
                } else {
                    if (protocol.getData()[1] == getUid()) {
                        System.out.println("server not permit fly");
                        provider.getAircrafts(getUid())[protocol.getData()[2]].rollBack();
                    }
                }
                break;
            default:
                System.out.println("unknow opt " + protocol.getOpt());
        }
    }

    protected void handleRequest(Protocol protocol) {
        switch (protocol.getOpt()) {
            default:
                send(Protocol.createPacket((byte) 0, Server.ALIVE, (byte) 1, null));
        }
    }

    public boolean isPrepare() {
        return prepare;
    }

    public boolean isHost() {
        return host;
    }

    @Override
    public void dice() {
        send(Protocol.createPacket((byte) 1, Server.DICE, (byte) 1, null));
    }

    public void sendReadyMsg() {
        send(Protocol.createPacket((byte) 1, Server.READY, (byte) 1, null));
    }

    public void sendAddbot() {
        send(Protocol.createPacket((byte) 1,Server.ADDBOT,(byte) 1,null));
    }

    public void depute()
    {
        depute = true;
        send(Protocol.createPacket((byte) 1,Server.DEPUTEON,(byte) 1,null));
    }

    public void noDepute()
    {
        depute = false;
        send(Protocol.createPacket((byte) 1,Server.DEPUTEOFF,(byte) 1,null));
    }

    @Override
    public boolean resignUid(int uid) {
        super.resignUid(uid);
        return true;
    }

    public void toggleDepute() {
        if (depute)
        {
            noDepute();
        }
        else
        {
            depute();
        }
    }

    @Override
    public boolean canTouch() {
        return !depute;
    }

    public static void scanRoomsFromServer(Handler handler)
    {
        Socket socket = null;
        System.out.println("scan start");
        try {
            socket = new Socket(Server.serverAddress,Server.port);
            socket.setSoTimeout(5000);
            send(Protocol.createPacket((byte) 1,Server.SCAN,(byte) 1,null),socket.getOutputStream());
            byte[] data = new byte[1024];
            while (read(data,socket.getInputStream(),0)) {
                if (data[0] < 11)
                    continue;
                int id = 0;
                for (int i = 0;i < 4;i++)
                {
                    id = id | data[5+i]<<i*8;
                }

                ServerInfo serverInfo = new ServerInfo(new String(data,11,(int)data[0]-11),id,data[9],data[10]);
                System.out.println("scan: id:" + id +"\n"+new String(data,11,(int)data[0]-11));
                System.out.println("type:"+serverInfo.getType());
                System.out.println("players:"+serverInfo.getPlayers());
                Message msg = handler.obtainMessage();
                msg.what = 0;
                msg.obj = serverInfo;
                handler.sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void JoinRoom(int id,String playerName,Handler handler) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("join room");
                    SocketPrint socket = new SocketPrint(Server.serverAddress, Server.port);
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    byte[] data = new byte[100];
                    int pos = 1;
                    data[pos++] =  (byte) id;
                    data[pos++] =  (byte) (id >> 8);
                    data[pos++] =  (byte) (id >> 16);
                    data[pos++] =  (byte) (id >> 24);
                    byte[] names = playerName.getBytes();
                    for (int i = 0;i < names.length && i < 20;i++)
                    {
                        data[pos++] = names[i];
                    }
                    data[0] = (byte) pos;
                    System.out.println("jaja");
                    if (!send(Protocol.createPacket((byte) 1, Server.JOIN, (byte) 1, data), out))
                    {
                        System.out.println("send error");
                        return;
                    }
                    data = new byte[100];
                    if (!read(data, in, 2))
                        return;
                    Protocol protocol = new Protocol(data);
                    if (protocol.getOpt() != Server.JOIN || !protocol.isPermit()) {
                        handler.sendEmptyMessage(-3);
                        System.out.println("server not permit join room");
                        return;
                    }
                    System.out.println("joined");

                    NetPlayer netPlayer = getInstance(socket,true,handler);
                    netPlayer.inRoom = true;
                    if (netPlayer == null)
                    {
                        System.out.println("Netplayer is null");
                        handler.sendEmptyMessage(-3);
                        return;
                    }
                    netPlayer.gameDatabase.addData(data);
                    netPlayer.resignUid(protocol.getData()[1]);
                    System.out.println("netplayer's uid:"+netPlayer.getUid());
                    System.out.println("uid " + (int) protocol.getData()[1]);
                    if (protocol.getData()[1] == 2)
                        netPlayer.host = true;
                    Message msg = handler.obtainMessage();
                    msg.obj = netPlayer;
                    msg.what = -2;
                    handler.sendMessage(msg);
                    handler.sendEmptyMessage(4);
                    netPlayer.startService();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        });
        t.start();
        /*try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public Socket getSocket() {
        return socket;
    }

    public void setHost() {
        host = true;
    }

    public GameDatabase getGameDatabase() {
        return gameDatabase;
    }
    public void goOn()
    {
        System.out.println("goOn netPlayer");
    }
    public void doSomeThing(GameActivity gameActivity)
    {
        if (gameActivity == null)
        {
            return;
        }
    }
    public static void createHome(String name, Context context)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketPrint socket = new SocketPrint(Server.serverAddress, Server.port);
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    byte[] data = new byte[100];
                    int pos = 0;
                    data[pos++] = (byte) (name.getBytes().length + 1);
                    for (int i = 0;i < 20 && i< name.getBytes().length;i++)
                    {
                        data[pos++] = name.getBytes()[i];
                    }
                    send(Protocol.createPacket((byte)1,Server.NEHHOME,(byte) 1,data),out);
                    read(data,in,0);
                    Protocol p = new Protocol(data);
                    byte[] ids = p.getData();
                    int id = 0;
                    for (int i = 0;i < 4;i++)
                    {
                        id = id | ids[1+i] << i*8;
                    }
                    if (p.isPermit())
                    {
                        RoomActivity.setRoomID(id);
                        Intent i = new Intent();
                        i.setClass(context, RoomActivity.class);
                        context.startActivity(i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
