package com.experiment.granddictionary;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static class DBManagerHolder {
        public static DBManager instance = new DBManager();
    }

    private DBManager() {}
    public static DBManager getInstance() {
        return DBManagerHolder.instance;
    }

    private MyDatabaseHelper mDbHelper;

    public boolean helperCheck() { return mDbHelper!=null; }
    public void setDbHelper(MyDatabaseHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    public long insert(String table, Word word) {
        if (!helperCheck()) throw new RuntimeException("Helper Check Failed");
        if (!checkRecordValidation(word)) return -1;
        return insert(table,word.getWord(),word.getExplanation(),word.getLevel());
    }

    public long insert(String table, String word, String explanation, Integer level) {
        return insert(table,word,explanation,level,true,true);
    }

    public long insert(String table, String word, String explanation, Integer level,
                       boolean nullCheck, boolean allowOverride) {
        if (!helperCheck()) throw new RuntimeException("Helper Check Failed");
        else if (nullCheck && !checkRecordValidation(word,explanation,level)) return -1;

        //Log.d("DBManager", "insert: check = "+checkRecordValidation(word,explanation,level));
        //Log.d("DBManager", "word = "+word);
        //Log.d("DBManager", "explanation = "+explanation);
        //Log.d("DBManager", "level = "+level);

        long newId = 0;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (allowOverride) {
            if (checkWordValidation(word)) values.put("word", word);
            if (checkExplanationValidation(explanation)) values.put("explanation", explanation);
            if (checkLevelValidation(level)) values.put("level", level);
        } else {
            values.put("word", word);
            values.put("explanation", explanation);
            values.put("level", level);
        }

        long id = getExistedWordId(table,word);
        if (id==-1) {
            // Force null check
            if (!checkRecordValidation(word,explanation,level)) {
                //Log.d("DBManager", "insert: null check failed!");
                return -1;
            } else {
                values.put("modified_time", SystemClock.elapsedRealtime());
                newId = db.insert(table,null,values);
            }
        } else if (allowOverride) {
            values.put("modified_time", SystemClock.elapsedRealtime());
            db.update(table,values,"id = ?",new String[]{""+id});
            newId = id;
        } else {
            newId = -1;
        }

        return newId;
    }

    public List<Word> query(String table, String[] columns, String selection,
                            String[] selectionArgs, String groupBy, String having,
                            String orderBy, String limit) {
        if (!helperCheck())  throw new RuntimeException("Helper Check Failed");

        List<Word> queryResult = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(table, columns,selection,selectionArgs,groupBy,having,orderBy,limit);
        if (cursor.moveToFirst()) {
            do {
                Word _wrapper = new Word();
                for (int i = 0; i < columns.length; i++) {
                    _wrapper.put(columns[i],
                            cursor.getString(cursor.getColumnIndex(columns[i])));
                }
                queryResult.add(_wrapper);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return queryResult;
    }

    public void delete(String table, String whereClause, String[] whereArgs) {
        if (!helperCheck()) throw new RuntimeException("Helper Check Failed");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(table,whereClause,whereArgs);
    }

    public void update(String table, Word w, String whereClause, String[] whereArgs) {
        if (!helperCheck()) throw new RuntimeException("Helper Check Failed");
        else if (!checkRecordValidation(w)) {
            Log.d("DBManager", "update: null check failed!");
            return ;
        }
        ContentValues values = new ContentValues();
        values.put("word",w.getWord());
        values.put("explanation",w.getExplanation());
        values.put("level",w.getLevel());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        values.put("modified_time", SystemClock.elapsedRealtime());
        db.update(table,values,whereClause,whereArgs);
    }

    public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        if (!helperCheck()) throw new RuntimeException("Helper Check Failed");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        values.put("modified_time", SystemClock.elapsedRealtime());
        db.update(table,values,whereClause,whereArgs);
    }

    private long getExistedWordId(String table, String word) {
        if (!helperCheck()) throw new RuntimeException("Helper Check Failed");
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(table, new String[]{"id"}, "word = ?", new String[]{word}, null,null,null);
        long id = -1;
        if (cursor.moveToFirst()) {
            if (cursor.getCount()>=1) id = cursor.getLong(cursor.getColumnIndex("id"));
        }
        cursor.close();

        return id;
    }

    private boolean checkRecordValidation(Word w) {
        return checkRecordValidation(w.getWord(),w.getExplanation(),w.getLevel());
    }

    private boolean checkRecordValidation(String word, String expl, Integer level) {
        return (word!=null && !word.isEmpty()) &&
                (expl!=null && !expl.isEmpty()) &&
                (level!=null && level >= 0 && level <= 8);
    }

    private boolean checkWordValidation(String word) { return (word!=null && !word.isEmpty()); }
    private boolean checkExplanationValidation(String expl) { return (expl!=null && !expl.isEmpty()); }
    private boolean checkLevelValidation(Integer level) { return (level!=null && level>=0 && level<=8);}
}
