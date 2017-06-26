package game;


import local.server.LocalServer;
import local.server.Message;
import local.server.Protocol;

/**
 * Created by like1 on 2017/6/6.
 */

public class StepAIPlayer extends AIPlayer {
    public StepAIPlayer(int uid, PathProvider provider) {
        super(uid, provider);
        setName("StepFirstBot"+uid);
    }



    @Override
    protected void think() {
        dice();
        if (turnIsOver)
            return;
        Aircraft[] craftsAtHome = new Aircraft[4]; int i = 0;
        Aircraft stepMax = null;
        for (Aircraft aircraft : Map.getInstance().getAircrafts(uid))
        {
            if (aircraft.atHome())
            {
                craftsAtHome[i++] = aircraft;
            }
            else if (!aircraft.isArrive())
            {
                if (aircraft.moreThan(stepMax,dice))
                {
                    stepMax = aircraft;
                }
            }
        }
        if (dice == 6)
        {
            if (craftsAtHome[0] != null)
            {
                craftsAtHome[0].setCanFly(true);
                craftsAtHome[0].fly(1);
                byte[] other = new byte[]{4,(byte) getUid(),(byte) craftsAtHome[0].getId(),(byte) 1};
                byte[] data = Protocol.createPacket((byte) 0,LocalServer.FLY,(byte) 1,other);
                Message msg = new Message(null,data);
                LocalServer.getLocalRoomInstance().sendMessage(msg);
            }
            else
            {
                if (stepMax == null) {
                    setTurnIsOver();
                    return;
                }
                /*if (stepMax.isArrive()) {
                    System.out.println("error");
                    setTurnIsOver();
                    return;
                }*/
                stepMax.setCanFly(true);
                stepMax.fly(dice);
                byte[] other = new byte[]{4,(byte) getUid(),(byte) stepMax.getId(),(byte) getDice()};
                byte[] data = Protocol.createPacket((byte) 0,LocalServer.FLY,(byte) 1,other);
                Message msg = new Message(null,data);
                LocalServer.getLocalRoomInstance().sendMessage(msg);
            }
            finishFly();
            think();
        }
        else
        {
            if (stepMax == null) {
                setTurnIsOver();
                return;
            }
            /*if (stepMax.isArrive()) {
                System.out.println("error");
                setTurnIsOver();
                return;
            }*/
            stepMax.setCanFly(true);
            stepMax.fly(dice);
            byte[] other = new byte[]{4,(byte) getUid(),(byte) stepMax.getId(),(byte) getDice()};
            byte[] data = Protocol.createPacket((byte) 0,LocalServer.FLY,(byte) 1,other);
            Message msg = new Message(null,data);
            LocalServer.getLocalRoomInstance().sendMessage(msg);
            finishFly();
            setTurnIsOver();
        }
    }
}
