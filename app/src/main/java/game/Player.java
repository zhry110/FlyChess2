package game;

import local.server.LocalServer;
import local.server.Message;
import local.server.PlayerService;
import local.server.Protocol;
import local.server.Timer;

import java.util.ArrayList;

import static java.lang.Thread.*;

/**
 * Created by like1 on 2017/4/13.
 */
public  class Player {
    public static final int USER_ALL = -1;
    public static final int USER1 = 0;
    public static final int USER2 = 1;
    public static final int USER3 = 2;
    public static final int USER4 = 3;
    private String name;
    protected int uid;
    protected int arrives = 0;
    protected PathProvider provider;
    protected boolean turnIsOver;
    protected int dice;
    protected boolean canDice;
    protected boolean flyed;
    protected boolean diced;
    private PlayerService service;
    private boolean ready;
    private Timer timer;
    private boolean operationOver ;

    public Player(int uid,PathProvider provider)
    {
        this.uid = uid;
        this.provider = provider;
        dice = 0;
        name = ""+uid;
        ready = false;
        timer = null;
        operationOver = true;
    }
    public synchronized void dice()
    {
        resetTimer();
        if (isCanDice()) {
            if (provider == null)
                provider = Map.getInstance();
            dice = provider.dicing();
            byte[] dices = new byte[]{3, (byte) getUid(), (byte) getDice()};
            Message msg = new Message(null, Protocol.createPacket((byte) 0, LocalServer.DICE, (byte) 1, dices));
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
                    if (!as[i].atHome()&&!as[i].isArrive())
                    {
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

    public void arrive() {
        arrives ++;
        if (arrives == 4)
        {
            Map.getInstance().win(this);
        }
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }
    public void play() {
        if (arrives == 4)
        {
            setTurnIsOver();
            return;
        }
        turnIsOver = false;
        canDice = true;
        flyed = true;
        diced = false;
        timer = new Timer();
        timer.start();
    }
    public synchronized void setTurnIsOver() {
        turnIsOver = true;
        if (Map.getCurPlayer() != this)
        {
            System.out.println("server:invalid turnOver");
            return;
        }
        System.out.println("server:turn over");
        interruptTimer();
        Map.getInstance().schedule();
    }

    public int getDice() {
        return dice;
    }

    public boolean isCanDice() {
        return canDice&&flyed;
    }
    public void finishFly()
    {
        flyed = true;
        diced = false;
        resetTimer();
    }
    public boolean isFlyed() {
        return flyed;
    }
    public boolean isDiced() {
        return diced;
    }
    public boolean isWin()
    {
        return arrives == 4;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProvider(PathProvider provider) {
        this.provider = provider;
    }

    public void setService(PlayerService service) {
        this.service = service;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }
    public void interruptTimer()
    {
        if (timer != null)
            timer.interrupt();
    }
    public void resetTimer()
    {
        interruptTimer();
        timer = new Timer();
        timer.start();
    }
    public boolean operationOver()
    {
        return operationOver;
    }
    public void setOperationOver(boolean value)
    {
        operationOver = true;
    }
}

