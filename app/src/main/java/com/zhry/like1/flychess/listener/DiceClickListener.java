package com.zhry.like1.flychess.listener;

import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;

import com.zhry.like1.flychess.data.Aircraft;
import com.zhry.like1.flychess.data.Map;
import com.zhry.like1.flychess.data.NetPlayer;
import com.zhry.like1.flychess.data.Player;

/**
 * Created by like1 on 2017/4/30.
 */

public class DiceClickListener implements ImageView.OnClickListener {
    private Player player;
    private NetPlayer netPlayer;

    public DiceClickListener(NetPlayer netPlayer)
    {
        this.netPlayer = netPlayer;
    }
    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        if ((player = Map.getInstance().getCurPlayer()) != null) {
            if (netPlayer != null) {
                if (player.getUid() != netPlayer.getUid()) {
                    System.out.println("not your turn");
                    return;
                }
            }
            if (!player.canTouch())
            {
                v.setEnabled(true);
                return;
            }
            if (!player.isCanDice() || !player.isFlyed()) {
                if (!player.isFlyed()) {
                    System.out.println("not fly");
                } else {
                    System.out.println("can not dice");
                }
                v.setEnabled(true);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    player.dice();
                }
            }).start();
        }

    }
}
