package local.server; /**
 * Created by like1 on 2017/4/28.
 */


import game.Map;
import game.Player;
import game.StepAIPlayer;


import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by like1 on 2017/4/28.
 */
public class GameRoom {
    private String name;
    private char gameType;
    private List<PlayerService> playerServices;
    private PlayerService hostService;
    private Thread waitPlayerThread;
    private boolean allReady = false;
    private boolean gameInitFinish = false;
    private MessageDispatcher dispatcher;
    private Map map;
    public  void sendMessageToAll(Message msg) {
        for (PlayerService players : playerServices) {
            players.send(msg.getData());
        }
    }

    public enum GAME_STATE {PLAYING, LOSEUSER, STOP, PREPATE, WAITREADY, NEWHOME}

    private MessageQueue messageQueue;
    private GAME_STATE game_state = GAME_STATE.PREPATE;
    private byte[] data = new byte[96];

    public GameRoom(String name, char gameType) throws IOException {
        if (name == null)
            throw new IllegalArgumentException();
        if (name.getBytes().length > 100)
            throw new IllegalArgumentException("server:Name Too Lang");
        this.name = name;
        this.gameType = gameType;
        playerServices = new ArrayList<>();
        messageQueue = new MessageQueue(100);
        dispatcher = new MessageDispatcher(messageQueue);
        dispatcher.start();
        waitPlayerThread = null;
    }

    public boolean updateServices(PlayerService cur) {
        synchronized (playerServices) {
            if (cur == null)
                return false;
            if (playerServices.size() == gameType)
                return false;
            for (PlayerService playerService : playerServices) {
                if (playerService.getPlayer().getName().equals(cur.getPlayer().getName())) {
                    return false;
                }
            }
            if (cur.getPlayer().getUid() == 2) {
                if (true)//cur.getRemoteHostAddress().contains("127.0.0.1")
                {
                    hostService = cur;
                } else {
                    System.out.println(cur.getRemoteHostAddress());
                    System.out.println("server:cheat");
                    return false;
                }
            }
            playerServices.add(cur);
            return true;
        }
    }

    public char getPlayersNum() {
        return (char) playerServices.size();
    }

    public String getName() {
        return name;
    }

    public char getGameType() {
        return gameType;
    }

    public void show() {
        System.out.println(playerServices.size());
        System.out.println(gameType);
        System.out.println(name);
    }

    public synchronized void removePlayer(PlayerService playerService) {
            if (playerService == hostService) {
                //host left
                System.out.println("server:host was left");
                for (PlayerService p : playerServices) {
                    p.shutDownService();
                }
                playerServices.clear();
                game_state = GAME_STATE.PREPATE;
                return;
            }
            if (game_state == GAME_STATE.PREPATE) {
                System.out.println("server:"+playerService.getPlayer().getName() + " was left");
                playerServices.remove(playerService);
                playerService.shutDownService();
                playerChangedLeft(playerService.getPlayer());
                reAssignPlayersId();

            } else {
                System.out.println("server:player " + playerService.getPlayer().getName() + " disconnected");
                playerService.shutDownService();
                //playerServices.remove(playerService);
                playerChangedLeft(playerService.getPlayer());
                //play with an AI
            }

    }

    private void reAssignPlayersId() {
        int i = 0;
        for (PlayerService p : playerServices) {
            if (p.getPlayer().getUid() == 2) {
                continue;
            }
            if (i == 2)
                i++;
            playerChangedLeft(p.getPlayer());
            p.getPlayer().setUid(i++);
            playerChangedAdd(p.getPlayer());
            byte[] uids = new byte[]{3, 1, (byte) p.getPlayer().getUid()};
            sendMessage(new Message(null,
                    Protocol.createPacket((byte) 0, LocalServer.PLAYERSTATECHANGE, (byte) 1, uids)));
        }
    }

    public PlayerService getHostService() {
        return hostService;
    }

    public GAME_STATE getGame_state() {
        return game_state;
    }

    public Player createPlayer() {
        synchronized (playerServices) {
            if (playerServices.size() == 0) {
                return new Player(2, null);
            } else if (playerServices.size() == 1) {
                return new Player(0, null);
            } else if (playerServices.size() == 2) {
                return new Player(1, null);
            } else if (playerServices.size() == 3) {
                return new Player(3, null);
            } else {
                return null;
            }
        }
    }

    public boolean roomReady() {
        //return hostService != null;
        return game_state == GAME_STATE.PREPATE;
    }

    public boolean allPreparey() {
        for (PlayerService p : playerServices) {
            if (p.getPlayer().getUid() == 2)
                continue;
            if (!p.isPrepare())
                return false;
        }
        return true;
    }

    public Player[] getPlayers() {
        Player[] players = new Player[gameType];
        for (PlayerService playerService : playerServices) {
            players[playerService.getPlayer().getUid()] = playerService.getPlayer();
            players[playerService.getPlayer().getUid()].setProvider(Map.getInstance());
        }
        return players;
    }

    public void startGame(int players) {
        Message msg;
        byte[] arr;
        /*players += 3;
        playerServices.add(new BotService(null,new StepAIPlayer(3,null)));
        playerServices.add(new BotService(null,new StepAIPlayer(1,null)));
        playerServices.add(new BotService(null,new StepAIPlayer(0,null)));
        morePlayAdd();*/
        for (PlayerService p : playerServices)
        {
            arr = new byte[]{3,(byte) players,(byte) p.getPlayer().getUid()};
            sendMessage(new Message(p,Protocol.createPacket((byte) 0, LocalServer.START, (byte) 1, arr)));
        }
        map = new Map(players);
        gameInitFinish = true;
    }

    public synchronized boolean sendMessage(Message msg) {
        return messageQueue.addMessage(msg);
    }



    public void setGameState(GAME_STATE game_state) {
        this.game_state = game_state;
    }




    public void playerChangedAdd(Player player) {
        byte[] data = new byte[player.getName().getBytes().length + 2];
        data[0] = (byte) (player.getName().getBytes().length + 2);
        data[1] = (byte) player.getUid();
        byte[] bytes = player.getName().getBytes();
        for (int i = 0; i < bytes.length; i++) {
            data[i + 2] = bytes[i];
        }
        sendMessage(new Message(null, Protocol.createPacket((byte) 0, LocalServer.PLAYERCHANGEDADD, (byte) 1, data)));
    }

    public void morePlayAdd() {
        for (PlayerService p : playerServices) {
            playerChangedAdd(p.getPlayer());
        }
    }

    public void playerChangedLeft(Player player) {
        byte[] data = new byte[2];
        data[0] = (byte) (2);
        data[1] = (byte) player.getUid();
        sendMessage(new Message(null, Protocol.createPacket((byte) 0, LocalServer.PLAYERCHANGEDLEFT, (byte) 1, data)));
    }
    public synchronized void playerReady(Player player)
    {
        player.setReady(true);
        System.out.println("server:"+player.getName()+" ready");
        boolean wake = true;
        for (PlayerService p:playerServices)
        {
            if (!p.getPlayer().isReady()) {
                wake = false;
                break;
            }
        }
        if (wake)
        {
            while (!isGameInitFinish())
                System.out.println("server:game not init finish");
            System.out.println("server:all ready");
            allReady = true;

            map.startGame();
        }
    }
    public void respawn()
    {
        System.out.println("server:game room respawn");
        game_state = GAME_STATE.PREPATE;
        allReady = false;
        gameInitFinish = false;
        for (PlayerService p:playerServices)
        {
            p.setPrepare(false);
            playerChangedLeft(p.getPlayer());
            playerChangedAdd(p.getPlayer());
        }
    }

    public boolean isGameInitFinish() {
        return gameInitFinish;
    }

    public void waitOperationEnd()
    {
        for (PlayerService playerService : playerServices)
        {
            playerService.getPlayer().setOperationOver(false);
        }
    }
    public boolean addBot()
    {
        StepAIPlayer p;
        synchronized (playerServices) {
            if (playerServices.size() == 0) {
                p =  new StepAIPlayer(2, null);
            } else if (playerServices.size() == 1) {
                p =  new StepAIPlayer(0, null);
            } else if (playerServices.size() == 2) {
                p =  new StepAIPlayer(1, null);
            } else if (playerServices.size() == 3) {
                p =  new StepAIPlayer(3, null);
            } else {
                return false;
            }
            playerServices.add(new BotService(null,p));
            morePlayAdd();
        }
        byte[] uids = new byte[]{3, 1, (byte) p.getUid()};
        sendMessage(new Message(null,
                Protocol.createPacket((byte) 0, LocalServer.PLAYERSTATECHANGE, (byte) 1, uids)));
        return true;
    }
    public void destory()
    {
        dispatcher.interrupt();
    }
}