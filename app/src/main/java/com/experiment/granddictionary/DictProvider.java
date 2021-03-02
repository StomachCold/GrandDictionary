package com.experiment.granddictionary;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DictProvider extends ContentProvider {
    public static final int DICTIONARY_DIR = 0;
    public static final int DICTIONARY_ITEM = 1;
    public static final String AUTHORITY = "com.experiment.granddictionary.provider";
    private static UriMatcher uriMatcher;

    private MyDatabaseHelper databaseHelper;
    private static final String TABLE_NAME= "Dictionary";

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,"dictionary",DICTIONARY_DIR);
        uriMatcher.addURI(AUTHORITY,"dictionary/#",DICTIONARY_ITEM);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new MyDatabaseHelper(getContext(),"Dictionary.db", null, 1);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Query data
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case DICTIONARY_DIR:
                cursor = db.query(TABLE_NAME,projection,selection,selectionArgs,
                            null,null,sortOrder);
                break;
            case DICTIONARY_ITEM:
                String wordId = uri.getPathSegments().get(1);
                cursor = db.query(TABLE_NAME,projection,"id = ?",new String[]{wordId},
                        null,null,sortOrder);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Insert data
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Uri returnUri = null;
        switch (uriMatcher.match(uri)) {
            case DICTIONARY_DIR:
            case DICTIONARY_ITEM:
                long newWordId = db.insert(TABLE_NAME,null,values);
                returnUri = Uri.parse("content://"+AUTHORITY+"/dictionary/"+newWordId);
                break;
            default:
                break;
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Delete data
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int deletedRows = 0;
        switch (uriMatcher.match(uri)) {
            case DICTIONARY_DIR:
                deletedRows = db.delete(TABLE_NAME,selection,selectionArgs);
                break;
            case DICTIONARY_ITEM:
                String wordId = uri.getPathSegments().get(1);
                deletedRows = db.delete(TABLE_NAME,"id = ?",new String[]{wordId});
                break;
            default:
                break;
        }

        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Update data
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int updatedRows = 0;
        switch (uriMatcher.match(uri)) {
            case DICTIONARY_DIR:
                updatedRows = db.update(TABLE_NAME,values,selection,selectionArgs);
                break;
            case DICTIONARY_ITEM:
                String wordId = uri.getPathSegments().get(1);
                updatedRows = db.update(TABLE_NAME,values,"id = ?",new String[]{wordId});
                break;
            default:
                break;
        }
        return updatedRows;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case DICTIONARY_DIR:
                return "vnd.android.cursor.dir/vnd.com.experiment.granddictionary.provider.dictionary";
            case DICTIONARY_ITEM:
                return "vnd.android.cursor.item/vnd.com.experiment.granddictionary.provider.dictionary";
            default:
                break;
        }
        return null;
    }
}
