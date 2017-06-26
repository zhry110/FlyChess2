package com.zhry.like1.flychess.view;

import android.os.Handler;
import android.os.Message;

import com.zhry.like1.flychess.data.Dice;
import com.zhry.like1.flychess.data.Map;

/**
 * Created by like1 on 2017/6/16.
 */

public class DiceThread extends Thread {
    private Handler handler;
    public DiceThread(Handler handler)
    {
        this.handler = handler;
    }
    @Override
    public void run() {
        super.run();
        Dice dice = new Dice();
        for (int i = 0;i < 4;i++)
        {
            Message msg = handler.obtainMessage();
            msg.obj = new Integer(dice.dicing());
            msg.what = 5;
            handler.sendMessage(msg);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
