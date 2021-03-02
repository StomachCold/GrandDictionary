package com.experiment.granddictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;


public class DownloadTask {
    private static final String TAG = "DownloadTask";
    private Callback mCallback;
    private Handler mHandler;
    private static DBManager mDBManager = DBManager.getInstance();

    public DownloadTask(Handler handler) {
        this.mHandler = handler;
        this.mCallback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Message msg = mHandler.obtainMessage();
                msg.what = GlobalUtil.DOWNLOAD_FAIL;
                mHandler.sendMessage(msg);
                Log.d(TAG, "Download Failed");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //Log.d(TAG, "Download Succeed");
                //Log.d(TAG, "onResponse: Sub Thread ID = "+Thread.currentThread().getId());
                //Log.d(TAG, "onResponse: Sub Thread DBManager.dbhelper existed: "+mDBManager.helperCheck());
                Message msg = mHandler.obtainMessage();
                msg.what = GlobalUtil.DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);
                parseJSON(response.body().string());
            }
        };
    }

    public boolean doDownload() {
        //Log.d(TAG, "doDownload: ThreadId = "+Thread.currentThread().getId());
        String downloadUrl = "http://103.26.79.35:8080/dict/";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        client.newCall(request).enqueue(mCallback);
        return true;
    }
    private void parseJSON(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            ContentValues values = new ContentValues();
            Log.d(TAG, "parseJSON: data count = "+jsonArray.length());
            //Log.d(TAG, "parseJSON: insert 10 record");

            Message msg = mHandler.obtainMessage();
            msg.what = GlobalUtil.PROGRESS_MAX;
            msg.arg1 = jsonArray.length();
            mHandler.sendMessage(msg);

            for (int i = 0; i < jsonArray.length(); i++) {
            //for (int i = 0; i < 10; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String word = jsonObject.getString("word");
                String explanation = jsonObject.getString("explanation");
                int level = jsonObject.getInt("level");

                long res = mDBManager.insert(GlobalUtil.TABLE_NAME,word,explanation,level);

                /*values.clear();
                values.put("word",word);
                values.put("explanation",explanation);
                values.put("level",level);
                int id = getExistedWordId(word);
                long res;
                if (id==-1) {
                    values.put("modified_time", SystemClock.elapsedRealtime());
                    res = db.insert(TABLE_NAME,null,values);
                }
                else {
                    values.put("modified_time", SystemClock.elapsedRealtime());
                    res = db.update(TABLE_NAME,values,"id = ?",new String[]{""+id});
                }*/
                //Log.d(TAG, "parseJSON: record "+i+" insert result: "+res);

                if (i>0 && i % 10 == 0) {
                    msg = mHandler.obtainMessage();
                    msg.what = GlobalUtil.PROGRESS_UPDATE;
                    msg.arg1 = i;
                    mHandler.sendMessage(msg);
                }
            }

            // Final
            msg = mHandler.obtainMessage();
            msg.what = GlobalUtil.PROGRESS_UPDATE;
            msg.arg1 = jsonArray.length();
            mHandler.sendMessage(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
