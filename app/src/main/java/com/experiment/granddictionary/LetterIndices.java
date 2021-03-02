package com.experiment.granddictionary;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.os.Handler;

import androidx.appcompat.view.ContextThemeWrapper;


public class LetterIndices {
    private static final String TAG = "MainActivity";
    private Button[] mButtonList = new Button[26];
    private Context mContext;
    private int mSelectedId = -1;
    private Handler mHandler;

    LetterIndices(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        initView();
    }

    public Button[] getButtons() { return mButtonList; }
    public int getSelectedId() { return mSelectedId; }
    public void setSelectedId(int id) {
        if (id>=-1&&id<26) mSelectedId = id;
        else id = -1;
        refreshState(false);
    }

    private void initView() {
        for (int i = 0; i < 26; i++) {
            //Button btn = new Button(MainActivity.this);
            // https://stackoverflow.com/questions/11723881/android-set-view-style-programmatically/28613069#28613069
            Button btn = new Button(new ContextThemeWrapper(mContext, R.style.SideButtonStyle),
                            null, 0);
            btn.setText(""+((char)('A'+i)));
            btn.setGravity(Gravity.CENTER);
            final char ch = (char)('A'+i);
            final int id = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //GlobalUtil.showToast(mContext,"You click "+ch);
                    if (!v.isSelected()) {
                        mSelectedId = id;
                    } else {
                        mSelectedId = -1;
                    }

                    refreshState(true);
                }
            });
            mButtonList[i] = btn;
        }

    }

    private void refreshState(boolean sendMsg) {
        for (int i = 0; i < 26; i++) {
            if (i==mSelectedId && !mButtonList[i].isSelected()) {
                mButtonList[i].setSelected(true);
            } else {
                mButtonList[i].setSelected(false);
            }
        }

        if (sendMsg) {
            Message msg = mHandler.obtainMessage();
            msg.what = GlobalUtil.REFRESH_LIST;
            //Log.d(TAG, "refreshState: msg.arg1 = "+msg.arg1);
            //Log.d(TAG, "refreshState: msg.arg2 = "+msg.arg2);
            //msg.arg1 = mSelectedId;
            mHandler.sendMessage(msg);
        }

    }
}
