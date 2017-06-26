package com.zhry.like1.flychess.data;

/**
 * Created by like1 on 2017/6/21.
 */

public class Cmd {
    private int uid, aid, dice;

    public Cmd(String s) {


        if (s.startsWith("player")) {
            s = s.substring(7);
            uid = new Integer(s.charAt(0) - 48);
            s = s.substring(2);
            if (s.startsWith("move")) {
                s = s.substring(5);
                aid = new Integer(s.charAt(0) - 48);
                s = s.substring(2);
                if (s.startsWith("with")) {
                    s = s.substring(5);
                    dice = new Integer(s.charAt(0) - 48);
                    Map.getInstance().getAircrafts(uid)[aid].setCanFly(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map.getInstance().getAircrafts(uid)[aid].fly(dice);
                        }
                    }).start();
                }
            } else {
                if (s.startsWith("respawn")) {
                    s = s.substring(8);
                    aid = new Integer(s.charAt(0) - 48);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map.getInstance().getAircrafts(uid)[aid].respawn();
                        }
                    }).start();

                }
            }
        } else if (s.startsWith("schedule")) {
            Map.getInstance().schedule();
        }
    }
}
