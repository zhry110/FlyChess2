package local.server;

import com.zhry.like1.flychess.MainActivity;
import com.zhry.like1.flychess.server.Server;

import game.Map;
import game.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Enumeration;
import java.lang.Runnable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by like1 on 2017/4/19.
 */
public class LocalServer extends Thread {
    public static final byte FIND = 0X00;
    public static final byte SCAN = 0X01;//(char)len(char)0x01
    public static final byte JOIN = 0X02;//(char)len(char)0x02(char)username
    public static final byte PREPARE = 0X03;
    public static final byte UNPREPARE = 0x04;
    public static final byte DICE = 0X05;
    public static final byte FLY = 0X06;
    public static final byte START = 0X07;
    public static final byte READY = 0X08;
    public static final byte HOSTPLAYERLEFT = 0X09;
    public static final byte SCHEDULE = 0XA;
    public static final byte SOMEPLAYERREADY = 0XB;
    public static final byte TURN = 0XC;
    public static final byte PLAYERCHANGEDADD = 0XD;
    public static final byte PLAYERCHANGEDLEFT = 0XE;
    public static final byte ALIVE = 0XF;
    public static final byte PLAYERSTATECHANGE = 0X10;
    public static final byte STARTGAME = 0X11;
    public static final byte ADDBOT = 0X12;
    public static final byte DEPUTEON = 0X13;
    public static final byte DEPUTEOFF = 0X14;
    public int onlineUsers = 1;
    private static LocalServer instance;

    private static GameRoom gameRoom;

    private static Map map;
    private ServerSocket serverSocket = null;
    ExecutorService cachedThreadPool ;
    private boolean exit = false;
    public LocalServer() {
        cachedThreadPool = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(10006);
            System.out.println("server:server start");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        instance = this;
        try {
            gameRoom = new GameRoom(MainActivity.playerName+"'s Room", (char) 4);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listenScan();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!exit) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("server:accept from " + socket.getInetAddress() + ":" + socket.getPort());
                //System.out.println(socket.getInputStream().read());
                cachedThreadPool.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                handleRequst(socket);
                            }
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void listenScan() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            while (!exit) {
                //System.out.println("SCAN");
                if (gameRoom.roomReady()) {
                    byte[] roomData = new byte[gameRoom.getName().getBytes().length + 3];
                    int pos = 1;
                    roomData[pos++] = (byte) gameRoom.getGameType();
                    roomData[pos++] = (byte) gameRoom.getPlayersNum();
                    byte[] roomNames = gameRoom.getName().getBytes();
                    for (int i = 0; i < roomNames.length; i++) {
                        roomData[pos++] = roomNames[i];
                    }
                    roomData[0] = (byte) pos;
                    byte[] data = Protocol.createPacket((byte) 0, (byte) SCAN, (byte) 1, roomData);
                    DatagramPacket dataPacket = new DatagramPacket(data, data.length,
                            InetAddress.getByName(Server.getLocalNetBroadcastAddress()), 10005);
                    datagramSocket.send(dataPacket);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequst(Socket socket) {
        if (socket == null)
            return;
        InputStream in;
        OutputStream out;
        try {
            socket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] arr = new byte[128];
        int len = 0, tlen;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            if (read(arr, in))
                handle(len, arr, out, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("server:handle finished");

    }

    private void handle(int len, byte[] arr, OutputStream out, Socket socket) {
        if (out == null)
            return;
        Protocol protocol = new Protocol(arr);
        if (protocol.getOpt() == JOIN && protocol.isRequest()) {
            System.out.println("server:handle join");
            playerJoin(protocol, out, socket);
        } else //other msg refused
        {
            try {
                out.write(Protocol.createPacket((byte) 0, (byte) 0, (byte) 0, null));
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private synchronized char getPlayersNum() {
        return gameRoom.getPlayersNum();
    }

    public static GameRoom getLocalRoomInstance() {
        return gameRoom;
    }

    public static LocalServer getInstance() {
        return instance;
    }


    private synchronized void playerJoin(Protocol protocol, OutputStream out, Socket socket) {
        //System.out.println(socket.getRemoteSocketAddress());

        try {
            Player player = gameRoom.createPlayer();
            if (player == null) {
                System.out.println("server:cannot create player");
                out.write(Protocol.createPacket((byte) 0, JOIN, (byte) 0, null));
                return;
            }
            player.setName(new String(protocol.getData(), 1, protocol.getData()[0] - 1));
            PlayerService playerService = new PlayerService(socket, player);
            if (gameRoom.updateServices(playerService)) {
                System.out.println("server:"+player.getName() + " joined");
                System.out.println("server:"+player.getUid());
                byte[] uid = new byte[]{2, (byte) player.getUid()};
                out.write(Protocol.createPacket((byte) 0, JOIN, (byte) 1, uid));
                Service service = new Service(playerService);
                service.start();
                //while (!playerService.isStart()) ;
                gameRoom.morePlayAdd();
            } else {
                System.out.println("server:cannot update player");
                out.write(Protocol.createPacket((byte) 0, JOIN, (byte) 0, null));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalNetAddress() {
        try {
            Enumeration<NetworkInterface> interfaceList = NetworkInterface.getNetworkInterfaces();
            if (interfaceList == null) {
                System.out.println("--No interface found--");
                return null;
            } else {
                while (interfaceList.hasMoreElements()) {
                    NetworkInterface iface = interfaceList.nextElement();
                    System.out.println("Interface " + iface.getName() + ":");
                    if (!iface.getName().equals("wlan0"))
                        continue;
                    Enumeration<InetAddress> addrList = iface.getInetAddresses();
                    if (!addrList.hasMoreElements()) {
                        System.out.println("\t(No address for this address)");
                    }
                    while (addrList.hasMoreElements()) {
                        InetAddress address = addrList.nextElement();
                        if (address instanceof Inet4Address) {
                            return address.getHostAddress();
                        }
                        System.out.print("\tAddress " + ((address instanceof InetAddress ? "v4"
                                : (address instanceof Inet6Address ? "(v6)" : "(?)"))));
                        System.out.println(":" + address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Error getting network interfaces:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static boolean read(byte[] buf, InputStream in) throws IOException {
        int len;
        len = in.read();
        if (len <= 0)
            return false;
        else if (len == 0xff)
            return false; // break read();no use
        byte[] data = new byte[len];
        data[0] = (byte) len;
        if (in.read(data, 1, len - 1) <= 0) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            buf[i] = data[i];
        }
        return true;
    }

    public void exit() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gameRoom.destory();
        exit = true;
    }
    public InetAddress getLocalAddress()
    {
        return serverSocket.getInetAddress();
    }
}
