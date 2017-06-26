package com.zhry.like1.flychess.net;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by like1 on 2017/6/18.
 */

public class SocketPrint extends Socket {
    public SocketPrint(String address,int port) throws IOException {
        super(address,port);
    }
    @Override
    public synchronized void close() throws IOException {
        super.close();
        System.out.println("---------------------------------------closed-------------------------------");
    }
}
