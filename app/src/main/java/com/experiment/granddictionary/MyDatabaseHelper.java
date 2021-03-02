package com.experiment.granddictionary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_DICTIONARY = "CREATE TABLE Dictionary("
            + "id integer primary key autoincrement, "
            + "word varchar(64) unique, "
            + "explanation text, "
            + "level int default 0, "
            + "modified_time timestamp)";

    /*"CREATE TABLE dict(_id integer primary key autoincrement, word varchar(64) unique, explanation text, level int default 0, modified_time timestamp)"*/

    private Context mContext;
    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DICTIONARY);
        //Toast.makeText(mContext,"Create succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Dictionary");
        onCreate(db);
    }
}
