package com.zhry.like1.flychess;


import com.zhry.like1.flychess.data.NetPlayer;

import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        NetPlayer.JoinRoom(675551521,"dfsadfadsf");
    }
}