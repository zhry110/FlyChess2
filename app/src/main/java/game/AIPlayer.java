package game;

import local.server.LocalServer;
import local.server.Message;
import local.server.Protocol;


/**
 * Created by like1 on 2017/6/3.
 */

public class AIPlayer extends Player {

    public AIPlayer(int uid, PathProvider provider) {
        super(uid, provider);

        setReady(true);
    }

    @Override
    public void play() {
        super.play();
        if (turnIsOver)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                think();
                System.out.println("server:AI play over");
            }
        }).start();
    }

    @Override
    public synchronized void dice() {
        resetTimer();
        if (isCanDice() && flyed) {
            if (provider == null)
                provider = Map.getInstance();
            dice = provider.dicing();
            byte[] dices = new byte[]{3,(byte) getUid(),(byte) getDice()};
            Message msg = new Message(null, Protocol.createPacket((byte) 0, LocalServer.DICE,(byte) 1,dices));
            LocalServer.getLocalRoomInstance().sendMessage(msg);
            System.out.println("server:dice with "+dice);
            flyed = false;
            diced = true;
            if (dice != 6) {
                canDice = false;
                Aircraft[] as = Map.getInstance().getAircrafts(uid);
                int i;
                for (i = 0;i<as.length;i++)
                {
                    if (!as[i].atHome())
                    {
                        if (!as[i].isArrive())
                            break;
                    }
                }
                if (i == as.length)
                {
                    setTurnIsOver();
                }
            }
        }
    }

    protected void think()
    {
        dice();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (turnIsOver)
            return;
        Aircraft[] craftsAtHome = new Aircraft[4]; int i = 0;
        Aircraft[] craftsAtPath = new Aircraft[4]; int j = 0;
        for (Aircraft aircraft : Map.getInstance().getAircrafts(uid))
        {
            if (aircraft.atHome())
            {
                craftsAtHome[i++] = aircraft;
                aircraft.testFly(dice);
            }
            else if (!aircraft.isArrive())
            {
                craftsAtPath[j++] = aircraft;
                aircraft.testFly(dice);
            }
        }
        if (dice == 6)
        {
            if (craftsAtHome[0] != null)
            {
                craftsAtHome[0].setCanFly(true);
                craftsAtHome[0].fly(1);
            }
            else
            {
                craftsAtPath[0].setCanFly(true);
                craftsAtPath[0].fly(6);
            }
            finishFly();
            think();
        }
        else
        {
            craftsAtPath[0].setCanFly(true);
            craftsAtPath[0].fly(dice);
            finishFly();
            setTurnIsOver();
        }
    }

    @Override
    public  void setTurnIsOver() {
        System.out.println("server:new turnover");
        super.setTurnIsOver();
    }

}
