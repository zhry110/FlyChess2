package local.server;

import game.Aircraft;
import game.Map;
import game.Player;
import game.StepAIPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by like1 on 2017/4/13.
 */
public class PlayerService {
    private Player player;
    private Socket socket;
    private boolean isPrepare = false;
    private boolean start;
    private boolean remoteAlive = true;
    private boolean depute = false;

    public PlayerService(Socket socket, Player player) {
        this.socket = socket;
        this.player = player;
        start = false;
        if (socket != null)
            try {
                socket.setSoTimeout(10000);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        player.setService(this);
    }


    public void startService() {
        start = true;
        byte[] arr = new byte[10];
        GameRoom gameRoom = LocalServer.getLocalRoomInstance();
        InputStream in = null;
        OutputStream out = null;
        System.out.println("server:start service for " + player.getName());
        while (start) {
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                Protocol protocol = null;
                if (LocalServer.read(arr, in)) {
                    remoteAlive = true;
                    protocol = new Protocol(arr);
                    if (protocol.isRequest()) {
                        switch (gameRoom.getGame_state()) {
                            case PREPATE:
                                if (protocol.getOpt() == LocalServer.PREPARE) {
                                    if (isPrepare)
                                        break;
                                    System.out.println("server:" + player.getName() + " is preparing");
                                    isPrepare = true;
                                    gameRoom.sendMessage(new Message(this, Protocol.createPacket((byte) 0, LocalServer.PREPARE, (byte) 1, null)));
                                    byte[] uids = new byte[]{3, 1, (byte) player.getUid()};
                                    gameRoom.sendMessage(new Message(null,
                                            Protocol.createPacket((byte) 0, LocalServer.PLAYERSTATECHANGE, (byte) 1, uids)));
                                } else if (protocol.getOpt() == LocalServer.UNPREPARE) {
                                    System.out.println("server:" + player.getName() + " undo prepare");
                                    isPrepare = false;
                                    gameRoom.sendMessage(new Message(this, Protocol.createPacket((byte) 0, LocalServer.UNPREPARE, (byte) 1, null)));
                                    byte[] uids = new byte[]{3, 0, (byte) player.getUid()};
                                    gameRoom.sendMessage(new Message(null,
                                            Protocol.createPacket((byte) 0, LocalServer.PLAYERSTATECHANGE, (byte) 1, uids)));
                                } else if (protocol.getOpt() == LocalServer.START) {
                                    if (player != LocalServer.getLocalRoomInstance().getHostService().getPlayer()) {
                                        System.out.println("server:only host can start game");
                                        gameRoom.sendMessage(new Message(this, Protocol.createPacket((byte) 0, LocalServer.START, (byte) 1, null)));
                                        break;
                                    }
                                    if (LocalServer.getLocalRoomInstance().getPlayersNum() <= 1) {
                                        System.out.println("server:need more players");
                                        gameRoom.sendMessage(new Message(this, Protocol.createPacket((byte) 0, LocalServer.START, (byte) 0, null)));
                                        break;
                                    }
                                    if (LocalServer.getLocalRoomInstance().allPreparey()) {
                                        gameRoom.setGameState(GameRoom.GAME_STATE.PLAYING);
                                        //gameRoom.sendMessage(new Message(null, Protocol.createPacket((byte) 0, LocalServer.START, (byte) 1, null)));
                                        LocalServer.getLocalRoomInstance().startGame(LocalServer.getLocalRoomInstance().getPlayersNum());
                                        break;
                                    }
                                    else {
                                        System.out.println("server:need all the players prepare");
                                        gameRoom.sendMessage(new Message(null, Protocol.createPacket((byte) 0, LocalServer.START, (byte) 0, null)));
                                    }
                                } else if (protocol.getOpt() == LocalServer.ADDBOT)
                                {
                                    if (this == gameRoom.getHostService())
                                    {
                                        if (!gameRoom.addBot())
                                        {
                                            gameRoom.sendMessage(new Message(null,
                                                    Protocol.createPacket((byte) 0, (byte) LocalServer.ADDBOT, (byte) 0, null)));
                                            System.out.println("cannot add bot");
                                        }

                                    }
                                }
                                else {
                                    gameRoom.sendMessage(new Message(null, Protocol.createPacket((byte) 0, (byte) -2, (byte) 0, null)));
                                    System.out.println("server:unknow request");
                                }
                                break;
                            case PLAYING:
                                System.out.println("server:opt" + protocol.getOpt());
                                if (depute)
                                {
                                    if (protocol.getOpt() != LocalServer.DEPUTEOFF)
                                        break;
                                }
                                if (protocol.getOpt() == LocalServer.READY) {
                                    gameRoom.playerReady(getPlayer());
                                } else if (!getPlayer().isReady()) {
                                    gameRoom.sendMessage(new Message(this,
                                            Protocol.createPacket((byte) 0, (byte) protocol.getOpt(), (byte) 0, null)));
                                    System.out.println("player not ready");
                                } else if (protocol.getOpt() == LocalServer.DICE) {
                                    if (Map.getCurPlayer() != player) {
                                        System.out.println("server:not your turn for dice");
                                        break;
                                    }
                                    if (!player.isCanDice() || !player.isFlyed()) {
                                        System.out.println("server:you canot dice");
                                        break;
                                    }
                                    getPlayer().dice();
                                } else if (protocol.getOpt() == LocalServer.FLY) {
                                    if (Map.getCurPlayer() != player || !player.isDiced()) {
                                        System.out.println("server:not your turn");
                                        break;
                                    }
                                    int aid = protocol.getData()[1];
                                    if (aid < 0 || aid > 3)
                                        break;
                                    Aircraft aircraft = Map.getInstance().getAircrafts(player.getUid())[aid];
                                    if (aircraft.atHome()) {
                                        if (player.getDice() == 6) {
                                            aircraft.setCanFly(true);
                                            aircraft.fly(1);
                                            byte[] other = new byte[]{4, (byte) player.getUid(), (byte) aid, (byte) 1};
                                            byte[] data = Protocol.createPacket((byte) 0, LocalServer.FLY, (byte) 1, other);
                                            Message msg = new Message(null, data);
                                            LocalServer.getLocalRoomInstance().sendMessage(msg);
                                        } else {
                                            break;
                                        }
                                    } else {
                                        aircraft.setCanFly(true);
                                        aircraft.fly(player.getDice());
                                        byte[] other = new byte[]{4, (byte) player.getUid(), (byte) aid, (byte) player.getDice()};
                                        byte[] data = Protocol.createPacket((byte) 0, LocalServer.FLY, (byte) 1, other);
                                        Message msg = new Message(null, data);
                                        LocalServer.getLocalRoomInstance().sendMessage(msg);
                                    }
                                    player.finishFly();
                                    if (!player.isCanDice())
                                        player.setTurnIsOver();
                                } else if (protocol.getOpt() == LocalServer.DEPUTEON)
                                {
                                    player = new StepAIPlayer(player.getUid(), Map.getInstance());
                                    player.setService(this);
                                    Map.getInstance().replacePlayer(player);
                                    depute = true;
                                }
                                else if (protocol.getOpt() == LocalServer.DEPUTEOFF)
                                {
                                    player = new Player(player.getUid(), Map.getInstance());
                                    player.setService(this);
                                    Map.getInstance().replacePlayer(player);
                                    depute = false;
                                }
                                break;
                            case STOP:
                                break;
                            case LOSEUSER:
                                break;

                        }
                    }
                } else {
                    System.out.println("server:stop service of " + player.getName());
                    LocalServer.getLocalRoomInstance().removePlayer(this);
                    break;
                }

            } catch (IOException e) {
                //System.out.println(getPlayer().getName()+" "+e.getMessage());
                if (remoteAlive == false) {
                    LocalServer.getLocalRoomInstance().removePlayer(this);
                } else {
                    remoteAlive = false;
                    LocalServer.getLocalRoomInstance().sendMessage(new Message(this,
                            Protocol.createPacket((byte) 1, LocalServer.ALIVE, (byte) 1, null)));
                }
            }
        }

    }

    public boolean isPrepare() {
        return isPrepare;
    }

    public Player getPlayer() {
        return player;
    }

    public String getRemoteHostAddress() {
        return socket.getRemoteSocketAddress().toString();
    }

    public boolean send(byte[] data) {
        if (socket.isClosed())
            return false;
        try {
            socket.getOutputStream().write(data);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void shutDownService() {
        start = false;
        player.interruptTimer();
        System.out.println("server:shutdown service " + getPlayer().getName());
        try {
            byte[] data = new byte[]{(byte) 0xff};
            send(data);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStart() {
        return start;
    }

    public void setPrepare(boolean prepare) {
        isPrepare = prepare;
    }

    public boolean areYouHuman()
    {
        return true;
    }
}
