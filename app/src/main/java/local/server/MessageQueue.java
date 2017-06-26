package local.server;


import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by like1 on 2017/5/6.
 */
public class MessageQueue {
    private LinkedBlockingQueue<Message> msgQueue;
    public MessageQueue(int max)
    {
        msgQueue = new LinkedBlockingQueue<>(max);
    }

    public boolean addMessage(Message message)
    {
        return msgQueue.offer(message);
    }

    public Message getMessage() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
