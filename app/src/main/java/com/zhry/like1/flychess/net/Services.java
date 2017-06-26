package com.zhry.like1.flychess.net;

import android.support.annotation.NonNull;

import com.zhry.like1.flychess.data.Player;
import com.zhry.like1.flychess.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by like1 on 2017/5/16.
 */

public class Services implements Runnable{
    private Socket socket;
    private InputStream in;
    public Services(@NonNull Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
    }

    @Override
    public void run() {
        while (true)
        {
            byte[] buf = new byte[100];
            int len = 0, tlen;
            try {
                while ((tlen = in.read(buf, len, buf.length - len)) > 0) {
                    len += tlen;
                    if (len == buf[0])
                        break;
                }
                switch (buf[1])
                {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
