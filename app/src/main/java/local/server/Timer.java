package local.server;

/**
 * Created by like1 on 2017/5/23.
 */
public class Timer extends Thread {
    public Timer()
    {
        super();
    }
    public Timer(Runnable runnable)
    {
        super(runnable);
    }
    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(30000);
            System.out.println("user not play until time out");
        } catch (InterruptedException e) {
            System.out.println("end");
        }
    }
}
