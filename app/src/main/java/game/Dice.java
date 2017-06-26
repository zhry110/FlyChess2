package game;

import java.util.Random;

/**
 * Created by like1 on 2017/4/12.
 */
public class Dice {
    private int currentPoints ;
    private Random random;
    public Dice()
    {
        random = new Random();
        dicing();
    }
    public int dicing()
    {
        currentPoints = random.nextInt(6) + 1;
        System.out.println(currentPoints);
        return currentPoints;
    }
    public int getCurrentPoints()
    {
        return currentPoints;
    }
}
