package com.zhry.like1.flychess.brodcastreciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhry.like1.flychess.server.Server;

/**
 * Created by like1 on 2017/5/1.
 */

public class WifiStateReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Server.wifiChanged();
    }
}
