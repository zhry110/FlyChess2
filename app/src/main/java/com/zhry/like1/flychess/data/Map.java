package com.zhry.like1.flychess.data;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhry.like1.flychess.GameActivity;
import com.zhry.like1.flychess.MainActivity;
import com.zhry.like1.flychess.R;
import com.zhry.like1.flychess.RoomActivity;
import com.zhry.like1.flychess.view.PathNodeView;
import com.zhry.like1.flychess.view.Tip;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by like1 on 2017/4/12.
 */
public class Map implements PathProvider {
    private int speed =  1;
    protected static int errno = -1;
    public static final int UNKNOW_ERROR = -1;
    public static final int BAD_POINTS_FOR_FLY = 0;
    protected int users;
    protected Player[] players;
    protected PrivatePath privatePath;
    protected PublicPath commonPath;
    protected Home homes;
    protected Dice dice;
    protected Player curPlayer;
    protected Aircraft[][] aircrafts;
    public static Resources resources;
    public static Drawable[] pos;
    public static Drawable[] aircraft;
    public static Drawable[] dices;
    protected static Drawable flag;
    protected static Map instance;
    protected List<Player> winners;
    public Handler handler;
    protected static SoundPool mediaPlayers;
    protected TextView[] names;
    private boolean exit = false;
    private static boolean music = true;
    private boolean pause = false;
    private boolean needSchedule = false;
    protected NetPlayer netPlayer = null;
    private Tip tip;
    private ImageView i,d;
    protected View root;
    protected GameActivity gameActivity;
    public Map(GameActivity gameActivity,NetPlayer netPlayer,int players,int bots, PathNodeView[] comViews, PathNodeView[] priViews, PathNodeView[] homeViews, Resources res, TextView[] names) {
        this.gameActivity = gameActivity;
        this.names = names;
        this.netPlayer = netPlayer;
        i = new ImageView(gameActivity);
        d = new ImageView(gameActivity);
        instance = this;
        root = (View) comViews[0].getParent();
        ((RelativeLayout)root).addView(Map.this.i);
        ((RelativeLayout)root).addView(Map.this.d);
        i.setVisibility(View.INVISIBLE);
        d.setVisibility(View.INVISIBLE);
        makeHandler();
        prepareMediaPlayers();
        prepareMap(comViews, priViews, homeViews, res);
        createPlayers(players,bots);

    }

    @Override
    public PathNode getHome(int uid, int id) {
        return homes.getHome()[uid * 5 + id + 1];
    }

    @Override
    public PathNode getBelowSuperFly(int uid) {
        uid = (uid + 2) % 4;
        return privatePath.getBelowSuperFly(uid);
    }

    @Override
    public Aircraft[] getAircrafts(int uid) {
        return aircrafts[uid];
    }

    @Override
    public boolean gameOver() {
        if (exit)
            return true;
        if (users == 1) {
            if (winners.size() == 1)
                return true;
        }else if (winners.size() == users - 1)
            return true;
        return false;
    }

    public static void setErrno(int errno) {
        Map.errno = errno;
    }

    public static int getErrno() {
        return errno;
    }

    public int dicing() {
        return dice.dicing();
    }

    public  Player getUser(int uid) {
        return players[uid];
    }

    public  Player getCurPlayer() {
        return curPlayer;
    }

    public boolean startGame() {
        int dice;
        if (users == 2) {
            dice = (dicing() & 1) == 1 ? 0 : 2;
        } else if (users == 1)
            dice = 2;
        else {
            while ((dice = dicing()) >= users) ;
        }
        curPlayer = players[dice];
        gameActivity.showTip("玩家"+names[getNextUser().getUid()].getText().toString()+"获得先手");
        System.out.println("user " + getNextUser().getUid() + " obtain first");
        return true;
    }

    public synchronized void schedule() {
        System.out.println("schedule");
        if (pause) {
            needSchedule = true;
            return;
        }
        needSchedule = false;
        if (gameOver()) {
            //Toast.makeText(GameActivity.getInstance(),"game over",0).show();
            System.out.println("game over");
            instance = null;
            return;
        }
        Message msg = handler.obtainMessage();
        msg.what = 4;
        msg.obj = curPlayer;
        handler.sendMessage(msg);
        curPlayer = getNextUser();
        System.out.println("it's " + curPlayer.getUid() + " turn");
        curPlayer.play();
    }

    public static Map getInstance() {
        if (instance == null)
            return LocalServerMap.getInstance();
        return instance;
    }

    public void win(Player player) {
        players[player.uid] = null;
        winners.add(player);
        if (winners.size() == users-1)
        {
            String s = "获得胜利的是\n";
            for (Player p : winners)
            {
                s += names[p.getUid()].getText().toString()+"\n";
            }
            Message msg = handler.obtainMessage();
            msg.what = 10;
            msg.obj = s;
            handler.sendMessage(msg);
        }
    }

    protected void createPlayers(int player,int bots) {
        int users = player + bots;
        if (users == 1) {
            players[2] = new Player(2, this,handler);
        } else if (users == 2) {
            players[2] = new Player(2, this,handler);
            players[0] = new Player(0, this,handler);
        } else {
            for (int i = 0; i < users; i++) {
                players[i] = new Player(i, this,handler);
            }
        }
        for (int i = 0;i < 4 && bots > 0;i++)
        {
            if (i == 2) {
                continue;
            }
            if (players[i] != null)
            {
                players[i] = new StepAIPlayer(i,this,handler);
                names[i].setText("Bot");
                gameActivity.showBotView(i);
                bots --;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (players[i] != null) {
                for (int j = 0; j < 4; j++) {
                    aircrafts[i][j] = new Aircraft(i, j, homes.getHome()[i * 5 + j + 1], this,handler);
                    homes.getHome()[i * 5 + j + 1].layoutAircraft(aircrafts[i][j]);
                }
            }
        }
        this.users = users;
    }

    protected void prepareMap(PathNodeView[] comViews, PathNodeView[] priViews, PathNodeView[] homeViews, Resources res) {
        pos = new Drawable[5];
        aircraft = new Drawable[4];
        dices = new Drawable[6];
        flag = res.getDrawable(R.drawable.flag);
        dices[0] = res.getDrawable(R.drawable.t1);
        dices[1] = res.getDrawable(R.drawable.t2);
        dices[2] = res.getDrawable(R.drawable.t3);
        dices[3] = res.getDrawable(R.drawable.t4);
        dices[4] = res.getDrawable(R.drawable.t5);
        dices[5] = res.getDrawable(R.drawable.t6);
        pos[0] = res.getDrawable(R.drawable.blackpos);
        pos[1] = res.getDrawable(R.drawable.redpos);
        pos[2] = res.getDrawable(R.drawable.rangepos);
        pos[3] = res.getDrawable(R.drawable.greenpos);
        aircraft[0] = res.getDrawable(R.drawable.blackplane);
        aircraft[1] = res.getDrawable(R.drawable.redplane);
        aircraft[2] = res.getDrawable(R.drawable.rangeplane);
        aircraft[3] = res.getDrawable(R.drawable.greenplane);
        dice = new Dice();
        commonPath = new PublicPath(comViews,handler);
        privatePath = new PrivatePath(priViews, commonPath,handler);
        this.homes = new Home(homeViews, commonPath,handler);
        players = new Player[4];
        aircrafts = new Aircraft[4][4];
        resources = res;
        winners = new ArrayList<>();
    }

    public void makeHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == -1)
                {
                    Toast.makeText(gameActivity,"lose connect",0).show();
                }
                else if (msg.obj == null && (msg.what != 5 && msg.what != 6  && msg.what != 9 ))
                    return;
                if (msg.what == 0) //show aircraft
                {
                    PathNode pathNode = (PathNode) msg.obj;
                    if (pathNode.view != null)
                        pathNode.view.setImageDrawable(aircraft[curPlayer.getUid()]);
                } else if (msg.what == 1) // remove aircraft
                {
                    PathNode pathNode = (PathNode) msg.obj;
                    if (pathNode.view == null)
                        return;
                    if (pathNode.aircrafts.size() > 0)
                        pathNode.view.setImageDrawable(aircraft[pathNode.aircrafts.get(0).getUid()]);
                    else
                        pathNode.view.setImageDrawable(null);
                } else if (msg.what == 2) {
                    PathNode pathNode = (PathNode) msg.obj;
                    if (pathNode.view == null || pathNode.aircrafts.size() == 0)
                        return;
                    pathNode.view.setImageDrawable(aircraft[pathNode.aircrafts.get(0).getUid()]);
                } else if (msg.what == 3) //show who's turn
                {
                    GameActivity.flags[((Player) msg.obj).getUid()].setImageDrawable(flag);
                    gameActivity.getDice().setEnabled(true);
                } else if (msg.what == 4)// clear who's turn
                {
                    GameActivity.flags[((Player) msg.obj).getUid()].setImageDrawable(null);
                } else if (msg.what == 5)//fresh dice
                {
                    gameActivity.getDice().setImageDrawable(dices[(Integer) msg.obj - 1]);
                    gameActivity.getDice().setEnabled(true);
                } else if (msg.what == 6) //schedule
                {
                    System.out.println("recv schedule");
                    schedule();
                } else if (msg.what == 7) {
                    Aircraft aircraft = (Aircraft) msg.obj;
                    getHome(aircraft.getUid(), aircraft.getId()).view.setImageDrawable(resources.getDrawable(R.drawable.crown_));
                }
                else if (msg.what == 8)
                {
                    gameActivity.showTip((String) msg.obj);
                }
                else if (msg.what == 9)
                {
                    TranslateAnimation t = (TranslateAnimation) ((Object[]) msg.obj)[0];
                    Integer id = (Integer) ((Object[]) msg.obj)[1];
                    Map.this.i.setImageDrawable(aircraft[id]);
                    Map.this.i.setVisibility(View.VISIBLE);
                    Map.this.i.startAnimation(t);
                    t.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Map.this.i.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                else if (msg.what == 10)
                {
                    gameActivity.showGameOver((String) msg.obj);
                }
                else if (msg.what == 11)
                {
                    System.out.println("respawn anmi");
                    TranslateAnimation t = (TranslateAnimation) ((Object[]) msg.obj)[0];
                    Integer id = (Integer) ((Object[]) msg.obj)[1];
                    Map.this.d.setImageDrawable(aircraft[id]);
                    Map.this.d.setVisibility(View.VISIBLE);
                    Map.this.d.startAnimation(t);
                    t.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Map.this.d.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        };
    }

    public int getMeUid() {
        return Player.USER_ALL;
    }

    protected void prepareMediaPlayers() {
        mediaPlayers = new SoundPool(10, AudioManager.STREAM_MUSIC, 6);
        mediaPlayers.load(gameActivity, R.raw.destory, 1);
        mediaPlayers.load(gameActivity, R.raw.step, 1);
        mediaPlayers.load(gameActivity, R.raw.dice, 1);
        mediaPlayers.load(gameActivity, R.raw.superfly, 1);
        mediaPlayers.load(gameActivity, R.raw.six, 1);
        mediaPlayers.load(gameActivity, R.raw.start, 1);
    }

    public void playSound(int i) throws InterruptedException {
        if (exit)
            return;
        int id = 0;
        int sleep =  (800/speed);
        if (i == 2)
            sleep =  (310/speed);
        if (music)
            id = mediaPlayers.play(i, 1, 1, 0, 0, 1);
        final int finalSleep = sleep;
        Thread.sleep(finalSleep);
        mediaPlayers.stop(id);
    }

    public Player getNextUser() {
        int uid = curPlayer.getUid();
        while (players[uid = (++uid % 4)] == null) ;
        return players[uid];
    }

    public void setNetPlayer(NetPlayer netPlayer) {
        players[netPlayer.getUid()] = netPlayer;
    }

    public void setNames(String[] names) {
        for (int i = 0; i < this.names.length; i++) {
            if (names[i] != null)
                this.names[i].setText(names[i]);
        }
    }

    public void exit() {
        exit = true;
        mediaPlayers.release();
    }

    public static void noMusic() {
        music = false;
    }

    public static void openMusic() {
        music = true;
    }

    public static boolean isMusic() {
        return music;
    }

    public synchronized void setPause(boolean pause) {
        this.pause = pause;
        if (pause == false && needSchedule) {
            schedule();
        }
    }
    public synchronized void replaceCurPlayer(Player player)
    {
        if (player != null)
            curPlayer = player;
    }

    public NetPlayer getNetPlayer() {
        return netPlayer;
    }
}
