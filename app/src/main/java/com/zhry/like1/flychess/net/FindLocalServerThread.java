package com.zhry.like1.flychess.net;

import android.content.pm.ProviderInfo;
import android.os.Handler;
import android.os.Message;

import com.zhry.like1.flychess.server.LocalServer;
import com.zhry.like1.flychess.server.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by like1 on 2017/5/17.
 */

public class FindLocalServerThread extends Thread {
    private Handler handler;
    private DatagramPacket datagramPacket;
    private DatagramSocket socket;
    private boolean exit = false;
    private static String host = Server.getLocalNetAddress();
    private byte[] data;
    private byte[] scanData;
    private static FindLocalServerThread instance;
    private boolean wait = false;
    public FindLocalServerThread(Handler handler) throws SocketException {
        this.handler = handler;
        data = new byte[100];
        scanData = Protocol.createPacket((byte) 1, Server.SCAN, (byte) 1, null);
        socket = new DatagramSocket(10005);
        socket.setSoTimeout(2000);
        datagramPacket = new DatagramPacket(data, data.length);
        instance = this;
    }

    @Override
    public void run() {
        long last = System.currentTimeMillis();
        long current = last;
        Message msg;
        super.run();
        while (!exit) {
            synchronized (this) {
                while (wait) {
                    try {
                        System.out.println("pause");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                reciveServerAck(socket, datagramPacket);
                current = System.currentTimeMillis();
                if (current - last > 2000)
                {
                    for (LocalServer l:LocalServer.getLocalServers().values()) {
                        if (!l.isFreshed()) {
                            msg = handler.obtainMessage();
                            msg.what = 1;
                            msg.obj = l;
                            handler.sendMessage(msg);
                        }
                    }
                    for (LocalServer l:LocalServer.getLocalServers().values())
                        l.setFreshed(false);
                    last = current;
                }
            } catch (IOException e) {
                LocalServer.updateLocalServers(null, handler);
                e.printStackTrace();
            }
        }
        System.out.println("find server over");
        socket.close();
    }
    public void pause()
    {
        wait = true;
    }
    public synchronized void wake()
    {
        wait = false;
        notify();
    }
    /*private boolean scanHost(String host)
        {
            DatagramPacket datagramPacket = null;
            DatagramSocket socket = null;
            try {
                LocalServer.getLocalServers().clear();
                datagramPacket = new DatagramPacket(scanData,scanData.length, InetAddress.getByName(host+".255"),10006);
                socket = new DatagramSocket();
                socket.send(datagramPacket);
                socket.close();
                return true;
            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }*/
    private void reciveServerAck(DatagramSocket socket, DatagramPacket datagramPacket) throws IOException {
        socket.receive(datagramPacket);
        if (datagramPacket.getLength() >= 8) {
            Protocol protocol = new Protocol(datagramPacket.getData());
            if (protocol.getOpt() != Server.SCAN || !protocol.isPermit()) {
                return;
            }
            LocalServer localServer = new LocalServer(datagramPacket.getAddress(), protocol.getData()[1], protocol.getData()[2],
                    new String(protocol.getData(), 3, protocol.getData().length - 4));
            LocalServer.updateLocalServers(localServer, handler);
        }
    }

    public static synchronized FindLocalServerThread getInstance() {
        return instance;
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    public void exit()
    {
        exit = true;
        wake();
    }
}
