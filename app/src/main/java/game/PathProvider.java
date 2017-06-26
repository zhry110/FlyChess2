package game;

/**
 * Created by like1 on 2017/4/12.
 */
public interface PathProvider {
    PathNode getHome(int uid, int id);
    PathNode getBelowSuperFly(int uid);
    Aircraft[] getAircrafts(int uid);
    boolean gameOver();
    int dicing();
}
