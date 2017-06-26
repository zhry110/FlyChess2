package com.zhry.like1.flychess.sqlite;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhry.like1.flychess.R;
import com.zhry.like1.flychess.net.Protocol;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by like1 on 2017/6/19.
 */

public class GameDatabase implements Serializable{
    public static final String DATABASENAME = "game_data";
    private static final String TABLENAME = "game";
    private long time;
    private int id;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    public GameDatabase(Context context,long time,boolean server)
    {
        id = 0;
        this.time = time;
        sqLiteDatabase = context.openOrCreateDatabase(DATABASENAME,context.MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON;");
        sqLiteDatabase.execSQL(
                "create table if not exists game(time integer ,id integer ,protocol blob not null,constraint pk primary key(time,id),foreign key(time) references game_type(time));"
        );
        sqLiteDatabase.execSQL(
                "create table if not exists" +
                        " game_type(time integer,type integer not null,primary key(time));");
        sqLiteDatabase.execSQL("insert into game_type values (?,?);",new Object[]{time,server});
        this.context = context;
    }
    public synchronized boolean addData(byte[] data)
    {
        System.out.println(time);
        if (data[0] < 1)
            return false;
        byte[] write = new byte[data[0]];
        for (int i = 0;i < write.length;i++)
            write[i] = data[i];
        sqLiteDatabase.execSQL("insert into game values (?,?,?);",new Object[]{time,id++,write});
        return true;
    }
    public synchronized void deleteData(int id)
    {

        sqLiteDatabase.delete("game","time=? and id=?",new String[]{""+time,"+id"});

    }
    public static synchronized void deleteAllData(long time,Context context)
    {
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(DATABASENAME,context.MODE_PRIVATE,null);
        sqLiteDatabase.delete("game","time=?",new String[]{""+time});
        sqLiteDatabase.delete("game_type","time=?",new String[]{""+time});
        sqLiteDatabase.close();
    }
    public static synchronized Protocol[] getProtocols(long time,Context context)
    {
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(DATABASENAME,context.MODE_PRIVATE,null);
        Cursor cursor = sqLiteDatabase.rawQuery("select * from game where time = ? group by id",new String[]{""+time});
        System.out.println("time:"+time);
        Protocol[] protocol = new Protocol[cursor.getCount()];
        int pos = 0;
        while (cursor.moveToNext())
        {
            System.out.println("id:"+cursor.getInt(cursor.getColumnIndex("id")));
            protocol[pos++] = new Protocol(cursor.getBlob(cursor.getColumnIndex("protocol")));
        }
        cursor.close();
        sqLiteDatabase.close();
        return protocol;
    }

    public static LinearLayout[] getAllGames(Activity context)
    {
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase(DATABASENAME,context.MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON;");
        sqLiteDatabase.execSQL(
                "create table if not exists game(time integer ,id integer ,protocol blob not null,constraint pk primary key(time,id),foreign key(time) references game_type(time));"
        );
        sqLiteDatabase.execSQL(
                "create table if not exists" +
                        " game_type(time integer,type integer not null,primary key(time));");
        Cursor cursor =
                sqLiteDatabase.
                        rawQuery("select distinct * from game_type",null);
        if (cursor.getCount() == 0)
            return null;
        LinearLayout[] times = new LinearLayout[cursor.getCount()];
        int pos = 0;
        while (cursor.moveToNext())
        {
            LinearLayout replay = times[pos++] = (LinearLayout) context.getLayoutInflater().inflate(R.layout.replay,null);

            int server = cursor.getInt(cursor.getColumnIndex("type"));
            long time = cursor.getLong(cursor.getColumnIndex("time"));
            replay.setTag(time);
            ((TextView) replay.findViewById(R.id.trasScreenTextView04)).setText(server == 0? "局域网":"服务器");
            ((TextView) replay.findViewById(R.id.more)).setText(new Date(time).toLocaleString());
            replay.setGravity(Gravity.CENTER);
        }
        cursor.close();
        sqLiteDatabase.close();
        return times;
    }

    public void setTime(long time) {
        this.time = time;
    }
    public void destory()
    {
        if (sqLiteDatabase != null)
            sqLiteDatabase.close();
    }
}
