package com.zhry.like1.flychess.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by like1 on 2017/4/26.
 */

public class Server {
    private static String localAddress = null;
    private ServerSocket serverSocket;

    public static final String serverAddress = "115.159.82.245" ;
    public static final int port = 10006;

    public static final byte FIND = 0X00;
    public static final byte SCAN = 0X01;//(char)len(char)0x01
    public static final byte JOIN = 0X02;//(char)len(char)0x02(char)username
    public static final byte PREPARE = 0X03;
    public static final byte UNPREPARE = 0x04;
    public static final byte DICE = 0X05;
    public static final byte FLY = 0X06;
    public static final byte START = 0X07;
    public static final byte READY = 0X08;
    public static final byte HOSTPLAYERLEFT = 0X09;
    public static final byte SCHEDULE = 0XA;
    public static final byte SOMEPLAYERREADY = 0XB;
    public static final byte TURN = 0XC;
    public static final byte PLAYERCHANGEDADD = 0XD;
    public static final byte PLAYERCHANGEDLEFT = 0XE;
    public static final byte ALIVE = 0XF;
    public static final byte PLAYERSTATECHANGE = 0X10;
    public static final byte STARTGAME = 0X11;
    public static final byte ADDBOT = 0X12;
    public static final byte DEPUTEON = 0X13;
    public static final byte DEPUTEOFF = 0X14;

    public static final byte NEHHOME = 0x15;
    public Server() throws IOException {


    }
    public static String getLocalNetAddress()
    {
        if (localAddress != null)
            return localAddress;
        try{
            Enumeration<NetworkInterface> interfaceList=NetworkInterface.getNetworkInterfaces();
            if(interfaceList==null){
                System.out.println("--No interface found--");
                return null;
            }
            else{
                while(interfaceList.hasMoreElements()){
                    NetworkInterface iface = interfaceList.nextElement();
                    //System.out.println("Interface "+iface.getName()+":");
                    if (!iface.getName().equals("wlan0"))
                        continue;
                    Enumeration<InetAddress> addrList=iface.getInetAddresses();
                    if(!addrList.hasMoreElements()){
                        System.out.println("\t(No address for this address)");
                    }
                    while(addrList.hasMoreElements()){
                        InetAddress address = addrList.nextElement();
                        if (address instanceof Inet4Address)
                        {
                            return localAddress = address.getHostAddress();
                        }
                        System.out.print("\tAddress "+((address instanceof InetAddress? "v4"
                                :(address instanceof Inet6Address ? "(v6)":"(?)"))));
                        System.out.println(":"+address.getHostAddress());
                    }
                }
            }
        }
        catch(SocketException e){
            System.out.println("Error getting network interfaces:"+e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public static String getLocalNetBroadcastAddress()
    {
        String addr = getLocalNetAddress();
        if (addr == null)
            return addr;
        int pointPos = addr.lastIndexOf(".");
        return addr.substring(0,pointPos)+".255";
    }
    public static void wifiChanged()
    {
        localAddress = null;
    }
}
