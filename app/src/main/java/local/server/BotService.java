package local.server;

import java.net.Socket;

import game.Player;

/**
 * Created by like1 on 2017/6/14.
 */

public class BotService extends PlayerService {
    public BotService(Socket socket, Player player) {
        super(socket, player);
        setPrepare(true);
        player.setReady(true);
    }

    @Override
    public boolean send(byte[] data) {
        return true;
    }

    @Override
    public void shutDownService() {

    }

    @Override
    public String getRemoteHostAddress() {
        return "server:bot have not address";
    }

    @Override
    public void startService() {
        System.out.println("server:bot need'nt startService");
    }

    @Override
    public boolean areYouHuman() {
        return !super.areYouHuman();
    }
}
