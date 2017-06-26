package local.server;

/**
 * Created by like1 on 2017/5/6.
 */
public class Service extends Thread {
    private PlayerService service;
    public Service(PlayerService service)
    {
        this.service = service;
    }
    @Override
    public void run() {
        super.run();
        if (service != null) {
            service.startService();
        }
    }
}
