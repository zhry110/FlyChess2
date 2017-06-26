package local.server;

/**
 * Created by like1 on 2017/5/6.
 */
public class MessageDispatcher extends Thread {
    MessageQueue messageQueue;
    public MessageDispatcher(MessageQueue messageQueue)
    {
        this.messageQueue = messageQueue;
    }
    Message msg;
    @Override
    public void run() {
        super.run();
        while ((msg = messageQueue.getMessage()) != null)
        {
            PlayerService playerService = msg.getPlayerService();
            if (playerService == null)
            {
                LocalServer.getLocalRoomInstance().sendMessageToAll(msg);
            }
            else
            {
                if (!playerService.send(msg.getData()))
                {
                    LocalServer.getLocalRoomInstance().removePlayer(playerService);
                }
            }
        }
    }
}
