package game;



import local.server.LocalServer;
import local.server.Message;
import local.server.PlayerService;
import local.server.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by like1 on 2017/4/12.
 */
public class Map implements PathProvider {
    private static int errno = -1;
    public static final int UNKNOW_ERROR = -1;
    public static final int BAD_POINTS_FOR_FLY = 0;
    private int users;
    private static Player[] players;
    private PrivatePath privatePath;
    private PublicPath commonPath;
    private Home homes;
    private Dice dice;
    private static Player curPlayer;
    private Aircraft[][] aircrafts;
    private static Map instance;
    private List<Player> winners;
    public Map(int players) {
        prepareMap();
        createPlayers(players);
    }
    @Override
    public PathNode getHome(int uid, int id) {
        return homes.getHome()[uid * 5 + id + 1];
    }

    @Override
    public PathNode getBelowSuperFly(int uid) {
        uid = (uid + 2) % 4;
        return privatePath.getBelowSuperFly(uid);
    }

    @Override
    public Aircraft[] getAircrafts(int uid) {
        return aircrafts[uid];
    }

    @Override
    public boolean gameOver() {

        if (users == 1)
        {
            if (winners.size() == 1)
                return true;
        }
        else if (winners.size() == users-1)
            return true;
        return false;
    }

    public static void setErrno(int errno) {
        Map.errno = errno;
    }

    public static int getErrno() {
        return errno;
    }

    public int dicing() {
        return dice.dicing();
    }

    public static Player getUser(int uid) {
        return players[uid];
    }

    public static Player getCurPlayer() {
        return curPlayer;
    }

    public void startGame() {
        int dice;
        if (users == 2)
        {
            dice = (dicing()&1)==1? 0:2;
        }
        else if (users == 1)
            dice = 2;
        else {
            while ((dice = dicing()) >= users);
        }
        curPlayer = players[dice];
        System.out.println( "server:玩家" + getNextUser().getName() + "获得先手");
        byte[] uid = new byte[]{2,(byte) curPlayer.getUid()};
        Message msg = new Message(null,Protocol.createPacket((byte) 0,LocalServer.STARTGAME,(byte) 1,uid));
        LocalServer.getLocalRoomInstance().sendMessage(msg);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        schedule();
    }

    public synchronized void schedule() {
        System.out.println("server:schedule");
        if (gameOver()) {
            System.out.println("server:game over");
            return;
        }
        curPlayer = getNextUser();
        System.out.println("server:it's " + curPlayer.getUid() + " turn");
        curPlayer.play();
        byte[] data = Protocol.createPacket((byte) 0,LocalServer.TURN,(byte) 1,null);
        Message msg = new Message(null,data);
        LocalServer.getLocalRoomInstance().sendMessage(msg);
    }

    public static Map getInstance() {
        return instance;
    }
    public void win(Player player)
    {
        players[player.uid] = null;
        winners.add(player);
    }
    private void createPlayers(int users)
    {
        /*if (users == 1) {
            players[2] = new Player(2, this);
        }
        else if (users == 2)
        {
            players[2] = new Player(2,this);
            players[0] = new Player(0,this);
        }else
        {
            for (int i = 0;i<users;i++)
            {
                players[i] = new Player(i, this);
            }
        }*/
        players = LocalServer.getLocalRoomInstance().getPlayers();
        for (int i = 0; i < 4; i++) {
            if (players[i] != null)
            {
                for (int j = 0; j < 4; j++) {
                    aircrafts[i][j] = new Aircraft(i, j, homes.getHome()[i * 5 + j + 1], this);
                    homes.getHome()[i * 5 + j + 1].layoutAircraft(aircrafts[i][j]);
                }
            }
        }
        this.users = users;
    }
    private void prepareMap()
    {
        dice = new Dice();
        commonPath = new PublicPath();
        privatePath = new PrivatePath(commonPath);
        this.homes = new Home(commonPath);
        players = new Player[4];
        aircrafts = new Aircraft[4][4];
        instance = this;
        winners = new ArrayList<>();
    }
    public Player getMe()
    {
        return players[2];
    }
    public Player getNextUser()
    {
        int uid = curPlayer.getUid();
        while (players[uid = (++uid % 4)] == null) ;
        return players[uid];
    }
    public void replacePlayer(Player player)
    {
        if (player == null)
            return;
        if (player.getUid() == curPlayer.getUid()) {
            if (curPlayer.diced)
            {
                player.dice = curPlayer.dice;
            }
            players[player.getUid()] = player;
            curPlayer = player;
            player.play();
            return;
        }
        players[player.getUid()] = player;
    }
}
