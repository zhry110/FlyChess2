package local.server;

import game.Player;

/**
 * Created by like1 on 2017/5/6.
 */
public class Message {
    private PlayerService playerService;
    private byte[] data;
    public Message(PlayerService playerService,byte[] data)
    {
        this.playerService = playerService;
        this.data = data;
    }
    public PlayerService getPlayerService() {
        return playerService;
    }
    public byte[] getData() {
        return data;
    }
}
