package com.experiment.granddictionary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static DBManager mDBManager = DBManager.getInstance();
    private MyDatabaseHelper dbHelper;
    private ProgressDialog mProgressDialog;
    private int maxCount;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case GlobalUtil.DOWNLOAD_SUCCESS:
                    GlobalUtil.showToast(MainActivity.this,"Download Succeed");
                    mProgressDialog = DialogFactory.getInstance().createDownloadProgressDialog(MainActivity.this);
                    break;
                case GlobalUtil.DOWNLOAD_FAIL:
                    GlobalUtil.showToast(MainActivity.this,"Download FAILED");
                    break;
                case GlobalUtil.PROGRESS_UPDATE:
                    if (msg.arg1>=maxCount) {
                        // Update ListView Data
                        GlobalUtil.showToast(MainActivity.this,"Parsing Finished");
                        Message msg2 = mHandler.obtainMessage();
                        msg2.what = GlobalUtil.REFRESH_LIST;
                        mHandler.sendMessage(msg2);
                        mProgressDialog.dismiss();
                    } else {
                        mProgressDialog.setProgress(msg.arg1);
                    }

                    break;
                case GlobalUtil.PROGRESS_MAX:
                    GlobalUtil.showToast(MainActivity.this,"Start Parsing");
                    mProgressDialog.setMax(msg.arg1);
                    mProgressDialog.show();
                    maxCount = msg.arg1;
                    break;
                case GlobalUtil.REFRESH_LIST:
                    //GlobalUtil.showToast(MainActivity.this,"After adding new Word:");
                    data2.clear();
                    words.clear(); explanations.clear(); levels.clear();
                    mSeletedId = mLetterIndices.getSelectedId();
                    getDataFromDB();
                    mAdapter.notifyDataSetChanged();
                    mSimpleAdapter.notifyDataSetChanged();
                    if (msg.arg2==0) {
                        mWordTextView.setText("");
                        mExplanationTextView.setText("");
                        wordListView.setSelection(0);
                    } else {
                        mWordTextView.setText(words.get(msg.arg2-1));
                        mExplanationTextView.setText(explanations.get(msg.arg2-1));
                        wordListView.setSelection(msg.arg2-1);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private ListView wordListView;
    private List<String> words = new ArrayList<>();
    private List<String> explanations = new ArrayList<>();
    private List<Integer> levels = new ArrayList<>();
    private List<Map<String, String>> data2 = new ArrayList<Map<String, String>>();
    private ArrayAdapter<String> mAdapter;
    private SimpleAdapter mSimpleAdapter;

    private View explanationLayout;
    private boolean mShowExplanation = true;
    private boolean mShowExplanationInList = false;
    private TextView mWordTextView;
    private TextView mExplanationTextView;
    private LetterIndices mLetterIndices;
    private int mSeletedId = -1;
    private String mSerachPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 一 (1)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("简明英汉词典");
        //actionBar.setSubtitle("中山大学");

        // 一 (2)
        dbHelper = new MyDatabaseHelper(this, "Dictionary.db", null, 1);
        //dbHelper.getWritableDatabase();
        Log.d(TAG, "onCreate: Main Thread ID = "+Thread.currentThread().getId());
        mDBManager.setDbHelper(dbHelper);

        // 一 (3)
        wordListView = (ListView) findViewById(R.id.word_list);
        getDataFromDB();
        mAdapter = new ArrayAdapter<String>(
                MainActivity.this,android.R.layout.simple_list_item_1,words);
        wordListView.setAdapter(mAdapter);/**/
        mSimpleAdapter = new SimpleAdapter(MainActivity.this, data2,
                            android.R.layout.simple_list_item_2,
                            new String[]{"word","explanation"}, new int[]{android.R.id.text1,android.R.id.text2});
        //
        explanationLayout = findViewById(R.id.layout_explanation);
        mWordTextView = (TextView) findViewById(R.id.tv_word);
        mExplanationTextView = (TextView) findViewById(R.id.tv_explanation);

        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mShowExplanation) {
                    explanationLayout.setVisibility(View.VISIBLE);
                    mWordTextView.setText(words.get(position));
                    mExplanationTextView.setText(explanations.get(position));
                } else explanationLayout.setVisibility(View.GONE);
            }
        });
        wordListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popup = new PopupMenu(MainActivity.this,view);
                popup.getMenuInflater().inflate(R.menu.list_item,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                DialogFactory.getInstance().createConfirmDeleteAlertDialog(
                                        MainActivity.this, words.get(position),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mDBManager.delete(GlobalUtil.TABLE_NAME,"word = ?",new String[]{words.get(position)});
                                                Message msg = mHandler.obtainMessage();
                                                msg.what = GlobalUtil.REFRESH_LIST;
                                                //msg.arg1 = mSeletedId;
                                                mHandler.sendMessage(msg);
                                            }
                                        }
                                ).show();

                                break;
                            case R.id.modify:
                                modifyWord(position);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });

        // 首字母索引
        mLetterIndices = new LetterIndices(MainActivity.this,mHandler);
        // HorizontalScrollView
        int size = dpToPx(26);
        int margin = dpToPx(2);
        LinearLayout lettersLayout = (LinearLayout) findViewById(R.id.layout_letters);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                size,
                size);
        lp.setMargins(margin,0,margin,0);
        for (Button btn: mLetterIndices.getButtons()) {
            lettersLayout.addView(btn,lp);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                searchWord();
                break;
            case R.id.add:
                addNewWord();
                break;
            case R.id.download:
                //Toast.makeText(MainActivity.this,"Downloading...",Toast.LENGTH_LONG).show();
                GlobalUtil.showToast(MainActivity.this,"Downloading...");
                new DownloadTask(mHandler).doDownload();
                break;
            case R.id.interpretation:
                item.setChecked(!item.isChecked());
                mShowExplanationInList = item.isChecked();
                if (mShowExplanationInList) {
                    wordListView.setAdapter(mSimpleAdapter);
                    mShowExplanation = false;
                    explanationLayout.setVisibility(View.GONE);
                } else {
                    wordListView.setAdapter(mAdapter);
                    mShowExplanation = true;
                    explanationLayout.setVisibility(View.VISIBLE);
                }

                break;
            default:
                break;
        }
        return true;
    }

    private void getDataFromDB() {
        final int selectedId = mSeletedId;
        Log.d(TAG, "getDataFromDB: selectedId = "+selectedId);
        //selectedId -= 1;
        int cnt = 0;

        // 解决Like匹配时默认大小写不敏感 - https://www.oschina.net/question/1176258_212552?sort=time
        //db.rawQuery("PRAGMA case_sensitive_like = ON;",null); // 在查询前先执行这个语句 , 1 时区分大小写,0时不区分
        //Cursor cursor = db.rawQuery("select word from "+TABLE_NAME+" where word like 'a%' order by word",null);
        if (selectedId<0||selectedId>=26) {
            if (mSerachPattern!=null && !mSerachPattern.isEmpty()) {
                List<Word> result = mDBManager.query(GlobalUtil.TABLE_NAME,new String[]{"word","explanation","level"},
                        "word like ?", new String[]{"%"+mSerachPattern+"%"},
                        null,null,"word",null);
                cnt = result.size();
                Log.d(TAG, "getDataFromDB: count(%"+mSerachPattern+"%) = "+cnt);
                for (int j = 0; j < cnt; j++) {
                    Word word = result.get(j);
                    words.add(word.getWord());
                    explanations.add(word.getExplanation());
                    levels.add(word.getLevel());

                    Map<String, String> datum = new HashMap<String, String>();
                    datum.put("word", word.getWord());
                    datum.put("explanation", word.getExplanation());
                    data2.add(datum);
                }
                GlobalUtil.showLongToast(MainActivity.this,
                        "共找到 "+cnt+"个包含 "+mSerachPattern+" 的单词");
                mSerachPattern = null; // 清除查找模式串
                return;
            }
            // A,a,B,b,....
            for (int i = 0; i < 26; i++) {
                /*db.rawQuery("PRAGMA case_sensitive_like = ON;",null); // 在查询前先执行这个语句 , 1 时区分大小写,0时不区分
                cursor = db.query(TABLE_NAME, new String[]{"word","explanation"}, "word like ?", new String[]{(char)('a'+i)+"%"}, null,null,"word","5");
                //Log.d(TAG, "getDataFromDB: columnCount = "+cursor.getColumnCount());
                cnt = 0;
                if (cursor.moveToFirst()) cnt= (int)cursor.getCount();
                Log.d(TAG, "getDataFromDB: count("+(char)('a'+i)+"%) = "+cnt);
                if (cursor.moveToFirst()) {
                    do {
                        words.add(cursor.getString(cursor.getColumnIndex("word")));
                        explanations.add(cursor.getString(cursor.getColumnIndex("explanation")));
                    } while (cursor.moveToNext());
                }
                cursor.close();*/

                List<Word> result = mDBManager.query(GlobalUtil.TABLE_NAME,new String[]{"word","explanation","level"},
                        "word like ?", new String[]{(char)('a'+i)+"%"},
                        null,null,"word","5");
                cnt = result.size();
                Log.d(TAG, "getDataFromDB: count("+(char)('a'+i)+"%) = "+cnt);
                for (int j = 0; j < cnt; j++) {
                    Word word = result.get(j);
                    words.add(word.getWord());
                    explanations.add(word.getExplanation());
                    levels.add(word.getLevel());

                    Map<String, String> datum = new HashMap<String, String>();
                    datum.put("word",word.getWord());
                    datum.put("explanation",word.getExplanation());
                    data2.add(datum);
                }
            }
        } else {
            List<Word> result = mDBManager.query(GlobalUtil.TABLE_NAME,new String[]{"word","explanation","level"},
                    "word like ?", new String[]{(char)('a'+selectedId)+"%"},
                    null,null,"word",null);
            cnt = result.size();
            Log.d(TAG, "getDataFromDB: count("+(char)('a'+selectedId)+"%) = "+cnt);
            for (int j = 0; j < cnt; j++) {
                Word word = result.get(j);
                words.add(word.getWord());
                explanations.add(word.getExplanation());
                levels.add(word.getLevel());

                Map<String, String> datum = new HashMap<String, String>();
                datum.put("word", word.getWord());
                datum.put("explanation", word.getExplanation());
                data2.add(datum);
            }
        }

    }

    private void searchWord() {
        final LinearLayout contentView = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_search_word,null);
        //final EditText modWord = (EditText) contentView.findViewById(R.id.search_pattern);
        DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText inputPattern = (EditText) contentView.findViewById(R.id.search_pattern);
                mSerachPattern = inputPattern.getText().toString();
                mLetterIndices.setSelectedId(-1);
                Message msg = mHandler.obtainMessage();
                msg.what = GlobalUtil.REFRESH_LIST;
                mHandler.sendMessage(msg);
            }
        };

        DialogFactory.getInstance().
                createSearchWordAlertDialog(MainActivity.this, contentView, confirmListener);


    }

    private void addNewWord() {
        final LinearLayout contentView = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_add_word,null);

        final CheckedTextView allowOverride = (CheckedTextView) contentView.findViewById(R.id.tv_override);
        allowOverride.setTextColor(Color.parseColor(GlobalUtil.DEFAULT_TEXT_COLOR));
        allowOverride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowOverride.setChecked(!allowOverride.isChecked());
            }
        });

        DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText inputWord = (EditText) contentView.findViewById(R.id.input_word);
                EditText inputExpl = (EditText) contentView.findViewById(R.id.input_expl);
                EditText inputLevel = (EditText) contentView.findViewById(R.id.input_level);

                String word = inputWord.getText().toString();
                String expl = inputExpl.getText().toString();
                Integer level = null;
                if (!inputLevel.getText().toString().isEmpty())
                    level = Integer.valueOf(inputLevel.getText().toString());
                boolean allowOverrideFlag = allowOverride.isChecked();

                Log.d(TAG, "addNewWord::onClick: word="+word+" expl="+expl+" level="+level+" allowOv="+allowOverrideFlag);
                long res = DBManager.getInstance().insert(GlobalUtil.TABLE_NAME,word,expl,level,false,allowOverrideFlag);
                Log.d(TAG, "addNewWord::onClick: add result = "+res);
                mLetterIndices.setSelectedId(-1);
                Message msg = mHandler.obtainMessage();
                msg.what = GlobalUtil.REFRESH_LIST;
                mHandler.sendMessage(msg);
            }
        };

        DialogFactory.getInstance()
                .createAddOrModifyWordAlertDialog(MainActivity.this, contentView, "增加单词", confirmListener);

    }

    private void modifyWord(final int pos) {
        final LinearLayout contentView = (LinearLayout)getLayoutInflater().inflate(R.layout.layout_mod_word,null);
        final EditText modWord = (EditText) contentView.findViewById(R.id.mod_word);
        final EditText modExpl = (EditText) contentView.findViewById(R.id.mod_expl);
        final EditText modLevel = (EditText) contentView.findViewById(R.id.mod_level);
        Log.d(TAG, "modifyWord: oldWord="+words.get(pos)+" oldExpl="+explanations.get(pos)+" oldLevel="+levels.get(pos));
        modWord.setText(words.get(pos));
        modExpl.setText(explanations.get(pos));
        modLevel.setText(levels.get(pos).toString());

        DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String word = modWord.getText().toString();
                String expl = modExpl.getText().toString();
                Integer level = null;
                if (!modLevel.getText().toString().isEmpty())
                    level = Integer.valueOf(modLevel.getText().toString());

                Log.d(TAG, "modifyWord::onClick: word="+word+" expl="+expl+" level="+level);
                List<Word> queryResult = DBManager.getInstance().
                        query(GlobalUtil.TABLE_NAME,new String[]{"id"},"word = ?", new String[]{words.get(pos)},
                                null,null,null,null);

                queryResult.get(0).setWord(word);
                queryResult.get(0).setExplanation(expl);
                queryResult.get(0).setLevel(level);
                DBManager.getInstance().
                        update(GlobalUtil.TABLE_NAME,queryResult.get(0),
                                "id = ?",new String[]{queryResult.get(0).getId().toString()});

                Message msg = mHandler.obtainMessage();
                msg.what = GlobalUtil.REFRESH_LIST;
                msg.arg1 = mSeletedId;
                msg.arg2 = pos+1; // >1：保留下方释义
                mHandler.sendMessage(msg);
            }
        };

        DialogFactory.getInstance().
                createAddOrModifyWordAlertDialog(MainActivity.this, contentView, "增加单词", confirmListener);
    }

    private int dpToPx(int size) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return(size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }
}
